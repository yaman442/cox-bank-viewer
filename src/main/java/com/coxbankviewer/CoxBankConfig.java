package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("coxbank")
public interface CoxBankConfig extends Config
{
	@ConfigItem(
			keyName = "suppliesOnly",
			name = "Supplies only",
			description = "Only show food, potions, and other consumables in the CoX storage panel"
	)
	default boolean suppliesOnly()
	{
		return false;
	}
}