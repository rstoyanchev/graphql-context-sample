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
import sandbox.context.ContextSnapshot;
import sandbox.context.Scope;
import sandbox.context.ThreadLocalAccessor;
import sandbox.context.ScopeProvider;
import sandbox.graphql.context.ContextSnapshotThreadLocalAccessor;
import sandbox.graphql.context.ContextSnapshotThreadLocalHolder;
import sandbox.graphql.context.CustomThreadLocalHolder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphQlController {

	private static final Logger log = LoggerFactory.getLogger(GraphQlController.class);

	private final GraphQL graphQL;

	private final Map<String, ThreadLocalAccessor> threadLocalAccessors;

	private final List<ScopeProvider> scopes;

	private final ExecutorService executor;


	public GraphQlController(GraphQL graphQL, ExecutorService executor, Map<String, ThreadLocalAccessor> threadLocalAccessors, List<ScopeProvider> scopes) {
		this.graphQL = graphQL;
		this.executor = executor;
		this.threadLocalAccessors = threadLocalAccessors;
		this.scopes = scopes;
	}

	@PostMapping("/graphql")
	Mono<Map<String, Object>> handle(@RequestBody Map<String, Object> body) {
		return Mono.deferContextual(contextView -> {
					ContextSnapshot snapshot = contextView.get(ContextSnapshot.class.getName());

					try (Scope scope = snapshot.open()) {
						log.info("FOO 1 {}", CustomThreadLocalHolder.getValue());
						assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
						assertThatValueIsEqualTo("bar", snapshot.get("foo"));
						executor.execute(new InstrumentedRunnable(snapshot, () -> {
							assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
							assertThatValueIsEqualTo("bar", snapshot.get("foo"));
							assertThatValueIsEqualTo("bar", ContextSnapshotThreadLocalHolder.getValue().get("foo"));
							assertThatValueIsEqualTo(snapshot, ContextSnapshotThreadLocalHolder.getValue());
						}));
						executor.execute(new InstrumentedRunnable(() -> {
							assertThatValueIsEqualTo("007", CustomThreadLocalHolder.getValue());
							assertThatValueIsEqualTo("bar", snapshot.get("foo"));
							assertThatValueIsEqualTo("bar", ContextSnapshotThreadLocalHolder.getValue().get("foo"));
							assertThatValueIsEqualTo(snapshot, ContextSnapshotThreadLocalHolder.getValue());
						}));
					}

					assertThatValueIsEqualTo(null, CustomThreadLocalHolder.getValue());
					assertThatValueIsEqualTo(null, ContextSnapshotThreadLocalHolder.getValue());

					ExecutionInput input = executionInput(body, contextView);
					return Mono.fromFuture(this.graphQL.executeAsync(input)).map(ExecutionResult::toSpecification);
				})
				.subscribeOn(Schedulers.boundedElastic())  // Switch threads to test context passing
				.contextWrite(context -> {
					// Capture + save ThreadLocal's in Reactor Context
					ContextSnapshot snapshot = new ContextSnapshot(this.threadLocalAccessors, scopes);
					snapshot.captureThreadLocalValues();
					snapshot.put("foo", "bar");
					return context.put(ContextSnapshot.class.getName(), snapshot);
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
		return ExecutionInput.newExecutionInput()
				.query((String) body.get("query"))
				.graphQLContext(Collections.singletonMap(ContextView.class.getName(), contextView)) // Tunnel Reactor context through GraphQL
				.build();
	}

}
