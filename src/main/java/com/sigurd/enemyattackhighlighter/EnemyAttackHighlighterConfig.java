package com.sigurd.enemyattackhighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("enemyattackhighlighter")
public interface EnemyAttackHighlighterConfig extends Config
{
	@ConfigItem(
		keyName = "highlightMode",
		name = "Highlight mode",
		description = "Choose whether to draw outlines, tiles, or both",
		position = 0
	)
	default HighlightMode highlightMode()
	{
		return HighlightMode.BOTH;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for highlighted attackers",
		position = 1
	)
	default Color highlightColor()
	{
		return new Color(255, 80, 80, 180);
	}

	@Range(
		min = 1,
		max = 8
	)
	@ConfigItem(
		keyName = "outlineStrokeWidth",
		name = "Outline width",
		description = "Width of the attacker outline",
		position = 2
	)
	default int outlineStrokeWidth()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "ignoreDeadNpcs",
		name = "Ignore dead NPCs",
		description = "Skip dead NPCs when highlighting attackers",
		position = 3
	)
	default boolean ignoreDeadNpcs()
	{
		return true;
	}
}
