package com.bgsoftware.wildstacker.utils.legacy;

import com.bgsoftware.wildstacker.utils.ServerVersion;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;

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

    private static final EnumMap<Material, EnumSet<Tag>> MATERIAL_TAGS = setupMaterialTags();
    private static final EnumMap<DyeColor, ItemStack> WOOL_CONVERTERS = setupWoolConverters();
    private static final EnumMap<EntityType, Material> ENTITY_TO_TYPE_SPAWN_EGG_CONVERTERS = setupEntityToMaterialSpawnEggConverters();
    private static final EnumMap<Material, EntityTypes> TYPE_TO_ENTITY_SPAWN_EGG_CONVERTERS = setupMaterialToEntitySpawnEggConverters();

    private final String bukkitType;
    private final short bukkitData;

    Materials(String bukkitType) {
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData) {
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    public static boolean isValidAndSpawnEgg(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.SPAWN_EGG);
    }

    public static boolean isWool(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.WOOL);
    }

    public static boolean isFishBucket(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.FISH_BUCKET);
    }

    public static boolean isSoup(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.SOUP);
    }

    public static boolean isShulkerBox(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.SHULKER_BOX);
    }

    public static boolean isPickaxe(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.PICKAXE);
    }

    public static boolean isChickenEgg(@Nullable ItemStack itemStack) {
        return hasTag(itemStack, Tag.CHICKEN_EGG);
    }

    public static boolean isCauldron(@Nullable Material material) {
        return hasTag(material, Tag.CAULDRON);
    }

    public static boolean isSpawner(@Nullable Material material) {
        return hasTag(material, Tag.SPAWNER);
    }

    public static boolean isBucket(@Nullable Material material) {
        return hasTag(material, Tag.BUCKET);
    }

    public static boolean isSword(@Nullable Material material) {
        return hasTag(material, Tag.SWORD);
    }

    public static boolean isTool(@Nullable Material material) {
        return hasTag(material, Tag.TOOL);
    }

    private static boolean hasTag(@Nullable ItemStack itemStack, Tag tag) {
        return hasTag(itemStack == null ? null : itemStack.getType(), tag);
    }

    private static boolean hasTag(@Nullable Material material, Tag tag) {
        return material != null && MATERIAL_TAGS.getOrDefault(material, Tag.EMPTY_TAGS).contains(tag);
    }

    @Nullable
    public static Material getSpawnEgg(EntityType entityType) {
        return ENTITY_TO_TYPE_SPAWN_EGG_CONVERTERS.get(entityType);
    }

    @Nullable
    public static EntityTypes getSpawnEgg(Material material) {
        return TYPE_TO_ENTITY_SPAWN_EGG_CONVERTERS.get(material);
    }

    @Nullable
    public static ItemStack getWool(DyeColor dyeColor) {
        return WOOL_CONVERTERS.get(dyeColor).clone();
    }

    public Material toBukkitType() {
        try {
            try {
                return Material.valueOf(bukkitType);
            } catch (IllegalArgumentException ex) {
                return Material.valueOf(name());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem() {
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount) {
        return bukkitData == 0 ? new ItemStack(toBukkitType(), amount) : new ItemStack(toBukkitType(), amount, bukkitData);
    }

    @Nullable
    public static Material getMaterialOrNull(String name) {
        try {
            return Material.valueOf(name);
        } catch (Exception error) {
            return null;
        }
    }

    private static EnumMap<Material, EnumSet<Tag>> setupMaterialTags() {
        EnumMap<Material, EnumSet<Tag>> enumMap = new EnumMap<>(Material.class);

        for (Material material : Material.values()) {
            EnumSet<Tag> materialTags = EnumSet.noneOf(Tag.class);
            String materialName = material.name();

            if (!material.isBlock() && materialName.contains(ServerVersion.isLegacy() ? "MONSTER_EGG" : "SPAWN_EGG")) {
                materialTags.add(Tag.SPAWN_EGG);
            }
            if (materialName.contains("WOOL")) {
                materialTags.add(Tag.WOOL);
            }
            if (materialName.equals("COD_BUCKET") || materialName.equals("PUFFERFISH_BUCKET") ||
                    materialName.equals("SALMON_BUCKET") || materialName.equals("TROPICAL_FISH_BUCKET")) {
                materialTags.add(Tag.FISH_BUCKET);
            }
            if (materialName.contains("SPAWNER")) {
                materialTags.add(Tag.SPAWNER);
            }
            if (materialName.contains("CAULDRON")) {
                materialTags.add(Tag.CAULDRON);
            }
            if (materialName.contains("BUCKET")) {
                materialTags.add(Tag.BUCKET);
            }
            if (materialName.contains("STEW") || materialName.contains("SOUP")) {
                materialTags.add(Tag.SOUP);
            }
            if (materialName.contains("SHULKER_BOX")) {
                materialTags.add(Tag.SHULKER_BOX);
            }
            if (materialName.contains("SWORD")) {
                materialTags.add(Tag.SWORD);
            }
            if (materialName.endsWith("_SPADE") || materialName.endsWith("_SHOVEL") ||
                    materialName.endsWith("_PICKAXE") || materialName.endsWith("_AXE")) {
                materialTags.add(Tag.TOOL);
            }
            if (materialName.endsWith("_PICKAXE")) {
                materialTags.add(Tag.PICKAXE);
            }
            if (materialName.equals("EGG") || materialName.equals("BLUE_EGG") || materialName.equals("BROWN_EGG")) {
                materialTags.add(Tag.CHICKEN_EGG);
            }

            if (!materialTags.isEmpty())
                enumMap.put(material, materialTags);
        }

        return enumMap;
    }

    private static EnumMap<DyeColor, ItemStack> setupWoolConverters() {
        EnumMap<DyeColor, ItemStack> enumMap = new EnumMap<>(DyeColor.class);

        for (DyeColor dyeColor : DyeColor.values()) {
            ItemStack woolItem = ServerVersion.isLegacy() ?
                    new ItemStack(Material.matchMaterial("WOOL"), 1, dyeColor.getWoolData()) :
                    new ItemStack(Material.matchMaterial(dyeColor.name() + "_WOOL"));
            enumMap.put(dyeColor, woolItem);
        }

        return enumMap;
    }

    private static EnumMap<EntityType, Material> setupEntityToMaterialSpawnEggConverters() {
        EnumMap<EntityType, Material> enumMap = new EnumMap<>(EntityType.class);

        for (EntityType entityType : EntityType.values()) {
            try {
                Material spawnEgg = getMaterialOrNull((ServerVersion.isLegacy() ? "MONSTER_EGG" : EntityTypes.fromName(entityType.name()) + "_SPAWN_EGG"));
                if (spawnEgg != null)
                    enumMap.put(entityType, spawnEgg);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return enumMap;
    }

    private static EnumMap<Material, EntityTypes> setupMaterialToEntitySpawnEggConverters() {
        EnumMap<Material, EntityTypes> enumMap = new EnumMap<>(Material.class);

        for(Material material : Material.values()) {
            if(hasTag(material, Tag.SPAWN_EGG)) {
                try {
                    enumMap.put(material, EntityTypes.fromName(material.name().replace("_SPAWN_EGG", "")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return enumMap;
    }

    public enum Tag {

        SPAWN_EGG,
        WOOL,
        FISH_BUCKET,
        CAULDRON,
        SPAWNER,
        BUCKET,
        SOUP,
        SHULKER_BOX,
        SWORD,
        TOOL,
        PICKAXE,
        CHICKEN_EGG;

        private static final EnumSet<Tag> EMPTY_TAGS = EnumSet.noneOf(Tag.class);

    }

}
