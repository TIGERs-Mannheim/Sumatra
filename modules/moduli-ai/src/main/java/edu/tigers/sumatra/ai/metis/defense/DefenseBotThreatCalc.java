/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Rate opponent bot threats based on shooting angle to goal.
 */
@RequiredArgsConstructor
public class DefenseBotThreatCalc extends ADefenseThreatCalc
{
	private final DefenseThreatRater defenseThreatRater = new DefenseThreatRater();
	private final Map<BotID, Hysteresis> opponentDangerZoneHysteresis = new HashMap<>();


	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<List<BotDistance>> opponentsToBallDist;


	@Getter
	private List<DefenseBotThreat> defenseBotThreats;


	@Override
	public void doCalc()
	{
		updateOpponentDangerZoneHystereses();

		defenseBotThreats = getWFrame().getOpponentBots().values().stream()
				.filter(this::movingInDangerZone)
				.filter(this::noPassReceiver)
				.filter(this::notCloseToBall)
				.map(this::defenseBotThreat)
				.sorted(Comparator.comparingDouble(DefenseBotThreat::getThreatRating).reversed())
				.collect(Collectors.toUnmodifiableList());

		drawBotThreads(defenseBotThreats);
	}


	private void updateOpponentDangerZoneHystereses()
	{
		for (ITrackedBot bot : getWFrame().getOpponentBots().values())
		{
			final Hysteresis hysteresis = opponentDangerZoneHysteresis.computeIfAbsent(bot.getBotId(),
					botID -> new Hysteresis(0, 1));
			hysteresis.setLowerThreshold(DefenseThreatRater.getDangerZoneX() - 100);
			hysteresis.setUpperThreshold(DefenseThreatRater.getDangerZoneX() + 100);
			hysteresis.update(predictedOpponentPos(bot).x());
		}
	}


	private IVector2 predictedOpponentPos(final ITrackedBot bot)
	{
		return bot.getPosByTime(DefenseConstants.getLookaheadBotThreats(bot.getVel().getLength()));
	}


	private boolean movingInDangerZone(final ITrackedBot bot)
	{
		return opponentDangerZoneHysteresis.get(bot.getBotId()).isLower();
	}


	private boolean noPassReceiver(final ITrackedBot bot)
	{
		final DefenseBallThreat ballThreat = defenseBallThreat.get();

		return ballThreat.getPassReceiver().isEmpty() ||
				(ballThreat.getPassReceiver().get().getBotId() != bot.getBotId());
	}


	private boolean notCloseToBall(final ITrackedBot bot)
	{
		return opponentsToBallDist.get().stream()
				.filter(d -> d.getDist() < 350)
				.noneMatch(d -> d.getBotId() == bot.getBotId());
	}


	private DefenseBotThreat defenseBotThreat(ITrackedBot bot)
	{
		final ILineSegment threatLine = threatLine(bot);
		final ILineSegment protectionLine = centerBackProtectionLine(threatLine, Geometry.getBotRadius() * 2);
		double threatRating = defenseThreatRater
				.getThreatRating(getBall().getPos(), predictedOpponentPos(bot));
		return new DefenseBotThreat(bot, threatLine, protectionLine, threatRating);
	}


	private ILineSegment threatLine(final ITrackedBot bot)
	{
		IVector2 pointInGoal = Geometry.getGoalOur().bisection(predictedOpponentPos(bot));

		return Lines.segmentFromPoints(bot.getPos(), pointInGoal);
	}


	private void drawBotThreads(final List<DefenseBotThreat> timeComparedThreats)
	{
		int threatId = 0;
		for (DefenseBotThreat threat : timeComparedThreats)
		{
			DrawableAnnotation angle = new DrawableAnnotation(threat.getPos(),
					String.format("-> %d <-%nAngle: %.2f", threatId++, threat.getThreatRating()),
					Vector2.fromY(200));
			angle.withCenterHorizontally(true);
			getShapes(EAiShapesLayer.DEFENSE_BOT_THREATS).add(angle);
			drawThreat(threat);
		}
	}
}
