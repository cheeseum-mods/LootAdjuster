package cheeseum.lootadjuster;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;

public class LootConfigHelper
{
    private LootConfigHelper () {
    }

    public static Set<String> getLootCategories(Configuration config) {
        return config.getCategory("loot").keySet();
    }

    public static List<WeightedRandomChestContent> getLootForCategory(Configuration config, String category) {
        List<WeightedRandomChestContent> ret = new ArrayList<WeightedRandomChestContent>();
        Property lootCategory = config.getCategory("loot").get(category);

        // "mod:item:meta",min,max,weight
        if (lootCategory.isList()) {
            for (String lootEntry: lootCategory.getStringList()) {
                lootEntry = lootEntry.replace("\"", "");
                String lootData[] = lootEntry.split(",");
                String itemData[] = lootData[0].split(":");

                if (lootData.length < 4 || itemData.length < 2) {
                    FMLLog.severe("Improperly formatted loot config! Grues ahead!");
                    continue;
                }

                String modId = itemData[0];
                String itemName = itemData[1];
                int itemMeta = itemData.length > 2 ? Integer.parseInt(itemData[2]) : 0;
                
                ItemStack item = GameRegistry.findItemStack(modId, itemName, 1);
                item.setItemDamage(itemMeta);
                
                ret.add(
                    new WeightedRandomChestContent(
                    	item,
                        Integer.parseInt(lootData[1]),
                        Integer.parseInt(lootData[2]),
                        Integer.parseInt(lootData[3])
                    )
                );
            }
        }
        return ret;
    }

    public static String getLootString(WeightedRandomChestContent loot) {
    	GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(loot.theItemId.getItem());
    
    	if (uid == null) {
    		FMLLog.severe("Couldn't find item data for loot entry!");
    		return "";
    	}
    	
        return String.format("\"%s:%s:%d,%d,%d,%d\"", 
            uid.modId, uid.name,
            loot.theItemId.getItemDamage(),
            loot.theMinimumChanceToGenerateItem,
            loot.theMaximumChanceToGenerateItem,
            loot.itemWeight
        );
    }
}
