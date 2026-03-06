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

	@ConfigItem(
		keyName = "multicombatOnly",
		name = "Multicombat only",
		description = "Only tag/highlight NPCs while in multicombat zones",
		position = 2
	)
	default boolean multicombatOnly()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 600
	)
	@ConfigItem(
		keyName = "tagDurationSeconds",
		name = "Tag duration (seconds)",
		description = "How long tags persist; set to 0 to keep tags until death/despawn",
		position = 3
	)
	default int tagDurationSeconds()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "debugTaggedNpcs",
		name = "Debug tagged NPCs",
		description = "Show debug text above tagged NPCs",
		position = 4
	)
	default boolean debugTaggedNpcs()
	{
		return false;
	}

	@ConfigItem(
		keyName = "enableWhitelist",
		name = "Enable whitelist",
		description = "Only tag NPCs listed in the whitelist CSV",
		position = 5
	)
	default boolean enableWhitelist()
	{
		return false;
	}

	@ConfigItem(
		keyName = "whitelistCsv",
		name = "Whitelist (CSV)",
		description = "Comma-separated NPC names, e.g. goblin, dust devil",
		position = 6
	)
	default String whitelistCsv()
	{
		return "";
	}

	@Range(
		min = 1,
		max = 64
	)
	@ConfigItem(
		keyName = "rangeTiles",
		name = "Range tiles",
		description = "Max distance before NPC is considered out of range",
		position = 7
	)
	default int rangeTiles()
	{
		return 15;
	}

	@Range(
		min = 1,
		max = 120
	)
	@ConfigItem(
		keyName = "outOfRangeSeconds",
		name = "Out of range seconds",
		description = "How long NPC can stay out of range before tag is removed",
		position = 8
	)
	default int outOfRangeSeconds()
	{
		return 3;
	}

}
