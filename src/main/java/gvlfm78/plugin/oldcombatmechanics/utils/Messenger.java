package gvlfm78.plugin.oldcombatmechanics.utils;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.logging.Logger;

public class Messenger {

    public static final String HORIZONTAL_BAR = ChatColor.STRIKETHROUGH + "----------------------------------------------------";

    private static OldCombatMechanics plugin;

    public static void init(OldCombatMechanics plugin) {
        Messenger.plugin = plugin;
    }

    public static void debug(String msg) {
        if (!OldCombatMechanics.isDebugEnabled()) {
            return;
        }

        info("[DEBUG] " + msg);
    }

    public static void info(String msg) {
        plugin.getLogger().info(msg);
    }

    public static void warning(String msg) {
        plugin.getLogger().warning(msg);
    }

    public static void severe(String msg) {
        Logger logger = plugin.getLogger();

        logger.severe("------------------------------------------------------------");
        logger.severe("OldCombatMechanics has encountered a serious problem:");
        logger.severe(msg);
        logger.severe("------------------------------------------------------------");
    }

    public static void send(CommandSender sender, String message) {
        send(sender, message, (Object[]) null);
    }

    /**
     * This will format any ampersand (&) color codes, format any args passed to it using {@link String#format(String, Object...)}, and then send the message to the specified {@link CommandSender}.
     *
     * @param sender  The {@link CommandSender} to send the message to.
     * @param message The message to send.
     * @param args    The args to format the message with.
     */
    public static void send(CommandSender sender, String message, Object... args) {
        Objects.requireNonNull(sender, "sender cannot be null!");
        Objects.requireNonNull(message, "message cannot be null!");

        sender.sendMessage(TextUtils.colorize(String.format(message, args)));
    }
}