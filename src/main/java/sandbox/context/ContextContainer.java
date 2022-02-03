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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Holds context values to be propagated different context environments along
 * with the accessors required to propagate to and from those environments.
 */
public class ContextContainer {

	private final Map<String, Object> values = new ConcurrentHashMap<>();

	private final List<ThreadLocalAccessor> threadLocalAccessors;

	private final Map<String, List<?>> accessors = new ConcurrentHashMap<>(1);


	private ContextContainer(List<ThreadLocalAccessor> accessors) {
		this.threadLocalAccessors = new ArrayList<>(accessors);
	}


	public Object get(String key) {
		return this.values.get(key);
	}

	public boolean containsKey(String key) {
		return this.values.containsKey(key);
	}

	public Object put(String key, Object value) {
		return this.values.put(key, value);
	}

	public Object remove(String key) {
		return this.values.remove(key);
	}


	public <A> void setAccessors(String key, List<A> accessors) {
		this.accessors.put(key, accessors);
	}

	@SuppressWarnings("unchecked")
	public <A> List<A> getAccessors(String key) {
		return (List<A>) this.accessors.getOrDefault(key, Collections.emptyList());
	}


	public void captureThreadLocalValues() {
		this.threadLocalAccessors.forEach(accessor -> accessor.captureValues(this));
	}

	public Scope restoreThreadLocalValues() {
		this.threadLocalAccessors.forEach(accessor -> accessor.restoreValues(this));
		return () -> this.threadLocalAccessors.forEach(accessor -> accessor.resetValues(this));
	}


	/**
	 * Create an instance with the given ThreadLocalAccessors to use.
	 */
	public static ContextContainer create(List<ThreadLocalAccessor> accessors) {
		return new ContextContainer(accessors);
	}


	/**
	 * Demarcates the scope of restored ThreadLocal values.
	 */
	public interface Scope extends AutoCloseable {

		@Override
		void close();

	}
}
