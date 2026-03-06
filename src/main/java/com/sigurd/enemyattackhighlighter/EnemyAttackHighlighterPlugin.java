package com.sigurd.enemyattackhighlighter;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Enemy Attack Highlighter",
	description = "Highlights NPCs you have tagged",
	tags = {"npc", "combat", "highlight", "attackers", "aggro"}
)
public class EnemyAttackHighlighterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EnemyAttackHighlighterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EnemyAttackHighlighterOverlay overlay;

	private final Set<NPC> taggedNpcs = new HashSet<>();
	private final Map<NPC, Integer> taggedAtTick = new WeakHashMap<>();
	private final Map<NPC, Integer> lastTagTick = new WeakHashMap<>();
	private final Map<NPC, Integer> outOfRangeSinceTick = new WeakHashMap<>();
	private int tickCount = 0;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		clearTags();
		log.debug("Enemy Attack Highlighter started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clearTags();
		log.debug("Enemy Attack Highlighter stopped");
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		removeTag(event.getNpc());
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!isPluginActiveInCurrentZone())
		{
			return;
		}

		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		if (event.getHitsplat() == null || !event.getHitsplat().isMine())
		{
			return;
		}

		addTag((NPC) event.getActor());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			clearTags();
			return;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		tickCount++;
		cleanupTags();
	}

	Set<NPC> getAttackers()
	{
		return Collections.unmodifiableSet(taggedNpcs);
	}

	int getSecondsRemaining(NPC npc)
	{
		int durationSeconds = config.tagDurationSeconds();
		if (durationSeconds <= 0)
		{
			return -1;
		}

		Integer taggedTick = taggedAtTick.get(npc);
		if (taggedTick == null)
		{
			return 0;
		}

		int durationTicks = durationSecondsToTicks(durationSeconds);
		int remainingTicks = Math.max(0, durationTicks - (tickCount - taggedTick));
		return (int) Math.ceil(remainingTicks * 0.6d);
	}

	int getRetagCooldownSeconds()
	{
		int durationSeconds = config.tagDurationSeconds();
		if (durationSeconds <= 0)
		{
			return -1;
		}

		return durationSeconds / 2;
	}

	int getRetagCooldownRemainingSeconds(NPC npc)
	{
		int cooldownSeconds = getRetagCooldownSeconds();
		if (cooldownSeconds < 0)
		{
			return taggedNpcs.contains(npc) ? -1 : 0;
		}

		if (cooldownSeconds == 0)
		{
			return 0;
		}

		Integer lastTick = lastTagTick.get(npc);
		if (lastTick == null)
		{
			return 0;
		}

		int cooldownTicks = durationSecondsToTicks(cooldownSeconds);
		int remainingTicks = Math.max(0, cooldownTicks - (tickCount - lastTick));
		return (int) Math.ceil(remainingTicks * 0.6d);
	}

	private void addTag(NPC npc)
	{
		if (npc == null || npc.isDead())
		{
			return;
		}

		if (!isNpcAllowedByWhitelist(npc))
		{
			return;
		}

		if (isNpcOutOfRange(npc))
		{
			return;
		}

		if (isRetagOnCooldown(npc))
		{
			return;
		}

		taggedNpcs.add(npc);
		taggedAtTick.put(npc, tickCount);
		lastTagTick.put(npc, tickCount);
		outOfRangeSinceTick.remove(npc);
	}

	private void removeTag(NPC npc)
	{
		taggedNpcs.remove(npc);
		taggedAtTick.remove(npc);
		outOfRangeSinceTick.remove(npc);
	}

	private void clearTags()
	{
		taggedNpcs.clear();
		taggedAtTick.clear();
		outOfRangeSinceTick.clear();
	}

	private void cleanupTags()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			clearTags();
			return;
		}

		if (!isPluginActiveInCurrentZone())
		{
			clearTags();
			return;
		}

		Iterator<NPC> iterator = taggedNpcs.iterator();
		while (iterator.hasNext())
		{
			NPC npc = iterator.next();
			if (npc == null || npc.isDead())
			{
				iterator.remove();
				removeTagState(npc);
				continue;
			}

			if (!isNpcAllowedByWhitelist(npc))
			{
				iterator.remove();
				removeTagState(npc);
				continue;
			}

			if (isOutOfRangeExpired(npc))
			{
				iterator.remove();
				removeTagState(npc);
				continue;
			}

			if (isTagExpired(npc))
			{
				iterator.remove();
				removeTagState(npc);
			}
		}
	}

	private void removeTagState(NPC npc)
	{
		taggedAtTick.remove(npc);
		outOfRangeSinceTick.remove(npc);
	}

	private boolean isTagExpired(NPC npc)
	{
		int durationSeconds = config.tagDurationSeconds();
		if (durationSeconds <= 0)
		{
			return false;
		}

		Integer taggedTick = taggedAtTick.get(npc);
		if (taggedTick == null)
		{
			return true;
		}

		return tickCount - taggedTick >= durationSecondsToTicks(durationSeconds);
	}

	private int durationSecondsToTicks(int durationSeconds)
	{
		return Math.max(1, (int) Math.ceil(durationSeconds / 0.6d));
	}

	private boolean isRetagOnCooldown(NPC npc)
	{
		int cooldownSeconds = getRetagCooldownSeconds();
		if (cooldownSeconds < 0)
		{
			return taggedNpcs.contains(npc);
		}

		if (cooldownSeconds == 0)
		{
			return false;
		}

		Integer lastTick = lastTagTick.get(npc);
		if (lastTick == null)
		{
			return false;
		}

		return tickCount - lastTick < durationSecondsToTicks(cooldownSeconds);
	}

	private boolean isOutOfRangeExpired(NPC npc)
	{
		if (!isNpcOutOfRange(npc))
		{
			outOfRangeSinceTick.remove(npc);
			return false;
		}

		Integer outSince = outOfRangeSinceTick.get(npc);
		if (outSince == null)
		{
			outOfRangeSinceTick.put(npc, tickCount);
			return false;
		}

		return tickCount - outSince >= durationSecondsToTicks(config.outOfRangeSeconds());
	}

	private boolean isNpcOutOfRange(NPC npc)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return false;
		}

		LocalPoint playerLocation = localPlayer.getLocalLocation();
		LocalPoint npcLocation = npc.getLocalLocation();
		if (playerLocation == null || npcLocation == null)
		{
			return true;
		}

		int deltaX = Math.abs(playerLocation.getX() - npcLocation.getX()) / 128;
		int deltaY = Math.abs(playerLocation.getY() - npcLocation.getY()) / 128;
		int tileDistance = Math.max(deltaX, deltaY);
		return tileDistance > config.rangeTiles();
	}

	private boolean isPluginActiveInCurrentZone()
	{
		if (!config.multicombatOnly())
		{
			return true;
		}

		return client.getVarbitValue(Varbits.MULTICOMBAT_AREA) == 1;
	}

	private boolean isNpcAllowedByWhitelist(NPC npc)
	{
		if (!config.enableWhitelist())
		{
			return true;
		}

		String npcName = normalizeName(npc.getName());
		if (npcName.isEmpty())
		{
			return false;
		}

		String whitelistCsv = config.whitelistCsv();
		if (whitelistCsv == null || whitelistCsv.trim().isEmpty())
		{
			return false;
		}

		for (String rawEntry : whitelistCsv.split(","))
		{
			String entry = normalizeName(rawEntry);
			if (!entry.isEmpty() && entry.equals(npcName))
			{
				return true;
			}
		}

		return false;
	}

	private String normalizeName(String name)
	{
		if (name == null)
		{
			return "";
		}

		return name.trim().toLowerCase(Locale.ENGLISH);
	}

	@Provides
	EnemyAttackHighlighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EnemyAttackHighlighterConfig.class);
	}
}
