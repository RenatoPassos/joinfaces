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

package org.joinfaces.autoconfigure.rewrite;

import org.joinfaces.rewrite.SpringBootAnnotationConfigProvider;
import org.joinfaces.servlet.WebFragmentRegistrationBean;
import org.ocpsoft.rewrite.servlet.RewriteFilter;
import org.ocpsoft.rewrite.servlet.impl.RewriteServletContextListener;
import org.ocpsoft.rewrite.servlet.impl.RewriteServletRequestListener;
import org.ocpsoft.rewrite.spring.SpringExpressionLanguageProvider;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWarDeployment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot Auto Configuration of Rewrite.
 *
 * @author Marcelo Fernandes
 * @author Lars Grefer
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableConfigurationProperties({RewriteProperties.class, RewriteFilterProperties.class})
@ConditionalOnClass(RewriteFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RewriteAutoConfiguration {

	/**
	 * This {@link WebFragmentRegistrationBean} is equivalent to the
	 * {@code META-INF/web-fragment.xml} of the {@code rewrite-servlet.jar}.
	 *
	 * @return rewriteWebFragmentRegistrationBean
	 */
	@Bean
	public WebFragmentRegistrationBean rewriteWebFragmentRegistrationBean() {
		WebFragmentRegistrationBean bean = new WebFragmentRegistrationBean();

		bean.getListeners().add(RewriteServletRequestListener.class);
		bean.getListeners().add(RewriteServletContextListener.class);

		return bean;
	}

	@Bean
	@ConditionalOnMissingFilterBean
	@ConditionalOnNotWarDeployment
	public FilterRegistrationBean<RewriteFilter> rewriteFilterRegistrationBean(RewriteFilterProperties rewriteFilterProperties) {
		FilterRegistrationBean<RewriteFilter> rewriteFilterRegistrationBean = new FilterRegistrationBean<>(new RewriteFilter());

		// The same name as in the META-INF/web-fragment.xml of the rewrite-servlet.jar
		// so it purposely clashes on war deployments.
		rewriteFilterRegistrationBean.setName("OCPsoft Rewrite Filter");

		rewriteFilterRegistrationBean.setEnabled(rewriteFilterProperties.isEnabled());
		rewriteFilterRegistrationBean.setOrder(rewriteFilterProperties.getOrder());
		rewriteFilterRegistrationBean.setUrlPatterns(rewriteFilterProperties.getUrlPatterns());
		rewriteFilterRegistrationBean.setDispatcherTypes(rewriteFilterProperties.getDispatcherTypes());

		return rewriteFilterRegistrationBean;
	}

	@Bean
	public SpringExpressionLanguageProvider rewriteExpressionLanguageProvider() {
		return new SpringExpressionLanguageProvider();
	}

	/**
	 * This {@link SpringBootAnnotationConfigProvider} adds a
	 * {@link org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider} which scans for Rewrite annotations within
	 * the classpath.
	 *
	 * @return rewrite annotation scanner
	 */
	@Bean
	@ConfigurationProperties("joinfaces.rewrite.annotation-config-provider")
	public SpringBootAnnotationConfigProvider rewriteAnnotationConfigProvider() {
		return new SpringBootAnnotationConfigProvider();
	}
}
