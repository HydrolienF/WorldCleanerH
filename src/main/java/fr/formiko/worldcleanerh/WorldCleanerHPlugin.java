package fr.formiko.worldcleanerh;

import fr.formiko.worldcleanerh.commands.CleanCommand;
import fr.formiko.worldcleanerh.commands.CleanTabCompleter;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldCleanerHPlugin extends JavaPlugin {
    private static Collection<EntityType> entitiesToRemove = List.of(EntityType.DROPPED_ITEM, EntityType.VILLAGER);
    private static Collection<Material> blocksToRemove = List.of(Material.SPAWNER, Material.RAIL);
    private static Collection<Material> blocksToUpdate = List.of(Material.WATER, Material.LAVA);
    private static Collection<Material> blocksToSupport = List.of(Material.SAND, Material.RED_SAND, Material.GRAVEL);
    private static Map<Material, Material> supportBlocks = Map.of(Material.SAND, Material.SANDSTONE, Material.RED_SAND,
            Material.RED_SANDSTONE, Material.GRAVEL, Material.ANDESITE);
    private static Collection<Material> blockThatCanBeFlood = List.of(Material.AIR, Material.GRASS, Material.TALL_GRASS, Material.FERN,
            Material.SNOW, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM, Material.WHEAT, Material.SUGAR_CANE, Material.BAMBOO_SAPLING, Material.BAMBOO, Material.CACTUS,
            Material.VINE, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY, Material.POTATOES, Material.CARROTS,
            Material.BEETROOTS);
    private static Collection<Material> blocksThatCantFly = blockThatCanBeFlood.stream()
            .filter(material -> material != Material.AIR && material != Material.VINE).toList();

    public static WorldCleanerHPlugin plugin;
    public static final Random random = new Random();

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
    public static Collection<Material> getBlocksToSupport() { return blocksToSupport; }
    public static Material getSupportBlock(Material block) { return supportBlocks.get(block); }
    public static Collection<Material> getBlocksThatCantFly() { return blocksThatCantFly; }
    public static Collection<Material> getBlockThatCanBeFlood() { return blockThatCanBeFlood; }


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