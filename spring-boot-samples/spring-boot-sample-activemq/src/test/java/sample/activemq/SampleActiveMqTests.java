/*
 * Copyright 2012-2019 the original author or authors.
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

package sample.activemq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.extension.OutputCaptureExtension;
import org.springframework.boot.test.io.CapturedOutput;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for demo application.
 *
 * @author Eddú Meléndez
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SampleActiveMqTests {

	@Autowired
	private Producer producer;

	@Test
	void sendSimpleMessage(CapturedOutput capturedOutput) throws InterruptedException {
		this.producer.send("Test message");
		Thread.sleep(1000L);
		assertThat(capturedOutput).contains("Test message");
	}

}
