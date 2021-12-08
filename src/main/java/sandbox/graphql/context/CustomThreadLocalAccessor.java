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
package sandbox.graphql.context;

import java.util.Map;

import sandbox.context.ThreadLocalAccessor;

import org.springframework.stereotype.Component;

@Component
public class CustomThreadLocalAccessor implements ThreadLocalAccessor {

	private static final String KEY = CustomThreadLocalAccessor.class.getName();

	@Override
	public void extractValues(Map<String, Object> container) {
		String value = CustomThreadLocalHolder.getValue();
		if (value != null) {
			container.put(KEY, CustomThreadLocalHolder.getValue());
		}
	}

	@Override
	public void restoreValues(Map<String, Object> values) {
		if (values.containsKey(KEY)) {
			CustomThreadLocalHolder.setValue((String) values.get(KEY));
		}
	}

	@Override
	public void resetValues(Map<String, Object> values) {
		CustomThreadLocalHolder.remove();
	}

}
