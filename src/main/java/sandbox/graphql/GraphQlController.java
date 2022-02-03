/*
 * Copyright 2002-2021 the original author or authors.
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
package sandbox.graphql;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;
import sandbox.context.ContextContainer;
import sandbox.context.ContextContainer.Scope;
import sandbox.context.ReactorContextAccessor;
import sandbox.context.ReactorContextUtils;
import sandbox.context.ThreadLocalAccessor;
import sandbox.graphql.context.CustomThreadLocalHolder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphQlController {

	private static final Logger log = LoggerFactory.getLogger(GraphQlController.class);

	private final GraphQL graphQL;

	private final ExecutorService executor;

	private final List<ThreadLocalAccessor> threadLocalAccessors;

	private final List<ReactorContextAccessor> reactorAccessors;


	public GraphQlController(GraphQL graphQL, ExecutorService executor,
			List<ThreadLocalAccessor> threadLocalAccessors, List<ReactorContextAccessor> reactorAccessors) {

		this.graphQL = graphQL;
		this.executor = executor;
		this.threadLocalAccessors = threadLocalAccessors;
		this.reactorAccessors = reactorAccessors;
	}


	@PostMapping("/graphql")
	Mono<Map<String, Object>> handle(@RequestBody Map<String, Object> body) {
		return Mono.deferContextual(contextView -> {
					ContextContainer container = contextView.get(ContextContainer.class.getName());

					try (Scope scope = container.restoreThreadLocalValues()) {
						log.info("FOO 1 {}", CustomThreadLocalHolder.getValue());
						assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
						assertThatValueIsEqualTo("bar", container.get("foo"));
						executor.execute(new InstrumentedRunnable(container, () -> {
							assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
							assertThatValueIsEqualTo("bar", container.get("foo"));
						}));
						executor.execute(new InstrumentedRunnable(container, () -> {
							assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
							assertThatValueIsEqualTo("bar", container.get("foo"));
						}));
					}

					assertThatValueIsEqualTo(null, CustomThreadLocalHolder.getValue());

					ExecutionInput input = executionInput(body, contextView);
					return Mono.fromFuture(this.graphQL.executeAsync(input)).map(ExecutionResult::toSpecification);
				})
				.subscribeOn(Schedulers.boundedElastic())  // Switch threads to test context passing
				.contextWrite(context -> {
					// Create ContextContainer
					ContextContainer container = ReactorContextUtils.create(this.threadLocalAccessors, this.reactorAccessors);

					// Capture Reactor context values
					container.captureThreadLocalValues();

					// Capture Reactor context values
					ReactorContextUtils.captureReactorContext(context, container);

					// Add other values
					container.put("foo", "bar");

					// Save the container
					return ReactorContextUtils.saveContainer(context, container);
				});
	}

	private void assertThatValueIsEqualTo(Object expected, Object actual) {
		log.info("Checking if object {}, is equal to {}", actual, expected);
		if (!Objects.equals(expected, actual)) {
			log.error("Expected {}, got {}", expected, actual);
			throw new IllegalStateException("BOOM!");
		}
		log.info("Assertion passed!");
	}

	private ExecutionInput executionInput(Map<String, Object> body, ContextView contextView) {

		// Save ContextContainer in GraphQLContext
		Map<String, Object> map = Collections.singletonMap(
				ContextContainer.class.getName(), ReactorContextUtils.getContainer(contextView));

		return ExecutionInput.newExecutionInput()
				.query((String) body.get("query"))
				.graphQLContext(map)
				.build();
	}

}
