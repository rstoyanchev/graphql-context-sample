package sandbox.graphql.context;

import sandbox.context.ContextSnapshot;
import sandbox.context.Scope;
import sandbox.context.ScopeProvider;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalScopeProvider implements ScopeProvider {

	@Override
	public Scope apply(ContextSnapshot contextSnapshot) {
		return new MyScope(contextSnapshot);
	}

	static class MyScope implements Scope {

		private final ContextSnapshot contextSnapshot;

		MyScope(ContextSnapshot contextSnapshot) {
			this.contextSnapshot = contextSnapshot;
			contextSnapshot.captureThreadLocalValues();
		}

		@Override
		public Scope open(ContextSnapshot propagationContext) {
			this.contextSnapshot.restoreThreadLocalValues();
			return this;
		}

		@Override
		public void close() {
			this.contextSnapshot.resetValues();
		}
	}
}
