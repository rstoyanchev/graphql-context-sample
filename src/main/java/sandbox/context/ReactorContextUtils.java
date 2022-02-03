/*
 * Copyright 2002-2022 the original author or authors.
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
package sandbox.context;

import java.util.List;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

import org.springframework.util.Assert;

/**
 * Utility methods to apply {@link ReactorContextAccessor} to a
 * {@link ContextContainer} without introducing a dependency on Reactor in
 * {@link ContextContainer}.
 */
public class ReactorContextUtils {

	private final static String ACCESSORS_KEY = "REACTOR";


	/**
	 * Create a {@link ContextContainer} with the given ThreadLocal and Reactor
	 * Context accessors. A shortcut for creating the container first with
	 * {@link ContextContainer#create(List)} and then calling
	 * {@link ReactorContextUtils#setAccessors(ContextContainer, List)}.
	 */
	public static ContextContainer create(
			List<ThreadLocalAccessor> threadLocalAccessors, List<ReactorContextAccessor> reactorAccessors) {

		ContextContainer container = ContextContainer.create(threadLocalAccessors);
		container.setAccessors(ACCESSORS_KEY, reactorAccessors);
		return container;
	}

	/**
	 * Set the given Reactor accessors on an already existing {@link ContextContainer}.
	 */
	public static void setAccessors(ContextContainer contextContainer, List<ReactorContextAccessor> accessors) {
		contextContainer.setAccessors(ACCESSORS_KEY, accessors);
	}

	/**
	 * Capture Reactor context values and save them in the given
	 * {@link ContextContainer}.
	 */
	public static void captureReactorContext(Context context, ContextContainer container) {
		List<ReactorContextAccessor> accessors = container.getAccessors(ACCESSORS_KEY);
		accessors.forEach(accessor -> accessor.captureValues(context, container));
	}

	/**
	 * Restore Reactor context values previously saved in the given
	 * {@link ContextContainer}.
	 */
	public static Context restoreReactorContext(Context context, ContextContainer container) {
		List<ReactorContextAccessor> accessors = container.getAccessors(ACCESSORS_KEY);
		for (ReactorContextAccessor accessor : accessors) {
			context = accessor.restoreValues(context, container);
		}
		return context;
	}

	/**
	 * Save the {@link ContextContainer} in the Reactor context.
	 */
	public static Context saveContainer(Context context, ContextContainer container) {
		return context.put(ContextContainer.class.getName(), container);
	}

	/**
	 * Retrieve a {@link ContextContainer} previously saved in the Reactor context.
	 */
	public static ContextContainer getContainer(ContextView contextView) {
		ContextContainer container = contextView.get(ContextContainer.class.getName());
		Assert.notNull(container, "ContextContainer not found");
		return container;
	}

}
