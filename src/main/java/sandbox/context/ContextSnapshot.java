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
package sandbox.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextSnapshot {

	private final Map<String, ThreadLocalAccessor> threadLocalAccessors = new ConcurrentHashMap<>();

	private final Map<String, Object> threadLocalValues = new ConcurrentHashMap<>();


	public ContextSnapshot(Map<String, ThreadLocalAccessor> accessors) {
		this.threadLocalAccessors.putAll(accessors);
	}


	public void captureThreadLocalValues() {
		this.threadLocalAccessors.values().forEach(accessor -> accessor.extractValues(this.threadLocalValues));
	}

	public void restoreThreadLocalValues() {
		this.threadLocalAccessors.values().forEach(accessor -> accessor.restoreValues(threadLocalValues));
	}

	public void resetValues() {
		this.threadLocalAccessors.values().forEach(accessor -> accessor.resetValues(threadLocalValues));
	}

}
