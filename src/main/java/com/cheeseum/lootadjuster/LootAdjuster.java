package com.cheeseum.lootadjuster;

import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import net.minecraft.util.WeightedRandomChestContent;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.FMLLog;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "LootAdjuster", name = "Dungeon Loot Chest Adjuster", version = "@VERSION@", dependencies = "after:*")
public class LootAdjuster
{
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
        this.config.addCustomCategoryComment("loot", "Format for items in each category is as follows: \"ItemId:ItemMeta,MinFrequency,MaxFrequency,Weight\". Non-existant categories are ignored.");
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
                    itemEntries.add(LootConfigHelper.getLootString(item)); 
                }
                
                Property lootEntry = new Property(category, itemEntries.toArray(new String[0]), Property.Type.STRING);
                this.config.getCategory("loot").put(category, lootEntry);
            }
        }
        
        for(String category : LootConfigHelper.getLootCategories(this.config)) {
            List<WeightedRandomChestContent> lootEntries = LootConfigHelper.getLootForCategory(this.config, category);

            ReflectionHelper.setPrivateValue(ChestGenHooks.class, ChestGenHooks.getInfo(category), lootEntries, "contents");
        }

        this.config.save();
    }
    
    @NetworkCheckHandler
    public boolean checkNetworkMods(Map mods, Side side) {
    	return true; // server-side only mod
    }
}
