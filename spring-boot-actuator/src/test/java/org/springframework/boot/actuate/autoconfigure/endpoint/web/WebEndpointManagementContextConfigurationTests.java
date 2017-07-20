/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.endpoint.AuditEventsEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.web.AuditEventsWebEndpointExtension;
import org.springframework.boot.actuate.endpoint.web.HealthWebEndpointExtension;
import org.springframework.boot.actuate.endpoint.web.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.LogFileWebEndpoint;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ApplicationContextTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link WebEndpointManagementContextConfiguration}.
 *
 * @author Andy Wilkinson
 */
public class WebEndpointManagementContextConfigurationTests {

	@Test
	public void heapDumpWebEndpointIsAutoConfigured() {
		beanIsAutoConfigured(HeapDumpWebEndpoint.class);
	}

	@Test
	public void heapDumpWebEndpointCanBeDisabled() {
		beanIsNotAutoConfiguredWhenEndpointIsDisabled(HeapDumpWebEndpoint.class,
				"heapdump");
	}

	@Test
	public void healthWebEndpointExtensionIsAutoConfigured() {
		beanIsAutoConfigured(HealthWebEndpointExtension.class,
				HealthEndpointConfiguration.class);
	}

	@Test
	public void healthStatusMappingCanBeCustomized() {
		ApplicationContextTester contextTester = context()
				.withPropertyValues("endpoints.health.mapping.CUSTOM=500")
				.withUserConfiguration(HealthEndpointConfiguration.class);
		contextTester.run(context -> {
			HealthWebEndpointExtension extension = context
					.getBean(HealthWebEndpointExtension.class);
			@SuppressWarnings("unchecked")
			Map<String, Integer> statusMappings = (Map<String, Integer>) ReflectionTestUtils
					.getField(extension, "statusMapping");
			assertThat(statusMappings).containsEntry("DOWN", 503);
			assertThat(statusMappings).containsEntry("OUT_OF_SERVICE", 503);
			assertThat(statusMappings).containsEntry("CUSTOM", 500);
		});
	}

	@Test
	public void healthWebEndpointExtensionCanBeDisabled() {
		beanIsNotAutoConfiguredWhenEndpointIsDisabled(HealthWebEndpointExtension.class,
				"health", HealthEndpointConfiguration.class);
	}

	@Test
	public void auditEventsWebEndpointExtensionIsAutoConfigured() {
		beanIsAutoConfigured(AuditEventsWebEndpointExtension.class,
				AuditEventsEndpointConfiguration.class);
	}

	@Test
	public void auditEventsWebEndpointExtensionCanBeDisabled() {
		beanIsNotAutoConfiguredWhenEndpointIsDisabled(
				AuditEventsWebEndpointExtension.class, "auditevents",
				AuditEventsEndpointConfiguration.class);
	}

	@Test
	public void logFileWebEndpointIsAutoConfiguredWhenLoggingFileIsSet() {
		context().withPropertyValues("logging.file:test.log").run((context) -> {
			assertThat(context.getBeansOfType(LogFileWebEndpoint.class)).hasSize(1);
		});
	}

	@Test
	public void logFileWebEndpointIsAutoConfiguredWhenLoggingPathIsSet() {
		context().withPropertyValues("logging.path:test/logs").run((context) -> {
			assertThat(context.getBeansOfType(LogFileWebEndpoint.class)).hasSize(1);
		});
	}

	@Test
	public void logFileWebEndpointIsAutoConfiguredWhenExternalFileIsSet() {
		context().withPropertyValues("endpoints.logfile.external-file:external.log")
				.run((context) -> {
					assertThat(context.getBeansOfType(LogFileWebEndpoint.class))
							.hasSize(1);
				});
	}

	@Test
	public void logFileWebEndpointCanBeDisabled() {
		context()
				.withPropertyValues("logging.file:test.log",
						"endpoints.logfile.enabled:false")
				.run((context) -> assertThat(context)
						.hasSingleBean(LogFileWebEndpoint.class));
	}

	private void beanIsAutoConfigured(Class<?> beanType, Class<?>... config) {
		context().withUserConfiguration(config)
				.run((context) -> assertThat(context).hasSingleBean(beanType));
	}

	private void beanIsNotAutoConfiguredWhenEndpointIsDisabled(Class<?> webExtension,
			String id, Class<?>... config) {
		context().withPropertyValues("endpoints." + id + ".enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(webExtension));
	}

	private ApplicationContextTester context() {
		return new ApplicationContextTester().withConfiguration(
				AutoConfigurations.of(WebEndpointManagementContextConfiguration.class));
	}

	@Configuration
	static class HealthEndpointConfiguration {

		@Bean
		public HealthEndpoint healthEndpoint() {
			return new HealthEndpoint(new OrderedHealthAggregator(),
					Collections.emptyMap());
		}

	}

	@Configuration
	static class AuditEventsEndpointConfiguration {

		@Bean
		public AuditEventsEndpoint auditEventsEndpoint() {
			return new AuditEventsEndpoint(mock(AuditEventRepository.class));
		}

	}

}
