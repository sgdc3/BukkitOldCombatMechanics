package gvlfm78.plugin.oldcombatmechanics.modules;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import org.apache.commons.lang.WordUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public abstract class Module implements Listener {

    private OldCombatMechanics plugin;

    private String name;
    private ConfigurationSection configuration;

    private boolean enabled;
    private List<String> enabledWorlds;

    public Module(OldCombatMechanics plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        configuration = plugin.getConfig().getConfigurationSection(name);

        enabled = configuration.getBoolean("enabled");
        enabledWorlds = configuration.getStringList("worlds");
    }

    public abstract void onEnable();

    public abstract void onDisable();

    /**
     * Returns if the module is currently enabled.
     *
     * @return true if the module is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns if the module should activate in the specified world
     *
     * @param world the world
     * @return true if the module needs to be activated
     */
    public boolean shouldActivate(World world) {
        return enabled && (enabledWorlds.isEmpty() || enabledWorlds.contains(world.getName()));
    }

    /**
     * Returns if the module should activate with the specified entity
     *
     * @param entity the entity
     * @return true if the module needs to be activated
     */
    public boolean shouldActivate(Entity entity) {
        return shouldActivate(entity.getWorld());
    }

    /**
     * Returns if the module should activate with the specified block
     *
     * @param block the block
     * @return true if the module needs to be activated
     */
    public boolean shouldActivate(Block block) {
        return shouldActivate(block.getWorld());
    }

    public String getName() {
        return name;
    }

    /**
     * Returns if the module configuration section
     *
     * @return the configuration section
     */
    protected ConfigurationSection getConfiguration() {
        return configuration;
    }

    protected BukkitRunnable schedule(BukkitRunnable runnable) {
        runnable.runTask(plugin);
        return runnable;
    }

    protected BukkitRunnable scheduleDelayed(BukkitRunnable runnable, long delay) {
        runnable.runTaskLater(plugin, delay);
        return runnable;
    }

    protected BukkitRunnable scheduleTimer(BukkitRunnable runnable, long interval) {
        runnable.runTaskTimer(plugin, 0, interval);
        return runnable;
    }

    protected void debug(String text) {
        Messenger.debug("[" + this + "] " + text);
    }

    protected void debug(String text, Player player) {
        if (OldCombatMechanics.isDebugEnabled()) {
            Messenger.send(player, "&8&l[&fDEBUG&8&l][&f" + this + "&8&l]&7 " + text);
        }
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name.replaceAll("-", " "));
    }
}