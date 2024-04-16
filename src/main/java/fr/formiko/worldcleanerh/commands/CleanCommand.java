package fr.formiko.worldcleanerh.commands;

import fr.formiko.worldcleanerh.BoatType;
import fr.formiko.worldcleanerh.MineshaftBarrel;
import fr.formiko.worldcleanerh.WorldCleanerHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
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
    private static Random random = new Random();

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
            private long printTime, cpt, cptTotal, cptBlockAddedToAvoidFlooding, cptBlockRemovedToAvoidFlooding, cptWheatFarmToGrass,
                    cptPowerSnow;
            private Block block;
            private Map<Material, Integer> cptByMaterialToRemove = WorldCleanerHPlugin.getBlocksToRemove().stream().collect(HashMap::new,
                    (map, material) -> map.put(material, 0), HashMap::putAll);
            private Map<Material, Integer> cptByMaterialToUpdate = WorldCleanerHPlugin.getBlocksToUpdate().stream().collect(HashMap::new,
                    (map, material) -> map.put(material, 0), HashMap::putAll);
            private Map<Material, Integer> cptByMaterialThatCantFly = WorldCleanerHPlugin.getBlocksThatCantFly().stream()
                    .collect(HashMap::new, (map, material) -> map.put(material, 0), HashMap::putAll);
            private Map<BoatType, List<String>> boatChestLocation = Stream.of(BoatType.values()).collect(HashMap::new,
                    (map, boatType) -> map.put(boatType, new LinkedList<>()), HashMap::putAll);
            private Map<Material, Long> cptAllBlocks = new EnumMap<>(Material.class);


            @Override
            public void run() {
                long execTime = System.currentTimeMillis();
                while (execTime + 45 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = -64; y < 320; y++) {
                                block = chunk.getBlock(x, y, z);

                                // Block block = WorldSelectorHPlugin.getSelector().nextBlock();
                                if (WorldCleanerHPlugin.getBlocksToRemove().contains(block.getType())) {
                                    cptByMaterialToRemove.put(block.getType(), cptByMaterialToRemove.get(block.getType()) + 1);
                                    block.setType(Material.AIR);
                                    cpt++;
                                    // } else if (block.getState() instanceof Chest chest) {
                                    // BoatType boatType = BoatType.randomBoatType();
                                    // boatChestLocation.get(boatType).add(block.getX() + " " + block.getY() + " " + block.getZ());
                                    // chest.getBlockInventory().setContents(generateBoatChestInventory(boatType));
                                    // cpt++;
                                } else if (WorldCleanerHPlugin.getBlocksToSupport().contains(block.getType())
                                        && block.getRelative(BlockFace.DOWN).getType() == Material.AIR) { // no floating block
                                    block.getRelative(BlockFace.DOWN).setType(WorldCleanerHPlugin.getSupportBlock(block.getType()));
                                    cpt++;
                                } else if (isUnstableBlock(block)) {
                                    cptByMaterialThatCantFly.put(block.getType(), cptByMaterialThatCantFly.get(block.getType()) + 1);
                                    block.setType(Material.AIR);
                                    cpt++;
                                    // If it's a water source block, check if it have some neighbor that can be flooded.
                                    // } else if (block.getY() >= 60 && isFloodingBlock(block)) {
                                    // preventFlooding();
                                }
                                // // if is a wheat farm, replace by grass at 90% chance. // COMMENT TO RUN ONCE ONLY
                                else if (block.getType() == Material.WHEAT && random.nextDouble() < 0.90) {
                                    // block.setType(Material.GRASS_BLOCK);
                                    block.setType(Material.AIR);
                                    block.getRelative(BlockFace.DOWN).setType(Material.GRASS_BLOCK);
                                    cptWheatFarmToGrass++;
                                    cpt++;
                                }
                                // if it is a snow block over y = 200, replace by powder snow
                                else if (block.getType() == Material.POWDER_SNOW) { // to avoid issue when running multiple time.
                                    block.setType(Material.SNOW_BLOCK);
                                } else if (block.getType() == Material.SNOW_BLOCK
                                        && ((block.getY() > 200 && random.nextDouble() < 0.90) || random.nextDouble() < 0.01)) {
                                    block.setType(Material.POWDER_SNOW);
                                    cptPowerSnow++;
                                    cpt++;
                                }

                                // Not working.
                                // if (WorldCleanerHPlugin.getBlocksToUpdate().contains(block.getType())) {
                                // cptByMaterialToUpdate.put(block.getType(), cptByMaterialToUpdate.get(block.getType()) + 1);
                                // block.getState().update(true);
                                // cpt++;
                                // }
                                cptAllBlocks.put(block.getType(), cptAllBlocks.getOrDefault(block.getType(), 0L) + 1L);
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
                    endPrintProgress();
                }
            }

            // sub function
            private void preventFlooding() {
                // Remove lonely water source block that will flood
                int cptWater = 0;
                int cptAir = 0;
                for (Block neighborToWaterBlock : List.of(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH),
                        block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.SOUTH_WEST),
                        block.getRelative(BlockFace.SOUTH_EAST), block.getRelative(BlockFace.NORTH_WEST),
                        block.getRelative(BlockFace.NORTH_EAST), block.getRelative(BlockFace.DOWN))) {
                    if (isWaterSource(neighborToWaterBlock)) {
                        cptWater++;
                    } else if (isFloodableBlock(neighborToWaterBlock)) {
                        // && !neighborToWaterBlock.getRelative(BlockFace.DOWN).isLiquid()
                        cptAir++;
                    }
                }
                // If there is a lot of floodable block compare to water source block, remove the water source block.
                if (cptAir > cptWater) { // && cptWater < 4
                    block.setType(Material.AIR);
                    cpt++;
                    cptBlockRemovedToAvoidFlooding++;
                } else { // else replace floodable block with the block under it.
                    // Some flooding might already occured.
                    // Some flooding won't be previsible untill water update.
                    for (Block neighborToWaterBlock : List.of(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH),
                            block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.SOUTH_WEST),
                            block.getRelative(BlockFace.SOUTH_EAST), block.getRelative(BlockFace.NORTH_WEST),
                            block.getRelative(BlockFace.NORTH_EAST))) {
                        // if neighbor can be flooded and it have a solid block under it,
                        // replace it with the block under it to avoid flooding
                        // (if the block under it will prevent flooding).
                        Block lowerBlock = neighborToWaterBlock.getRelative(BlockFace.DOWN);
                        if (isFloodableBlock(neighborToWaterBlock)
                                && (lowerBlock.isSolid() || lowerBlock.getRelative(BlockFace.DOWN).isSolid())) {
                            neighborToWaterBlock.setType(lowerBlock.getType());
                            if (lowerBlock.getType() == Material.GRASS_BLOCK || lowerBlock.getType() == Material.FARMLAND) {
                                lowerBlock.setType(Material.DIRT);
                            }
                            cpt++;
                            cptBlockAddedToAvoidFlooding++;
                        }
                    }
                }
            }

            private void endPrintProgress() {
                printProgress(sender, cpt, startTime);
                sender.sendMessage(
                        "Edit " + cpt + "/" + cptTotal + " blocks. in " + Duration.ofMillis(System.currentTimeMillis() - startTime));
                sender.sendMessage("By material to remove: " + cptByMaterialToRemove);
                sender.sendMessage("By material to update: " + cptByMaterialToUpdate);
                sender.sendMessage("Wheat farm to grass: " + cptWheatFarmToGrass);
                sender.sendMessage("Power snow: " + cptPowerSnow);
                sender.sendMessage("By material that can't fly: " + cptByMaterialThatCantFly);
                sender.sendMessage("By boat type: " + boatChestLocation.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue().size()).collect(java.util.stream.Collectors.joining(", ")));
                sender.sendMessage("Block added to avoid flooding: " + cptBlockAddedToAvoidFlooding);
                sender.sendMessage("Block removed to avoid flooding: " + cptBlockRemovedToAvoidFlooding);
                WorldCleanerHPlugin.plugin.saveData(
                        boatChestLocation.entrySet().stream().collect(HashMap::new,
                                (map, entry) -> map.put(entry.getKey().toString(), entry.getValue()), HashMap::putAll),
                        "boatChestLocation");
                WorldCleanerHPlugin.plugin.saveData(cptAllBlocks.entrySet().stream().collect(HashMap::new,
                        (map, entry) -> map.put(entry.getKey().toString(), entry.getValue()), HashMap::putAll), "allBlocks");
                cancel();
                if (runCleanEntities) {
                    WorldSelectorHPlugin.resetSelector();
                    cleanEntities(sender);
                }
            }
        }.runTaskTimer(WorldCleanerHPlugin.plugin, 0, 1);

    }

    private static boolean isFloodableBlock(Block block) {
        return WorldCleanerHPlugin.getBlockThatCanBeFlood().contains(block.getType())
                || (block.getType() == Material.WATER && !isWaterSource(block));
    }
    private static boolean isWaterSource(Block block) {
        return block.getType() == Material.WATER && block.getBlockData() instanceof Levelled levelled && levelled.getLevel() == 0;
    }
    private static boolean isFloodingBlock(Block block) {
        return block.isLiquid() || (block.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged());
    }
    private static boolean isUnstableBlock(Block block) {
        return WorldCleanerHPlugin.getBlocksThatCantFly().contains(block.getType())
                && (block.getRelative(BlockFace.DOWN).isEmpty() || block.getRelative(BlockFace.DOWN).isLiquid());
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
        long timeForFullProgressLeft = timeForFullProgress - (System.currentTimeMillis() - startTime);
        sender.sendMessage("Progress: " + cpt + "   " + progress * 100 + "% ETA: " + Duration.ofMillis(timeForFullProgressLeft));
    }
    // private static String ramUsedPercentage() {
    // return "RAM use: "
    // + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (Runtime.getRuntime().totalMemory() * 100D)
    // + "%";
    // }

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