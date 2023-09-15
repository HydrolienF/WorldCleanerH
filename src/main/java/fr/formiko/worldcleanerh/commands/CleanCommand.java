package fr.formiko.worldcleanerh.commands;

import fr.formiko.worldcleanerh.BoatType;
import fr.formiko.worldcleanerh.MineshaftBarrel;
import fr.formiko.worldcleanerh.WorldCleanerHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CleanCommand implements CommandExecutor {
    private static boolean runCleanEntities = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("entities")) {
                cleanEntities(sender);
            } else if (args[0].equalsIgnoreCase("blocks")) {
                cleanBlocks(sender);
            }
        } else {
            cleanBlocks(sender);
            runCleanEntities = true;
        }
        return true;
    }

    private static void cleanBlocks(CommandSender sender) {
        sender.sendMessage("Cleaning blocks...");
        final long startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            private long printTime, execTime, cpt, cptTotal;
            private Map<Material, Integer> cptByMaterialToRemove = WorldCleanerHPlugin.getBlocksToRemove().stream().collect(HashMap::new,
                    (map, material) -> map.put(material, 0), HashMap::putAll);
            private Map<Material, Integer> cptByMaterialToUpdate = WorldCleanerHPlugin.getBlocksToUpdate().stream().collect(HashMap::new,
                    (map, material) -> map.put(material, 0), HashMap::putAll);
            private Map<BoatType, List<String>> boatChestLocation = Stream.of(BoatType.values()).collect(HashMap::new,
                    (map, boatType) -> map.put(boatType, new LinkedList<>()), HashMap::putAll);


            @Override
            public void run() {
                execTime = System.currentTimeMillis();
                while (execTime + 45 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = -64; y < 120; y++) { // 256 is not needed and 120 will be enough to clean world.
                                Block block = chunk.getBlock(x, y, z);

                                // Block block = WorldSelectorHPlugin.getSelector().nextBlock();
                                if (WorldCleanerHPlugin.getBlocksToRemove().contains(block.getType())) {
                                    cptByMaterialToRemove.put(block.getType(), cptByMaterialToRemove.get(block.getType()) + 1);
                                    block.setType(Material.AIR);
                                    cpt++;
                                } else if (block.getState() instanceof Chest chest) {
                                    BoatType boatType = BoatType.randomBoatType();
                                    boatChestLocation.get(boatType).add(block.getX() + " " + block.getY() + " " + block.getZ());
                                    chest.getBlockInventory().setContents(generateBoatChestInventory(boatType));
                                    cpt++;
                                } else if (WorldCleanerHPlugin.getBlocksToSupport().contains(block.getType())
                                        && block.getRelative(BlockFace.DOWN).getType() == Material.AIR) { // no floating block
                                    block.getRelative(BlockFace.DOWN).setType(WorldCleanerHPlugin.getSupportBlock(block.getType()));
                                    cpt++;
                                } else if (block.getType() == Material.WATER && block.getY() >= 55) { // no watter flowing in surface
                                    for (Block b : List.of(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH),
                                            block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST))) {
                                        if (b.getType() == Material.AIR && b.getRelative(BlockFace.DOWN).getType() != Material.WATER) {
                                            b.setType(Material.GRASS_BLOCK);
                                        }
                                    }
                                    cpt++;
                                }
                                if (WorldCleanerHPlugin.getBlocksToUpdate().contains(block.getType())) {
                                    cptByMaterialToUpdate.put(block.getType(), cptByMaterialToUpdate.get(block.getType()) + 1);
                                    block.getState().update(true);
                                    cpt++;
                                }
                                cptTotal++;
                            }
                        }
                    }
                }
                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender, cpt, startTime);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    printProgress(sender, cpt, startTime);
                    sender.sendMessage(
                            "Edit " + cpt + "/" + cptTotal + " blocks. in " + Duration.ofMillis(System.currentTimeMillis() - startTime));
                    sender.sendMessage("By material to remove: " + cptByMaterialToRemove);
                    sender.sendMessage("By material to update: " + cptByMaterialToUpdate);
                    sender.sendMessage("By boat type: " + boatChestLocation.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue().size()).collect(java.util.stream.Collectors.joining(", ")));
                    WorldCleanerHPlugin.plugin.saveData(
                            boatChestLocation.entrySet().stream().collect(HashMap::new,
                                    (map, entry) -> map.put(entry.getKey().toString(), entry.getValue()), HashMap::putAll),
                            "boatChestLocation");
                    cancel();
                    if (runCleanEntities) {
                        WorldSelectorHPlugin.resetSelector();
                        cleanEntities(sender);
                    }
                }
            }
        }.runTaskTimer(WorldCleanerHPlugin.plugin, 0, 1);
    }

    private static void cleanEntities(CommandSender sender) {
        sender.sendMessage("Cleaning entities...");
        final long startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            private long printTime, execTime, cpt, cptTotal;
            private Map<EntityType, Integer> cptByEntity = WorldCleanerHPlugin.getEntitiesToRemove().stream().collect(HashMap::new,
                    (map, entity) -> map.put(entity, 0), HashMap::putAll);
            private Map<MineshaftBarrel, List<String>> mineshaftBarrelLocation = Stream.of(MineshaftBarrel.values()).collect(HashMap::new,
                    (map, mineshaftBarrel) -> map.put(mineshaftBarrel, new LinkedList<>()), HashMap::putAll);

            @Override
            public void run() {
                execTime = System.currentTimeMillis();
                while (execTime + 45 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    for (Entity entity : chunk.getEntities()) {
                        if (WorldCleanerHPlugin.getEntitiesToRemove().contains(entity.getType())) {
                            entity.remove();
                            cpt++;
                            cptByEntity.put(entity.getType(), cptByEntity.get(entity.getType()) + 1);
                        } else if (entity instanceof StorageMinecart container && entity.getType() == EntityType.MINECART_CHEST) {
                            // remove minecart chest without droping items
                            container.getInventory().clear();
                            entity.remove();
                            // replace with barrel
                            Block b = container.getWorld().getBlockAt(container.getLocation());
                            b.setType(Material.BARREL);
                            Barrel barrel = (Barrel) b.getState();
                            MineshaftBarrel mineshaftBarrel = MineshaftBarrel.randomMineshaftBarrel();
                            barrel.getInventory().setContents(generateMineshaftBarrelInventory(mineshaftBarrel));
                            cpt++;
                            // cptByMineshaftBarrel.put(mineshaftBarrel, cptByMineshaftBarrel.get(mineshaftBarrel) + 1);
                            mineshaftBarrelLocation.get(mineshaftBarrel).add(b.getX() + " " + b.getY() + " " + b.getZ());
                        }
                        cptTotal++;
                    }
                }
                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender, cpt, startTime);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    sender.sendMessage("Removed or replace " + cpt + "/" + cptTotal + " entities in "
                            + Duration.ofMillis(System.currentTimeMillis() - startTime));
                    sender.sendMessage("By entity type: " + cptByEntity);
                    // sender.sendMessage("By mineshaft barrel: " + cptByMineshaftBarrel);
                    sender.sendMessage("By mineshaft barrel location: " + mineshaftBarrelLocation.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue().size()).collect(java.util.stream.Collectors.joining(", ")));
                    WorldCleanerHPlugin.plugin.saveData(
                            mineshaftBarrelLocation.entrySet().stream().collect(HashMap::new,
                                    (map, entry) -> map.put(entry.getKey().toString(), entry.getValue()), HashMap::putAll),
                            "mineshaftBarrelLocation");
                    cancel();
                }
            }
        }.runTaskTimer(WorldCleanerHPlugin.plugin, 0, 1);
    }

    private static void printProgress(CommandSender sender, long cpt, long startTime) {
        double progress = WorldSelectorHPlugin.getSelector().progress();
        long timeForFullProgress = (long) ((System.currentTimeMillis() - startTime) / progress);
        long timeForFullProgressLeft = timeForFullProgress - (long) (System.currentTimeMillis() - startTime);
        sender.sendMessage("Progress: " + cpt + "   " + progress * 100 + "% ETA: " + Duration.ofMillis(timeForFullProgressLeft));
    }

    private static ItemStack @NotNull [] generateBoatChestInventory(BoatType boatType) {
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            if (WorldCleanerHPlugin.random.nextDouble() < 0.4) {
                items[i] = boatType.getRandomItem();
            }
        }
        return items;
    }
    private static ItemStack @NotNull [] generateMineshaftBarrelInventory(MineshaftBarrel mineshaftBarrel) {
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            if (WorldCleanerHPlugin.random.nextDouble() < 0.4) {
                items[i] = mineshaftBarrel.getRandomItem();
            }
        }
        return items;
    }
}