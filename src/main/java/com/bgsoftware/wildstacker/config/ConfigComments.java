package com.bgsoftware.wildstacker.config;

@SuppressWarnings("unused")
public final class ConfigComments {

    @Comment("###############################################")
    @Comment("##                                           ##")
    @Comment("##         WildStacker Configuration         ##")
    @Comment("##            Developed by Ome_R             ##")
    @Comment("##                                           ##")
    @Comment("###############################################")
    @Comment("")
    public static String HEADER = "";

    @Comment("How much time should be passed between saves? (in ticks)")
    @Comment("Set 0 to disable (not recommended. saving is done async, and will not lag your server)")
    public static String SAVE_INTERVAL = "save-interval";

    @Comment("")
    @Comment("How should the item that is given to players by the give command be called?")
    @Comment("{0} represents stack size")
    @Comment("{1} represents entity/block type")
    @Comment("{2} represents item type (Egg / Spawner / Barrel)")
    public static String GIVE_ITEM_NAME = "give-item-name";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked items.")
    public static String ITEMS = "items";

    @Comment("Should items get stacked on the server?")
    public static String ITEMS_ENABLED = "items.enabled";

    @Comment("")
    @Comment("How many blocks from the item should be for checking for other items to stack into?")
    public static String ITEMS_MERGE_RADIUS = "items.merge-radius";

    @Comment("")
    @Comment("Custom display-name for the items on ground.")
    @Comment("If you don't want a display-name, use \"custom-name: ''\"")
    @Comment("{0} represents stack amount")
    @Comment("{1} represents display name")
    @Comment("{2} represents display name in upper case")
    public static String ITEMS_CUSTOM_NAME = "items.custom-name";

    @Comment("")
    @Comment("Blacklisted items are items that won't get stacked.")
    @Comment("Material list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")
    @Comment("If you wish to enable stacking of all items, use \"blacklist: []\"")
    public static String ITEMS_BLACKLIST = "items.blacklist";

    @Comment("")
    @Comment("Set a maximum stack for specific items.")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all items to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String ITEMS_LIMITS = "items.limits";

    @Comment("")
    @Comment("A list of worlds items won't get stacked inside them (case-sensitive)")
    public static String ITEMS_DISABLED_WORLDS = "items.disabled-worlds";

    @Comment("")
    @Comment("When enabled, all items will have a custom name (even if not stacked)")
    public static String ITEMS_UNSTACKED_CUSTOM_NAME = "items.unstacked-custom-name";

    @Comment("")
    @Comment("When fix-stack is disabled, items with a max-stack of 1 will be added to inventories")
    @Comment("with a max-stack size of 64. If a player picks up 80 picks, he will get 64 + 16, instead")
    @Comment("of 80 different items.")
    public static String ITEMS_FIX_STACK = "items.fix-stack";

    @Comment("")
    @Comment("When item-display is enabled, the item's name will be displayed instead of it's type")
    @Comment("This will take place on all items, and can only be overridden by custom-display section.")
    public static String ITEMS_ITEM_DISPLAY = "items.item-display";

    @Comment("")
    @Comment("When buckets stacker is enabled, water & lava buckets will be stacked in your inventory")
    @Comment("with a max-stack size of 16.")
    public static String BUCKETS_STACKER = "items.buckets-stacker";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked entities.")
    public static String ENTITIES = "entities";

    @Comment("Should entities get stacked on the server?")
    public static String ENTITIES_ENABLED = "entities.enabled";

    @Comment("")
    @Comment("How many blocks from the entity should be for checking for other entities to stack into?")
    public static String ENTITIES_MERGE_RADIUS = "entities.merge-radius";

    @Comment("")
    @Comment("Custom display-name for the entities.")
    @Comment("If you don't want a display-name, use \"custom-name: ''\"")
    @Comment("{0} represents stack amount")
    @Comment("{1} represents entity type")
    @Comment("{2} represents entity type in upper case")
    public static String ENTITIES_CUSTOM_NAME = "entities.custom-name";

    @Comment("")
    @Comment("Blacklisted entities are entities that won't get stacked.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("If you wish to enable stacking of all entities, use \"blacklist: []\"")
    public static String ENTITIES_BLACKLIST = "entities.blacklist";

    @Comment("")
    @Comment("Set a maximum stack for specific entities.")
    @Comment("Make sure you follow the \"ENTITY-TYPE\" format.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all entities to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String ENTITIES_LIMITS = "entities.limits";

    @Comment("")
    @Comment("A list of worlds entities won't get stacked inside them (case-sensitive)")
    public static String ENTITIES_DISABLED_WORLDS = "entities.disabled-worlds";

    @Comment("")
    @Comment("Blacklisted spawn reasons are spawn reasons that entities that were spawned with these reasons won't get stacked.")
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("If you wish to enable stacking of all entities that are spawned, use \"spawn-blacklist: []\"")
    public static String ENTITIES_SPAWN_BLACKLIST = "entities.spawn-blacklist";

    @Comment("")
    @Comment("Blacklisted names is a list of names that when an entity has this name, it won't get stacked.")
    @Comment("Color codes are supported, as well as regex.")
    @Comment("If you wish to enable stacking of all entities based on their name, use \"name-blacklist: []\"")
    public static String ENTITIES_NAME_BLACKLIST = "entities.name-blacklist";

    @Comment("")
    @Comment("How much time should be passed between auto-stacking? (in ticks)")
    @Comment("Use it on your own risk. Every run, the plugin will go over *all* the entities")
    @Comment("on your server and will try to stack *each* one of them. It may cause lag with incorrect value.")
    @Comment("I recommend setting it to at least 10 seconds (200 ticks)")
    @Comment("If you wish to disable the auto-stacking task, set the stack-interval to 0.")
    public static String ENTITIES_STACK_INTERVAL = "entities.stack-interval";

    @Comment("")
    @Comment("Here you can configurable all features related to the kill-all task.")
    @Comment("Every run, the plugin will remove all the stacked entities from your server.")
    @Comment("No drops will be drops, and it won't lag your server at all.")
    public static String ENTITIES_KILL_ALL = "entities.kill-all";

    @Comment("How much time should be passed between auto-killing? (in ticks)")
    @Comment("If you wish to disable the auto-killing task, set the interval to 0.")
    public static String ENTITIES_KILL_ALL_INTERVAL = "entities.kill-all.interval";

    @Comment("")
    @Comment("When enabled, the plugin will remove all stacked-entities when clearlagg removes items & entities.")
    @Comment("This feature will work if the interval is set to 0 - these are two different features!")
    public static String ENTITIES_KILL_ALL_CLEAR_LAGG = "entities.kill-all.clear-lagg";

    @Comment("")
    @Comment("A list of all checks that the plugin does before trying to stack two entities together.")
    public static String ENTITIES_STACK_CHECKS = "entities.stack-checks";

    @Comment("")
    @Comment("A list of all actions that the plugin check before unstacking an entity stack.")
    public static String ENTITIES_STACK_SPLIT = "entities.stack-split";

    @Comment("")
    @Comment("Linked-entities are entities that are linked to one spawner or more.")
    @Comment("A spawner that has an entity linked to, will try to stack it's entities first to the linked one.")
    @Comment("Linked entities feature won't work if entities-stacking is disabled.")
    public static String ENTITIES_LINKED_ENTITIES = "entities.linked-entities";

    @Comment("Should entities will be linked to spawners?")
    public static String ENTITIES_LINKED_ENTITIES_ENABLED = "entities.linked-entities.enabled";

    @Comment("")
    @Comment("The maximum distance that the linked entity can be from the spawner.")
    @Comment("If the entity is too far, it will get unlinked automatically from the spawner.")
    public static String ENTITIES_LINKED_ENTITIES_MAX_DISTANCE = "entities.linked-entities.max-distance";

    @Comment("")
    @Comment("Instant-kill will kill the entire stack instead of unstack it by one.")
    @Comment("When an entire stack dies, their drops are getting multiplied.")
    @Comment("DamageCause list:  https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html")
    @Comment("If you don't want instant-kill, use \"instant-kill: []\"")
    public static String ENTITIES_INSTANT_KILL = "entities.instant-kill";

    @Comment("")
    @Comment("Nerfed entities are entities that cannot attack / target other entities and players.")
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("If you don't want nerfed entities, use \"nerfed-spawning: []\"")
    public static String ENTITIES_NERFED_SPAWNING = "entities.nerfed-spawning";

    @Comment("")
    @Comment("Stack-down is a feature that will force entities to stack to other entities that are below their y level.")
    @Comment("This feature is great for flying entities, like blazes and ghasts.")
    public static String ENTITIES_STACK_DOWN = "entities.stack-down";

    @Comment("Should the stack-down feature will be enabled on the server?")
    public static String ENTITIES_STACK_DOWN_ENABLED = "entities.stack-down.enabled";

    @Comment("A list of entities that will be forced to only stack down.")
    public static String ENTITIES_STACK_DOWN_TYPES = "entities.stack-down.stack-down-types";

    @Comment("")
    @Comment("If a stacked entity dies from fire, should the fire continue to the next entity?")
    public static String ENTITIES_KEEP_FIRE = "entities.keep-fire";

    @Comment("")
    @Comment("If mythic mobs is enabled, will it's entities get stacked?")
    public static String ENTITIES_MYTHIC_MOBS_STACK = "entities.mythic-mobs-stack";

    @Comment("")
    @Comment("When this feature is enabled, blazes will always drop blazes and not only when they are killed by players.")
    public static String ENTITIES_BLAZES_ALWAYS_DROP = "entities.blazes-always-drop";

    @Comment("")
    @Comment("When enabled, the stack will keep the lowest health between the two entities that are stacked.")
    public static String ENTITIES_KEEP_LOWEST_HEALTH = "entities.keep-lowest-health";

    @Comment("")
    @Comment("When enabled, parents of the entity will get stacked together after breeding.")
    public static String ENTITIES_STACK_AFTER_BREED = "entities.stack-after-breed";

    @Comment("")
    @Comment("When enabled, entities' names will be shown only if player looks exactly towards them.")
    public static String ENTITIES_HIDE_NAMES = "entities.hide-names";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked spawners.")
    public static String SPAWNERS = "spawners";

    @Comment("Should spawners get stacked on the server?")
    public static String SPAWNERS_ENABLED = "spawners.enabled";

    @Comment("")
    @Comment("How many blocks from the spawner should be for checking for other spawners to stack into?")
    public static String SPAWNERS_MERGE_RADIUS = "spawners.merge-radius";

    @Comment("")
    @Comment("Custom hologram for the spawners.")
    @Comment("Holograms will be displayed only if one of the following plugins is enabled: HolographicDisplay, Holograms, Arconix")
    @Comment("If you don't want a hologram, use \"custom-name: ''\"")
    @Comment("{0} represents stack amount")
    @Comment("{1} represents entity type")
    @Comment("{2} represents entity type in upper case")
    public static String SPAWNERS_CUSTOM_NAME = "spawners.custom-name";

    @Comment("")
    @Comment("Blacklisted spawners are spawners that won't get stacked.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("If you wish to enable stacking of all spawners, use \"blacklist: []\"")
    public static String SPAWNERS_BLACKLIST = "spawners.blacklist";

    @Comment("")
    @Comment("Set a maximum stack for specific spawners.")
    @Comment("Make sure you follow the \"ENTITY-TYPE\" format.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all spawners to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String SPAWNERS_LIMITS = "spawners.limits";

    @Comment("")
    @Comment("A list of worlds spawners won't get stacked inside them (case-sensitive)")
    public static String SPAWNERS_DISABLED_WORLDS = "spawners.disabled-worlds";

    @Comment("")
    @Comment("When enabled, the plugin will try to find a spawner in the whole chunk instead")
    @Comment("of only in the provided radius. merge-radius will be overridden, and will be used")
    @Comment("as a y-level range only.")
    public static String SPAWNERS_CHUNK_MERGE = "spawners.chunk-merge";

    @Comment("")
    @Comment("Should explosions break the entire stack, or just reducing it by one?")
    public static String SPAWNERS_EXPLOSIONS_BREAK_STACK = "spawners.explosions-break-stack";

    @Comment("")
    @Comment("Chance of spawners to be dropped after an explosion.")
    public static String SPAWNERS_EXPLOSIONS_BREAK_CHANCE = "spawners.explosions-break-chance";

    @Comment("")
    @Comment("Should spawners get dropped without silktouch?")
    @Comment("If enabled, only players with wildstacker.nosilkdrop will be able to get the spawners.")
    @Comment("This feature will not work with other spawner-providers, such as SilkSpawners")
    public static String SPAWNERS_DROP_WITHOUT_SILK = "spawners.drop-without-silk";

    @Comment("")
    @Comment("Here you can configurable all features related to silk-touch enchantment.")
    public static String SPAWNERS_SILK_SPAWNERS = "spawners.silk-spawners";

    @Comment("Should spawners get dropped when mining them using a silk-touch pickaxe?")
    @Comment("If another similar plugin to this feature is enabled, it will override this feature.")
    public static String SPAWNERS_SILK_SPAWNERS_ENABLED = "spawners.silk-spawners.enabled";

    @Comment("")
    @Comment("Custom name for the item.")
    @Comment("If you don't want a custom name, use \"custom-name: ''\"")
    @Comment("{0} represents stack amount")
    @Comment("{1} represents entity type")
    public static String SPAWNERS_SILK_SPAWNERS_CUSTOM_NAME = "spawners.silk-spawners.custom-name";

    @Comment("")
    @Comment("Should explosions will act like silk-touch and drop the spawner?")
    public static String SPAWNERS_SILK_SPAWNERS_EXPLOSIONS_DROP_SPAWNER = "spawners.silk-spawners.explosions-drop-spawner";

    @Comment("")
    @Comment("Should the spawner item go straight into the player's inventory instead of dropping on ground?")
    public static String SPAWNERS_SILK_SPAWNERS_DROP_TO_INVENTORY = "spawners.silk-spawners.drop-to-inventory";

    @Comment("")
    @Comment("Should sneaking while mining will break the entire stack instead of reducing it by one?")
    public static String SPAWNERS_SHIFT_GET_WHOLE_STACK = "spawners.shift-get-whole-stack";

    @Comment("")
    @Comment("Should one item will be dropped for a stacked spawner instead of multiple items?")
    @Comment("This feature will not work with other spawner-providers, such as SilkSpawners")
    public static String SPAWNERS_GET_STACKED_ITEM = "spawners.get-stacked-item";

    @Comment("")
    @Comment("When enabled, holograms will only be displayed when clicking on spawners.")
    @Comment("They will be displayed for 3 seconds.")
    @Comment("You must set a valid custom-name for it to work.")
    public static String SPAWNERS_FLOATING_NAMES = "spawners.floating-names";

    @Comment("")
    @Comment("Here you can configurable all features related to the break menu.")
    public static String SPAWNERS_BREAK_MENU = "spawners.break-menu";

    @Comment("When enabled and shift right-clicking a spawner, a \"break-menu\" will be opened.")
    @Comment("You can select there how many spawners you want to remove from the stack.")
    public static String SPAWNERS_BREAK_MENU_ENABLED = "spawners.break-menu.enabled";

    @Comment("")
    @Comment("How many rows should the gui have?")
    public static String SPAWNERS_BREAK_MENU_ROWS = "spawners.break-menu.rows";

    @Comment("")
    @Comment("The title of the gui.")
    public static String SPAWNERS_BREAK_MENU_TITLE = "spawners.break-menu.title";

    @Comment("")
    @Comment("Here you can list all fill items for the gui.")
    @Comment("If you don't want any, set the type to AIR.")
    public static String SPAWNERS_BREAK_MENU_FILL_ITEMS = "spawners.break-menu.fill-items";

    @Comment("")
    @Comment("Here you can list all break slots.")
    public static String SPAWNERS_BREAK_MENU_BREAK_SLOTS = "spawners.break-menu.break-slots";

    @Comment("")
    @Comment("When enabled, you must have the permission wildstacker.place.<entity> to place an entity.")
    @Comment("You can give a player the ability to place all spawners with wildstacker.place.*")
    public static String SPAWNERS_PLACEMENT_PERMISSION = "spawners.placement-permission";

    @Comment("")
    @Comment("When enabled and player is placing a spawner while sneaking, all of the spawners the player")
    @Comment("is holding will be placed instead of only 1.")
    public static String SPAWNERS_SHIFT_PLACE_STACK = "spawners.shift-place-stack";

    @Comment("")
    @Comment("Set a charge amount for breaking spawners.")
    public static String SPAWNERS_BREAK_CHARGE = "spawners.break-charge";

    @Comment("")
    @Comment("Set a charge amount for placing spawners.")
    public static String SPAWNERS_PLACE_CHARGE = "spawners.place-charge";

    @Comment("")
    @Comment("When enabled, players will be able to change spawners by clicking them with spawn eggs.")
    public static String SPAWNERS_CHANGE_USING_EGGS = "spawners.change-using-eggs";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked barrels (aka stacked blocks).")
    public static String BARRELS = "barrels";

    @Comment("Should blocks get stacked into barrels on the server?")
    public static String BARRELS_ENABLED = "barrels.enabled";

    @Comment("")
    @Comment("How many blocks from the barrel should be for checking for other blocks to stack into?")
    public static String BARRELS_MERGE_RADIUS = "barrels.merge-radius";

    @Comment("")
    @Comment("Custom hologram for the barrels.")
    @Comment("Holograms will be displayed only if one of the following plugins is enabled: HolographicDisplay, Holograms, Arconix")
    @Comment("If you don't want a hologram, use \"custom-name: ''\"")
    @Comment("{0} represents stack amount")
    @Comment("{1} represents barrel type")
    @Comment("{2} represents barrel type in upper case")
    public static String BARRELS_CUSTOM_NAME = "barrels.custom-name";

    @Comment("")
    @Comment("Whitelisted blocks are blocks that will get stacked.")
    @Comment("Material list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("If you wish to disable stacking of all blocks, use \"enabled: false\"")
    public static String BARRELS_WHITELIST = "barrels.whitelist";

    @Comment("")
    @Comment("Set a maximum stack for a specific barrel.")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE;DATA\" formats.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all barrels to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String BARRELS_LIMITS = "barrels.limits";

    @Comment("")
    @Comment("A list of worlds barrels won't get stacked inside them (case-sensitive)")
    public static String BARRELS_DISABLED_WORLD = "barrels.disabled-worlds";

    @Comment("")
    @Comment("When enabled, the plugin will try to find a block in the whole chunk instead")
    @Comment("of only in the provided radius. merge-radius will be overridden, and will be used")
    @Comment("as a y-level range only.")
    public static String BARRELS_CHUNK_MERGE = "barrels.chunk-merge";

    @Comment("")
    @Comment("Should explosions break the entire stack, or just reducing it by one?")
    public static String BARRELS_EXPLOSIONS_BREAK_STACK = "barrels.explosions-break-stack";

    @Comment("")
    @Comment("Here you can configurable all features related to toggle commands.")
    public static String BARRELS_TOGGLE_COMMAND = "barrels.toggle-command";

    @Comment("Should toggle-commands be enabled?")
    public static String BARRELS_TOGGLE_COMMAND_ENABLED = "barrels.toggle-command.enabled";

    @Comment("")
    @Comment("What's the toggle command will be?")
    public static String BARRELS_TOGGLE_COMMAND_COMMAND = "barrels.toggle-command.command";

    @Comment("")
    @Comment("When enabled and player is clicking a barrel while sneaking, an inventory will be opened, there")
    @Comment("he can put blocks to add to the barrel.")
    public static String BARRELS_PLACE_INVENTORY = "barrels.place-inventory";

}
