package fr.formiko.worldcleanerh;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MineshaftBarrel {
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
        return directions[WorldCleanerHPlugin.random.nextInt(directions.length)];
    }

    public ItemStack getRandomItem() {
        ItemStack item = items[WorldCleanerHPlugin.random.nextInt(items.length)];
        return new ItemStack(item.getType(), 1 + WorldCleanerHPlugin.random.nextInt(item.getAmount()));
    }
}