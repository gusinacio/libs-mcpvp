package me.libraryaddict.librarys.Abilities;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Fisherman extends AbilityListener implements Disableable {
    public double biteChanceWhereOneIsInstant = 0.05D;
    public int chanceInOneOfJunk = 5;
    public String[] junk = new String[] { "ACTIVATOR_RAIL", "ANVIL", "APPLE", "ARROW", "BAKED_POTATO", "BEACON", "BED",
            "BIRCH_WOOD_STAIRS", "BLAZE_POWDER", "BLAZE_ROD", "BOAT", "BONE", "BOOK", "BOOK_AND_QUILL", "BOOKSHELF", "BOW",
            "BOWL", "BREAD", "BREWING_STAND_ITEM", "BRICK", "BRICK_STAIRS", "BROWN_MUSHROOM", "BUCKET", "CACTUS", "CAKE",
            "CARROT_ITEM", "CARROT_STICK", "CAULDRON_ITEM", "CHAINMAIL_BOOTS", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_HELMET",
            "CHAINMAIL_LEGGINGS", "CHEST", "CLAY", "CLAY_BALL", "CLAY_BRICK", "COAL", "COAL_ORE", "COBBLE_WALL", "COBBLESTONE",
            "COBBLESTONE_STAIRS", "COCOA", "COMPASS", "COOKED_BEEF", "COOKED_CHICKEN", "COOKIE", "DAYLIGHT_DETECTOR",
            "DEAD_BUSH", "DETECTOR_RAIL", "DIAMOND", "DIAMOND_AXE", "DIAMOND_BLOCK", "DIAMOND_BOOTS", "DIAMOND_CHESTPLATE",
            "DIAMOND_HELMET", "DIAMOND_HOE", "DIAMOND_LEGGINGS", "DIAMOND_ORE", "DIAMOND_PICKAXE", "DIAMOND_SPADE",
            "DIAMOND_SWORD", "DIODE", "DIRT", "DISPENSER", "DOUBLE_STEP", "DRAGON_EGG", "DROPPER", "EGG", "EMERALD",
            "EMERALD_BLOCK", "EMERALD_ORE", "ENCHANTED_BOOK", "ENCHANTMENT_TABLE", "ENDER_CHEST", "ENDER_PEARL", "ENDER_STONE",
            "EXP_BOTTLE", "EXPLOSIVE_MINECART", "EYE_OF_ENDER", "FEATHER", "FENCE", "FENCE_GATE", "FERMENTED_SPIDER_EYE", "FIRE",
            "FIREBALL", "FIREWORK", "FIREWORK_CHARGE", "FISHING_ROD", "FLINT", "FLINT_AND_STEEL", "FLOWER_POT_ITEM", "FURNACE",
            "GHAST_TEAR", "GLASS", "GLASS_BOTTLE", "GLOWSTONE", "GLOWSTONE_DUST", "GOLD_AXE", "GOLD_BLOCK", "GOLD_BOOTS",
            "GOLD_CHESTPLATE", "GOLD_HELMET", "GOLD_HOE", "GOLD_INGOT", "GOLD_LEGGINGS", "GOLD_NUGGET", "GOLD_ORE",
            "GOLD_PICKAXE", "GOLD_PLATE", "GOLD_RECORD", "GOLD_SPADE", "GOLD_SWORD", "GOLDEN_APPLE", "GOLDEN_CARROT", "GRASS",
            "GRAVEL", "GREEN_RECORD", "GRILLED_PORK", "HOPPER", "HOPPER_MINECART", "HUGE_MUSHROOM_1", "HUGE_MUSHROOM_2", "ICE",
            "INK_SACK", "IRON_AXE", "IRON_BLOCK", "IRON_BOOTS", "IRON_CHESTPLATE", "IRON_DOOR", "IRON_FENCE", "IRON_HELMET",
            "IRON_HOE", "IRON_INGOT", "IRON_LEGGINGS", "IRON_ORE", "IRON_PICKAXE", "IRON_PLATE", "IRON_SPADE", "IRON_SWORD",
            "ITEM_FRAME", "JACK_O_LANTERN", "JUKEBOX", "JUNGLE_WOOD_STAIRS", "LADDER", "LAPIS_BLOCK", "LAPIS_ORE", "LAVA",
            "LAVA_BUCKET", "LEATHER", "LEATHER_BOOTS", "LEATHER_CHESTPLATE", "LEATHER_HELMET", "LEATHER_LEGGINGS", "LEAVES",
            "LEVER", "LOCKED_CHEST", "LOG", "LONG_GRASS", "MAGMA_CREAM", "MAP", "MELON", "MELON_BLOCK", "MELON_SEEDS",
            "MELON_STEM", "MILK_BUCKET", "MINECART", "MOSSY_COBBLESTONE", "MUSHROOM_SOUP", "MYCEL", "NETHER_BRICK_ITEM",
            "NETHER_BRICK_STAIRS", "NETHER_FENCE", "NETHER_STALK", "NETHER_STAR", "NETHER_WARTS", "NETHERRACK", "NOTE_BLOCK",
            "OBSIDIAN", "PAINTING", "PAPER", "PISTON_BASE", "PISTON_STICKY_BASE", "POISONOUS_POTATO", "PORK", "POTATO_ITEM",
            "POTION", "POWERED_MINECART", "POWERED_RAIL", "PUMPKIN", "PUMPKIN_PIE", "PUMPKIN_SEEDS", "PUMPKIN_STEM", "QUARTZ",
            "QUARTZ_BLOCK", "QUARTZ_ORE", "QUARTZ_STAIRS", "RAILS", "RAW_BEEF", "RAW_CHICKEN", "RECORD_10", "RECORD_11",
            "RECORD_12", "RECORD_3", "RECORD_4", "RECORD_5", "RECORD_6", "RECORD_7", "RECORD_8", "RECORD_9", "RED_MUSHROOM",
            "RED_ROSE", "REDSTONE", "REDSTONE_BLOCK", "REDSTONE_COMPARATOR", "REDSTONE_LAMP_OFF", "REDSTONE_ORE",
            "REDSTONE_TORCH_OFF", "REDSTONE_TORCH_ON", "REDSTONE_WIRE", "ROTTEN_FLESH", "SADDLE", "SAND", "SANDSTONE",
            "SANDSTONE_STAIRS", "SAPLING", "SEEDS", "SHEARS", "SIGN", "SIGN_POST", "SKULL_ITEM", "SLIME_BALL", "SMOOTH_BRICK",
            "SMOOTH_STAIRS", "SNOW", "SNOW_BALL", "SNOW_BLOCK", "SOIL", "SOUL_SAND", "SPECKLED_MELON", "SPIDER_EYE",
            "SPRUCE_WOOD_STAIRS", "STEP", "STICK", "STONE", "STONE_AXE", "STONE_BUTTON", "STONE_HOE", "STONE_PICKAXE",
            "STONE_PLATE", "STONE_SPADE", "STONE_SWORD", "STORAGE_MINECART", "STRING", "SUGAR", "SUGAR_CANE", "SULPHUR",
            "THIN_GLASS", "TNT", "TORCH", "TRAP_DOOR", "TRAPPED_CHEST", "TRIPWIRE", "TRIPWIRE_HOOK", "VINE", "WATCH",
            "WATER_BUCKET", "WATER_LILY", "WEB", "WHEAT", "WOOD", "WOOD_AXE", "WOOD_BUTTON", "WOOD_DOUBLE_STEP", "WOOD_HOE",
            "WOOD_PICKAXE", "WOOD_PLATE", "WOOD_SPADE", "WOOD_STAIRS", "WOOD_STEP", "WOOD_SWORD", "WOODEN_DOOR", "WOOL",
            "WORKBENCH", "YELLOW_FLOWER" };
    public String landedJunk = ChatColor.BLUE + "Oh dear! You hooked some junk!";

    private ItemStack getJunk() {
        Material material = Material.getMaterial(junk[new Random().nextInt(junk.length)].toUpperCase());
        short dura = material.getMaxDurability();
        if (dura <= 0)
            dura = 1;
        ItemStack item = new ItemStack(material, 1, (short) new Random().nextInt(dura));
        return item;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (hasAbility(event.getPlayer())) {
            if (event.getState() == State.FISHING) {
                event.getHook().setBiteChance(biteChanceWhereOneIsInstant);
            } else if (event.getState() == State.CAUGHT_FISH) {
                if (event.getCaught() != null) {
                    if (junk.length > 0 && new Random().nextInt(chanceInOneOfJunk) == 0) {
                        ((Item) event.getCaught()).setItemStack(getJunk());
                        event.setExpToDrop(0);
                        event.getPlayer().sendMessage(landedJunk);
                    }
                }
            }
        }
    }
}
