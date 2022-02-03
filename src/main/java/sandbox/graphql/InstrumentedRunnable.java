package sandbox.graphql;

import sandbox.context.ContextSnapshot;
import sandbox.context.ContextSnapshot.Scope;

public class InstrumentedRunnable implements Runnable {

	private final ContextSnapshot contextSnapshot;

	private final Runnable delegate;

	public InstrumentedRunnable(ContextSnapshot contextSnapshot, Runnable delegate) {
		this.contextSnapshot = contextSnapshot;
		this.delegate = delegate;
	}

	@Override
	public void run() {
		try (Scope scope = contextSnapshot.restoreThreadLocalValues()) {
			this.delegate.run();
		}
	}
}
