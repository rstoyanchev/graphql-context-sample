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

import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Mono;
import sandbox.context.ContextContainer;
import sandbox.context.ContextContainer.Scope;
import sandbox.context.ReactorContextUtils;

import org.springframework.util.Assert;

public class DataFetcherAdapter<T> implements DataFetcher<T> {

	private final DataFetcher<T> delegate;


	public DataFetcherAdapter(DataFetcher<T> delegate) {
		this.delegate = delegate;
	}


	@Override
	@SuppressWarnings("unchecked")
	public T get(DataFetchingEnvironment environment) throws Exception {

		GraphQLContext graphQlContext = environment.getGraphQlContext();
		ContextContainer container = graphQlContext.get(ContextContainer.class.getName());
		Assert.notNull(container, "No Reactor Context");

		Object result;
		try (Scope scope = container.restoreThreadLocalValues()) {
			result = this.delegate.get(environment);
		}

		if (result instanceof Mono) {
			Mono<?> valueMono = (Mono<?>) result;
			result = valueMono.contextWrite(context -> ReactorContextUtils.restoreReactorContext(context, container)).toFuture();
		}

		return (T) result;
	}

}
