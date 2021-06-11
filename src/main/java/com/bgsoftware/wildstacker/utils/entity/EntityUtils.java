package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.CitizensHook;
import com.bgsoftware.wildstacker.hooks.LevelledMobsHook;
import com.bgsoftware.wildstacker.hooks.MythicMobsHook;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedReturnValue")
public final class EntityUtils {

    private static final ReflectMethod<Entity> GET_SHOULDER_ENTITY_RIGHT = new ReflectMethod<>(HumanEntity.class, "getShoulderEntityRight");
    private static final ReflectMethod<Void> SET_SHOULDER_ENTITY_RIGHT = new ReflectMethod<>(HumanEntity.class, "setShoulderEntityRight", Entity.class);
    private static final ReflectMethod<Entity> GET_SHOULDER_ENTITY_LEFT = new ReflectMethod<>(HumanEntity.class, "getShoulderEntityLeft");
    private static final ReflectMethod<Void> SET_SHOULDER_ENTITY_LEFT = new ReflectMethod<>(HumanEntity.class, "setShoulderEntityLeft", Entity.class);

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Object NULL = new Object();
    private static final Enchantment CURSE_OF_VANISH = Arrays.stream(Enchantment.values())
            .filter(enchantment -> enchantment.getName() != null && enchantment.getName().equals("VANISHING_CURSE"))
            .findFirst().orElse(null);

    public static String getFormattedType(String typeName) {
        if(typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        String customName = plugin.getSettings().customNames.get(typeName);
        if(customName != null)
            return customName;

        return format(typeName);
    }

    public static String format(String type){
        StringBuilder name = new StringBuilder();

        type = type.replace(" ", "_");

        for (String section : type.split("_")) {
            name.append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase()).append(" ");
        }

        return name.substring(0, name.length() - 1);
    }

    public static boolean isNameBlacklisted(String name){
        if(name == null)
            return false;

        if(!name.contains(ChatColor.COLOR_CHAR + ""))
            name = ChatColor.WHITE + name;

        List<Pattern> blacklistedNames = plugin.getSettings().blacklistedEntitiesNames;

        for(Pattern pattern : blacklistedNames){
            if(pattern.matcher(name).matches())
                return true;
        }

        return false;
    }

    public static void setKiller(LivingEntity livingEntity, Player killer){
        plugin.getNMSAdapter().setKiller(livingEntity, killer);
        if(killer != null)
            plugin.getNMSAdapter().updateLastDamageTime(livingEntity);
    }

    public static void removeParrotIfShoulder(Parrot parrot){
        if(GET_SHOULDER_ENTITY_RIGHT.isValid()) {
            EntitiesGetter.getNearbyEntities(((Animals) parrot).getLocation(), 1, entity -> entity instanceof Player).forEach(entity -> {
                if(parrot.equals(GET_SHOULDER_ENTITY_RIGHT.invoke(entity))){
                    SET_SHOULDER_ENTITY_RIGHT.invoke(entity, (Object) null);
                }
                if(parrot.equals(GET_SHOULDER_ENTITY_LEFT.invoke(entity))){
                    SET_SHOULDER_ENTITY_LEFT.invoke(entity, (Object) null);
                }
            });
        }
    }

    public static boolean isStackable(Entity entity){
        return (MythicMobsHook.isMythicMob(entity) ||
                (entity instanceof LivingEntity && !entity.getType().name().equals("ARMOR_STAND") && !(entity instanceof Player) && !CitizensHook.isNPC(entity)));
    }

    public static void giveExp(Player player, int amount){
        plugin.getNMSAdapter().giveExp(player, amount);
    }

    public static void spawnExp(Location location, int amount){
        Optional<Entity> closestOrb = EntitiesGetter.getNearbyEntities(location, 2, entity ->
                entity instanceof ExperienceOrb).findFirst();

        ExperienceOrb experienceOrb;

        if(closestOrb.isPresent()){
            experienceOrb = (ExperienceOrb) closestOrb.get();
            experienceOrb.setExperience(experienceOrb.getExperience() + amount);
        }
        else{
            experienceOrb = location.getWorld().spawn(location, ExperienceOrb.class);
            experienceOrb.setExperience(amount);
        }
    }

    public static String getEntityName(StackedEntity stackedEntity){
        int stackAmount = stackedEntity.getStackAmount();

        if(stackedEntity.getCustomName() != null) {
            if (stackedEntity.getSpawnCause() == SpawnCause.MYTHIC_MOBS) {
                return MythicMobsHook.getMythicName(stackedEntity.getLivingEntity()).replace("{}", String.valueOf(stackAmount));
            }
            else if (PluginHooks.isLevelledMobsEnabled && LevelledMobsHook.isLevelledMob(stackedEntity.getLivingEntity())) {
                return plugin.getNMSAdapter().getCustomName(stackedEntity.getLivingEntity()).replace("{}", String.valueOf(stackAmount));
            }
        }

        if (plugin.getSettings().entitiesCustomName.isEmpty())
            throw new NullPointerException();

        return stackAmount <= 1 && stackedEntity.getUpgrade().isDefault() ? "" :
                plugin.getSettings().entitiesNameBuilder.build(stackedEntity);
    }

    public static int getBadOmenAmplifier(Player player){
        int amplifier = 0;

        for(PotionEffect potionEffect : player.getActivePotionEffects()){
            if(potionEffect.getType().getName().equals("BAD_OMEN")) {
                amplifier = potionEffect.getAmplifier() + 1;
                break;
            }
        }

        if(amplifier >= 0 && amplifier <= 5)
            player.removePotionEffect(PotionEffectType.getByName("BAD_OMEN"));

        return Math.max(0, Math.min(5, amplifier));
    }

    public static boolean killedByZombie(LivingEntity livingEntity){
        EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
        return entityDamageEvent instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) entityDamageEvent).getDamager() instanceof Zombie;
    }

    @SuppressWarnings("all")
    public static StackCheckResult areSimilar(Entity en1, Entity en2){
        EntityTypes entityType = EntityTypes.fromEntity((LivingEntity) en1);

        if(en1.getType() != en2.getType())
            return StackCheckResult.NOT_SIMILAR;

        if(!MythicMobsHook.areSimilar(en1.getUniqueId(), en2.getUniqueId()))
            return StackCheckResult.MYTHIC_MOB_TYPE;

        if(PluginHooks.isLevelledMobsEnabled && !LevelledMobsHook.areSimilar(en1, en2))
            return StackCheckResult.LEVELLED_MOB_LEVEL;

        if(StackCheck.AGE.isEnabled() && en1 instanceof Ageable){
            if((((Ageable) en1).getAge() >= 0) != (((Ageable) en2).getAge() >= 0))
                return StackCheckResult.AGE;
        }

        if(StackCheck.EXACT_AGE.isEnabled() && en1 instanceof Ageable){
            if(((Ageable) en1).getAge() != ((Ageable) en2).getAge())
                return StackCheckResult.EXACT_AGE;
        }

        if(StackCheck.IS_TAMED.isEnabled() && en1 instanceof Tameable){
            if(((Tameable) en1).isTamed() != ((Tameable) en2).isTamed())
                return StackCheckResult.IS_TAMED;
        }

        if(StackCheck.ANIMAL_OWNER.isEnabled() && en1 instanceof Tameable){
            if(!requireNotNull(((Tameable) en1).getOwner()).equals(requireNotNull(((Tameable) en2).getOwner())))
                return StackCheckResult.ANIMAL_OWNER;
        }

        if(StackCheck.SKELETON_TYPE.isEnabled() && StackCheck.SKELETON_TYPE.isTypeAllowed(entityType)){
            try {
                if (((Skeleton) en1).getSkeletonType() != ((Skeleton) en2).getSkeletonType())
                    return StackCheckResult.SKELETON_TYPE;
            }catch(Throwable ignored){}
        }

        if(StackCheck.ZOMBIE_BABY.isEnabled() && StackCheck.ZOMBIE_BABY.isTypeAllowed(entityType)){
            if (((Zombie) en1).isBaby() != ((Zombie) en2).isBaby())
                return StackCheckResult.ZOMBIE_BABY;
        }

        if(StackCheck.SLIME_SIZE.isEnabled() && StackCheck.SLIME_SIZE.isTypeAllowed(entityType)){
            if (((Slime) en1).getSize() != ((Slime) en2).getSize())
                return StackCheckResult.SLIME_SIZE;
        }

        if(StackCheck.ZOMBIE_PIGMAN_ANGRY.isEnabled() && StackCheck.ZOMBIE_PIGMAN_ANGRY.isTypeAllowed(entityType)){
            if((((PigZombie) en1).getAnger() >= 0) != (((PigZombie) en2).getAnger() >= 0))
                return StackCheckResult.ZOMBIE_PIGMAN_ANGRY;
        }

        if(StackCheck.ENDERMAN_CARRIED_BLOCK.isEnabled() && StackCheck.ENDERMAN_CARRIED_BLOCK.isTypeAllowed(entityType)){
            if(!plugin.getNMSAdapter().getEndermanCarried((Enderman) en1).equals(plugin.getNMSAdapter().getEndermanCarried((Enderman) en2)))
                return StackCheckResult.ENDERMAN_CARRIED_BLOCK;
        }

        if(StackCheck.BAT_AWAKE.isEnabled() && StackCheck.BAT_AWAKE.isTypeAllowed(entityType)){
            if (((Bat) en1).isAwake() != ((Bat) en2).isAwake())
                return StackCheckResult.BAT_AWAKE;
        }

        if(StackCheck.GUARDIAN_ELDER.isEnabled() && StackCheck.GUARDIAN_ELDER.isTypeAllowed(entityType)){
            if (((Guardian) en1).isElder() != ((Guardian) en2).isElder())
                return StackCheckResult.GUARDIAN_ELDER;
        }

        if(StackCheck.PIG_SADDLE.isEnabled() && StackCheck.PIG_SADDLE.isTypeAllowed(entityType)){
            if (((Pig) en1).hasSaddle() != ((Pig) en2).hasSaddle())
                return StackCheckResult.PIG_SADDLE;
        }

        if(StackCheck.SHEEP_SHEARED.isEnabled() && StackCheck.SHEEP_SHEARED.isTypeAllowed(entityType)){
            if (((Sheep) en1).isSheared() != ((Sheep) en2).isSheared())
                return StackCheckResult.SHEEP_SHEARED;
        }

        if(StackCheck.SHEEP_COLOR.isEnabled() && StackCheck.SHEEP_COLOR.isTypeAllowed(entityType)){
            if (((Sheep) en1).getColor() != ((Sheep) en2).getColor())
                return StackCheckResult.SHEEP_COLOR;
        }

        if(StackCheck.WOLF_ANGRY.isEnabled() && StackCheck.WOLF_ANGRY.isTypeAllowed(entityType)){
            if (((Wolf) en1).isAngry() != ((Wolf) en2).isAngry())
                return StackCheckResult.WOLF_ANGRY;
        }

        if(StackCheck.WOLF_COLLAR_COLOR.isEnabled() && StackCheck.WOLF_COLLAR_COLOR.isTypeAllowed(entityType)){
            if (((Wolf) en1).getCollarColor() != ((Wolf) en2).getCollarColor())
                return StackCheckResult.WOLF_COLLAR_COLOR;
        }

        if(StackCheck.OCELOT_TYPE.isEnabled() && StackCheck.OCELOT_TYPE.isTypeAllowed(entityType)){
            if (((Ocelot) en1).getCatType() != ((Ocelot) en2).getCatType())
                return StackCheckResult.OCELOT_TYPE;
        }

        if(StackCheck.CAT_TYPE.isEnabled() && StackCheck.CAT_TYPE.isTypeAllowed(entityType)){
            if (((Cat) en1).getCatType() != ((Cat) en2).getCatType())
                return StackCheckResult.CAT_TYPE;
        }

        if(StackCheck.CAT_COLLAR_COLOR.isEnabled() && StackCheck.CAT_COLLAR_COLOR.isTypeAllowed(entityType)){
            if (((Cat) en1).getCollarColor() != ((Cat) en2).getCollarColor())
                return StackCheckResult.CAT_COLLAR_COLOR;
        }

        if(StackCheck.HORSE_TYPE.isEnabled() && StackCheck.HORSE_TYPE.isTypeAllowed(entityType)){
            if(en1 instanceof Horse) {
                if (((Horse) en1).getVariant() != ((Horse) en2).getVariant())
                    return StackCheckResult.HORSE_TYPE;
            }
            else{
                if (((AbstractHorse) en1).getVariant() != ((AbstractHorse) en2).getVariant())
                    return StackCheckResult.HORSE_TYPE;
            }
        }

        if(StackCheck.HORSE_COLOR.isEnabled() && en1 instanceof Horse){
            if(((Horse) en1).getColor() != ((Horse) en2).getColor())
                return StackCheckResult.HORSE_COLOR;
        }

        if(StackCheck.HORSE_STYLE.isEnabled() && en1 instanceof Horse){
            if(((Horse) en1).getStyle() != ((Horse) en2).getStyle())
                return StackCheckResult.HORSE_STYLE;
        }

        if(StackCheck.HORSE_CARRYING_CHEST.isEnabled() && en1 instanceof Horse){
            if (((Horse) en1).isCarryingChest() != ((Horse) en2).isCarryingChest())
                return StackCheckResult.HORSE_CARRYING_CHEST;
        }

        if(StackCheck.HORSE_TAME_PROGRESS.isEnabled() && StackCheck.HORSE_TAME_PROGRESS.isTypeAllowed(entityType)){
            if(en1 instanceof Horse) {
                if (((Horse) en1).getDomestication() != ((Horse) en2).getDomestication())
                    return StackCheckResult.HORSE_TAME_PROGRESS;
            }
            else{
                if (((AbstractHorse) en1).getDomestication() != ((AbstractHorse) en2).getDomestication())
                    return StackCheckResult.HORSE_TAME_PROGRESS;
            }
        }

        if(StackCheck.HORSE_MAX_TAME_PROGRESS.isEnabled() && StackCheck.HORSE_MAX_TAME_PROGRESS.isTypeAllowed(entityType)){
            if(en1 instanceof Horse) {
                if (((Horse) en1).getMaxDomestication() != ((Horse) en2).getMaxDomestication())
                    return StackCheckResult.HORSE_MAX_TAME_PROGRESS;
            }
            else{
                if (((AbstractHorse) en1).getMaxDomestication() != ((AbstractHorse) en2).getMaxDomestication())
                    return StackCheckResult.HORSE_MAX_TAME_PROGRESS;
            }
        }

        if(StackCheck.HORSE_JUMP.isEnabled() && StackCheck.HORSE_JUMP.isTypeAllowed(entityType)){
            if(en1 instanceof Horse) {
                if (((Horse) en1).getJumpStrength() != ((Horse) en2).getJumpStrength())
                    return StackCheckResult.HORSE_JUMP;
            }
            else{
                if (((AbstractHorse) en1).getJumpStrength() != ((AbstractHorse) en2).getJumpStrength())
                    return StackCheckResult.HORSE_JUMP;
            }
        }

        if(StackCheck.RABBIT_TYPE.isEnabled() && StackCheck.RABBIT_TYPE.isTypeAllowed(entityType)){
            if(((Rabbit) en1).getRabbitType() != ((Rabbit) en2).getRabbitType())
                return StackCheckResult.RABBIT_TYPE;
        }

        if(StackCheck.VILLAGER_PROFESSION.isEnabled() && StackCheck.VILLAGER_PROFESSION.isTypeAllowed(entityType)){
            if(en1 instanceof Villager) {
                if (((Villager) en1).getProfession() != ((Villager) en2).getProfession())
                    return StackCheckResult.VILLAGER_PROFESSION;
            }else if(en1 instanceof Zombie){
                if (((Zombie) en1).isVillager() != ((Zombie) en2).isVillager())
                    return StackCheckResult.NOT_SIMILAR;
                try{
                    if(en1 instanceof ZombieVillager){
                        if (((ZombieVillager) en1).getVillagerProfession() != ((ZombieVillager) en2).getVillagerProfession())
                            return StackCheckResult.VILLAGER_PROFESSION;
                    }
                }catch(Throwable ignored){}
            }
        }

        if(StackCheck.LLAMA_COLOR.isEnabled() && StackCheck.LLAMA_COLOR.isTypeAllowed(entityType)){
            if(((Llama) en1).getColor() != ((Llama) en2).getColor())
                return StackCheckResult.LLAMA_COLOR;
        }

        if(StackCheck.LLAMA_STRENGTH.isEnabled() && StackCheck.LLAMA_STRENGTH.isTypeAllowed(entityType)){
            if(((Llama) en1).getStrength() != ((Llama) en2).getStrength())
                return StackCheckResult.LLAMA_STRENGTH;
        }

        if(StackCheck.PARROT_TYPE.isEnabled() && StackCheck.PARROT_TYPE.isTypeAllowed(entityType)){
            if(((Parrot) en1).getVariant() != ((Parrot) en2).getVariant())
                return StackCheckResult.PARROT_TYPE;
        }

        if(StackCheck.PUFFERFISH_STATE.isEnabled() && StackCheck.PUFFERFISH_STATE.isTypeAllowed(entityType)){
            if(((PufferFish) en1).getPuffState() != ((PufferFish) en2).getPuffState())
                return StackCheckResult.PUFFERFISH_STATE;
        }

        if (StackCheck.TROPICALFISH_TYPE.isEnabled() && StackCheck.TROPICALFISH_TYPE.isTypeAllowed(entityType)){
            if(((TropicalFish) en1).getPattern() != ((TropicalFish) en2).getPattern())
                return StackCheckResult.TROPICALFISH_TYPE;
        }

        if (StackCheck.TROPICALFISH_BODY_COLOR.isEnabled() && StackCheck.TROPICALFISH_BODY_COLOR.isTypeAllowed(entityType)){
            if(((TropicalFish) en1).getBodyColor() != ((TropicalFish) en2).getBodyColor())
                return StackCheckResult.TROPICALFISH_BODY_COLOR;
        }

        if(StackCheck.TROPICALFISH_TYPE_COLOR.isEnabled() && StackCheck.TROPICALFISH_TYPE_COLOR.isTypeAllowed(entityType)){
            if(((TropicalFish) en1).getPatternColor() != ((TropicalFish) en2).getPatternColor())
                return StackCheckResult.TROPICALFISH_TYPE_COLOR;
        }

        if(StackCheck.PHANTOM_SIZE.isEnabled() && StackCheck.PHANTOM_SIZE.isTypeAllowed(entityType)){
            if(((Phantom) en1).getSize() != ((Phantom) en2).getSize())
                return StackCheckResult.PHANTOM_SIZE;
        }

        if(StackCheck.MOOSHROOM_TYPE.isEnabled() && StackCheck.MOOSHROOM_TYPE.isTypeAllowed(entityType)){
            if(plugin.getNMSAdapter().getMooshroomType((MushroomCow) en1) != plugin.getNMSAdapter().getMooshroomType((MushroomCow) en2))
                return StackCheckResult.MOOSHROOM_TYPE;
        }

        return StackCheckResult.SUCCESS;
    }

    public static boolean canBeBred(Animals animal) {
        return animal.getAge() == 0 && !plugin.getNMSAdapter().isInLove(animal);
    }

    public static List<ItemStack> getEquipment(LivingEntity livingEntity, int lootBonusLevel){
        List<ItemStack> drops = new ArrayList<>();

        EntityEquipment entityEquipment = livingEntity.getEquipment();

        addDropArmor(drops, livingEntity, entityEquipment.getItemInHand(), lootBonusLevel,
                plugin.getNMSAdapter().getItemInMainHandDropChance(entityEquipment));

        if(ServerVersion.isAtLeast(ServerVersion.v1_9)) {
            addDropArmor(drops, livingEntity, plugin.getNMSAdapter().getItemInOffHand(entityEquipment), lootBonusLevel,
                    plugin.getNMSAdapter().getItemInOffHandDropChance(entityEquipment));
        }

        addDropArmor(drops, livingEntity, entityEquipment.getHelmet(), lootBonusLevel, entityEquipment.getHelmetDropChance());
        addDropArmor(drops, livingEntity, entityEquipment.getChestplate(), lootBonusLevel, entityEquipment.getChestplateDropChance());
        addDropArmor(drops, livingEntity, entityEquipment.getLeggings(), lootBonusLevel, entityEquipment.getLeggingsDropChance());
        addDropArmor(drops, livingEntity, entityEquipment.getBoots(), lootBonusLevel, entityEquipment.getBootsDropChance());

        switch (EntityTypes.fromEntity(livingEntity)){
            case PIG:
                if(((Pig) livingEntity).hasSaddle())
                    drops.add(new ItemStack(Material.SADDLE));
                break;
            case STRIDER:
                if(plugin.getNMSAdapter().doesStriderHaveSaddle((org.bukkit.entity.Strider) livingEntity))
                    drops.add(new ItemStack(Material.SADDLE));
                break;
            case HORSE:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case LLAMA:
            case TRADER_LLAMA: {
                Inventory inventory;

                if (livingEntity instanceof Horse) {
                    inventory = ((Horse) livingEntity).getInventory();
                } else {
                    inventory = ((InventoryHolder) livingEntity).getInventory();
                }

                for(ItemStack itemStack : inventory.getContents()){
                    if(itemStack != null && itemStack.getType() != Material.AIR &&
                            (CURSE_OF_VANISH == null || itemStack.getEnchantmentLevel(CURSE_OF_VANISH) <= 0))
                        drops.add(itemStack);
                }

                try{
                    if(livingEntity instanceof org.bukkit.entity.ChestedHorse && ((org.bukkit.entity.ChestedHorse) livingEntity).isCarryingChest())
                        drops.add(new ItemStack(Material.CHEST));
                }catch (Throwable ex){
                    if(livingEntity instanceof Horse && ((Horse) livingEntity).isCarryingChest())
                        drops.add(new ItemStack(Material.CHEST));
                }

                break;
            }
        }

        return drops;
    }

    public static void clearEquipment(LivingEntity livingEntity){
        boolean clearEquipment = plugin.getSettings().entitiesClearEquipment;
        EntityEquipment entityEquipment = livingEntity.getEquipment();

        if(clearEquipment || plugin.getNMSAdapter().getItemInMainHandDropChance(entityEquipment) >= 2.0F)
            plugin.getNMSAdapter().setItemInMainHand(entityEquipment, new ItemStack(Material.AIR));

        if(ServerVersion.isAtLeast(ServerVersion.v1_9) && (clearEquipment || plugin.getNMSAdapter().getItemInOffHandDropChance(entityEquipment) >= 2.0F))
                plugin.getNMSAdapter().setItemInOffHand(entityEquipment, new ItemStack(Material.AIR));

        if(clearEquipment || entityEquipment.getHelmetDropChance() >= 2.0F)
            entityEquipment.setHelmet(new ItemStack(Material.AIR));

        if(clearEquipment || entityEquipment.getChestplateDropChance() >= 2.0F)
            entityEquipment.setChestplate(new ItemStack(Material.AIR));

        if(clearEquipment || entityEquipment.getLeggingsDropChance() >= 2.0F)
            entityEquipment.setLeggings(new ItemStack(Material.AIR));

        if(clearEquipment || entityEquipment.getBootsDropChance() >= 2.0F)
            entityEquipment.setBoots(new ItemStack(Material.AIR));

        switch (EntityTypes.fromEntity(livingEntity)){
            case PIG:
                ((Pig) livingEntity).setSaddle(false);
                break;
            case STRIDER:
                plugin.getNMSAdapter().removeStriderSaddle((org.bukkit.entity.Strider) livingEntity);
                break;
            case HORSE:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case LLAMA:
            case TRADER_LLAMA: {
                Inventory inventory;

                if (livingEntity instanceof Horse) {
                    inventory = ((Horse) livingEntity).getInventory();
                } else {
                    inventory = ((InventoryHolder) livingEntity).getInventory();
                }

                for(int i = 0; i < inventory.getSize(); i++)
                    inventory.setItem(i, new ItemStack(Material.AIR));

                try{
                    if(livingEntity instanceof org.bukkit.entity.ChestedHorse)
                        ((org.bukkit.entity.ChestedHorse) livingEntity).setCarryingChest(false);
                }catch (Throwable ex){
                    if(livingEntity instanceof Horse)
                        ((Horse) livingEntity).setCarryingChest(false);
                }

                break;
            }
        }

    }

    private static void addDropArmor(List<ItemStack> drops, LivingEntity livingEntity, ItemStack itemStack, int lootBonusLevel, double dropChance){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if(itemStack != null && itemStack.getType() != Material.AIR &&
                (CURSE_OF_VANISH == null || itemStack.getEnchantmentLevel(CURSE_OF_VANISH) <= 0) &&
                (livingEntity.getKiller() != null || dropChance > 1) &&
                random.nextFloat() - (float) lootBonusLevel * 0.01F < dropChance){
            ItemStack toDrop = itemStack.clone();
            int maxDurability = toDrop.getType().getMaxDurability();

            if (dropChance <= 1 && plugin.getNMSAdapter().shouldArmorBeDamaged(itemStack)) {
                int maxDur = ServerVersion.isAtLeast(ServerVersion.v1_11) ? 3 : 25;
                toDrop.setDurability((short) (maxDurability - random.nextInt(1 + random.nextInt(Math.max(maxDurability - maxDur, 1)))));
            }

            drops.add(toDrop);
        }
    }

    private static Object requireNotNull(Object obj){
        return obj == null ? NULL : obj;
    }

}
