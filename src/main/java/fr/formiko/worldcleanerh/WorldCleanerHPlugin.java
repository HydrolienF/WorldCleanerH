package fr.formiko.worldcleanerh;

import fr.formiko.worldcleanerh.commands.CleanCommand;
import fr.formiko.worldcleanerh.commands.CleanTabCompleter;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldCleanerHPlugin extends JavaPlugin {
    private static Collection<Material> itemsToRemove = List.of();
    private static Collection<Material> blocksToRemove = List.of(Material.GRASS_BLOCK);
    public static WorldCleanerHPlugin plugin;

    @Override
    public void onEnable() {
        getCommand("clean").setExecutor(new CleanCommand());
        getCommand("clean").setTabCompleter(new CleanTabCompleter());
        plugin = this;
        // TODO add timer in config.
    }

    public static Collection<Material> getItemsToRemove() { return itemsToRemove; }
    public static Collection<Material> getBlocksToRemove() { return blocksToRemove; }

}