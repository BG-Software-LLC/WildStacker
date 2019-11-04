package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public class WStackedItem extends WStackedObject<Item> implements StackedItem {

    public static final Object stackingMutex = new Object();

    private final static int MAX_PICKUP_DELAY = 32767;

    public WStackedItem(Item item){
        this(item, item.getItemStack().getAmount());
    }

    public WStackedItem(Item item, int stackAmount){
        super(item, stackAmount);
    }

    @Override
    public Location getLocation() {
        return object.getLocation();
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
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        synchronized (stackingMutex) {
            if (!plugin.getSettings().itemsStackingEnabled)
                return StackCheckResult.NOT_ENABLED;

            StackCheckResult superResult = super.runStackCheck(stackedObject);

            if (superResult != StackCheckResult.SUCCESS)
                return superResult;

            if (!plugin.getSettings().itemsMaxPickupDelay && getItem().getPickupDelay() >= MAX_PICKUP_DELAY)
                return StackCheckResult.PICKUP_DELAY_EXCEEDED;

            if (object.isDead())
                return StackCheckResult.ALREADY_DEAD;

            StackedItem targetItem = (StackedItem) stackedObject;

            if (!plugin.getSettings().itemsMaxPickupDelay && targetItem.getItem().getPickupDelay() >= MAX_PICKUP_DELAY)
                return StackCheckResult.TARGET_PICKUP_DELAY_EXCEEDED;

            if (targetItem.getItem().isDead())
                return StackCheckResult.TARGET_ALREADY_DEAD;

            if (getItem().getLocation().getBlock().getType() == Materials.NETHER_PORTAL.toBukkitType())
                return StackCheckResult.INSIDE_PORTAL;

            if (targetItem.getItem().getLocation().getBlock().getType() == Materials.NETHER_PORTAL.toBukkitType())
                return StackCheckResult.TARGET_INSIDE_PORTAL;

            return StackCheckResult.SUCCESS;
        }
    }

    @Override
    public void runStackAsync(Consumer<Optional<Item>> result) {
        int range = plugin.getSettings().itemsCheckRange;

        List<Entity> nearbyEntities = plugin.getNMSAdapter().getNearbyEntities(object, range, entity -> entity instanceof Item);

        StackService.execute(() -> {
            synchronized (stackingMutex) {
                Location itemLocation = getItem().getLocation();

                Optional<StackedItem> itemOptional = nearbyEntities.stream().map(WStackedItem::of)
                        .filter(stackedItem -> runStackCheck(stackedItem) == StackCheckResult.SUCCESS)
                        .min(Comparator.comparingDouble(o -> o.getItem().getLocation().distanceSquared(itemLocation)));

                if (itemOptional.isPresent()) {
                    StackedItem targetItem = itemOptional.get();

                    StackResult stackResult = runStack(targetItem);

                    if (stackResult == StackResult.SUCCESS) {
                        if (result != null)
                            result.accept(itemOptional.map(StackedItem::getItem));
                        return;
                    }
                }

                updateName();

                if (result != null)
                    result.accept(Optional.empty());
            }
        });
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if(!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
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

        int giveAmount = getStackAmount();

        if (giveAmount <= 0) {
            remove();
            return;
        }

        /*
         * I am not using ItemUtil#addItem so it won't drop the leftovers
         * (If it will, the leftovers will get stacked again - infinite loop)
         */

        int amountLeft = 0;
        int maxStackAmount = itemStack.getMaxStackSize();

        if (maxStackAmount != 64 &&
                (plugin.getSettings().itemsFixStackEnabled || itemStack.getType().name().contains("SHULKER_BOX")))
            maxStackAmount = 64;

        int amountOfStacks = giveAmount / maxStackAmount;
        int leftOvers = giveAmount % maxStackAmount;
        boolean inventoryFull = false;

        itemStack.setAmount(maxStackAmount);

        for (int i = 0; i < amountOfStacks; i++) {
            if (inventoryFull) {
                amountLeft += maxStackAmount;
            } else {
                int _amountLeft = giveItem(inventory, itemStack.clone());
                if (_amountLeft > 0) {
                    inventoryFull = true;
                    amountLeft += _amountLeft;
                }
            }
        }

        if (leftOvers > 0) {
            itemStack.setAmount(leftOvers);
            amountLeft += giveItem(inventory, itemStack.clone());
        }

        if (amountLeft <= 0) {
            remove();
        } else {
            setStackAmount(amountLeft, true);
        }
    }

    private int giveItem(Inventory inventory, ItemStack itemStack){
        Map<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);

        if(itemStack.getType().name().contains("BUCKET"))
            ItemUtils.stackBucket(itemStack, inventory);
        if(itemStack.getType().name().contains("STEW") || itemStack.getType().name().contains("SOUP"))
            ItemUtils.stackStew(itemStack, inventory);

        return additionalItems.values().stream().findFirst().orElse(new ItemStack(Material.STONE, 0)).getAmount();
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
