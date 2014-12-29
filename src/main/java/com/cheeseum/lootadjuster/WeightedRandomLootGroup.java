package com.cheeseum.lootadjuster;

import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public class WeightedRandomLootGroup extends WeightedRandomChestContent {
	public WeightedRandomChestContent[] weightedLoot;
	
	public WeightedRandomLootGroup(WeightedRandomChestContent[] entries, int minWeight, int maxWeight, int weight) {
		super(entries[0].theItemId, minWeight, maxWeight, weight);
		this.weightedLoot = entries;
	}

	@Override
	protected ItemStack[] generateChestContent(Random random, IInventory newInventory) {
		// pick a random item from the group
		WeightedRandomChestContent randomLoot = (WeightedRandomChestContent)WeightedRandom.getRandomItem(random, this.weightedLoot);
		return ChestGenHooks.generateStacks(random, randomLoot.theItemId, this.theMinimumChanceToGenerateItem, this.theMaximumChanceToGenerateItem);
	}
}
