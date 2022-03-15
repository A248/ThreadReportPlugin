package space.arim.threadreport;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class Task {

	private final Duration period;
	private final ThreadDumpDestination destination;

	private final AtomicInteger state = new AtomicInteger();

	private static final int NOT_STARTED = 0;
	private static final int RUNNING = 1;
	private static final int SHUTDOWN = 2;

	Task(Duration period, ThreadDumpDestination destination) {
		this.period = period;
		this.destination = destination;
	}

	void start() {
		if (!state.compareAndSet(NOT_STARTED, RUNNING)) {
			throw new IllegalStateException("Already started");
		}
		executeAndReschedule();
	}

	private void executeAndReschedule() {
		if (state.get() == SHUTDOWN) {
			return;
		}
		destination.performThreadDump();
		if (state.get() == SHUTDOWN) {
			return;
		}
		CompletableFuture.delayedExecutor(period.toNanos(), TimeUnit.NANOSECONDS)
				.execute(this::executeAndReschedule);
	}

	void cancel() {
		if (!state.compareAndSet(RUNNING, SHUTDOWN)) {
			throw new IllegalStateException("Not started");
		}
	}
}
