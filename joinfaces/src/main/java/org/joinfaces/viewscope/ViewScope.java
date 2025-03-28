/*
 * Copyright 2016-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.joinfaces.viewscope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.ViewMapListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.context.request.FacesRequestAttributes;

/**
 * Implementation of view scope.
 *
 * This class exposes the JSF {@link UIViewRoot#getViewMap() view map} as spring {@link Scope}.
 *
 * @author Marcelo Fernandes
 * @author Lars Grefer
 */
@Slf4j
public class ViewScope implements Scope {

	/**
	 * Scope identifier for view scope: "view".
	 *
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_SESSION
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST
	 */
	public static final String SCOPE_VIEW = "view";

	/**
	 * Constant identifying the {@link String} prefixed to the name of a
	 * destruction callback when it is stored in a {@link UIViewRoot#getViewMap() view map}.
	 *
	 * @see org.springframework.web.context.request.ServletRequestAttributes#DESTRUCTION_CALLBACK_NAME_PREFIX
	 */
	public static final String DESTRUCTION_CALLBACK_NAME_PREFIX = ViewScope.class.getName() + ".DESTRUCTION_CALLBACK.";

	@Getter(AccessLevel.PACKAGE)
	private final PreDestroyViewMapListener preDestroyViewMapListener = new PreDestroyViewMapListener();

	@Override
	public Object get(String name, ObjectFactory objectFactory) {
		Map<String, Object> viewMap = getViewRoot().getViewMap();

		Object bean = viewMap.get(name);

		if (bean == null) {
			bean = objectFactory.getObject();
			viewMap.put(name, bean);
		}

		return bean;
	}

	@Override
	@Nullable
	public Object remove(String name) {
		UIViewRoot viewRoot = getViewRoot();
		Object bean = viewRoot.getViewMap().remove(name);
		DestructionCallbackWrapper destructionCallbackWrapper = (DestructionCallbackWrapper) viewRoot.getViewMap().remove(DESTRUCTION_CALLBACK_NAME_PREFIX + name);

		if (destructionCallbackWrapper != null) {
			getSessionListener().unregister(destructionCallbackWrapper);
		}

		return bean;
	}

	@Override
	@Nullable
	public String getConversationId() {
		getFacesContext(); //maybe throws an Exception
		return null;
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		DestructionCallbackWrapper wrapper = new DestructionCallbackWrapper(name, callback);

		getFacesContext().getApplication().subscribeToEvent(PreDestroyViewMapEvent.class, this.preDestroyViewMapListener);
		getViewRoot().getViewMap().put(DESTRUCTION_CALLBACK_NAME_PREFIX + name, wrapper);
		getSessionListener().register(wrapper);
	}

	@Override
	@Nullable
	public Object resolveContextualObject(String key) {
		return new FacesRequestAttributes(getFacesContext()).resolveReference(key);
	}

	private UIViewRoot getViewRoot() {
		UIViewRoot viewRoot = getFacesContext().getViewRoot();
		if (viewRoot == null) {
			throw new IllegalStateException("No ViewRoot found");
		}

		return viewRoot;
	}

	private FacesContext getFacesContext() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			throw new IllegalStateException("No FacesContext found.");
		}
		return facesContext;
	}

	private SessionListener getSessionListener() {
		return (SessionListener) getFacesContext()
				.getExternalContext()
				.getSessionMap()
				.computeIfAbsent(SessionListener.class.getName(), k -> new SessionListener());
	}

	/**
	 * This class acts as "session-destroyed" listener, which call the destruction callbacks
	 * of all view scoped beans that have not been destructed yet.
	 *
	 * @author Lars Grefer
	 * @see org.springframework.web.context.request.DestructionCallbackBindingListener
	 */
	@Getter
	static class SessionListener implements HttpSessionBindingListener, Serializable {

		private final List<DestructionCallbackWrapper> destructionCallbackWrappers = new ArrayList<>();

		void register(DestructionCallbackWrapper destructionCallbackWrapper) {
			cleanup();
			this.destructionCallbackWrappers.add(destructionCallbackWrapper);
		}

		void unregister(DestructionCallbackWrapper destructionCallbackWrapper) {
			cleanup();
			this.destructionCallbackWrappers.remove(destructionCallbackWrapper);
		}

		synchronized void cleanup() {
			this.destructionCallbackWrappers.removeIf(DestructionCallbackWrapper::isCallbackCalled);
		}

		@Override
		public void valueBound(@Nullable HttpSessionBindingEvent httpSessionBindingEvent) {
		}

		@Override
		public void valueUnbound(@Nullable HttpSessionBindingEvent httpSessionBindingEvent) {
			this.destructionCallbackWrappers.forEach(DestructionCallbackWrapper::onSessionDestroy);
			cleanup();
		}
	}

	/**
	 * This class acts as {@link PreDestroyViewMapEvent}-listener, which calls all destruction callbacks
	 * which are stored in the view map to be destroyed.
	 *
	 * @author Lars Grefer
	 * @see #registerDestructionCallback(String, Runnable)
	 */
	class PreDestroyViewMapListener implements ViewMapListener, Serializable {

		@Override
		public void processEvent(SystemEvent event) {
			UIViewRoot root = (UIViewRoot) event.getSource();
			root.getViewMap(false).values().stream()
					.filter(DestructionCallbackWrapper.class::isInstance)
					.map(DestructionCallbackWrapper.class::cast)
					.forEach(DestructionCallbackWrapper::onViewDestroy);
			getSessionListener().cleanup();
		}

		@Override
		public boolean isListenerForSource(Object source) {
			return source instanceof UIViewRoot;
		}
	}

	/**
	 * Wrapper around the {@link ViewScope#registerDestructionCallback(String, Runnable) destruction callback} of
	 * view scoped beans.
	 *
	 * @author Lars Grefer
	 * @see #registerDestructionCallback(String, Runnable)
	 */
	static class DestructionCallbackWrapper implements Serializable {

		@Getter
		private final String beanName;

		@Nullable
		private Runnable callback;

		DestructionCallbackWrapper(String beanName, Runnable callback) {
			Assert.hasText(beanName, "beanName must not be null or empty");
			Assert.notNull(callback, "callback must not be null");
			this.beanName = beanName;
			this.callback = callback;
		}

		void onViewDestroy() {
			doRunCallback(false);
		}

		void onSessionDestroy() {
			doRunCallback(true);
		}

		private synchronized void doRunCallback(boolean session) {
			if (this.callback != null) {
				log.debug("Calling destruction callbacks for bean {} because the {} is destroyed", getBeanName(), session ? "session" : "view map");
				this.callback.run();
				this.callback = null;
			}
		}

		boolean isCallbackCalled() {
			return this.callback == null;
		}
	}
}
