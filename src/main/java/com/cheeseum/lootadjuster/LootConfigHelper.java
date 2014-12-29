package com.cheeseum.lootadjuster;

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
    
    /**
     * Parses a loot string from the config format into a WeightedRandomChestContent
     * formatted as: "mod:item:meta",min,max,weight, quotes optional
     * returns null on unsuccessful parse
     */
    public static WeightedRandomChestContent fromLootString (String entry) {
        entry = entry.replace("\"", "");
        String lootData[] = entry.split(",");
        String itemData[] = lootData[0].split(":");

        WeightedRandomChestContent ret = null;
        
        if (!entry.isEmpty()) {
	        if (lootData.length < 4 || itemData.length < 2) {
	            LootAdjuster.logger.warn("Improperly formatted loot config: '" + entry + "'! Grues ahead!");
	        } else {
	        	// begin parsing the entry
	        	
		        String modId = itemData[0];
		        String itemName = itemData[1];
		        int itemMeta = itemData.length > 2 ? Integer.parseInt(itemData[2]) : 0;
		        
		        ItemStack item = GameRegistry.findItemStack(modId, itemName, 1);
		        if (item == null) {
		        	LootAdjuster.logger.error("Unrecognized item data %s, skipping!", lootData[0]);
		        } else {
		        	// successfully parsed the item
    		        item.setItemDamage(itemMeta);
                    ret = new WeightedRandomChestContent(
                        	item,
                            Integer.parseInt(lootData[1]),
                            Integer.parseInt(lootData[2]),
                            Integer.parseInt(lootData[3])
                    );
		        }
	        }
        }
        
        return ret;
    }

    public static String toLootString(WeightedRandomChestContent loot) {
    	GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(loot.theItemId.getItem());
    	String combinedName = "";
    	
    	if (uid != null) {
    		combinedName = String.format("%s:%s", uid.modId, uid.name);
    	} else {
    		combinedName = loot.theItemId.getItem().getUnlocalizedName();
    		LootAdjuster.logger.error("Couldn't find unique identifier for item %s, falling back to unlocalized name, things might break!", combinedName);
    	}
    	
        return String.format("\"%s:%d,%d,%d,%d\"", 
            combinedName,
        	loot.theItemId.getItemDamage(),
            loot.theMinimumChanceToGenerateItem,
            loot.theMaximumChanceToGenerateItem,
            loot.itemWeight
        );
    }
    
    public static List<WeightedRandomChestContent> getLootData(String[] lootEntries) {
        List<WeightedRandomChestContent> ret = new ArrayList<WeightedRandomChestContent>();
        //Property lootCategory = config.getCategory("loot").get(category);

        for (String lootEntry: lootEntries) {
        	WeightedRandomChestContent data = LootConfigHelper.fromLootString(lootEntry);
        	if (data != null) {
        		ret.add(data);
        	}
        }
        return ret;
    }
}
