package cheeseum.lootadjuster;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.FMLLog;

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
        Property lootEntry = config.getCategory("loot").get(category);

        if (lootEntry.isList()) {
            for (String loot: lootEntry.getStringList()) {
                loot = loot.replace("\"", "");
                String genData[] = loot.split(",");
                String itemData[] = genData[0].split(":");

                if (genData.length < 4 || itemData.length < 1) {
                    FMLLog.severe("Improperly formatted loot config! Grues ahead!");
                    continue;
                }

                int itemId = Integer.parseInt(itemData[0]);
                int itemMeta = itemData.length > 1 ? Integer.parseInt(itemData[1]) : 0;
                    
                ret.add(
                    new WeightedRandomChestContent(
                        new ItemStack(itemId, 1, itemMeta),
                        Integer.parseInt(genData[1]),
                        Integer.parseInt(genData[2]),
                        Integer.parseInt(genData[3])
                    )
                );
            }
        }
        return ret;
    }

    public static String getLootString(WeightedRandomChestContent loot) {
        return String.format("\"%d:%d,%d,%d,%d\"", 
            loot.theItemId.itemID, loot.theItemId.getItemDamage(),
            loot.theMinimumChanceToGenerateItem,
            loot.theMaximumChanceToGenerateItem,
            loot.itemWeight
        );
    }
}
