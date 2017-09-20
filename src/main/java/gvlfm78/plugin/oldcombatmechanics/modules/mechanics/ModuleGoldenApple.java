package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class ModuleGoldenApple extends Module {

    private static final ItemStack NAPPLE = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);

    private List<PotionEffect> enchantedGoldenAppleEffects, goldenAppleEffects;
    private boolean noConflictMode, enableEnchantedGoldenAppleCrafting, oldPotionEffects;

    public ModuleGoldenApple(OldCombatMechanics plugin) {
        super(plugin, "old-golden-apples");

        enchantedGoldenAppleEffects = getPotionEffects("napple");
        goldenAppleEffects = getPotionEffects("gapple");
        noConflictMode = getConfiguration().getBoolean("no-conflict-mode");
        enableEnchantedGoldenAppleCrafting = getConfiguration().getBoolean("enchanted-golden-apple-crafting");
        oldPotionEffects = getConfiguration().getBoolean("old-potion-effects");

        if (enableEnchantedGoldenAppleCrafting) {
            ShapedRecipe nappleRecipe;
            try {
                nappleRecipe = new ShapedRecipe(new NamespacedKey(plugin, "MINECRAFT"), NAPPLE);
            } catch (Exception e) {
                nappleRecipe = new ShapedRecipe(NAPPLE);
            }
            nappleRecipe.shape("ggg", "gag", "ggg").setIngredient('g', Material.GOLD_BLOCK).setIngredient('a', Material.APPLE);
            plugin.getServer().addRecipe(nappleRecipe);
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    private List<PotionEffect> getPotionEffects(String apple) {
        List<PotionEffect> appleEffects = new ArrayList<>();

        ConfigurationSection section = getConfiguration().getConfigurationSection(apple + "-effects");
        for (String key : section.getKeys(false)) {
            int duration = section.getInt(key + ".duration");
            int amplifier = section.getInt(key + ".amplifier");
            PotionEffect effect = new PotionEffect(PotionEffectType.getByName(key), duration, amplifier);
            appleEffects.add(effect);
        }

        return appleEffects;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getInventory() == null) { // TODO: should never happen
            return;
        }

        ItemStack item = event.getInventory().getResult();
        if (item == null) {
            return;
        }

        if (item.getType() == Material.GOLDEN_APPLE && item.getDurability() == (short) 1) {
            if (noConflictMode) {
                return;
            }

            HumanEntity player = event.getView().getPlayer();
            if (!shouldActivate(player)) {
                event.getInventory().setResult(null);
            } else if (shouldActivate(player) && !enableEnchantedGoldenAppleCrafting) {
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.GOLDEN_APPLE) {
            return;
        }

        if (!shouldActivate(event.getPlayer()) || !oldPotionEffects) {
            return;
        }

        event.setCancelled(true);

        ItemStack originalItem = event.getItem();
        ItemStack item = event.getItem();

        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();

        int foodLevel = player.getFoodLevel();
        foodLevel = foodLevel + 4 > 20 ? 20 : foodLevel + 4;

        item.setAmount(item.getAmount() - 1);

        player.setFoodLevel(foodLevel);

        if (item.getDurability() == (short) 1) {
            for (PotionEffect effect : enchantedGoldenAppleEffects) {
                event.getPlayer().removePotionEffect(effect.getType());
            }

            event.getPlayer().addPotionEffects(enchantedGoldenAppleEffects);
        } else {
            for (PotionEffect effect : goldenAppleEffects) {
                event.getPlayer().removePotionEffect(effect.getType());
            }

            event.getPlayer().addPotionEffects(goldenAppleEffects);
        }

        if (item.getAmount() <= 0) {
            item = null;
        }

        //The bug occurs here, so we must check which hand has the apples
        //A player can't eat food in the offhand if there is any in the main hand
        //On this principle if there are gapples in the mainhand it must be that one, else it's the offhand
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();

        if (mainHand.equals(originalItem)) {
            inv.setItemInMainHand(item);
        } else if (offHand.equals(originalItem)) {
            inv.setItemInOffHand(item);
        } else if (mainHand.getType() == Material.GOLDEN_APPLE) {
            inv.setItemInMainHand(item);
        }
    }
}