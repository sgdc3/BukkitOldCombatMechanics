package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ModuleWithMaterialList extends Module {

    private ArrayList<Material> materials;
    private boolean whitelistMode;

    public ModuleWithMaterialList(OldCombatMechanics plugin, String moduleName) {
        super(plugin, moduleName);

        materials = new ArrayList<>();
        List<String> materialList = getConfiguration().getStringList("items");
        whitelistMode = getConfiguration().getBoolean("whitelist");

        //There is no list, just block everything
        if (materialList == null) {
            return;
        }

        //Looping through name list and adding valid materials to list
        for (String materialName : materialList) {
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                debug("Found material: " + material);
                materials.add(material);
            }
        }
    }

    protected boolean shouldBeCancelled(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material material = item.getType();
        boolean isContained = materials.contains(material);

        return whitelistMode && !isContained || !whitelistMode && isContained;
    }
}
