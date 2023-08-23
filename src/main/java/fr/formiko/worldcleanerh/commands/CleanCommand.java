package fr.formiko.worldcleanerh.commands;

import fr.formiko.worldcleanerh.WorldCleanerHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
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
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CleanCommand implements CommandExecutor {
    private static final Random random = new Random();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("entities")) {
                cleanEntities(sender);

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
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Block block = WorldSelectorHPlugin.getSelector().nextBlock();
                    if (WorldCleanerHPlugin.getBlocksToRemove().contains(block.getType())) {
                        cptByMaterialToRemove.put(block.getType(), cptByMaterialToRemove.get(block.getType()) + 1);
                        block.setType(Material.AIR);
                        cpt++;
                    } else if (WorldCleanerHPlugin.getBlocksToUpdate().contains(block.getType())) {
                        cptByMaterialToUpdate.put(block.getType(), cptByMaterialToUpdate.get(block.getType()) + 1);
                        block.getState().update(true);
                        cpt++;
                    } else if (block.getState() instanceof Chest chest) {
                        BoatType boatType = BoatType.randomBoatType();
                        // cptByBoatType.put(boatType, cptByBoatType.get(boatType) + 1);
                        boatChestLocation.get(boatType).add(block.getX() + " " + block.getY() + " " + block.getZ());
                        chest.getBlockInventory().setContents(generateBoatChestInventory(boatType));
                        cpt++;
                    }
                    cptTotal++;
                }
                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender, cpt);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    printProgress(sender, cpt);
                    sender.sendMessage("Edit " + cpt + "/" + cptTotal + " blocks.");
                    sender.sendMessage("By material to remove: " + cptByMaterialToRemove);
                    sender.sendMessage("By material to update: " + cptByMaterialToUpdate);
                    sender.sendMessage("By boat type: " + boatChestLocation.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue().size()).collect(java.util.stream.Collectors.joining(", ")));
                    WorldCleanerHPlugin.plugin.saveData(
                            boatChestLocation.entrySet().stream().collect(HashMap::new,
                                    (map, entry) -> map.put(entry.getKey().toString(), entry.getValue()), HashMap::putAll),
                            "boatChestLocation");
                    cancel();
                }
            }
        }.runTaskTimer(WorldCleanerHPlugin.plugin, 0, 1);
    }

    private static void cleanEntities(CommandSender sender) {
        sender.sendMessage("Cleaning entities...");

        new BukkitRunnable() {
            private long printTime, execTime, cpt, cptTotal;
            private Map<EntityType, Integer> cptByEntity = WorldCleanerHPlugin.getEntitiesToRemove().stream().collect(HashMap::new,
                    (map, entity) -> map.put(entity, 0), HashMap::putAll);
            private Map<MineshaftBarrel, List<String>> mineshaftBarrelLocation = Stream.of(MineshaftBarrel.values()).collect(HashMap::new,
                    (map, mineshaftBarrel) -> map.put(mineshaftBarrel, new LinkedList<>()), HashMap::putAll);

            @Override
            public void run() {
                execTime = System.currentTimeMillis();
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
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
                    printProgress(sender, cpt);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    sender.sendMessage("Removed or replace " + cpt + "/" + cptTotal + " entities.");
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

    private static void printProgress(CommandSender sender, long cpt) {
        sender.sendMessage("Progress: " + cpt + "   " + WorldSelectorHPlugin.getSelector().progress() * 100 + "%");
    }

    private static ItemStack @NotNull [] generateBoatChestInventory(BoatType boatType) {
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            if (random.nextDouble() < 0.4) {
                items[i] = boatType.getRandomItem();
            }
        }
        return items;
    }
    private static ItemStack @NotNull [] generateMineshaftBarrelInventory(MineshaftBarrel mineshaftBarrel) {
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            if (random.nextDouble() < 0.4) {
                items[i] = mineshaftBarrel.getRandomItem();
            }
        }
        return items;
    }


    enum BoatType {
        // @formatter:off
        FARMER(new ItemStack(Material.WHEAT, 20), new ItemStack(Material.HAY_BLOCK, 5), new ItemStack(Material.CARROT, 20)),
        ORE(new ItemStack(Material.IRON_INGOT, 12), new ItemStack(Material.COAL, 23)),
        TOOL(new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.IRON_AXE), new ItemStack(Material.IRON_SHOVEL),
                new ItemStack(Material.IRON_HOE)),
        EXOTIC(new ItemStack(Material.BAMBOO, 20), new ItemStack(Material.COCOA_BEANS, 20), new ItemStack(Material.SUGAR_CANE, 20),
                new ItemStack(Material.CACTUS, 20), new ItemStack(Material.CHERRY_SAPLING, 10)),
        RICH(new ItemStack(Material.CANDLE, 20), new ItemStack(Material.GOLD_INGOT, 10), new ItemStack(Material.GOLD_BLOCK, 3),
                new ItemStack(Material.DIAMOND, 3)),
        FISHER(new ItemStack(Material.SALMON, 26), new ItemStack(Material.COD, 26), new ItemStack(Material.FISHING_ROD)),
        WOOD1(new ItemStack(Material.OAK_LOG, 32), new ItemStack(Material.SPRUCE_LOG, 32), new ItemStack(Material.BIRCH_LOG, 32),
                new ItemStack(Material.DARK_OAK_LOG, 32), new ItemStack(Material.CHERRY_LOG, 32)),
        WOOD2(new ItemStack(Material.JUNGLE_LOG, 32), new ItemStack(Material.ACACIA_LOG, 32), new ItemStack(Material.MANGROVE_LOG, 32)),
        WOOL(new ItemStack(Material.WHITE_WOOL, 10), new ItemStack(Material.WHITE_CARPET, 2), new ItemStack(Material.WHITE_BED, 2));
        // MAGIC(new ItemStack(Material.BOOK, 64), enchantedBook(Enchantment.MENDING, 1), enchantedBook(Enchantment.LOOT_BONUS_BLOCKS, 3));
        // TODO add WEAPON.
        // @formatter:on
        private final ItemStack[] items;

        private BoatType(ItemStack... items) { this.items = items; }


        public static BoatType randomBoatType() {
            BoatType[] directions = values();
            return directions[random.nextInt(directions.length)];
        }

        public ItemStack getRandomItem() {
            ItemStack item = items[random.nextInt(items.length)];
            return new ItemStack(item.getType(), 1 + random.nextInt(item.getAmount()));
        }

        private static ItemStack enchantedBook(Enchantment enchantment, int level) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(meta);
            return item;
        }
    }
    enum MineshaftBarrel {
        // @formatter:off
        COAL(new ItemStack(Material.COAL, 64), new ItemStack(Material.COAL_BLOCK, 32)),
        IRON(new ItemStack(Material.IRON_INGOT, 48), new ItemStack(Material.IRON_BLOCK, 16)),
        GOLD(new ItemStack(Material.GOLD_INGOT, 32), new ItemStack(Material.GOLD_BLOCK, 16)),
        DIAMOND(new ItemStack(Material.DIAMOND, 32), new ItemStack(Material.DIAMOND_BLOCK, 8)),
        EMERALD(new ItemStack(Material.EMERALD, 32), new ItemStack(Material.EMERALD_BLOCK, 16));
        // @formatter:on
        private final ItemStack[] items;

        private MineshaftBarrel(ItemStack... items) { this.items = items; }

        public static MineshaftBarrel randomMineshaftBarrel() {
            MineshaftBarrel[] directions = values();
            return directions[random.nextInt(directions.length)];
        }

        public ItemStack getRandomItem() {
            ItemStack item = items[random.nextInt(items.length)];
            return new ItemStack(item.getType(), 1 + random.nextInt(item.getAmount()));
        }
    }
}