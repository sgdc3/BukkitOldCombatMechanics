package gvlfm78.plugin.oldcombatmechanics.modules;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private OldCombatMechanics plugin;
    private Map<String, Module> modules;

    public ModuleManager(OldCombatMechanics plugin) {
        this.plugin = plugin;
        modules = new HashMap<>();
    }

    public void register(Module module) {
        modules.put(module.getName(), module);
    }

    public void unregister(Module module) {
        modules.put(module.getName(), module);
    }

    private void enable(Module module) {
        module.onEnable();
        plugin.getServer().getPluginManager().registerEvents(module, plugin);
        Messenger.debug("Enabled " + module.getName());
    }

    private void disable(Module module) {
        HandlerList.unregisterAll(module);
        module.onDisable();
        Messenger.debug("Disabled " + module.getName());
    }

    public void initModules() {
        for (Module module : modules.values()) {
            if (module.isEnabled()) { // Enable only if was enabled by config
                enable(module);
            }
        }
    }

    public void disableModules() {
        for (Module module : modules.values()) {
            if (module.isEnabled()) { // Disable only if was enabled
                disable(module);
            }
        }
    }

    public boolean enable(String name) {
        Module module = modules.get(name);
        if (module == null || module.isEnabled()) {
            return false;
        }
        enable(module);
        return true;
    }

    public boolean disable(String name) {
        Module module = modules.get(name);
        if (module == null || !module.isEnabled()) {
            return false;
        }
        HandlerList.unregisterAll(module);
        disable(module);
        return true;
    }

    public Collection<Module> getModules() {
        return modules.values();
    }
}
