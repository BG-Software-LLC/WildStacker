package com.bgsoftware.wildstacker.utils.legacy;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public enum Materials {

    SPAWNER("MOB_SPAWNER"),
    CLOCK("WATCH"),
    MAP("EMPTY_MAP"),
    CAULDRON("CAULDRON_ITEM"),
    BEEF("RAW_BEEF"),
    GUNPOWDER("SULPHUR"),
    CHICKEN("RAW_CHICKEN"),
    COOKED_PORKCHOP("GRILLED_PORK"),
    PORKCHOP("PORK"),
    COD("FISH"),
    COOKED_COD("COOKED_FISH"),
    INK_SAC("INK_SACK"),
    BONE_MEAL("INK_SACK", 15),
    GOLDEN_SWORD("GOLD_SWORD"),
    MUSIC_DISC_13(""),
    MUSIC_DISC_CAT(""),
    MUSIC_DISC_BLOCKS(""),
    MUSIC_DISC_CHIRP(""),
    MUSIC_DISC_FAR(""),
    MUSIC_DISC_MALL(""),
    MUSIC_DISC_MELLOHI(""),
    MUSIC_DISC_STAL(""),
    MUSIC_DISC_STRAD(""),
    MUSIC_DISC_WARD(""),
    MUSIC_DISC_11(""),
    MUSIC_DISC_WAIT(""),
    TROPICAL_FISH(""),
    PUFFERFISH(""),
    SALMON(""),
    PHANTOM_MEMBRANE(""),
    WITHER_SKELETON_SKULL("SKULL", 3),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    SNOWBALL("SNOW_BALL"),
    TOTEM_OF_UNDYING(""),
    SEAGRASS(""),
    SHULKER_SHELL(""),
    POPPY("RED_ROSE"),
    WET_SPONGE("SPONGE", 1),
    TIPPED_ARROW("");

    Materials(String bukkitType){
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData){
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    private String bukkitType;
    private short bukkitData;

    public Material toBukkitType(){
        try {
            try {
                return Material.valueOf(bukkitType);
            } catch (IllegalArgumentException ex) {
                return Material.valueOf(name());
            }
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem(){
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount){
        return bukkitData == 0 ? new ItemStack(toBukkitType(), amount) : new ItemStack(toBukkitType(), amount, bukkitData);
    }

    private static boolean legacy = !Bukkit.getBukkitVersion().contains("1.13");

    public static Material getSpawnEgg(EntityType entityType){
        return Material.matchMaterial((legacy ? "MONSTER_EGG" : EntityTypes.fromName(entityType.name()) + "_SPAWN_EGG"));
    }

    public static boolean isValidAndSpawnEgg(ItemStack itemStack){
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(legacy ? "MONSTER_EGG" : "SPAWN_EGG");
    }

    public static ItemStack getWool(DyeColor dyeColor){
        return legacy ? new ItemStack(Material.matchMaterial("WOOL"), 1, dyeColor.getWoolData()) : new ItemStack(Material.matchMaterial(dyeColor.name() + "_WOOL"));
    }

}
