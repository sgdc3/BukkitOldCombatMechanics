package gvlfm78.plugin.oldcombatmechanics;

import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static gvlfm78.plugin.oldcombatmechanics.utils.Messenger.HORIZONTAL_BAR;

public class CommandHandler implements CommandExecutor {
    private static final String NO_PERMISSION = "&cYou need the permission '%s' to do that!";

    private OldCombatMechanics plugin;

    public CommandHandler(OldCombatMechanics plugin) {
        this.plugin = plugin;
    }

    // TODO: implement mtoggle command to disable/enable modules on the go
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {//Tell them about available commands
            Messenger.send(sender, ChatColor.DARK_GRAY + HORIZONTAL_BAR);

            Messenger.send(sender, "&6&lOldCombatMechanics&e by &cgvlfm78&e and &cRayzr522&e version &6" + plugin.getVersion());
            Messenger.send(sender, "&eYou can use &c/ocm reload&e to reload the config file");

            Messenger.send(sender, ChatColor.DARK_GRAY + HORIZONTAL_BAR);
            return true;
        }

        // Get the sub-command
        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("reload")) {// Reloads config
            if (!sender.hasPermission("oldcombatmechanics.reload")) {
                Messenger.send(sender, NO_PERMISSION, "oldcombatmechanics.reload");
                return true;
            }

            plugin.reload();

            Messenger.send(sender, "&6&lOldCombatMechanics&e config file reloaded");
            return true;
        }

        return false;
    }
}