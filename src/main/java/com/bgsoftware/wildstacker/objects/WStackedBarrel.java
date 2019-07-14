package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("RedundantIfStatement")
public class WStackedBarrel extends WStackedObject<Block> implements StackedBarrel {

    private ItemStack barrelItem;
    private ArmorStand blockDisplay;

    public WStackedBarrel(Block block, ItemStack itemStack){
        this(block, itemStack, 1);
    }

    public WStackedBarrel(Block block, ItemStack itemStack, int stackAmount){
        super(block, stackAmount);
        this.barrelItem = itemStack;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(getLocation().getBlock().getType() == Material.CAULDRON){
                Query.BARREL_INSERT.getStatementHolder()
                        .setLocation(getLocation())
                        .setInt(getStackAmount())
                        .setItemStack(itemStack)
                        .execute(true);
            }
        }, 1L);
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        super.setStackAmount(stackAmount, updateName);
        Query.BARREL_UPDATE_STACK_AMOUNT.getStatementHolder()
                .setInt(getStackAmount())
                .setLocation(getLocation())
                .execute(true);
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
    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        return plugin.getSettings().barrelsLimits.getOrDefault(getBarrelItem(1), Integer.MAX_VALUE);
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedBarrels.contains(getBarrelItem(1));
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedBarrels.isEmpty() ||
                plugin.getSettings().whitelistedBarrels.contains(getBarrelItem(1));
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().barrelsDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);

        Query.BARREL_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);

        plugin.getProviders().deleteHologram(this);
        removeDisplayBlock();
    }

    @Override
    public void updateName() {
        String customName = plugin.getSettings().barrelsCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();

        if(amount < 1) {
            plugin.getProviders().deleteHologram(this);
            return;
        }

        customName = customName
                .replace("{0}", Integer.toString(amount))
                .replace("{1}", ItemUtil.getFormattedType(barrelItem))
                .replace("{2}", ItemUtil.getFormattedType(barrelItem).toUpperCase());
        plugin.getProviders().changeLine(this, customName, true);
    }

    @Override
    public Block tryStack() {
        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;
        int range = plugin.getSettings().spawnersCheckRange;

        Location location = getLocation();

        int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
        int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

        if(chunkMerge) {
            minX = 0; maxX = 16;
            minZ = 0; maxZ = 16;
        }

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = chunkMerge ? location.getChunk().getBlock(x, y, z) : location.getWorld().getBlockAt(x, y, z);
                    if (plugin.getSystemManager().isStackedBarrel(block) && !block.getLocation().equals(location)) {
                        StackedBarrel targetBarrel = of(block);
                        if (tryStackInto(targetBarrel))
                            return targetBarrel.getBlock();
                    }
                }
            }
        }

        updateName();
        return null;
    }

    @Override
    public boolean canStackInto(StackedObject stackedObject) {
        if(!plugin.getSettings().barrelsStackingEnabled)
            return false;

        if(equals(stackedObject) || !(stackedObject instanceof StackedBarrel) || !isSimilar(stackedObject))
            return false;

        if(!isWhitelisted() || isBlacklisted() || isWorldDisabled())
            return false;

        StackedBarrel targetBarrel = (StackedBarrel) stackedObject;

        if(!targetBarrel.isWhitelisted() || targetBarrel.isBlacklisted() || targetBarrel.isWorldDisabled())
            return false;

        int newStackAmount = this.getStackAmount() + targetBarrel.getStackAmount();

        if(getStackLimit() < newStackAmount)
            return false;

        return true;
    }

    @Override
    public boolean tryStackInto(StackedObject stackedObject) {
        if(!canStackInto(stackedObject))
            return false;

        StackedBarrel targetBarrel = (StackedBarrel) stackedObject;
        int newStackAmount = this.getStackAmount() + targetBarrel.getStackAmount();

        BarrelStackEvent barrelStackEvent = new BarrelStackEvent(targetBarrel, this);
        Bukkit.getPluginManager().callEvent(barrelStackEvent);

        if(barrelStackEvent.isCancelled())
            return false;

        targetBarrel.setStackAmount(newStackAmount, true);

        this.remove();

        return true;
    }

    @Override
    public boolean tryUnstack(int amount) {
        BarrelUnstackEvent barrelUnstackEvent = new BarrelUnstackEvent(this, amount);
        Bukkit.getPluginManager().callEvent(barrelUnstackEvent);

        if(barrelUnstackEvent.isCancelled())
            return false;

        setStackAmount(stackAmount - amount, true);

        if(stackAmount < 1)
            remove();

        return true;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedBarrel && getType() == ((StackedBarrel) stackedObject).getType() &&
                getData() == ((StackedBarrel) stackedObject).getData();
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
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static StackedBarrel of(Block block){
        return plugin.getSystemManager().getStackedBarrel(block);
    }

}
