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

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import reactor.core.publisher.Mono;
import sandbox.graphql.context.CustomThreadLocalHolder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class GraphQlConfig {

	private static final Map<String, DataFetcher<?>> dataFetchers = new LinkedHashMap<>();

	static {
		// Imperative DataFetcher
		dataFetchers.put("greeting", env -> "Hello " + CustomThreadLocalHolder.getValue());

		// Reactive DataFetcher
		dataFetchers.put("greetingMono", env ->
				Mono.delay(Duration.ofMillis(10)).map(aLong -> "Hello")
		);
	}

	@Bean ExecutorService executorService() {
		return Executors.newSingleThreadExecutor();
	}

	@Bean
	GraphQL graphQl() throws IOException {
		Resource schemaResource = new ClassPathResource("schema.graphqls");
		TypeDefinitionRegistry registry = new SchemaParser().parse(schemaResource.getInputStream());

		RuntimeWiring.Builder wiringBuilder = RuntimeWiring.newRuntimeWiring();
		for (Map.Entry<String, DataFetcher<?>> entry : dataFetchers.entrySet()) {
			wiringBuilder.type("Query", builder -> {
				DataFetcher<?> dataFetcher = new DataFetcherAdapter<>(entry.getValue());
				return builder.dataFetcher(entry.getKey(), dataFetcher);
			});
		}
		RuntimeWiring runtimeWiring = wiringBuilder.build();

		GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
		return GraphQL.newGraphQL(schema).build();
	}

}
