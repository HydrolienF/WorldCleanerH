package fr.formiko.worldcleanerh;

import fr.formiko.worldcleanerh.commands.CleanCommand;
import fr.formiko.worldcleanerh.commands.CleanTabCompleter;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldCleanerHPlugin extends JavaPlugin {
    private static Collection<EntityType> entitiesToRemove = List.of(EntityType.DROPPED_ITEM, EntityType.VILLAGER);
    private static Collection<Material> blocksToRemove = List.of(Material.SPAWNER, Material.RAIL);
    private static Collection<Material> blocksToUpdate = List.of(Material.WATER, Material.LAVA);
    public static WorldCleanerHPlugin plugin;

    @Override
    public void onEnable() {
        getCommand("clean").setExecutor(new CleanCommand());
        getCommand("clean").setTabCompleter(new CleanTabCompleter());
        plugin = this;
        // TODO add timer in config.
    }

    public static Collection<EntityType> getEntitiesToRemove() { return entitiesToRemove; }
    public static Collection<Material> getBlocksToRemove() { return blocksToRemove; }
    public static Collection<Material> getBlocksToUpdate() { return blocksToUpdate; }


    public boolean saveData(Map dataToSave, String name) {
        File dataFile = new File("plugins/WorldCleanerH/" + name + ".yml");
        File parentFile = dataFile.getParentFile();
        parentFile.mkdirs();
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        try {
            data.set(name, dataToSave);
            data.save(dataFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}