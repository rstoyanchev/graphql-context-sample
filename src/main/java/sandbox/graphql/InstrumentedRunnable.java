package sandbox.graphql;

import sandbox.context.ContextContainer;
import sandbox.context.ContextContainer.Scope;

public class InstrumentedRunnable implements Runnable {

	private final ContextContainer values;

	private final Runnable delegate;

	public InstrumentedRunnable(ContextContainer values, Runnable delegate) {
		this.values = values;
		this.delegate = delegate;
	}

	@Override
	public void run() {
		try (Scope scope = values.restoreThreadLocalValues()) {
			this.delegate.run();
		}
	}
}
