package gvlfm78.plugin.oldcombatmechanics;

import gvlfm78.plugin.oldcombatmechanics.modules.ModuleManager;
import gvlfm78.plugin.oldcombatmechanics.modules.ModuleUpdater;
import gvlfm78.plugin.oldcombatmechanics.modules.mechanics.*;
import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class OldCombatMechanics extends JavaPlugin {

    private static boolean debug = false;
    private ModuleManager moduleManager;

    // TODO: move to a proper class
    private List<Material> interactiveBlocks;

    public static boolean isDebugEnabled() {
        return debug;
    }

    @Override
    public void onEnable() {
        Messenger.init(this);
        reload();
        Messenger.info(getName() + " v" + getVersion() + " has been enabled correctly");
    }

    @Override
    public void onDisable() {
        unload();
        Messenger.info(getName() + " v" + getVersion() + " has been disabled correctly");
    }

    public void unload() {
        if (moduleManager != null) {
            moduleManager.disableModules();
            moduleManager = null;
        }
    }

    // TODO: create a config manager class
    public void reload() {
        // Unload the modules if active
        unload();

        // Reload config
        saveDefaultConfig();
        reloadConfig();

        // Check config version
        if (isConfigOutdated()) {
            Messenger.severe("Config version does not match, backing up old config and creating a new one");
            File configFile = new File(getDataFolder(), "config.yml");
            File backupFile = new File(getDataFolder(), "config-backup.yml");
            try {
                Files.move(configFile.toPath(), backupFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                setEnabled(false);
                return;
            }
            reload();
            return;
        }

        // Load global options
        debug = getConfig().getBoolean("debug.enabled");
        loadInteractiveBlocks();

        // Initialize the module manager
        moduleManager = new ModuleManager(this);
        registerBuiltInModules();

        // Register command handler
        getCommand("OldCombatMechanics").setExecutor(new CommandHandler(this));

        // BStats Metrics
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimpleBarChart("enabled_modules", () -> {
            HashMap<String, Integer> values = new HashMap<>();
            moduleManager.getModules().forEach(module -> {
                if (module.isEnabled()) {
                    values.put(module.toString(), 1);
                }
            });
            return values;
        }));
    }

    public void loadInteractiveBlocks() {
        interactiveBlocks = new ArrayList<>();

        List<String> rawBlocks = getConfig().getStringList("interactive");
        if (rawBlocks == null) {
            return;
        }

        for (String name : rawBlocks) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                interactiveBlocks.add(material);
            }
        }
    }

    public List<Material> getInteractiveBlocks() {
        return interactiveBlocks;
    }

    private boolean isConfigOutdated() {
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("config.yml")));
        return getConfig().getInt("config-version") != defaultConfig.getInt("config-version");
    }

    private void registerBuiltInModules() {
        moduleManager.register(new ModuleUpdater(this));
        moduleManager.register(new ModuleAttackCooldown(this));
        moduleManager.register(new ModulePlayerCollisions(this));
        moduleManager.register(new ModuleOldToolDamage(this));
        moduleManager.register(new ModuleSwordSweep(this));
        moduleManager.register(new ModuleGoldenApple(this));
        moduleManager.register(new ModuleFishingKnockback(this));
        moduleManager.register(new ModulePlayerRegen(this));
        moduleManager.register(new ModuleSwordBlocking(this));
        moduleManager.register(new ModuleOldArmourStrength(this));
        moduleManager.register(new ModuleDisableCrafting(this));
        moduleManager.register(new ModuleDisableOffHand(this));
        moduleManager.register(new ModuleOldBrewingStand(this));
        moduleManager.register(new ModuleDisableElytra(this));
        moduleManager.register(new ModuleDisableProjectileRandomness(this));
        moduleManager.register(new ModuleDisableBowBoost(this));
        moduleManager.register(new ModuleProjectileKnockback(this));
        moduleManager.register(new ModuleNoLapisEnchantments(this));
        moduleManager.register(new ModuleDisableEnderpearlCooldown(this));
    }

    public String getVersion() {
        return getDescription().getVersion();
    }
}