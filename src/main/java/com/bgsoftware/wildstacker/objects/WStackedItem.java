package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.events.ItemStackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings({"RedundantIfStatement", "WeakerAccess"})
public class WStackedItem extends WStackedObject<Item> implements StackedItem {

    public WStackedItem(Item item){
        this(item, item.getItemStack().getAmount());
    }

    public WStackedItem(Item item, int stackAmount){
        super(item, stackAmount);
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        super.setStackAmount(stackAmount, updateName);
        if(stackAmount > 0) {
            ItemStack itemStack = object.getItemStack().clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), stackAmount));
            object.setItemStack(itemStack);
        }
    }

    /*
     * Item's methods
     */

    @Override
    public Item getItem(){
        return object;
    }

    @Override
    public UUID getUniqueId(){
        return object.getUniqueId();
    }

    @Override
    public void setItemStack(ItemStack itemStack){
        if(itemStack == null || itemStack.getType() == Material.AIR)
            remove();
        else
            object.setItemStack(itemStack);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack is = object.getItemStack().clone();
        is.setAmount(getStackAmount());
        return is;
    }

    /*
     * StackedObject's methods
     */

    @Override
    public Chunk getChunk() {
        return object.getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        return plugin.getSettings().itemsLimits.getOrDefault(getItemStack(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedItems.contains(getItemStack());
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedItems.isEmpty() ||
                plugin.getSettings().whitelistedItems.contains(getItemStack());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().itemsDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);
        object.remove();
    }

    @Override
    public void updateName() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::updateName);
            return;
        }

        if(!plugin.getSettings().itemsStackingEnabled)
            return;

        String customName = plugin.getSettings().itemsCustomName;

        ItemStack itemStack = getItemStack();

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();
        boolean updateName = plugin.getSettings().itemsUnstackedCustomName || amount > 1;

        if (updateName) {
            String itemType = ItemUtils.getFormattedType(itemStack);
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemType;

            if(plugin.getSettings().itemsDisplayEnabled)
                itemType = displayName;

            itemType = itemType.replace("{0}", displayName);

            customName = customName
                    .replace("{0}", Integer.toString(amount))
                    .replace("{1}", itemType)
                    .replace("{2}", itemType.toUpperCase());
        }


        object.setCustomName(customName);
        object.setCustomNameVisible(updateName);
    }

    @Override
    public boolean canStackInto(StackedObject stackedObject) {
        if (!plugin.getSettings().itemsStackingEnabled)
            return false;

        if (equals(stackedObject) || !(stackedObject instanceof StackedItem) || !isSimilar(stackedObject))
            return false;

        if(!isWhitelisted() || isBlacklisted() || isWorldDisabled())
            return false;

        StackedItem targetItem = (StackedItem) stackedObject;

        if(!targetItem.isWhitelisted() || targetItem.isBlacklisted() || targetItem.isWorldDisabled())
            return false;

        if(targetItem.getItem().getLocation().getBlock().getType() == Materials.NETHER_PORTAL.toBukkitType())
            return false;

        int newStackAmount = this.getStackAmount() + targetItem.getStackAmount();

        if (getStackLimit() < newStackAmount)
            return false;

        return true;
    }

    @Override
    public void runStackAsync(Consumer<Optional<Item>> result) {
        int range = plugin.getSettings().itemsCheckRange;

        List<Entity> nearbyEntities = plugin.getNMSAdapter().getNearbyEntities(object, range, entity -> entity instanceof Item);

        StackService.execute(() -> {
            Location itemLocation = getItem().getLocation();

            Optional<StackedItem> itemOptional = nearbyEntities.stream().map(WStackedItem::of).filter(this::canStackInto)
                    .min(Comparator.comparingDouble(o -> o.getItem().getLocation().distance(itemLocation)));

            if(itemOptional.isPresent()){
                StackedItem targetItem = itemOptional.get();

                StackResult stackResult = runStack(targetItem);

                if(stackResult == StackResult.SUCCESS) {
                    if(result != null)
                        result.accept(itemOptional.map(StackedItem::getItem));
                    return;
                }
            }

            updateName();

            if(result != null)
                result.accept(Optional.empty());
        });
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if(!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (!canStackInto(stackedObject))
            return StackResult.NOT_SIMILAR;

        StackedItem targetItem = (StackedItem) stackedObject;

        ItemStackEvent itemStackEvent = new ItemStackEvent(targetItem, this);
        Bukkit.getPluginManager().callEvent(itemStackEvent);

        if (itemStackEvent.isCancelled())
            return StackResult.EVENT_CANCELLED;

        targetItem.setStackAmount(this.getStackAmount() + targetItem.getStackAmount(), false);

        Executor.sync(() -> {
            if (targetItem.getItem().isValid())
                targetItem.updateName();
        }, 2L);

        this.remove();

        if(plugin.getSettings().itemsParticlesEnabled) {
            Location location = getItem().getLocation();
            for(ParticleWrapper particleWrapper : plugin.getSettings().itemsParticles)
                particleWrapper.spawnParticle(location);
        }

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount) {
        throw new UnsupportedOperationException("Cannot unstack stacked items. Use giveItemStack() method.");
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedItem && getItemStack().isSimilar(((StackedItem) stackedObject).getItemStack());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedItem ? getUniqueId().equals(((StackedItem) obj).getUniqueId()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedItem{uuid=%s,amount=%s,item=%s}", getUniqueId(), getStackAmount(), object.getItemStack());
    }

    /*
     * StackedItem's methods
     */

    @Override
    public void giveItemStack(Inventory inventory) {
        ItemStack itemStack = getItemStack();

        int freeSpace = ItemUtils.getFreeSpace(inventory, itemStack);
        int startAmount = itemStack.getAmount();
        int giveAmount = Math.min(itemStack.getAmount(), freeSpace);

        if(giveAmount <= 0)
            return;

        /*
         * I am not using ItemUtil#addItem so it won't drop the leftovers
         * (If it will, the leftovers will get stacked again - infinite loop)
         */

        if (itemStack.getMaxStackSize() != 64 &&
                (plugin.getSettings().itemsFixStackEnabled || itemStack.getType().name().contains("SHULKER_BOX"))) {
            int amountOfStacks = giveAmount / itemStack.getMaxStackSize();
            int leftOvers = giveAmount % itemStack.getMaxStackSize();

            itemStack.setAmount(itemStack.getMaxStackSize());

            for(int i = 0; i < amountOfStacks; i++)
                giveItem(inventory, itemStack);

            if(leftOvers > 0) {
                itemStack.setAmount(leftOvers);
                giveItem(inventory, itemStack);
            }
        }
        else {
            itemStack.setAmount(giveAmount);
            giveItem(inventory, itemStack);
        }

        setStackAmount(startAmount - giveAmount, true);
    }

    private void giveItem(Inventory inventory, ItemStack itemStack){
        if(!itemStack.getType().name().contains("BUCKET") || !ItemUtils.stackBucket(itemStack, inventory))
            inventory.addItem(itemStack);
    }

    public static StackedItem of(Entity entity){
        if(entity instanceof Item)
            return of((Item) entity);
        throw new IllegalArgumentException("Only items can be applied to StackedItem object");
    }

    public static StackedItem of(Item item){
        return plugin.getSystemManager().getStackedItem(item);
    }

}
