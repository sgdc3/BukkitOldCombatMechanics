package gvlfm78.plugin.oldcombatmechanics.modules;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import java.util.ArrayList;
import java.util.List;

public class ModuleUpdater extends Module implements Listener {

    private OldCombatMechanics plugin;
    private SpigetUpdate updater;

    public ModuleUpdater(OldCombatMechanics plugin) {
        super(plugin, "update-checker");
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        updater = new SpigetUpdate(plugin, 19510);
        updater.setVersionComparator(VersionComparator.SEM_VER);
        updateCheck(null);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("OldCombatMechanics.notify")) {
            return;
        }
        updateCheck(player);
    }

    private void updateCheck(final Player player) {
        updater.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                List<String> updateMessages = new ArrayList<>(2);
                updateMessages.add(ChatColor.BLUE + "An update for OldCombatMechanics to version " + newVersion + " is available!");
                updateMessages.add(ChatColor.BLUE + "Click here to download it: " + ChatColor.GRAY + downloadUrl);
                if (player == null) {
                    for (String message : updateMessages) {
                        plugin.getLogger().info(ChatColor.stripColor(message));
                    }
                } else {
                    for (String message : updateMessages) {
                        player.sendMessage(message);
                    }
                }
            }

            @Override
            public void upToDate() {
            }
        });
    }
}