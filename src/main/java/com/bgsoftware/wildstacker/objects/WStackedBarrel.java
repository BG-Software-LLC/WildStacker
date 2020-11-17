package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class WStackedBarrel extends WStackedHologramObject<Block> implements StackedBarrel {

    private final List<Inventory> linkedInventories = new ArrayList<>();

    private final ItemStack barrelItem;
    private ArmorStand blockDisplay;

    public WStackedBarrel(Block block, ItemStack itemStack){
        this(block, itemStack, 1);
    }

    public WStackedBarrel(Block block, ItemStack itemStack, int stackAmount){
        super(block, stackAmount);
        this.barrelItem = itemStack;
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        super.setStackAmount(stackAmount, updateName);
        Query.BARREL_INSERT.insertParameters()
                .setLocation(getLocation())
                .setObject(getStackAmount())
                .setItemStack(getBarrelItem(1))
                .queue(getLocation());
    }

    @Override
    public Block getBlock() {
        return object;
    }

    @Override
    public Material getType() {
        return barrelItem.getType();
    }

    @Override
    public short getData() {
        return barrelItem.getDurability();
    }

    @Override
    public Location getLocation() {
        return object.getLocation();
    }

    @Override
    public World getWorld() {
        return object.getWorld();
    }

    @Override
    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        int limit = plugin.getSettings().barrelsLimits.getOrDefault(barrelItem.getType(), Integer.MAX_VALUE);
        return limit < 1 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public int getMergeRadius() {
        int radius = plugin.getSettings().barrelsMergeRadius.getOrDefault(barrelItem.getType(), 0);
        return radius < 1 ? 0 : radius;
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedBarrels.contains(barrelItem.getType());
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedBarrels.size() == 0 ||
                plugin.getSettings().whitelistedBarrels.contains(barrelItem.getType());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().barrelsDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public boolean isCached() {
        return plugin.getSettings().barrelsStackingEnabled && super.isCached();
    }

    @Override
    public void remove() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::remove);
            return;
        }

        plugin.getSystemManager().removeStackObject(this);

        Query.BARREL_DELETE.insertParameters().setLocation(getLocation()).queue(getLocation());

        removeHologram();
        removeDisplayBlock();

        List<HumanEntity> viewers = new ArrayList<>();
        linkedInventories.forEach(i ->  viewers.addAll(i.getViewers()));

        viewers.forEach(HumanEntity::closeInventory);

        linkedInventories.clear();
    }

    @Override
    public void updateName() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::updateName);
            return;
        }

        String customName = plugin.getSettings().barrelsCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();

        if(amount < 1) {
            removeHologram();
            return;
        }

        customName = customName
                .replace("{0}", Integer.toString(amount))
                .replace("{1}", ItemUtils.getFormattedType(barrelItem))
                .replace("{2}", ItemUtils.getFormattedType(barrelItem).toUpperCase());

        setHologramName(customName, true);
    }

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        if(!plugin.getSettings().barrelsStackingEnabled)
            return StackCheckResult.NOT_ENABLED;

        return super.runStackCheck(stackedObject);
    }

    @Override
    public Optional<Block> runStack() {
        if(getStackLimit() <= 1)
            return Optional.empty();

        Chunk chunk = getChunk();

        boolean chunkMerge = plugin.getSettings().chunkMergeBarrels;
        Location blockLocation = getLocation();

        Stream<StackedBarrel> barrelStream;

        if (chunkMerge) {
            barrelStream = plugin.getSystemManager().getStackedBarrels(chunk).stream();
        } else {
            int range = getMergeRadius();

            if(range <= 0)
                return Optional.empty();

            Location location = getLocation();

            int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
            int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

            barrelStream = plugin.getSystemManager().getStackedBarrels().stream()
                    .filter(stackedBarrel -> {
                        Location loc = stackedBarrel.getLocation();
                        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
                    });
        }

        Optional<StackedBarrel> barrelOptional = GeneralUtils.getClosest(blockLocation, barrelStream
                .filter(stackedBarrel -> runStackCheck(stackedBarrel) == StackCheckResult.SUCCESS));

        if (barrelOptional.isPresent()) {
            StackedBarrel targetBarrel = barrelOptional.get();

            StackResult stackResult = runStack(targetBarrel);

            if (stackResult == StackResult.SUCCESS) {
                return barrelOptional.map(StackedBarrel::getBlock);
            }
        }

        return Optional.empty();
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if (!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
            return StackResult.NOT_SIMILAR;

        StackedBarrel targetBarrel = (StackedBarrel) stackedObject;
        int newStackAmount = this.getStackAmount() + targetBarrel.getStackAmount();

        if(!EventsCaller.callBarrelStackEvent(targetBarrel, this))
            return StackResult.EVENT_CANCELLED;

        targetBarrel.setStackAmount(newStackAmount, true);

        this.remove();

        spawnStackParticle(true);

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount, Entity entity) {
        if(!EventsCaller.callBarrelUnstackEvent(this, entity, amount))
            return UnstackResult.EVENT_CANCELLED;

        int stackAmount = this.getStackAmount() - amount;

        setStackAmount(stackAmount, true);

        if(stackAmount < 1)
            remove();

        return UnstackResult.SUCCESS;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedBarrel && getType() == ((StackedBarrel) stackedObject).getType() &&
                getData() == ((StackedBarrel) stackedObject).getData();
    }

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if (!checkEnabled || plugin.getSettings().barrelsParticlesEnabled) {
            Location location = getLocation();
            for (ParticleWrapper particleWrapper : plugin.getSettings().barrelsParticles)
                particleWrapper.spawnParticle(location);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedBarrel ? getLocation().equals(((StackedBarrel) obj).getLocation()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedBarrel{location=%s,amount=%s,type=%s,data=%s}", getLocation(), getStackAmount(), getType().name(), getData());
    }

    @Override
    public void createDisplayBlock() {
        Location location = getLocation();

        removeDisplayBlock();

        if(location.getBlock().getType() == Material.CAULDRON) {
            blockDisplay = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
            blockDisplay.setVisible(false);
            blockDisplay.setSmall(true);
            blockDisplay.setGravity(false);
            blockDisplay.setHelmet(barrelItem);
            plugin.getNMSAdapter().setCustomName(blockDisplay, "BlockDisplay");
            plugin.getNMSAdapter().setCustomNameVisible(blockDisplay, false);
        }
    }

    @Override
    public void removeDisplayBlock() {
        Location location = getLocation();
        //Making sure there isn't already a blockDisplay
        for(Entity entity : location.getChunk().getEntities()){
            //Entity should be on this barrel
            if(entity instanceof ArmorStand && ((ArmorStand) entity).getHelmet() != null &&
                    !((ArmorStand) entity).isVisible() && ((ArmorStand) entity).isSmall() &&
                    entity.getLocation().getBlock().getLocation().equals(location)){
                entity.remove();
            }
        }
    }

    @Override
    public ArmorStand getDisplayBlock() {
        return blockDisplay;
    }

    @Override
    public ItemStack getBarrelItem(int amount) {
        ItemStack itemStack = barrelItem.clone();

        if(plugin.getSettings().dropStackedItem){
            itemStack = ItemUtils.setSpawnerItemAmount(itemStack, amount);
            itemStack.setAmount(1);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(WildStackerPlugin.getPlugin().getSettings().giveItemName
                    .replace("{0}", amount + "")
                    .replace("{1}", ItemUtils.getFormattedType(new ItemStack(getType())))
                    .replace("{2}", "Barrel")
            );

            itemStack.setItemMeta(itemMeta);
        }
        else {
            itemStack.setAmount(amount);
        }

        return itemStack;
    }

    public void linkInventory(Inventory inventory){
        this.linkedInventories.add(inventory);
    }

    public void unlinkInventory(Inventory inventory){
        this.linkedInventories.remove(inventory);
    }

    public static StackedBarrel of(Block block){
        return plugin.getSystemManager().getStackedBarrel(block);
    }

    public static StackedBarrel of(Location location){
        return plugin.getSystemManager().getStackedBarrel(location);
    }

}
