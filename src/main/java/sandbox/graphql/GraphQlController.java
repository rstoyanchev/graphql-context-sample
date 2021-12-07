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

import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphQlController {

	private final GraphQL graphQL;


	public GraphQlController(GraphQL graphQL) {
		this.graphQL = graphQL;
	}


	@PostMapping("/graphql")
	Mono<Map<String, Object>> handle(@RequestBody Map<String, Object> body) {

		ExecutionInput input = ExecutionInput.newExecutionInput()
				.query((String) body.get("query"))
				.build();

		return Mono.deferContextual(contextView -> Mono.fromFuture(this.graphQL.executeAsync(input)))
				.map(ExecutionResult::toSpecification)
				.subscribeOn(Schedulers.boundedElastic()); // Switch threads for context passing
	}

}
