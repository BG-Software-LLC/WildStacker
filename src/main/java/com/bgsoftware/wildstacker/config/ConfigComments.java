package com.bgsoftware.wildstacker.config;

@SuppressWarnings("unused")
public final class ConfigComments {

    @Comment("###############################################")
    @Comment("##                                           ##")
    @Comment("##         WildStacker Configuration         ##")
    @Comment("##            Developed by Ome_R             ##")
    @Comment("##                                           ##")
    @Comment("###############################################")
    public static String HEADER = "";

    @Comment("")
    @Comment("How should the item that is given to players by the give command be called?")
    @Comment("{0} represents stack size")
    @Comment("{1} represents entity/block type")
    @Comment("{2} represents item type (Egg / Spawner / Barrel)")
    public static String GIVE_ITEM_NAME = "give-item-name";

    @Comment("")
    @Comment("The inspect tool of the plugin.")
    @Comment("When clicking an item, entity, barrel or spawner, all the information")
    @Comment("about the object will be displayed to the player.")
    public static String INSPECT_TOOL = "inspect-tool";

    @Comment("")
    @Comment("Settings related to database.")
    public static String DATABASE = "database";

    @Comment("Should data of worlds that no longer exist be deleted?")
    public static String DATABASE_INVALID_WORLDS = "database.delete-invalid-worlds";

    @Comment("Should data of blocks that no longer exist be deleted?")
    public static String DATABASE_INVALID_BLOCKS = "database.delete-invalid-blocks";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked items.")
    public static String ITEMS = "items";

    @Comment("Should items get stacked on the server?")
    public static String ITEMS_ENABLED = "items.enabled";

    @Comment("")
    @Comment("How many blocks from the item should be checked for other items to stack into?")
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
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("If you wish to disable blacklisted items, use \"blacklist: []\"")
    public static String ITEMS_BLACKLIST = "items.blacklist";

    @Comment("")
    @Comment("Whitelisted items are items that will get stacked.")
    @Comment("Material list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("If you wish to disable whitelisted items, use \"whitelist: []\"")
    public static String ITEMS_WHITELIST = "items.whitelist";

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
    @Comment("Set a maximum amount of item objects in a chunk.")
    @Comment("If you want to disable the feature, set it to 0.")
    public static String ITEMS_CHUNK_LIMIT = "items.chunk-limit";

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

    @Comment("Should buckets get stacked on the server?")
    public static String BUCKETS_STACKER_ENABLED = "items.buckets-stacker.enabled";

    @Comment("")
    @Comment("A list of blacklisted bucket names.")
    public static String BUCKETS_STACKER_NAME_BLACKLIST = "items.buckets-stacker.name-blacklist";

    @Comment("")
    @Comment("The new max-stack size for buckets. Must be a number between 1 and 64.")
    public static String BUCKETS_STACKER_MAX_STACK = "items.buckets-stacker.max-stack";

    @Comment("")
    @Comment("Should items get removed when the kill all task is performed?")
    @Comment("If you want to configure the task, check the entities section.")
    public static String ITEMS_KILL_ALL = "items.kill-all";

    @Comment("")
    @Comment("Should players be able to disable item names for themselves?")
    @Comment("In order to work, ProtocolLib should be installed.")
    public static String ITEMS_NAMES_TOGGLE = "items.names-toggle";

    @Comment("Can item names be toggled?")
    public static String ITEMS_NAMES_TOGGLE_ENABLED = "items.names-toggle.enabled";

    @Comment("")
    @Comment("What the toggle command will be?")
    public static String ITEMS_NAMES_TOGGLE_COMMAND = "items.names-toggle.command";

    @Comment("")
    @Comment("When enabled, a pickup sound will be played to the player.")
    public static String ITEMS_PICKUP_SOUND = "items.pickup-sound";

    @Comment("Should pickup-sound be enabled for stacked items?")
    public static String ITEMS_PICKUP_SOUND_ENABLED = "items.pickup-sound.enabled";

    @Comment("")
    @Comment("The volume of the sound.")
    public static String ITEMS_PICKUP_SOUND_VOLUME = "items.pickup-sound.volume";

    @Comment("")
    @Comment("The pitch of the volume.")
    public static String ITEMS_PICKUP_SOUND_PITCH = "items.pickup-sound.pitch";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked entities.")
    public static String ENTITIES = "entities";

    @Comment("Should entities get stacked on the server?")
    public static String ENTITIES_ENABLED = "entities.enabled";

    @Comment("")
    @Comment("How many blocks from the entity should be checked for other entities to stack into?")
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
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("You can combine both filters using \"ENTITY_TYPE:SPAWN_REASON\"")
    @Comment("If you wish to disable blacklisted entities, use \"blacklist: []\"")
    public static String ENTITIES_BLACKLIST = "entities.blacklist";

    @Comment("")
    @Comment("Whitelisted entities are entities that will get stacked.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("You can combine both filters using \"ENTITY_TYPE:SPAWN_REASON\"")
    @Comment("If you wish to disable whitelisted entities, use \"whitelist: []\"")
    public static String ENTITIES_WHITELIST = "entities.whitelist";

    @Comment("")
    @Comment("Set a maximum stack for specific entities.")
    @Comment("Make sure you follow the \"ENTITY-TYPE\" format.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all entities to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String ENTITIES_LIMITS = "entities.limits";

    @Comment("")
    @Comment("Set a minimum stack for specific entities.")
    @Comment("Make sure you follow the \"ENTITY-TYPE\" format.")
    @Comment("You can use 'all' as a global limit (all: 20 will set all entities to be limited to 20 per stack)")
    @Comment("If you don't want any limits, you can set a random type.")
    public static String ENTITIES_MINIMUM_LIMITS = "entities.minimum-limits";

    @Comment("")
    @Comment("A list of worlds entities won't get stacked inside them (case-sensitive)")
    public static String ENTITIES_DISABLED_WORLDS = "entities.disabled-worlds";

    @Comment("")
    @Comment("Set a maximum amount of entity objects in a chunk.")
    @Comment("If you want to disable the feature, set it to 0.")
    public static String ENTITIES_CHUNK_LIMIT = "entities.chunk-limit";

    @Comment("")
    @Comment("A list of WorldGuard regions entities won't get stacked inside them (case-sensitive)")
    public static String ENTITIES_DISABLED_REGIONS = "entities.disabled-regions";

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

    @Comment("Should entities be linked to spawners?")
    public static String ENTITIES_LINKED_ENTITIES_ENABLED = "entities.linked-entities.enabled";

    @Comment("")
    @Comment("The maximum distance that the linked entity can be from the spawner.")
    @Comment("If the entity is too far, it will get unlinked automatically from the spawner.")
    public static String ENTITIES_LINKED_ENTITIES_MAX_DISTANCE = "entities.linked-entities.max-distance";

    @Comment("")
    @Comment("Instant-kill will kill the entire stack instead of unstack it by one.")
    @Comment("When an entire stack dies, their drops are getting multiplied.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("DamageCause list:  https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html")
    @Comment("You can combine both filters using \"ENTITY_TYPE:DAMAGE_CAUSE\" or \"ENTITY_TYPE:SPAWN_REASON\"")
    @Comment("If you don't want instant-kill, use \"instant-kill: []\"")
    public static String ENTITIES_INSTANT_KILL = "entities.instant-kill";

    @Comment("")
    @Comment("Nerfed entities are entities that cannot attack / target other entities and players.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("You can combine filters using \"ENTITY_TYPE:SPAWN_REASON\"")
    @Comment("If you don't want nerfed entities, use \"nerfed-spawning: []\"")
    public static String ENTITIES_NERFED_SPAWNING = "entities.nerfed-spawning";

    @Comment("")
    @Comment("A list of worlds that entities can get nerfed in.")
    public static String ENTITIES_NERFED_WORLDS = "entities.nerfed-worlds";

    @Comment("")
    @Comment("All settings related to ai of mobs.")
    public static String ENTITIES_NO_AI = "entities.no-ai";

    @Comment("A list of entities that won't have AI.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("SpawnReason list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html")
    @Comment("You can combine filters using \"ENTITY_TYPE:SPAWN_REASON\"")
    @Comment("If you want AI to all entities, use \"whitelist: []\"")
    public static String ENTITIES_NO_AI_WHITELIST = "entities.no-ai.whitelist";

    @Comment("")
    @Comment("A list of worlds that entities can get nerfed in.")
    public static String ENTITIES_NO_AI_WORLDS = "entities.no-ai.worlds";

    @Comment("")
    @Comment("Stack-down is a feature that will force entities to stack to other entities that are below their y level.")
    @Comment("This feature is great for flying entities, like blazes and ghasts.")
    public static String ENTITIES_STACK_DOWN = "entities.stack-down";

    @Comment("Should the stack-down feature be enabled on the server?")
    public static String ENTITIES_STACK_DOWN_ENABLED = "entities.stack-down.enabled";

    @Comment("A list of entities that will be forced to only stack down.")
    @Comment("You can combine filters using \"ENTITY_TYPE:SPAWN_REASON\"")
    public static String ENTITIES_STACK_DOWN_TYPES = "entities.stack-down.stack-down-types";

    @Comment("")
    @Comment("If a stacked entity dies from fire, should the fire continue to the next entity?")
    public static String ENTITIES_KEEP_FIRE = "entities.keep-fire";

    @Comment("")
    @Comment("If enabled, the placeholder '{}' will be replaced with the stack amount for mythic mobs.")
    public static String ENTITIES_MYTHIC_MOBS_CUSTOM_NAME = "entities.mythic-mobs-custom-name";

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
    @Comment("Should players be able to disable entity names for themselves?")
    @Comment("In order to work, ProtocolLib should be installed.")
    public static String ENTITIES_NAMES_TOGGLE = "entities.names-toggle";

    @Comment("Can entity names be toggled?")
    public static String ENTITIES_NAMES_TOGGLE_ENABLED = "entities.names-toggle.enabled";

    @Comment("")
    @Comment("What the toggle command will be?")
    public static String ENTITIES_NAMES_TOGGLE_COMMAND = "entities.names-toggle.command";

    @Comment("")
    @Comment("Should entities get knockback after they are killed?")
    public static String ENTITIES_NEXT_STACK_KNOCKBACK = "entities.next-stack-knockback";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked spawners.")
    public static String SPAWNERS = "spawners";

    @Comment("Should spawners get stacked on the server?")
    public static String SPAWNERS_ENABLED = "spawners.enabled";

    @Comment("")
    @Comment("How many blocks from the spawner should be checked for other spawners to stack into?")
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
    @Comment("If you wish to disable blacklisted spawners, use \"blacklist: []\"")
    public static String SPAWNERS_BLACKLIST = "spawners.blacklist";

    @Comment("")
    @Comment("Whitelisted spawners are spawners that will get stacked.")
    @Comment("EntityType list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html")
    @Comment("If you wish to disable whitelisted spawners, use \"whitelist: []\"")
    public static String SPAWNERS_WHITELIST = "spawners.whitelist";

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
    @Comment("Set a maximum amount of spawner objects in a chunk.")
    @Comment("If you want to disable the feature, set it to 0.")
    public static String SPAWNERS_CHUNK_LIMIT = "spawners.chunk-limit";

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
    @Comment("Chance of spawners to be dropped after break of silk touch.")
    public static String SPAWNERS_SILK_TOUCH_BREAK_CHANCE = "spawners.silk-touch-break-chance";

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
    @Comment("Should explosions act like silk-touch and drop the spawner?")
    public static String SPAWNERS_SILK_SPAWNERS_EXPLOSIONS_DROP_SPAWNER = "spawners.silk-spawners.explosions-drop-spawner";

    @Comment("")
    @Comment("Should the spawner item go straight into the player's inventory instead of dropping on ground?")
    public static String SPAWNERS_SILK_SPAWNERS_DROP_TO_INVENTORY = "spawners.silk-spawners.drop-to-inventory";

    @Comment("")
    @Comment("Should sneaking while mining break the entire stack instead of reducing it by one?")
    public static String SPAWNERS_SHIFT_GET_WHOLE_STACK = "spawners.shift-get-whole-stack";

    @Comment("")
    @Comment("Should one item be dropped for a stacked spawner instead of multiple items?")
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
    @Comment("When enabled and player is clicking a spawner while sneaking, an inventory will be opened, there")
    @Comment("he can put spawner items to add to the spawner. Make sure break-menu is disabled!")
    public static String SPAWNERS_PLACE_INVENTORY = "spawners.place-inventory";

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
    @Comment("When enabled and change-using-eggs is enabled, the amount of eggs that will be required to change")
    @Comment("a spawner would be the same as the stack size of the spawner.")
    public static String SPAWNERS_EGGS_STACK_MULTIPLY = "spawners.eggs-stack-multiply";

    @Comment("")
    @Comment("Should there be the ablity to place spawners next to each other?")
    public static String SPAWNERS_NEXT_SPAWNER_PLACEMENT = "spawners.next-spawner-placement";

    @Comment("")
    @Comment("Should there be only one spawner in the merge radius?")
    public static String SPAWNERS_ONLY_ONE_SPAWNER = "spawners.only-one-spawner";

    @Comment("")
    @Comment("Here you can configurable all features related to stacked barrels (aka stacked blocks).")
    public static String BARRELS = "barrels";

    @Comment("Should blocks get stacked into barrels on the server?")
    public static String BARRELS_ENABLED = "barrels.enabled";

    @Comment("")
    @Comment("How many blocks from the barrel should be checked for other blocks to stack into?")
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
    @Comment("Blacklisted barrels are barrels that won't get stacked.")
    @Comment("Material list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("If you wish to disable blacklisted spawners, use \"blacklist: []\"")
    public static String BARRELS_BLACKLIST = "barrels.blacklist";

    @Comment("")
    @Comment("Whitelisted blocks are blocks that will get stacked.")
    @Comment("Material list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html")
    @Comment("Make sure you follow the \"TYPE\" and \"TYPE:DATA\" formats.")
    @Comment("If you wish to disable whitelisted spawners, use \"whitelist: []\"")
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
    @Comment("Set a maximum amount of barrel objects in a chunk.")
    @Comment("If you want to disable the feature, set it to 0.")
    public static String BARRELS_CHUNK_LIMIT = "barrels.chunk-limit";

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
    @Comment("What the toggle command will be?")
    public static String BARRELS_TOGGLE_COMMAND_COMMAND = "barrels.toggle-command.command";

    @Comment("")
    @Comment("When enabled and player is clicking a barrel while sneaking, an inventory will be opened, there")
    @Comment("he can put blocks to add to the barrel.")
    public static String BARRELS_PLACE_INVENTORY = "barrels.place-inventory";

    @Comment("")
    @Comment("This should not be set to true unless being told by the dev")
    @Comment("When enabled, the plugin sets the barrel types to cauldron if they aren't already.")
    public static String BARRELS_FORCE_CAULDRON = "barrels.force-cauldron";

}
