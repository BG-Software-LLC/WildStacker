package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public final class WStackedItem extends WAsyncStackedObject<Item> implements StackedItem {

    private String mmoItemName = null;
    private boolean saveItem = true;

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
    public World getWorld() {
        return object.getWorld();
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        super.setStackAmount(stackAmount, updateName);
        if(stackAmount > 0) {
            ItemStack itemStack = object.getItemStack().clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), stackAmount));
            if(itemStack.getType() != Material.AIR && itemStack.getAmount() > 0)
                object.setItemStack(itemStack);
            if(saveItem)
                plugin.getSystemManager().markToBeSaved(this);
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
    public String getCustomName() {
        return plugin.getNMSAdapter().getCustomName(object);
    }

    @Override
    public void setCustomName(String customName){
        plugin.getNMSAdapter().setCustomName(object, customName);
    }

    @Override
    public boolean isCustomNameVisible() {
        return plugin.getNMSAdapter().isCustomNameVisible(object);
    }

    @Override
    public void setCustomNameVisible(boolean visible){
        plugin.getNMSAdapter().setCustomNameVisible(object, visible);
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
        int limit = plugin.getSettings().itemsLimits.getOrDefault(object.getItemStack().getType(), Integer.MAX_VALUE);
        return limit < 1 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public int getMergeRadius() {
        int radius = plugin.getSettings().itemsMergeRadius.getOrDefault(object.getItemStack().getType(), 0);
        return radius < 1 ? 0 : radius;
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedItems.contains(object.getItemStack().getType());
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedItems.size() == 0 ||
                plugin.getSettings().whitelistedItems.contains(object.getItemStack().getType());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().itemsDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public boolean isCached() {
        return plugin.getSettings().itemsStackingEnabled && super.isCached();
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);
        object.remove();
    }

    @Override
    public void updateName() {
        if(!plugin.getSettings().itemsStackingEnabled || !ItemUtils.canPickup(object) || ServerVersion.isLessThan(ServerVersion.v1_8))
            return;

        ItemStack itemStack = getItemStack();

        boolean mmoItem = !plugin.getNMSAdapter().getTag(itemStack, "MMOITEMS_ITEM_TYPE", String.class, "NULL").equals("NULL");

        if(mmoItem && mmoItemName == null)
            mmoItemName = getCustomName();

        String customName = plugin.getSettings().itemsCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();
        boolean updateName = (mmoItem && mmoItemName != null) || plugin.getSettings().itemsUnstackedCustomName || amount > 1;

        if (updateName) {
            String itemType = mmoItem && mmoItemName != null ? mmoItemName : ItemUtils.getFormattedType(itemStack);
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemType;

            if(plugin.getSettings().itemsDisplayEnabled)
                itemType = displayName;

            itemType = itemType.replace("{0}", displayName);

            customName = customName
                    .replace("{0}", Integer.toString(amount))
                    .replace("{1}", itemType)
                    .replace("{2}", itemType.toUpperCase());
        }

        String CUSTOM_NAME = customName;

        Executor.sync(() -> {
            if(updateName) {
                setCustomName(CUSTOM_NAME);
                if(saveItem)
                    plugin.getSystemManager().markToBeSaved(this);
            }
            setCustomNameVisible(updateName);
        });
    }

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        if (!plugin.getSettings().itemsStackingEnabled)
            return StackCheckResult.NOT_ENABLED;

        StackCheckResult superResult = super.runStackCheck(stackedObject);

        if (superResult != StackCheckResult.SUCCESS)
            return superResult;

        if (!plugin.getSettings().itemsMaxPickupDelay && !ItemUtils.canPickup(object))
            return StackCheckResult.PICKUP_DELAY_EXCEEDED;

        if (object.isDead())
            return StackCheckResult.ALREADY_DEAD;

        StackedItem targetItem = (StackedItem) stackedObject;

        if (!plugin.getSettings().itemsMaxPickupDelay && !ItemUtils.canPickup(targetItem.getItem()))
            return StackCheckResult.TARGET_PICKUP_DELAY_EXCEEDED;

        if (targetItem.getItem().isDead())
            return StackCheckResult.TARGET_ALREADY_DEAD;

//            if (getItem().getLocation().getBlock().getType() == Materials.NETHER_PORTAL.toBukkitType())
//                return StackCheckResult.INSIDE_PORTAL;
//
//            if (targetItem.getItem().getLocation().getBlock().getType() == Materials.NETHER_PORTAL.toBukkitType())
//                return StackCheckResult.TARGET_INSIDE_PORTAL;

        return StackCheckResult.SUCCESS;
    }

    @Override
    public void runStackAsync(Consumer<Optional<Item>> result) {
        int range = getMergeRadius();

        if(range <= 0 || getStackLimit() <= 1){
            if (result != null)
                result.accept(Optional.empty());
            return;
        }

        EntityUtils.getNearbyEntities(object.getLocation(), range, item -> true).whenComplete((nearbyEntities, ex) ->
                StackService.execute(this, () -> {
                    Location itemLocation = getItem().getLocation();

                    Optional<StackedItem> itemOptional = GeneralUtils.getClosest(itemLocation,
                            nearbyEntities.stream()
                                    .filter(ItemUtils::isStackable)
                                    .map(entity -> WStackedItem.ofBypass((Item) entity))
                                    .filter(stackedItem -> runStackCheck(stackedItem) == StackCheckResult.SUCCESS)
                    );

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
            }));
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if(!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
            return StackResult.NOT_SIMILAR;

        StackedItem targetItem = (StackedItem) stackedObject;

        if(!EventsCaller.callItemStackEvent(targetItem, this))
            return StackResult.EVENT_CANCELLED;

        targetItem.setStackAmount(this.getStackAmount() + targetItem.getStackAmount(), false);

        Executor.sync(() -> {
            if (targetItem.getItem().isValid())
                targetItem.updateName();
        }, 2L);

        this.remove();

        spawnStackParticle(true);

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount, Entity entity) {
        throw new UnsupportedOperationException("Cannot unstack stacked items. Use giveItemStack() method.");
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedItem && getItemStack().isSimilar(((StackedItem) stackedObject).getItemStack());
    }

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if(!checkEnabled || plugin.getSettings().itemsParticlesEnabled) {
            Location location = getItem().getLocation();
            for(ParticleWrapper particleWrapper : plugin.getSettings().itemsParticles)
                particleWrapper.spawnParticle(location);
        }
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
        synchronized (this) {
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

            if (maxStackAmount != 64 && !plugin.getSettings().itemsFixStackEnabled &&
                    !itemStack.getType().name().contains("SHULKER_BOX"))
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

            if(amountLeft <= 0){
                remove();
            }
            else {
                setStackAmount(amountLeft, true);
            }
        }
    }

    public void setSaveItem(boolean saveItem){
        this.saveItem = saveItem && isCached();
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
        if(!ItemUtils.isStackable(item))
            throw new IllegalArgumentException("The item " + item + " is not a stackable item.");

        return ofBypass(item);
    }

    public static StackedItem ofBypass(Item item){
        return plugin.getSystemManager().getStackedItem(item);
    }

}
