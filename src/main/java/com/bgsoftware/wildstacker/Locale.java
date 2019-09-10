package com.bgsoftware.wildstacker;

import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.config.LangComments;
import com.bgsoftware.wildstacker.utils.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class Locale {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static Map<String, Locale> localeMap = new HashMap<>();

    public static Locale BARREL_BREAK = new Locale("BARREL_BREAK");
    public static Locale BARREL_PLACE = new Locale("BARREL_PLACE");
    public static Locale BARREL_UPDATE = new Locale("BARREL_UPDATE");
    public static Locale BARREL_TOGGLE_ON = new Locale("BARREL_TOGGLE_ON");
    public static Locale BARREL_TOGGLE_OFF = new Locale("BARREL_TOGGLE_OFF");
    public static Locale COMMAND_USAGE = new Locale("COMMAND_USAGE");
    public static Locale ENTITY_NAMES_TOGGLE_ON = new Locale("ENTITY_NAMES_TOGGLE_ON");
    public static Locale ENTITY_NAMES_TOGGLE_OFF = new Locale("ENTITY_NAMES_TOGGLE_OFF");
    public static Locale HELP_COMMAND_HEADER = new Locale("HELP_COMMAND_HEADER");
    public static Locale HELP_COMMAND_LINE = new Locale("HELP_COMMAND_LINE");
    public static Locale HELP_COMMAND_FOOTER = new Locale("HELP_COMMAND_FOOTER");
    public static Locale INVALID_BARREL = new Locale("INVALID_BARREL");
    public static Locale INVALID_ENTITY = new Locale("INVALID_ENTITY");
    public static Locale INVALID_NUMBER = new Locale("INVALID_NUMBER");
    public static Locale INVALID_PLAYER = new Locale("INVALID_PLAYER");
    public static Locale INVALID_TYPE = new Locale("INVALID_TYPE");
    public static Locale INSPECT_GIVE_PLAYER = new Locale("INSPECT_GIVE_PLAYER");
    public static Locale INSPECT_RECEIVE = new Locale("INSPECT_RECEIVE");
    public static Locale SIMULATE_GIVE_PLAYER = new Locale("SIMULATE_GIVE_PLAYER");
    public static Locale SIMULATE_RECEIVE = new Locale("SIMULATE_RECEIVE");
    public static Locale ITEM_NAMES_TOGGLE_ON = new Locale("ITEM_NAMES_TOGGLE_ON");
    public static Locale ITEM_NAMES_TOGGLE_OFF = new Locale("ITEM_NAMES_TOGGLE_OFF");
    public static Locale KILL_ALL_ANNOUNCEMENT = new Locale("KILL_ALL_ANNOUNCEMENT");
    public static Locale KILL_ALL_REMAINING_TIME = new Locale("KILL_ALL_REMAINING_TIME");
    public static Locale KILL_ALL_OPS = new Locale("KILL_ALL_OPS");
    public static Locale NEXT_SPAWNER_PLACEMENT = new Locale("NEXT_SPAWNER_PLACEMENT");
    public static Locale NO_PERMISSION = new Locale("NO_PERMISSION");
    public static Locale RELOAD_SUCCESS = new Locale("RELOAD_SUCCESS");
    public static Locale SPAWNER_BREAK = new Locale("SPAWNER_BREAK");
    public static Locale SPAWNER_BREAK_NOT_ENOUGH_MONEY = new Locale("SPAWNER_BREAK_NOT_ENOUGH_MONEY");
    public static Locale SPAWNER_PLACE = new Locale("SPAWNER_PLACE");
    public static Locale SPAWNER_PLACE_NOT_ENOUGH_MONEY = new Locale("SPAWNER_PLACE_NOT_ENOUGH_MONEY");
    public static Locale SPAWNER_PLACE_BLOCKED = new Locale("SPAWNER_PLACE_BLOCKED");
    public static Locale SPAWNER_UPDATE = new Locale("SPAWNER_UPDATE");
    public static Locale STACK_GIVE_PLAYER = new Locale("STACK_GIVE_PLAYER");
    public static Locale STACK_RECEIVE = new Locale("STACK_RECEIVE");
    public static Locale STACK_INFO_INVALID = new Locale("STACK_INFO_INVALID");
    public static Locale BARREL_INFO_HEADER = new Locale("BARREL_INFO_HEADER");
    public static Locale BARREL_INFO_TYPE = new Locale("BARREL_INFO_TYPE");
    public static Locale BARREL_INFO_AMOUNT = new Locale("BARREL_INFO_AMOUNT");
    public static Locale BARREL_INFO_FOOTER = new Locale("BARREL_INFO_FOOTER");
    public static Locale SPAWNER_INFO_HEADER = new Locale("SPAWNER_INFO_HEADER");
    public static Locale SPAWNER_INFO_TYPE = new Locale("SPAWNER_INFO_TYPE");
    public static Locale SPAWNER_INFO_AMOUNT = new Locale("SPAWNER_INFO_AMOUNT");
    public static Locale SPAWNER_INFO_FOOTER = new Locale("SPAWNER_INFO_FOOTER");
    public static Locale ENTITY_INFO_HEADER = new Locale("ENTITY_INFO_HEADER");
    public static Locale ENTITY_INFO_UUID = new Locale("ENTITY_INFO_UUID");
    public static Locale ENTITY_INFO_TYPE = new Locale("ENTITY_INFO_TYPE");
    public static Locale ENTITY_INFO_AMOUNT = new Locale("ENTITY_INFO_AMOUNT");
    public static Locale ENTITY_INFO_SPAWN_REASON = new Locale("ENTITY_INFO_SPAWN_REASON");
    public static Locale ENTITY_INFO_NERFED = new Locale("ENTITY_INFO_NERFED");
    public static Locale ENTITY_INFO_NO_AI = new Locale("ENTITY_INFO_NO_AI");
    public static Locale ENTITY_INFO_FOOTER = new Locale("ENTITY_INFO_FOOTER");
    public static Locale OBJECT_SIMULATE_CHOOSE_SECOND = new Locale("OBJECT_SIMULATE_CHOOSE_SECOND");
    public static Locale OBJECT_SIMULATE_SUCCESS_RESULT = new Locale("OBJECT_SIMULATE_SUCCESS_RESULT");
    public static Locale OBJECT_SIMULATE_FAIL_RESULT = new Locale("OBJECT_SIMULATE_FAIL_RESULT");

    private Locale(String identifier){
        localeMap.put(identifier, this);
    }

    private String message;

    public String getMessage(Object... objects){
        if(message != null && !message.equals("")) {
            String msg = message;

            for (int i = 0; i < objects.length; i++)
                msg = msg.replace("{" + i + "}", objects[i].toString());

            return msg;
        }

        return null;
    }

    public void send(CommandSender sender, Object... objects){
        String message = getMessage(objects);
        if(message != null && sender != null)
            sender.sendMessage(message);
    }

    private void setMessage(String message){
        this.message = message;
    }

    public static void reload(){
        WildStackerPlugin.log("Loading messages started...");
        long startTime = System.currentTimeMillis();
        int messagesAmount = 0;
        File file = new File(plugin.getDataFolder(), "lang.yml");

        if(!file.exists())
            FileUtils.saveResource("lang.yml");

        CommentedConfiguration cfg = new CommentedConfiguration(LangComments.class, file);

        cfg.resetYamlFile(plugin, "lang.yml");

        for(String identifier : localeMap.keySet()){
            localeMap.get(identifier).setMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString(identifier, "")));
            messagesAmount++;
        }

        WildStackerPlugin.log(" - Found " + messagesAmount + " messages in lang.yml.");
        WildStackerPlugin.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
