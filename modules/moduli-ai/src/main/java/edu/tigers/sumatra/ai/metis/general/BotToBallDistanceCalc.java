/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This {@link ACalculator} implementation calculates the distances of each bot to the ball, sorts the results and
 * stores them to {@link edu.tigers.sumatra.ai.data.TacticalField#getTigersToBallDist()} or
 * {@link edu.tigers.sumatra.ai.data.TacticalField#getEnemiesToBallDist()} resp.
 * 
 * @author Gero
 */
public class BotToBallDistanceCalc extends ACalculator
{
	private final ETeam team;
	
	
	/**
	 * @param team
	 */
	public BotToBallDistanceCalc(final ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		IBotIDMap<? extends ITrackedBot> bots = getBots(wFrame);
		
		
		final List<BotDistance> distances = new ArrayList<>(bots.size());
		for (ITrackedBot bot : bots.values())
		{
			final double distanceToBall = VectorMath.distancePP(bot.getPos(), wFrame.getBall().getPos());
			distances.add(new BotDistance(bot, distanceToBall));
		}
		
		distances.sort(BotDistance.ASCENDING);
		
		if (team == ETeam.TIGERS)
		{
			newTacticalField.setTigersToBallDist(distances);
		} else
		{
			newTacticalField.setEnemiesToBallDist(distances);
		}
		
	}
	
	
	private BotIDMapConst<? extends ITrackedBot> getBots(final WorldFrame wf)
	{
		if (team == ETeam.TIGERS)
		{
			return wf.getTigerBotsVisible();
		}
		return wf.getFoeBots();
	}
}
