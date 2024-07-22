/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Determine which bots are weak (not ok) and visualize them
 */
public class WeakBotsCalc extends ACalculator
{
	@Getter
	private List<BotID> weakBots;

	private static final Set<EFeature> botFeaturesToCheck = Set.of(EFeature.BARRIER, EFeature.DRIBBLER,
			EFeature.CHARGE_CAPS, EFeature.CHIP_KICKER, EFeature.STRAIGHT_KICKER, EFeature.ENERGETIC);

	private int yPos;


	@Override
	public void doCalc()
	{
		yPos = 8;
		weakBots = weakBots();
		weakBots.forEach(this::annotateRobotAnnoyingly);
		weakBots.forEach(this::annotateReasonForWeakness);
	}


	private List<BotID> weakBots()
	{
		// a bot that is completely out of battery, can not move any longer (no EFeature.MOVE) and is not available to AI,
		// therefore visible bots are used
		Set<ITrackedBot> weak = new HashSet<>();
		weak.addAll(getWFrame().getTigerBotsVisible().values().stream()
				.filter(bot -> !bot.getRobotInfo().isHealthy()).toList());
		weak.addAll(getWFrame().getTigerBotsVisible().values().stream()
				.filter(ITrackedBot::isMalFunctioning).toList());
		return weak.stream().sorted((bot1, bot2) -> Float.compare(bot1.getRobotInfo().getBatteryRelative(),
				bot2.getRobotInfo().getBatteryRelative())).map(ITrackedBot::getBotId).toList();
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


	private void annotateReasonForWeakness(final BotID botID)
	{
		ITrackedBot bot = getWFrame().getBot(botID);
		Map<EFeature, EFeatureState> botFeatures = bot.getRobotInfo().getBotFeatures();
		boolean malFunctioning = bot.isMalFunctioning();
		List<String> brokenFeatures = botFeaturesToCheck.stream()
				.filter(feat -> botFeatures.getOrDefault(feat, EFeatureState.UNKNOWN) == EFeatureState.KAPUT)
				.map(EFeature::getName)
				.toList();

		if (!brokenFeatures.isEmpty())
		{
			int x = botID.getTeamColor() == ETeamColor.YELLOW ? 1 : 57;
			String text = botID.getNumber() + botID.getTeamColor().toString().substring(0, 1) + " :";
			DrawableBorderText reasons = new DrawableBorderText(Vector2.fromXY(x, yPos), text);
			getShapes(EAiShapesLayer.AI_WEAK_BOT).add(reasons.setColor(botID.getTeamColor().getColor()));
			yPos += 1;
			for (String feat : brokenFeatures)
			{
				getShapes(EAiShapesLayer.AI_WEAK_BOT).add(new DrawableBorderText(Vector2.fromXY(x, yPos), feat)
						.setColor(Color.RED));
				yPos += 1;
			}
			if (malFunctioning)
			{
				getShapes(EAiShapesLayer.AI_WEAK_BOT).add(
						new DrawableBorderText(Vector2.fromXY(x, yPos), "MAL_FUNCTION").setColor(Color.RED));
				yPos += 1;
			}
		}
	}
}
