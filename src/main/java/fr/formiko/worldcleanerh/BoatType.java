package fr.formiko.worldcleanerh;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BoatType {
    // @formatter:off
        FARMER(new ItemStack(Material.WHEAT, 20), new ItemStack(Material.HAY_BLOCK, 5), new ItemStack(Material.CARROT, 20)),
        ORE(new ItemStack(Material.IRON_INGOT, 12), new ItemStack(Material.COAL, 23)),
        TOOL(new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.IRON_AXE), new ItemStack(Material.IRON_SHOVEL),
                new ItemStack(Material.IRON_HOE)),
        EXOTIC(new ItemStack(Material.BAMBOO, 20), new ItemStack(Material.COCOA_BEANS, 20), new ItemStack(Material.SUGAR_CANE, 20),
                new ItemStack(Material.CACTUS, 20), new ItemStack(Material.CHERRY_SAPLING, 10), new ItemStack(Material.MANGROVE_PROPAGULE, 10),
                new ItemStack(Material.PALE_OAK_SAPLING, 10)),
        RICH(new ItemStack(Material.CANDLE, 20), new ItemStack(Material.GOLD_INGOT, 10), new ItemStack(Material.GOLD_BLOCK, 3),
                new ItemStack(Material.DIAMOND, 9)),
        FISHER(new ItemStack(Material.SALMON, 26), new ItemStack(Material.COD, 26), new ItemStack(Material.FISHING_ROD)),
        WOOD1(new ItemStack(Material.OAK_LOG, 32), new ItemStack(Material.SPRUCE_LOG, 32), new ItemStack(Material.BIRCH_LOG, 32),
                new ItemStack(Material.DARK_OAK_LOG, 32), new ItemStack(Material.CHERRY_LOG, 32)),
        WOOD2(new ItemStack(Material.JUNGLE_LOG, 32), new ItemStack(Material.ACACIA_LOG, 32), new ItemStack(Material.MANGROVE_LOG, 32),
                new ItemStack(Material.PALE_OAK_LOG, 32)),
        WOOL(new ItemStack(Material.WHITE_WOOL, 10), new ItemStack(Material.WHITE_CARPET, 2), new ItemStack(Material.WHITE_BED, 2));
        // MAGIC(new ItemStack(Material.BOOK, 64), enchantedBook(Enchantment.MENDING, 1), enchantedBook(Enchantment.LOOT_BONUS_BLOCKS, 3));
        // TODO add WEAPON.
        // @formatter:on
    private final ItemStack[] items;

    private BoatType(ItemStack... items) { this.items = items; }


    public static BoatType randomBoatType() {
        BoatType[] directions = values();
        return directions[WorldCleanerHPlugin.random.nextInt(directions.length)];
    }

    public ItemStack getRandomItem() {
        ItemStack item = items[WorldCleanerHPlugin.random.nextInt(items.length)];
        return new ItemStack(item.getType(), 1 + WorldCleanerHPlugin.random.nextInt(item.getAmount()));
    }

    // private static ItemStack enchantedBook(Enchantment enchantment, int level) {
    // ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
    // EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
    // meta.addStoredEnchant(enchantment, level, true);
    // item.setItemMeta(meta);
    // return item;
    // }
}