package com.cheeseum.lootadjuster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

@Mod( modid=LootAdjuster.MODID, name="Dungeon Loot Chest Adjuster", version=LootAdjuster.VERSION, dependencies="after:*")
public class LootAdjuster
{
	public static final String MODID = "lootadjuster";
	public static final String VERSION = "@VERSION@";
	
	public static final Logger logger = LogManager.getFormatterLogger("LootAdjuster");
	
    private Configuration config;
    private boolean populateConfig = false;

    public LootAdjuster() {
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	File configFile = event.getSuggestedConfigurationFile();
    	if (!configFile.exists()) {
    		populateConfig = true;
    	}
        this.config = new Configuration(configFile);
        this.config.load();
        this.config.addCustomCategoryComment("lootgroups", "Groups of items to be inserted as loot.\n" +
        		"Format for Groups is S:GROUPNAME < ITEMS >, one item per line.\n" +
        		"Format for items is \"ModId:ItemId:ItemMeta,MinFreq,MaxFreq,Weight\"\n" +
        		"Weights are relative to the group, frequency of items in chests will be multiply with those in the group.");
        this.config.addCustomCategoryComment("loot", "Loot chest categories.\n" +
        		"Format for items in each category is as follows: \"ModId:ItemId:ItemMeta,MinFrequency,MaxFrequency,Weight\"\n" +
        		"Format for groups is \"<Group Name>,MinFrequency,MaxFrequency,Weight\"");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Populate the config with defaults if it didn't exist
        if (populateConfig) {
            LootAdjuster.logger.info("Populating configuration file with defaults!");

            Map<String, ChestGenHooks> chestInfo = ReflectionHelper.getPrivateValue(ChestGenHooks.class, null, "chestInfo");
            for (String category : chestInfo.keySet()) {
                List<String> itemEntries = new ArrayList<String>();

                ChestGenHooks cgh = chestInfo.get(category);
                List<WeightedRandomChestContent> contents = ReflectionHelper.getPrivateValue(ChestGenHooks.class, cgh, "contents");

                for (WeightedRandomChestContent item : contents) {
                	// TODO: option to ignore subclasses that specify custom behaivor
                    itemEntries.add(LootConfigHelper.toLootString(item)); 
                }
                
                Property lootEntry = new Property(category, itemEntries.toArray(new String[0]), Property.Type.STRING);
                this.config.getCategory("loot").put(category, lootEntry);
            }
            
            this.config.save();
        }
       
        // Temporarily holds the loot groups for use later
        Map<String, WeightedRandomChestContent[]> lootGroups = new HashMap<String, WeightedRandomChestContent[]>();
        
        // Parse the loot groups
        ConfigCategory lootGroupCategory = this.config.getCategory("lootgroups");
        for (String group: lootGroupCategory.keySet()) {
        	List<WeightedRandomChestContent> groupData = new ArrayList<WeightedRandomChestContent>();
        	for (String entry: lootGroupCategory.get(group).getStringList()) {
        		WeightedRandomChestContent d = LootConfigHelper.fromLootString(entry);
        		if (d != null) {
        			groupData.add(d);
        		}
        	}
        	
        	lootGroups.put(group, groupData.toArray(new WeightedRandomChestContent[groupData.size()]));
        }
        
        // Parse user-set loot entries for chest categories
        ConfigCategory lootCategory = this.config.getCategory("loot");
        for(String chestCategory : lootCategory.keySet()) {
            Property chestLoot = lootCategory.get(chestCategory);
            
            if (chestLoot.isList()) {
	            List<WeightedRandomChestContent> lootList = new ArrayList<WeightedRandomChestContent>();
	
	            for (String lootEntry: chestLoot.getStringList()) {
	            	if (lootEntry.replace("\"","").startsWith("!")) {
	            		// FIXME: move this to a function with more error handling
	            		String[] entryData = lootEntry.replace("\"","").split(",");
	            		String group = entryData[0].substring(1);
	            		if (lootGroups.containsKey(group)) {
	            			lootList.add(new WeightedRandomLootGroup(lootGroups.get(group),
	            					Integer.parseInt(entryData[1]),
	            					Integer.parseInt(entryData[2]),
	            					Integer.parseInt(entryData[3])));
	            		}
	            	} else {
		            	WeightedRandomChestContent data = LootConfigHelper.fromLootString(lootEntry);
		            	if (data != null) {
		            		lootList.add(data);
		            	}
	            	}
	            }
	            
	            // inject loot data into the chestgenhooks category
	            // there ARE public methods to remove and add items, but reflection lets us stright-up override them
	            ReflectionHelper.setPrivateValue(ChestGenHooks.class, ChestGenHooks.getInfo(chestCategory), lootList, "contents");
            }
        }
    }
    
    @NetworkCheckHandler
    public boolean checkNetworkMods(Map mods, Side side) {
    	return true; // server-side only mod
    }
}
