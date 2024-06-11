package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public final class WStackedItem extends WAsyncStackedObject<Item> implements StackedItem {

    private static final Pattern DISPLAY_NAME_PLACEHOLDER = Pattern.compile(Pattern.quote("{0}"));

    private final UUID cachedUUID;
    private final int cachedEntityId;
    private String mmoItemName = null;

    public WStackedItem(Item item) {
        this(item, item.getItemStack().getAmount());
    }

    public WStackedItem(Item item, int stackAmount) {
        super(item, stackAmount);
        cachedUUID = item.getUniqueId();
        cachedEntityId = item.getEntityId();
    }

    public static StackedItem of(Entity entity) {
        if (entity instanceof Item)
            return of((Item) entity);
        throw new IllegalArgumentException("Only items can be applied to StackedItem object");
    }

    public static StackedItem of(Item item) {
        if (!ItemUtils.isStackable(item))
            throw new IllegalArgumentException("The item " + item + " is not a stackable item.");

        return ofBypass(item);
    }

    public static StackedItem ofBypass(Item item) {
        return plugin.getSystemManager().getStackedItem(item);
    }

    /*
     * Item's methods
     */

    @Override
    public World getWorld() {
        return object.getWorld();
    }

    @Override
    public void onStackAmountChange(int newStackAmount) {
        super.onStackAmountChange(newStackAmount);
        if (newStackAmount > 0) {
            ItemStack itemStack = object.getItemStack().clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), newStackAmount));
            if (itemStack.getType() != Material.AIR && itemStack.getAmount() > 0)
                object.setItemStack(itemStack);
        }
    }

    @Override
    public Location getLocation() {
        return object.getLocation();
    }

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

    /*
     * StackedObject's methods
     */

    @Override
    public boolean isCached() {
        return plugin.getSettings().itemsStackingEnabled && super.isCached();
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);

        /* Items must be removed sync, otherwise they are not properly removed from chunks.
        Also, in 1.17, the remove() function must be called sync. */
        Scheduler.runTask(object, object::remove);

        EntityStorage.setMetadata(object, EntityFlag.REMOVED_ENTITY, true);
        Scheduler.runTask(() -> EntityStorage.clearMetadata(object), 100L);
    }

    @Override
    public void updateName() {
        if (!plugin.getSettings().itemsStackingEnabled || !ItemUtils.canPickup(object) || ServerVersion.isLessThan(ServerVersion.v1_8))
            return;

        ItemStack itemStack = getItemStack();

        boolean mmoItem = !plugin.getNMSAdapter().getTag(itemStack, "MMOITEMS_ITEM_TYPE", String.class, "NULL").equals("NULL");

        if (mmoItem && mmoItemName == null)
            mmoItemName = getCustomName();

        String customName = plugin.getSettings().itemsCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();
        boolean updateName = (mmoItem && mmoItemName != null) || plugin.getSettings().itemsUnstackedCustomName || amount > 1;

        if (updateName) {
            String cachedDisplayName = mmoItem && mmoItemName != null ? mmoItemName : ItemUtils.getFormattedType(itemStack);
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : cachedDisplayName;

            if (plugin.getSettings().itemsDisplayEnabled)
                cachedDisplayName = displayName;

            setCachedDisplayName(DISPLAY_NAME_PLACEHOLDER.matcher(cachedDisplayName).replaceAll(displayName));

            customName = plugin.getSettings().itemsNameBuilder.build(this);
        }

        String CUSTOM_NAME = customName;

        Scheduler.runTask(object, () -> {
            if (updateName) {
                setCustomName(CUSTOM_NAME);
            }
            setCustomNameVisible(updateName);
        });

        if (saveData)
            plugin.getSystemManager().markToBeSaved(this);
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

        if (isRemoved() || object.isDead())
            return StackCheckResult.ALREADY_DEAD;

        StackedItem targetItem = (StackedItem) stackedObject;

        if (!plugin.getSettings().itemsMaxPickupDelay && !ItemUtils.canPickup(targetItem.getItem()))
            return StackCheckResult.TARGET_PICKUP_DELAY_EXCEEDED;

        if (((WStackedItem) targetItem).isRemoved() || targetItem.getItem().isDead())
            return StackCheckResult.TARGET_ALREADY_DEAD;

        return StackCheckResult.SUCCESS;
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if (!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
            return StackResult.NOT_SIMILAR;

        StackedItem targetItem = (StackedItem) stackedObject;

        if (!EventsCaller.callItemStackEvent(targetItem, this))
            return StackResult.EVENT_CANCELLED;

        targetItem.increaseStackAmount(getStackAmount(), false);

        Scheduler.runTask(targetItem.getItem(), () -> {
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
        return stackedObject instanceof StackedItem && object.getItemStack()
                .isSimilar(((StackedItem) stackedObject).getItem().getItemStack());
    }

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if (!checkEnabled || plugin.getSettings().itemsParticlesEnabled) {
            Location location = getItem().getLocation();
            for (ParticleWrapper particleWrapper : plugin.getSettings().itemsParticles)
                particleWrapper.spawnParticle(location);
        }
    }

    @Override
    public Item getItem() {
        return object;
    }

    @Override
    public UUID getUniqueId() {
        return cachedUUID;
    }

    @Override
    public String getCustomName() {
        return plugin.getNMSEntities().getCustomName(object);
    }

    @Override
    public void setCustomName(String customName) {
        plugin.getNMSEntities().setCustomName(object, customName);
    }

    @Override
    public boolean isCustomNameVisible() {
        return plugin.getNMSEntities().isCustomNameVisible(object);
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        plugin.getNMSEntities().setCustomNameVisible(object, visible);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack is = object.getItemStack().clone();
        is.setAmount(Math.max(1, getStackAmount()));
        return is;
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            remove();
        else
            object.setItemStack(itemStack);
    }

    @Override
    public void giveItemStack(Inventory inventory) {
        if (isRemoved())
            return;

        ItemStack itemStack = getItemStack();

        /*
         * I am not using ItemUtil#addItem so it won't drop the leftovers
         * (If it will, the leftovers will get stacked again - infinite loop)
         */

        int originalStackAmount = getStackAmount();

        int maxStackAmount = itemStack.getMaxStackSize();
        boolean inventoryFull = false;

        if (maxStackAmount != 64 && !plugin.getSettings().itemsFixStackEnabled &&
                !itemStack.getType().name().contains("SHULKER_BOX"))
            maxStackAmount = 64;

        itemStack.setAmount(maxStackAmount);

        while (getStackAmount() >= maxStackAmount && !inventoryFull) {
            int amountLeft = giveItem(inventory, itemStack.clone());
            decreaseStackAmount(maxStackAmount - amountLeft, true);
            if (amountLeft > 0) {
                inventoryFull = true;
            }
        }

        // Inventory is still not full, but there is still more items to add.
        // However, there is less than a stack available to add.
        if (!inventoryFull) {
            int currentStackAmount = getStackAmount();
            itemStack.setAmount(currentStackAmount);
            int amountLeft = giveItem(inventory, itemStack.clone());
            decreaseStackAmount(currentStackAmount - amountLeft, true);
        }

        int finalStackAmount = getStackAmount();

        if (finalStackAmount <= 0)
            remove();

        int givenAmount = originalStackAmount - finalStackAmount;

        if (givenAmount > 0 && inventory instanceof PlayerInventory) {
            plugin.getProviders().notifyStackedItemListeners((Player) ((PlayerInventory) inventory).getHolder(),
                    object, givenAmount);
        }
    }

    @Override
    public int getId() {
        return cachedEntityId;
    }

    /*
     * StackedItem's methods
     */

    @Override
    public void runStackAsync(Consumer<Optional<Item>> result) {
        int range = getMergeRadius();

        if (range <= 0 || getStackLimit() <= 1) {
            if (result != null)
                result.accept(Optional.empty());
            return;
        }

        // Should be called sync due to collecting nearby entities
        if (!Bukkit.isPrimaryThread()) {
            Scheduler.runTask(() -> runStackAsync(result));
            return;
        }

        Location itemLocation = getItem().getLocation();
        Optional<StackedItem> itemOptional = EntitiesGetter.getNearbyEntities(itemLocation, range, ItemUtils::isStackable)
                .map(entity -> WStackedItem.ofBypass((Item) entity))
                .filter(stackedItem -> runStackCheck(stackedItem) == StackCheckResult.SUCCESS)
                .findFirst();

        if (itemOptional.isPresent()) {
            runStackAsync(itemOptional.get(), stackResult -> {
                if (stackResult == StackResult.SUCCESS) {
                    if (result != null)
                        result.accept(itemOptional.map(StackedItem::getItem));
                } else {
                    updateName();

                    if (result != null)
                        result.accept(Optional.empty());
                }
            });
        } else {
            updateName();

            if (result != null)
                result.accept(Optional.empty());
        }
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedItem ? getUniqueId().equals(((StackedItem) obj).getUniqueId()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedItem{uuid=%s,amount=%s,item=%s}", getUniqueId(), getStackAmount(), object.getItemStack());
    }

    public boolean isRemoved() {
        return EntityStorage.hasMetadata(object, EntityFlag.REMOVED_ENTITY);
    }

    private int giveItem(Inventory inventory, ItemStack itemStack) {
        Map<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);

        if (itemStack.getType().name().contains("BUCKET"))
            ItemUtils.stackBucket(itemStack, inventory);
        if (itemStack.getType().name().contains("STEW") || itemStack.getType().name().contains("SOUP"))
            ItemUtils.stackStew(itemStack, inventory);

        return additionalItems.values().stream().findFirst().orElse(new ItemStack(Material.STONE, 0)).getAmount();
    }

}
