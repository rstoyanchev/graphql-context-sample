package sandbox.graphql;

import sandbox.context.ContextSnapshot;
import sandbox.context.Scope;
import sandbox.graphql.context.ContextSnapshotThreadLocalAccessor;
import sandbox.graphql.context.ContextSnapshotThreadLocalHolder;

public class InstrumentedRunnable implements Runnable {

	private final ContextSnapshot contextSnapshot;

	private final Runnable delegate;

	public InstrumentedRunnable(ContextSnapshot contextSnapshot, Runnable delegate) {
		this.contextSnapshot = contextSnapshot;
		this.delegate = delegate;
	}

	public InstrumentedRunnable(Runnable delegate) {
		this(ContextSnapshotThreadLocalHolder.getValue(), delegate);
	}

	@Override
	public void run() {
		try (Scope scope = contextSnapshot.open()) {
			this.delegate.run();
		}
	}
}
