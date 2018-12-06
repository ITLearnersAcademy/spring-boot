/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.build;

import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPomDeveloperSpec;
import org.gradle.api.publish.maven.MavenPomLicenseSpec;
import org.gradle.api.publish.maven.MavenPomScm;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Plugin to apply conventions to projects that are part of Spring Boot's build.
 *
 * @author Andy Wilkinson
 */
public class ConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(MavenPublishPlugin.class,
				(plugin) -> customizeMavenPublishing(project));
	}

	private void customizeMavenPublishing(Project project) {
		PublishingExtension publishing = project.getExtensions()
				.getByType(PublishingExtension.class);
		if (project.hasProperty("deploymentRepository")) {
			publishing.getRepositories().maven((mavenRepository) -> {
				mavenRepository.setUrl(project.property("deploymentRepository"));
			});
		}
		NamedDomainObjectSet<MavenPublication> publications = publishing.getPublications()
				.withType(MavenPublication.class);
		if (publications.isEmpty()) {
			publishing.getPublications().create("maven", MavenPublication.class,
					(publication) -> {
						SoftwareComponent java = project.getComponents()
								.getByName("java");
						publication.from(java);
					});
		}
		publications.all((publication) -> publication.pom(this::customizePom));
	}

	private void customizePom(MavenPom pom) {
		pom.getUrl().set("https://projects.spring.io/spring-boot/#");
		pom.licenses(this::customizeLicences);
		pom.developers(this::customizeDevelopers);
		pom.scm(this::customizeScm);
	}

	private void customizeLicences(MavenPomLicenseSpec licences) {
		licences.license((licence) -> {
			licence.getName().set("Apache License, Version 2.0");
			licence.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0");
		});
	}

	private void customizeDevelopers(MavenPomDeveloperSpec developers) {
		developers.developer((developer) -> {
			developer.getName().set("Pivotal");
			developer.getEmail().set("info@pivotal.io");
			developer.getOrganization().set("Pivotal Software, Inc.");
			developer.getOrganizationUrl().set("http://www.spring.io");
		});
	}

	private void customizeScm(MavenPomScm scm) {
		scm.getUrl().set("https://github.com/spring-projects/spring-boot");
	}

}
