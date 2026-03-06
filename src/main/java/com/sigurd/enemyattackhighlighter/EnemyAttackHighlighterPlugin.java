package com.sigurd.enemyattackhighlighter;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Enemy Attack Highlighter",
	description = "Highlights NPCs that are currently attacking you",
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

	private final Set<NPC> attackers = new HashSet<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		refreshAttackers();
		log.debug("Enemy Attack Highlighter started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		attackers.clear();
		log.debug("Enemy Attack Highlighter stopped");
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!(event.getSource() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getSource();
		if (isAttackingLocalPlayer(npc))
		{
			attackers.add(npc);
		}
		else
		{
			attackers.remove(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		attackers.remove(event.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			attackers.clear();
			return;
		}

		refreshAttackers();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		refreshAttackers();
	}

	Set<NPC> getAttackers()
	{
		return Collections.unmodifiableSet(attackers);
	}

	private void refreshAttackers()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			attackers.clear();
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			attackers.clear();
			return;
		}

		Set<NPC> currentlyAttacking = new HashSet<>();
		for (NPC npc : client.getNpcs())
		{
			if (isAttackingLocalPlayer(npc))
			{
				currentlyAttacking.add(npc);
			}
		}

		attackers.clear();
		attackers.addAll(currentlyAttacking);
	}

	private boolean isAttackingLocalPlayer(NPC npc)
	{
		if (npc == null)
		{
			return false;
		}

		if (config.ignoreDeadNpcs() && npc.isDead())
		{
			return false;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return false;
		}

		Actor interacting = npc.getInteracting();
		return interacting == localPlayer;
	}

	@Provides
	EnemyAttackHighlighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EnemyAttackHighlighterConfig.class);
	}
}
