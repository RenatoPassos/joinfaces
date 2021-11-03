/*
 * Copyright 2016-2016 the original author or authors.
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

import lombok.Data;
import org.joinfaces.autoconfigure.servlet.initparams.ServletContextInitParameter;
import org.joinfaces.autoconfigure.servlet.initparams.ServletContextInitParameterProperties;
import org.primefaces.util.Constants;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Primefaces 6.2+.
 *
 * Values taken from https://www.primefaces.org/docs/guide/primefaces_user_guide_6_2.pdf page 13.
 *
 * @author Marcelo Fernandes
 */
@Data
@ConfigurationProperties("joinfaces.primefaces")
public class Primefaces6_2Properties implements ServletContextInitParameterProperties {

	@ServletContextInitParameter(Constants.ContextParams.MOVE_SCRIPTS_TO_BOTTOM)
	private Boolean moveScriptsToBottom;

}
