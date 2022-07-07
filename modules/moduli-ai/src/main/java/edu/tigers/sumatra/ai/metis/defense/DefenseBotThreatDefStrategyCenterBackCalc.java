/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Rate opponent bot threats based on shooting angle to goal.
 */
@RequiredArgsConstructor
public class DefenseBotThreatDefStrategyCenterBackCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "[mm] The distance at which opponents are considered close to ball", defValue = "350.0")
	private static double opponentsCloseToBallDistance = 350.0;

	private final Map<BotID, Hysteresis> opponentDangerZoneHysteresis = new HashMap<>();


	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<List<BotDistance>> opponentsToBallDist;


	@Getter
	private List<DefenseBotThreatDefStrategyData> centerBackDefData;


	@Override
	public void doCalc()
	{
		updateOpponentDangerZoneHystereses();

		centerBackDefData = getWFrame().getOpponentBots().values().stream()
				.filter(this::movingInDangerZone)
				.filter(this::noPassReceiver)
				.filter(this::notCloseToBall)
				.map(this::buildDefenseBotThreatDefStrategyData)
				.toList();

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


	private boolean movingInDangerZone(final ITrackedBot bot)
	{
		return opponentDangerZoneHysteresis.get(bot.getBotId()).isLower();
	}


	private boolean noPassReceiver(final ITrackedBot bot)
	{
		final DefenseBallThreat ballThreat = defenseBallThreat.get();

		final Optional<ITrackedBot> passReceiver = ballThreat.getPassReceiver();
		return passReceiver.isEmpty() ||
				(passReceiver.get().getBotId() != bot.getBotId());
	}


	private boolean notCloseToBall(final ITrackedBot bot)
	{
		return opponentsToBallDist.get().stream()
				.filter(d -> d.getDist() < opponentsCloseToBallDistance)
				.noneMatch(d -> d.getBotId().equals(bot.getBotId()));
	}


	private ILineSegment threatLineCenterBack(final ITrackedBot bot)
	{
		IVector2 pointInGoal = Geometry.getGoalOur().bisection(predictedOpponentPos(bot));

		return Lines.segmentFromPoints(bot.getPos(), pointInGoal);
	}


	private DefenseBotThreatDefStrategyData buildDefenseBotThreatDefStrategyData(ITrackedBot bot)
	{
		var threatLine = threatLineCenterBack(bot);
		var protectionLine = centerBackProtectionLine(threatLine, Geometry.getBotRadius() * 2);

		return new DefenseBotThreatDefStrategyData(
				bot.getBotId(),
				threatLine,
				protectionLine.orElse(null),
				threatLine.getStart(),
				bot.getVel(),
				protectionLine.map(ILineSegment::getStart).orElse(null),
				EDefenseBotThreatDefStrategy.CENTER_BACK
		);
	}

}
