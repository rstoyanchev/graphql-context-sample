package sandbox.graphql.context;

import java.util.Map;

import sandbox.context.ContextSnapshot;
import sandbox.context.ThreadLocalAccessor;

import org.springframework.stereotype.Component;

@Component
public class ContextSnapshotThreadLocalAccessor implements ThreadLocalAccessor {

	private static final String KEY = ContextSnapshotThreadLocalAccessor.class.getName();

	@Override
	public void extractValues(Map<String, Object> container) {
		ContextSnapshot contextSnapshot = ContextSnapshotThreadLocalHolder.getValue();
		if (contextSnapshot != null) {
			container.put(KEY, contextSnapshot);
		}
	}

	@Override
	public void restoreValues(Map<String, Object> values) {
		if (values.containsKey(KEY)) {
			ContextSnapshotThreadLocalHolder.setValue((ContextSnapshot) values.get(KEY));
		}
	}

	@Override
	public void resetValues(Map<String, Object> values) {
		ContextSnapshotThreadLocalHolder.remove();
	}
}