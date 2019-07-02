/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Rate opponent bot threats based on shooting angle to goal.
 */
public class DefenseBotThreatCalc extends ADefenseThreatCalc
{
	private final DefenseThreatRater defenseThreatRater = new DefenseThreatRater();
	private final Map<BotID, Hysteresis> opponentDangerZoneHysteresis = new HashMap<>();


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		updateOpponentDangerZoneHystereses();

		final List<DefenseBotThreat> threats = getWFrame().getFoeBots().values().stream()
				.filter(this::movingInDangerZone)
				.filter(this::noPassReceiver)
				.filter(this::notCloseToBall)
				.map(this::defenseBotThreat)
				.sorted(Comparator.comparingDouble(DefenseBotThreat::getShootingAngle).reversed())
				.collect(Collectors.toList());

		newTacticalField.setDefenseBotThreats(threats);

		drawBotThreads(threats);
	}


	private boolean notCloseToBall(final ITrackedBot bot)
	{
		return getNewTacticalField().getEnemiesToBallDist().stream()
				.filter(d -> d.getDist() < 250)
				.noneMatch(d -> d.getBot().getBotId() == bot.getBotId());
	}


	private boolean noPassReceiver(final ITrackedBot bot)
	{
		final DefenseBallThreat ballThreat = getNewTacticalField().getDefenseBallThreat();
		return !ballThreat.getPassReceiver().isPresent()
				|| (ballThreat.getPassReceiver().get().getBotId() != bot.getBotId());
	}


	private void updateOpponentDangerZoneHystereses()
	{
		for (ITrackedBot bot : getWFrame().getFoeBots().values())
		{
			final Hysteresis hysteresis = opponentDangerZoneHysteresis.computeIfAbsent(bot.getBotId(),
					botID -> new Hysteresis(0, 1));
			hysteresis.setLowerThreshold(DefenseThreatRater.getDangerZoneX() - 100);
			hysteresis.setUpperThreshold(DefenseThreatRater.getDangerZoneX() + 100);
			hysteresis.update(predictedOpponentPos(bot).x());
		}
	}


	private DefenseBotThreat defenseBotThreat(ITrackedBot bot)
	{
		final ILineSegment threatLine = threatLine(bot);
		final ILineSegment protectionLine = centerBackProtectionLine(threatLine, Geometry.getBotRadius() * 2);
		double threatRating = defenseThreatRater.getThreatRating(getBall().getPos(), predictedOpponentPos(bot));
		return new DefenseBotThreat(bot, threatLine, protectionLine, threatRating);
	}


	private boolean movingInDangerZone(final ITrackedBot bot)
	{
		return opponentDangerZoneHysteresis.get(bot.getBotId()).isLower();
	}


	private IVector2 predictedOpponentPos(final ITrackedBot bot)
	{
		return bot.getPosByTime(DefenseConstants.getLookaheadBotThreats(bot.getVel().getLength()));
	}


	private ILineSegment threatLine(final ITrackedBot bot)
	{
		IVector2 pointInGoal = DefenseMath.getBisectionGoal(predictedOpponentPos(bot));

		return Lines.segmentFromPoints(bot.getPos(), pointInGoal);
	}


	private void drawBotThreads(final List<DefenseBotThreat> timeComparedThreats)
	{
		final List<IDrawableShape> defenseShapes = getNewTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.DEFENSE_BOT_THREATS);

		int threatId = 0;
		for (DefenseBotThreat threat : timeComparedThreats)
		{
			DrawableAnnotation angle = new DrawableAnnotation(threat.getPos(),
					String.format("-> %d <-%nAngle: %.2f", threatId++, threat.getShootingAngle()),
					Vector2.fromY(200));
			angle.withCenterHorizontally(true);
			defenseShapes.add(angle);
			drawThreat(threat);
		}
	}
}
