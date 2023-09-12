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

package org.joinfaces.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class JoinfacesPluginIT {

	@ParameterizedTest
	@ValueSource(strings = {"7.5", "8.0"})
	public void postJdk17Build(String gradleVersion, @TempDir Path projectDir) throws IOException {
		build(gradleVersion, projectDir);
	}

	void build(String gradleVersion, @TempDir Path projectDir) throws IOException {
		Files.write(projectDir.resolve("settings.gradle"), Collections.singleton("rootProject.name = 'dummy'"));

		Files.write(projectDir.resolve("build.gradle"), Arrays.asList(
				"plugins {",
				"    id 'java'",
				"    id 'groovy'",
				"    id 'scala'",
				"    id 'org.joinfaces'",
				"}",
				"repositories { mavenCentral() }",
				"dependencies {",
				"    implementation 'org.apache.myfaces.core:myfaces-impl:3.0.1'",
				"}"
		));

		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(projectDir.toFile())
				.withPluginClasspath()
				.withArguments("jar", "-s", "--info")
				.withDebug(true)
				.withGradleVersion(gradleVersion)
				.build();

		BuildTask scanClasspath = buildResult.task(":scanJoinfacesClasspath");
		assertThat(scanClasspath).isNotNull();
		assertThat(scanClasspath.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

		File jarFile = projectDir.resolve("build/libs/dummy.jar").toFile();
		assertThat(jarFile).isFile();

		try (ZipFile zipFile = new ZipFile(jarFile)) {
			assertThat(zipFile.getEntry("META-INF/joinfaces/org.apache.myfaces.ee.MyFacesContainerInitializer.classes")).isNotNull();
			assertThat(zipFile.getEntry("META-INF/joinfaces/org.apache.myfaces.spi.AnnotationProvider.classes")).isNotNull();
		}
	}
}
