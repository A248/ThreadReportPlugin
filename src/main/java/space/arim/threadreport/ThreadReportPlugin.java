package space.arim.threadreport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ThreadReportPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		try {
			Files.createDirectories(dataFolder());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private Path dataFolder() {
		return getDataFolder().toPath();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage("This must be run from the console");
			return true;
		}
		Path destination = freeFile();
		threadDump(destination);
		sender.sendMessage("Thread dump saved to " + destination);
		return true;
	}

	private Path freeFile() {
		Path dataFolder = dataFolder();
		Path freeFile;
		int number = 0;
		do {
			freeFile = dataFolder.resolve("thread-dump-" + number++ + ".txt");
		} while (Files.exists(freeFile));
		return freeFile;
	}

	private void threadDump(Path destination) {
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
