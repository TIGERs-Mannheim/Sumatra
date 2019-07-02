/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.botdistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This {@link ACalculator} implementation calculates the distances of each bot to the ball, sorts the results and
 * stores them to {@link TacticalField#getTigersToBallDist()} or
 * {@link TacticalField#getEnemiesToBallDist()} resp.
 */
public class BotToBallDistanceCalc extends ACalculator
{
	@Configurable(comment = "Lookahead for ball position", defValue = "0.3")
	private static double ballLookahead = 0.3;

	private final ETeam team;


	public BotToBallDistanceCalc(final ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
	}


	@Override
	public void doCalc()
	{
		final List<BotDistance> distances = new ArrayList<>();
		for (ITrackedBot bot : getBots())
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			IVector2 predBallPos = getWFrame().getBall().getTrajectory().getPosByTime(ballLookahead).getXYVector();
			double distanceToBall = Lines.segmentFromPoints(ballPos, predBallPos).distanceTo(bot.getBotKickerPos());
			distances.add(new BotDistance(bot, distanceToBall));
		}

		distances.sort(Comparator.comparingDouble(BotDistance::getDist));

		if (team == ETeam.TIGERS)
		{
			getNewTacticalField().setTigersToBallDist(distances);
		} else
		{
			getNewTacticalField().setEnemiesToBallDist(distances);
		}
	}


	private Collection<ITrackedBot> getBots()
	{
		if (team == ETeam.TIGERS)
		{
			return getWFrame().getTigerBotsVisible().values();
		}
		return getWFrame().getFoeBots().values();
	}
}
