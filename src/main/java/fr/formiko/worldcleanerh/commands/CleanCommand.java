package fr.formiko.worldcleanerh.commands;

import fr.formiko.worldcleanerh.WorldCleanerHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CleanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("entities")) {
                sender.sendMessage("Cleaning entities...");

                return true;
            } else if (args[0].equalsIgnoreCase("blocks")) {
                cleanBlocks(sender);
                return true;
            }
        }
        sender.sendMessage("Usage: /clean <entities|blocks>");
        return false;
    }

    private static void cleanBlocks(CommandSender sender) {
        sender.sendMessage("Cleaning blocks...");

        new BukkitRunnable() {
            private long printTime, execTime;

            @Override
            public void run() {
                execTime = System.currentTimeMillis();
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Block block = WorldSelectorHPlugin.getSelector().nextBlock();
                    if (WorldCleanerHPlugin.getBlocksToRemove().contains(block.getType())) {
                        block.setType(Material.AIR);
                    }
                }
                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    sender.sendMessage("Progress: " + WorldSelectorHPlugin.getSelector().progress() * 100 + "%");
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    sender.sendMessage("Done.");
                    cancel();
                }
            }
        }.runTaskTimer(WorldCleanerHPlugin.plugin, 0, 1);
    }

}

class LongContainer {
    private long value;

    public LongContainer(long value) { this.value = value; }

    public long getValue() { return value; }

    public void setValue(long value) { this.value = value; }
}
