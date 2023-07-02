/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.util.List;


/**
 * Determine which bots are weak (not ok) and visualize them
 */
public class WeakBotsCalc extends ACalculator
{
	@Getter
	private List<BotID> weakBots;


	@Override
	public void doCalc()
	{
		weakBots = weakBots();
		weakBots.forEach(this::annotateRobotAnnoyingly);
	}


	private List<BotID> weakBots()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> !bot.getRobotInfo().isHealthy())
				.sorted((bot1, bot2) -> Float.compare(bot1.getRobotInfo().getBatteryRelative(),
						bot2.getRobotInfo().getBatteryRelative()))
				.map(ITrackedBot::getBotId)
				.toList();
	}


	private void annotateRobotAnnoyingly(final BotID botId)
	{
		Color violet = new Color(0xff, 0x33, 0x99);
		Color violetAlpha = new Color(violet.getRed(), violet.getGreen(), violet.getBlue(), 160);
		Color limeAlpha = new Color(0x0, 0x66, 0xff, 160);
		IDrawableShape rotating = AnimatedCrosshair.aCrazyCrosshair(getWFrame().getBot(botId).getPos(),
				(float) Geometry.getBotRadius(),
				(float) Geometry.getBotRadius() + 100, 0.8f, Color.BLACK, violetAlpha, limeAlpha);

		getShapes(EAiShapesLayer.AI_WEAK_BOT).add(rotating);
	}
}
