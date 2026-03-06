package com.sigurd.enemyattackhighlighter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EnemyAttackHighlighterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EnemyAttackHighlighterPlugin.class);
		RuneLite.main(args);
	}
}
