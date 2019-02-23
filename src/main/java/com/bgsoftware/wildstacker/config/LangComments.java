package com.bgsoftware.wildstacker.config;

@SuppressWarnings("unused")
public final class LangComments {

    @Comment("###############################################")
    @Comment("##                                           ##")
    @Comment("##           WildStacker Messages            ##")
    @Comment("##            Developed by Ome_R             ##")
    @Comment("##                                           ##")
    @Comment("###############################################")
    public static String HEADER = "";

    @Comment("")
    @Comment("Only sent to ops")
    public static String AUTO_SAVE = "AUTO_SAVE";

    @Comment("")
    @Comment("Called when a player places / breaks a stacked-block")
    public static String BARREL_BREAK = "BARREL_BREAK";

    @Comment("")
    @Comment("Called when a player runs the toggle command")
    public static String BARREL_TOGGLE_ON = "BARREL_TOGGLE_ON";

    @Comment("")
    @Comment("Called when a player runs a command not in the required usage")
    public static String COMMAND_USAGE = "COMMAND_USAGE";

    @Comment("")
    @Comment("Called when a player runs an invalid stacker sub-command.")
    public static String HELP_COMMAND_HEADER = "HELP_COMMAND_HEADER";

    @Comment("")
    @Comment("Called when a player runs give command with an invalid argument.")
    public static String INVALID_BARREL = "INVALID_BARREL";

    @Comment("")
    @Comment("Called when a kill-all task is running")
    public static String KILL_ALL_ANNOUNCEMENT = "KILL_ALL_ANNOUNCEMENT";

    @Comment("")
    @Comment("Called when running a stacker command without permission.")
    public static String NO_PERMISSION = "NO_PERMISSION";

    @Comment("")
    @Comment("Called when a player successfully reloaded all configuration files.")
    public static String RELOAD_SUCCESS = "RELOAD_SUCCESS";

    @Comment("")
    @Comment("Called when a player places / breaks a spawner")
    public static String SPAWNER_BREAK = "SPAWNER_BREAK";

    @Comment("")
    @Comment("Called when a player successfully runs a give command.")
    public static String STACK_GIVE_PLAYER = "STACK_GIVE_PLAYER";

    @Comment("")
    @Comment("Called when running info command")
    public static String STACK_INFO_INVALID = "STACK_INFO_INVALID";


}
