package com.coxbankviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Varbits;

import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.ui.overlay.OverlayManager;

import com.google.inject.Provides;

@Slf4j
@PluginDescriptor(
		name = "CoX Bank"
)
public class CoxBankPlugin extends Plugin
{
	private static final int PRIVATE_STORAGE_CONTAINER_ID = 583;

	private static final Set<Integer> SUPPLY_ITEM_IDS = new HashSet<>(Arrays.asList(
			// === Food (no doses) ===
			385,    // Shark
			397,	// Sea turtle
			7946,   // Monkfish
			391,    // Manta ray
			13441,  // Anglerfish
			11936,  // Dark crab
			3144,   // Karambwan (cooked)
			373,    // Cooked swordfish
			32352,  // Cooked Marlin
			32336,  // Cooked Halibut
			29143,  // Cooked moonlight antelope
			29140,  // Cooked sunlight antelope
			7060,   // Tuna potato
			7208, 7210,       // Cooked wild pie
			7218, 7220,       // Cooked summer pie
			4423, 4421, 4419, 4417,   // Guthix rest
			7056,   // Egg potato
			19662, 19659,     // Botanical pie
			361,    // Tuna
			379,    // Lobster
			32344,  //Bluefin
			32328,   //Yellofin
			32320,    //Haddock

			// === Prayer / restore potions (4,3,2,1 doses) ===
			2434, 139, 141, 143,               // Prayer potion
			3024, 3026, 3028, 3030,            // Super restore
			10925, 10927, 10929, 10931,        // Sanfew serum

			// === Other regular potions (4,3,2,1 doses) ===
			6685, 6687, 6689, 6691,            // Saradomin brew
			30884, 30881, 30878, 30875,        // Surge potion
			12701, 12699, 12697, 12695,        // Super combat potion
			23694, 23691, 23688, 23685,        // Divine super combat potion
			173, 171, 169, 2444,               // Ranging potion
			23742, 23739, 23736, 23733,        // Divine ranging potion
			12631, 12629, 12627, 12625,        // Stamina potion
			31647, 31644, 31641, 31638,        // Extended stamina potion
			3014, 3012, 3010, 3008,            // Energy potion
			3022, 3020, 3018, 3016,            // Super energy potion
			5958, 5956, 5954, 5952,            // Antidote++
			29833, 29830, 29827, 29824,        // Extended antivenom+
			12913, 12915, 12917, 12919,        // Anti-venom+
			12905, 12907, 12909, 12911,        // Anti-venom
			22470, 22467, 22464, 22461,        // Bastion potion
			24644, 24641, 24638, 24635,        // Divine bastion potion
			30134, 30131, 30128, 30125,        // Prayer regeneration potion
			27211, 27208, 27205, 27202,        // Menaphite remedy
			167, 165, 163, 2442,               // Super defence potion
			149, 147, 145, 2436,               // Super attack potion
			119, 117, 115, 113,                // Super strength potion
			185, 183, 181, 2448,               // Superantipoison

			// === CoX raid potions (- / normal / + tiers, 4,3,2,1 doses each) ===
			20973, 20974, 20975, 20976,        // Xeric's aid (-)
			20977, 20978, 20979, 20980,        // Xeric's aid
			20981, 20982, 20983, 20984,        // Xeric's aid (+)
			20961, 20962, 20963, 20964,        // Prayer enhance (-)
			20965, 20966, 20967, 20968,        // Prayer enhance
			20969, 20970, 20971, 20972,        // Prayer enhance (+)
			20913, 20914, 20915, 20916,        // Elder (-)
			20917, 20918, 20919, 20920,        // Elder
			20921, 20922, 20923, 20924,        // Elder (+)
			20937, 20938, 20939, 20940,        // Kodai (-)
			20941, 20942, 20943, 20944,        // Kodai
			20945, 20946, 20947, 20948,        // Kodai (+)
			20925, 20926, 20927, 20928,        // Twisted (-)
			20929, 20930, 20931, 20932,        // Twisted
			20933, 20934, 20935, 20936,        // Twisted (+)
			25754, 25755, 25756, 25757,        // Antipoison (-) [CoX]
			25758, 25759, 25760, 25761,        // Antipoison [CoX]
			25762, 25763, 25764, 25765,        // Antipoison (+) [CoX]
			20985, 20986, 20987, 20988,        // Overload (-)
			20989, 20990, 20991, 20992,        // Overload [CoX]
			20993, 20994, 20995, 20996,        // Overload (+)
			20949, 20950, 20951, 20952,        // Revitalisation (-)
			20953, 20954, 20955, 20956,        // Revitalisation
			20957, 20958, 20959, 20960         // Revitalisation (+)
	));

	private boolean bankOpen = false;
	private boolean inRaid = false;
	private boolean raidStarted = false;

	private List<Item> privateStorage = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CoxBankOverlay overlay;

	@Inject
	private CoxBankPanelOverlay panelOverlay;

	@Inject
	private CoxBankConfig config;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(panelOverlay);
		overlay.registerMouseListener();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(panelOverlay);
		overlay.unregisterMouseListener();
	}

	@Provides
	CoxBankConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxBankConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		boolean nowInRaid = client.getVarbitValue(Varbits.IN_RAID) == 1;

		if (nowInRaid != inRaid)
		{
			inRaid = nowInRaid;

			log.debug("In raid changed: {}", inRaid);

			if (!inRaid)
			{
				raidStarted = false;
				bankOpen = false;
				privateStorage = new ArrayList<>();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION
				&& e.getMessage().contains("The raid has begun!"))
		{
			raidStarted = true;
			log.debug("CoX raid started");
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e)
	{
		if (e.getContainerId() == PRIVATE_STORAGE_CONTAINER_ID)
		{
			List<Item> updated = new ArrayList<>();
			for (Item item : e.getItemContainer().getItems())
			{
				if (item.getId() != -1 && item.getQuantity() > 0)
				{
					updated.add(item);
				}
			}
			privateStorage = updated;
			log.debug("Private storage saved: {} items", privateStorage.size());
		}
	}

	public boolean isRaidStarted()
	{
		return raidStarted;
	}

	public boolean isBankOpen()
	{
		return bankOpen;
	}

	public void toggleBank()
	{
		bankOpen = !bankOpen;
		log.debug("Bank open: {}", bankOpen);
	}

	public List<Item> getPrivateStorage()
	{
		if (!config.suppliesOnly())
		{
			return privateStorage;
		}

		List<Item> filtered = new ArrayList<>();
		for (Item item : privateStorage)
		{
			if (SUPPLY_ITEM_IDS.contains(item.getId()))
			{
				filtered.add(item);
			}
		}
		return filtered;
	}
}