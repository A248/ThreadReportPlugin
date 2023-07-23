package space.arim.threadreport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class ThreadDumpDestination {

	private final Path destinationFolder;

	private static final System.Logger LOGGER = System.getLogger(ThreadDumpDestination.class.getName());

	ThreadDumpDestination(Path destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	void performThreadDump() {
		Path destination = freeFile();
		writeThreadDump(destination);
		LOGGER.log(System.Logger.Level.INFO, "Thread dump saved to " + destination);
	}

	private Path freeFile() {
		Path destinationFolder = this.destinationFolder;
		Path freeFile;
		int number = 0;
		do {
			freeFile = destinationFolder.resolve("thread-dump-" + number++ + ".txt");
		} while (Files.exists(freeFile));
		return freeFile;
	}

	private static void writeThreadDump(Path destination) {
		try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
				format(writer, threadInfo);
				writer.append('\n');
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	private static void format(Appendable output, ThreadInfo thread) throws IOException {
		output.append("\"" + thread.getThreadName() + "\"" + (thread.isDaemon() ? " daemon" : "") + " prio=" + thread.getPriority() + " Id=" + thread.getThreadId() + " " + thread.getThreadState());
		if (thread.getLockName() != null) {
			output.append(" on " + thread.getLockName());
		}

		if (thread.getLockOwnerName() != null) {
			String var10001 = thread.getLockOwnerName();
			output.append(" owned by \"" + var10001 + "\" Id=" + thread.getLockOwnerId());
		}

		if (thread.isSuspended()) {
			output.append(" (suspended)");
		}

		if (thread.isInNative()) {
			output.append(" (in native)");
		}

		output.append('\n');

		var stackTrace = thread.getStackTrace();
		var lockedMonitors = thread.getLockedMonitors();
		for (int i = 0; i < stackTrace.length; ++i) {
			StackTraceElement ste = stackTrace[i];
			output.append("\tat " + ste.toString());
			output.append('\n');
			if (i == 0 && thread.getLockInfo() != null) {
				Thread.State ts = thread.getThreadState();
				switch (ts) {
				case BLOCKED:
					output.append("\t-  blocked on " + thread.getLockInfo());
					output.append('\n');
					break;
				case WAITING:
				case TIMED_WAITING:
					output.append("\t-  waiting on " + thread.getLockInfo());
					output.append('\n');
					break;
				}
			}

			for (MonitorInfo mi : lockedMonitors) {
				if (mi.getLockedStackDepth() == i) {
					output.append("\t-  locked " + mi);
					output.append('\n');
				}
			}
		}

		LockInfo[] locks = thread.getLockedSynchronizers();
		if (locks.length > 0) {
			output.append("\n\tNumber of locked synchronizers = " + locks.length);
			output.append('\n');
			for (LockInfo li : locks) {
				output.append("\t- " + li);
				output.append('\n');
			}
		}

		output.append('\n');
	}

}
