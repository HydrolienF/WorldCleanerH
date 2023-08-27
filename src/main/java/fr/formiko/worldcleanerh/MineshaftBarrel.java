package fr.formiko.worldcleanerh;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MineshaftBarrel {
    // @formatter:off
        COAL(new ItemStack(Material.COAL, 32), new ItemStack(Material.COAL_BLOCK, 16)),
        IRON(new ItemStack(Material.IRON_INGOT, 32), new ItemStack(Material.IRON_BLOCK, 12)),
        GOLD(new ItemStack(Material.GOLD_INGOT, 16), new ItemStack(Material.GOLD_BLOCK, 8)),
        DIAMOND(new ItemStack(Material.DIAMOND, 20), new ItemStack(Material.DIAMOND_BLOCK, 8));
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