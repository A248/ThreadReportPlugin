package space.arim.threadreport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
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
				writer.append(threadInfo.toString());
				writer.append('\n');
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
