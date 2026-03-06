package com.sigurd.enemyattackhighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class EnemyAttackHighlighterOverlay extends Overlay
{
	private final Client client;
	private final EnemyAttackHighlighterPlugin plugin;
	private final EnemyAttackHighlighterConfig config;

	@Inject
	public EnemyAttackHighlighterOverlay(
		Client client,
		EnemyAttackHighlighterPlugin plugin,
		EnemyAttackHighlighterConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Set<NPC> attackers = plugin.getAttackers();
		if (attackers.isEmpty())
		{
			return null;
		}

		HighlightMode mode = config.highlightMode();
		Color color = config.highlightColor();

		for (NPC npc : attackers)
		{
			if (npc == null)
			{
				continue;
			}

			if (mode == HighlightMode.OUTLINE || mode == HighlightMode.BOTH)
			{
				Shape hull = npc.getConvexHull();
				if (hull != null)
				{
					graphics.setStroke(new BasicStroke(config.outlineStrokeWidth()));
					OverlayUtil.renderPolygon(graphics, hull, color);
				}
			}

			if (mode == HighlightMode.TILE || mode == HighlightMode.BOTH)
			{
				LocalPoint lp = npc.getLocalLocation();
				if (lp == null)
				{
					continue;
				}

				Polygon tile = Perspective.getCanvasTilePoly(client, lp);
				if (tile != null)
				{
					OverlayUtil.renderPolygon(graphics, tile, color);
				}
			}
		}

		return null;
	}
}
