package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.reflection.TeamPacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

//TODO: add packet filter to override the collision rule (support for plugins like featherboard)
public class ModulePlayerCollisions extends Module {

    private Map<Player, String> securedPlayers;

    public ModulePlayerCollisions(OldCombatMechanics plugin) {
        super(plugin, "disable-player-collisions");
    }

    @Override
    public void onEnable() {
        securedPlayers = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!shouldActivate(player)) {
                return;
            }
            addPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : securedPlayers.keySet()) {
            removePlayer(player);
        }
        securedPlayers = null;
    }

    private void addPlayer(Player player) {
        if (securedPlayers.containsKey(player)) {
            return;
        }
        securedPlayers.put(player, TeamPacketUtils.sendNewTeamPacket(player));
    }

    private void removePlayer(Player player) {
        String teamName = securedPlayers.get(player);
        if (teamName == null) {
            return;
        }
        if (!player.isOnline()) {
            return;
        }
        TeamPacketUtils.sendRemoveTeamPacket(player, teamName);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!shouldActivate(player)) {
            return;
        }
        addPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!shouldActivate(player)) {
            removePlayer(player);
            return;
        }
        addPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }
}
