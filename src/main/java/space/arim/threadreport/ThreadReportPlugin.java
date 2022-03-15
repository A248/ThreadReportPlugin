package space.arim.threadreport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class ThreadReportPlugin extends JavaPlugin {

	private ThreadDumpDestination manualDestination;
	private ThreadDumpDestination periodicDestination;

	private Task task;

	@Override
	public void onEnable() {
		Path dataFolder = getDataFolder().toPath();
		Path manualDestination = dataFolder.resolve("by-command");
		Path periodicDestination = dataFolder.resolve("by-periodic-task");
		try {
			Files.createDirectories(manualDestination);
			Files.createDirectories(periodicDestination);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		this.manualDestination = new ThreadDumpDestination(manualDestination);
		this.periodicDestination = new ThreadDumpDestination(periodicDestination);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage("This must be run from the console");
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("task")) {
			if (task == null) {
				task = new Task(Duration.ofSeconds(5L), periodicDestination);
				task.start();
				sender.sendMessage("Started the task. An automatic thread dump will be taken every 5 seconds");
			} else {
				task.cancel();
				task = null;
				sender.sendMessage("Ended the task.");
			}
			return true;
		}
		// Perform this on the main thread: We want a true snapshot at this exact time, without delay
		manualDestination.performThreadDump();
		return true;
	}

}
