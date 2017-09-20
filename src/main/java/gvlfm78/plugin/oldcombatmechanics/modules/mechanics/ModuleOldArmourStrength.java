package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.comphenix.attribute.Attributes;
import com.comphenix.attribute.Attributes.Attribute;
import com.comphenix.attribute.Attributes.AttributeType;
import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import gvlfm78.plugin.oldcombatmechanics.utils.reflection.ItemData;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class ModuleOldArmourStrength extends Module {

    private Map<String, Double> armourValues;
    private double customToughness;

    public ModuleOldArmourStrength(OldCombatMechanics plugin) {
        super(plugin, "old-armour-strength");

        ConfigurationSection section = getConfiguration();

        customToughness = section.getDouble("customToughness");
        armourValues = new HashMap<>();
        for (String type : section.getKeys(false)) {
            double value = section.getDouble(type);
            Messenger.debug("[ArmourValues] Loading value '" + value + "' for type '" + type + "'");
            armourValues.put(type, section.getDouble(type));
        }
    }

    private static int getDefaultToughness(Material material) {
        switch (material) {
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public void onEnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setArmourAccordingly(player, shouldActivate(player));
        }
    }

    @Override
    public void onDisable() {
    }

    private double getArmourValue(Material material) {
        if (!armourValues.containsKey(material.name())) {
            return 0;
        }
        return armourValues.get(material.name());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmourEquip(ArmorEquipEvent event) {
        final Player player = event.getPlayer();
        debug("OnArmourEquip was called", player);

        //Equipping
        ItemStack newPiece = event.getNewArmorPiece();
        if (newPiece != null && newPiece.getType() != Material.AIR) {
            debug("Equip detected, applying armour value to new armour piece", player);
            event.setNewArmorPiece(apply(newPiece, shouldActivate(player)));
        }

        //Unequipping
        ItemStack oldPiece = event.getOldArmorPiece();
        if (oldPiece != null && oldPiece.getType() != Material.AIR) {
            debug("Unequip detected, applying armour value to old armour piece", player);
            event.setOldArmorPiece(apply(oldPiece, false));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        debug("onPlayerJoin armour event was called", player);
        setArmourAccordingly(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        debug("onPlayerLeave armour event was called", player);
        setArmourToDefault(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        debug("onWorldChange armour event was called", player);
        setArmourAccordingly(player);
    }

    private void setArmourToDefault(final Player player) {
        // Tells method that getConfiguration is disabled in this world
        setArmourAccordingly(player, false);
    }

    private void setArmourAccordingly(final Player player) {
        setArmourAccordingly(player, shouldActivate(player));
    }

    private void setArmourAccordingly(final Player player, boolean enabled) {
        final PlayerInventory inv = player.getInventory();
        ItemStack[] armours = inv.getContents();
        // Check the whole inventory for armour pieces

        for (int i = 0; i < armours.length; i++) {
            ItemStack piece = armours[i];

            if (piece != null && piece.getType() != Material.AIR) {
                debug("Attempting to apply armour value to item", player);

                //If this piece is one of the ones being worn right now
                if (ArrayUtils.contains(inv.getArmorContents(), armours[i])) {
                    armours[i] = apply(piece, enabled); //Apply/remove values according state of getConfiguration in this world
                } else {
                    armours[i] = apply(piece, false); //Otherwise set values back to default
                }
            }
        }

        player.getInventory().setContents(armours);
    }

    private ItemStack apply(ItemStack item, boolean enable) {
        String slot;
        String type = item.getType().toString().toLowerCase();

        if (type.contains("helmet")) {
            slot = "head";
        } else if (type.contains("chestplate")) {
            slot = "chest";
        } else if (type.contains("leggings")) {
            slot = "legs";
        } else if (type.contains("boots")) {
            slot = "feet";
        } else {
            return item; //Not an armour piece
        }

        double strength = getArmourValue(item.getType());

        Attributes attributes = new Attributes(item);

        double toughness = enable ? customToughness : getDefaultToughness(item.getType());

        boolean armourTagPresent = false, toughnessTagPresent = false;

        for (int i = 0; i < attributes.size(); i++) {
            Attribute att = attributes.get(i);
            if (att == null) {
                continue;
            }

            AttributeType attType = att.getAttributeType();

            if (attType == AttributeType.GENERIC_ARMOR) { //Found a generic armour tag
                if (armourTagPresent) { //If we've already found another tag
                    attributes.remove(att); //Remove this one as it's a duplicate
                } else {
                    armourTagPresent = true;
                    if (att.getAmount() != strength) { //If its value does not match what it should be, remove it
                        attributes.remove(att);
                        armourTagPresent = false; //Set armour value anew
                    }
                }
            } else if (attType == AttributeType.GENERIC_ARMOR_TOUGHNESS) { //Found a generic armour customToughness tag
                if (toughnessTagPresent) { //If we've already found another tag
                    attributes.remove(att); //Remove this one as it's a duplicate
                } else {
                    toughnessTagPresent = true;
                    if (att.getAmount() != toughness) { //If its value does not match what it should be, remove it
                        attributes.remove(att);
                        toughnessTagPresent = false; //Set armour value anew
                    }
                }
            }
        }

        //If there's no armour tag present add it
        if (!armourTagPresent) {
            attributes.add(Attributes.Attribute.newBuilder().name("Armor").type(Attributes.AttributeType.GENERIC_ARMOR).amount(strength).slot(slot).build());
        }
        //If there's no customToughness tag present add it
        if (!toughnessTagPresent) {
            attributes.add(Attributes.Attribute.newBuilder().name("ArmorToughness").type(Attributes.AttributeType.GENERIC_ARMOR_TOUGHNESS).amount(toughness).slot(slot).build());
        }

        ItemData.mark(item, "ArmorModifier");
        return item;
    }
}