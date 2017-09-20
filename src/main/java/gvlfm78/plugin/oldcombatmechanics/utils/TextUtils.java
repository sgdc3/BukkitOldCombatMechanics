package gvlfm78.plugin.oldcombatmechanics.utils;

import org.bukkit.ChatColor;

public class TextUtils {
    /**
     * Converts ampersand (&) color codes to Minecraft ({@link ChatColor#COLOR_CHAR}) color codes.
     *
     * @param text The text to colorize.
     * @return The colorized text.
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Removes all Minecraft ({@link ChatColor#COLOR_CHAR}) color codes from a string.
     *
     * @param text The text to strip colors from.
     * @return The stripped text.
     */
    public static String stripColor(String text) {
        return ChatColor.stripColor(text);
    }
}
