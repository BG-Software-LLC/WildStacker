package com.bgsoftware.wildstacker.utils.legacy;

import com.bgsoftware.wildstacker.utils.ServerVersion;
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
    WITHER_SKELETON_SKULL("SKULL", 3),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    SNOWBALL("SNOW_BALL"),
    POPPY("RED_ROSE"),
    WET_SPONGE("SPONGE", 1),
    NETHER_PORTAL("PORTAL"),
    PLAYER_HEAD("SKULL_ITEM", 3),
    MUSHROOM_STEW("MUSHROOM_SOUP"),
    LEAD("LEASH");

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

    public static Material getSpawnEgg(EntityType entityType){
        return Material.matchMaterial((ServerVersion.isLegacy() ? "MONSTER_EGG" : EntityTypes.fromName(entityType.name())  + "_SPAWN_EGG"));
    }

    public static boolean isValidAndSpawnEgg(ItemStack itemStack){
        return !itemStack.getType().isBlock() && itemStack.getType().name().contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG");
    }

    public static ItemStack getWool(DyeColor dyeColor){
        return ServerVersion.isLegacy() ? new ItemStack(Material.matchMaterial("WOOL"), 1, dyeColor.getWoolData()) : new ItemStack(Material.matchMaterial(dyeColor.name() + "_WOOL"));
    }

}
