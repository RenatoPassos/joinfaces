/*
 * Copyright 2016-2018 the original author or authors.
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

package org.joinfaces.gradle;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BomPluginTest {

	private Project project;

	@BeforeEach
	void setUp() {
		this.project = ProjectBuilder.builder().build();

		this.project.getRepositories().add(this.project.getRepositories().mavenCentral());
	}

	@Test
	void apply() {
		this.project.getPlugins().apply(BomPlugin.class);

		assertThat(this.project.getPlugins()).anyMatch(DependencyManagementPlugin.class::isInstance);
		DependencyManagementExtension dependencyManagement = this.project.getExtensions().findByType(DependencyManagementExtension.class);

		assertThat(dependencyManagement).isNotNull();
	}
}
