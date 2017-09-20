package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class ModuleDisableCrafting extends ModuleWithMaterialList {

    public ModuleDisableCrafting(OldCombatMechanics plugin) {
        super(plugin, "disable-crafting");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getViewers().size() < 1) { // TODO: it should always be > 0
            return;
        }

        if (!shouldActivate(event.getViewers().get(0))) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        if (shouldBeCancelled(result)) {
            inventory.setResult(null);
        }
    }
}