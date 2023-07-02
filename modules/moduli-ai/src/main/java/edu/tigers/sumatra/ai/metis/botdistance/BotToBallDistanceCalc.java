/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.botdistance;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This {@link ACalculator} implementation calculates the distances of each bot to the ball and sorts the results.
 */
public class BotToBallDistanceCalc extends ACalculator
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.3")
	private static double ballLookahead = 0.3;

	@Getter
	private List<BotDistance> tigersToBallDist = Collections.emptyList();
	@Getter
	private List<BotDistance> opponentsToBallDist = Collections.emptyList();

	@Getter
	private BotDistance tigerClosestToBall;
	@Getter
	private BotDistance opponentClosestToBall;


	@Override
	public void doCalc()
	{
		tigersToBallDist = getBotDistances(ETeam.TIGERS);
		opponentsToBallDist = getBotDistances(ETeam.OPPONENTS);
		tigerClosestToBall = closest(tigersToBallDist);
		opponentClosestToBall = closest(opponentsToBallDist);
	}


	private BotDistance closest(List<BotDistance> distances)
	{
		return distances.stream().findFirst().orElse(BotDistance.NULL_BOT_DISTANCE);
	}


	private List<BotDistance> getBotDistances(ETeam team)
	{
		return getBots(team).stream()
				.map(this::createBotDistance)
				.sorted(Comparator.comparingDouble(BotDistance::getDist))
				.toList();
	}


	private BotDistance createBotDistance(final ITrackedBot bot)
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 predBallPos = getWFrame().getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector();
		double distanceToBall = Lines.segmentFromPoints(ballPos, predBallPos).distanceTo(bot.getBotKickerPos());
		return new BotDistance(bot.getBotId(), distanceToBall);
	}


	private Collection<ITrackedBot> getBots(ETeam team)
	{
		if (team == ETeam.TIGERS)
		{
			return getWFrame().getTigerBotsVisible().values();
		}
		return getWFrame().getOpponentBots().values();
	}
}
