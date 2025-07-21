/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
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
	@Configurable(comment = "[mm] The distance at which opponents are considered close to their own PenArea and therefore completely ignored from any sort of defense", defValue = "500.0")
	private static double opponentsCloseToPenAreaDistance = 500.0;


	private final Supplier<DefenseBallThreat> defenseBallThreat;


	@Getter
	private List<DefenseBotThreatDefStrategyData> centerBackDefData;


	@Override
	public void doCalc()
	{
		centerBackDefData = getWFrame().getOpponentBots().values().stream()
				.filter(this::isNotCloseToPenArea)
				.filter(bot -> !getAiFrame().getKeeperOpponentId().equals(bot.getId()))
				.filter(this::noPassReceiver)
				.filter(this::notCloseToBall)
				.map(this::buildDefenseBotThreatDefStrategyData)
				.toList();

	}


	private boolean isNotCloseToPenArea(ITrackedBot opponent)
	{
		return Geometry.getPenaltyAreaTheir().distanceTo(opponent.getPos()) > opponentsCloseToPenAreaDistance;
	}


	private boolean noPassReceiver(final ITrackedBot bot)
	{
		final DefenseBallThreat ballThreat = defenseBallThreat.get();

		final Optional<ITrackedBot> passReceiver = ballThreat.getPassReceiver();
		return passReceiver.isEmpty() ||
				(!passReceiver.get().getBotId().equals(bot.getBotId()));
	}


	private boolean notCloseToBall(final ITrackedBot bot)
	{
		return defenseBallThreat.get().getPos().distanceTo(bot.getPos()) > opponentsCloseToBallDistance;
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

		return DefenseBotThreatDefStrategyData.create(
				EDefenseBotThreatDefStrategy.CENTER_BACK,
				bot,
				threatLine.getPathStart(),
				threatLine,
				protectionLine.orElse(null),
				defenseBallThreat.get()
		);
	}
}
