package cheeseum.lootadjuster;

import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import net.minecraft.util.WeightedRandomChestContent;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.FMLLog;

import cpw.mods.fml.relauncher.ReflectionHelper;

@Mod( modid = "LootAdjuster", name = "Dungeon Loot Chest Adjuster", version = "1.1" )
@NetworkMod(clientSideRequired=false, serverSideRequired=true)
public class mod_LootAdjuster
{
    private Configuration config;

    public mod_LootAdjuster() {
    }
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        this.config.load();
        this.config.addCustomCategoryComment("loot", "Format for items in each category is as follows: \"ItemId:ItemMeta,MinFrequency,MaxFrequency,Weight\". Non-existant categories are ignored.");
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
        // Repopulate the config with defaults if instructed
        Property resetDefaults = this.config.get(Configuration.CATEGORY_GENERAL, "resetDefaults", false, "Resets the file with default loot values");
        if (resetDefaults.getBoolean(false)) {
            FMLLog.info("Repopulating configuration file with defaults!");

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
        resetDefaults.set(false);
        
        for(String category : LootConfigHelper.getLootCategories(this.config)) {
            List<WeightedRandomChestContent> lootEntries = LootConfigHelper.getLootForCategory(this.config, category);

            ReflectionHelper.setPrivateValue(ChestGenHooks.class, ChestGenHooks.getInfo(category), lootEntries, "contents");
        }

        this.config.save();
    }
}
