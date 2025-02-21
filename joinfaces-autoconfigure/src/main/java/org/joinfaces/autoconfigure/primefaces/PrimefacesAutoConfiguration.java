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

package org.joinfaces.autoconfigure.primefaces;

import org.joinfaces.autoconfigure.faces.JakartaFaces3AutoConfiguration;
import org.primefaces.PrimeFaces;
import org.primefaces.cache.CacheProvider;
import org.primefaces.component.captcha.Captcha;
import org.primefaces.util.Constants;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Auto Configuration for Primefaces.
 *
 * @author Marcelo Fernandes
 * @author Lars Grefer
 */
@AutoConfiguration(before = JakartaFaces3AutoConfiguration.class)
@ConditionalOnClass(Constants.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class PrimefacesAutoConfiguration {

	/**
	 * Spring bean definition for the {@link PrimeFaces} context.
	 *
	 * @return The current {@link PrimeFaces#current() PrimeFaces}.
	 */
	@Bean("primefaces")
	@ConditionalOnMissingBean
	public PrimeFaces primefaces() {
		return PrimeFaces.current();
	}

	/**
	 * Auto Configuration for Primefaces 4.0.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces4_0Properties.class)
	@ConditionalOnClass(Captcha.class)
	public static class Primefaces4_0AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 5.0.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces5_0Properties.class)
	@ConditionalOnClass(CacheProvider.class)
	public static class Primefaces5_0AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 5.2+.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces5_2Properties.class)
	public static class Primefaces5_2AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 6.0.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces6_0Properties.class)
	public static class Primefaces6_0AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 6.2+.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces6_2Properties.class)
	public static class Primefaces6_2AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 8.0+.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces8_0Properties.class)
	public static class Primefaces8_0AutoConfiguration {
	}

	/**
	 * Auto Configuration for Primefaces 10.0.0+.
	 */
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(Primefaces10_0_0Properties.class)
	public static class Primefaces10_0_0AutoConfiguration {
	}

}
