package sandbox.graphql.context;

import sandbox.context.ContextSnapshot;
import sandbox.context.Scope;
import sandbox.context.ScopeProvider;

import org.springframework.stereotype.Component;

@Component
public class ContextSnapshotScopeProvider implements ScopeProvider {

	@Override
	public Scope apply(ContextSnapshot contextSnapshot) {
		return new MyScope();
	}

	static class MyScope implements Scope {

		@Override
		public Scope open(ContextSnapshot propagationContext) {
			ContextSnapshotThreadLocalHolder.setValue(propagationContext);
			return this;
		}

		@Override
		public void close() {
			ContextSnapshotThreadLocalHolder.remove();
		}
	}
}
