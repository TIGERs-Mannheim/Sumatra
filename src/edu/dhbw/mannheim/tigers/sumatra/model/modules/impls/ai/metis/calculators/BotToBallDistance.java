/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.07.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


/**
 * This {@link ACalculator} implementation calculates the distances of each bot to the ball, sorts the results and
 * stores them to {@link TacticalField#getTigersToBallDist()} or {@link TacticalField#getEnemiesToBallDist()} resp.
 * 
 * @author Gero
 */
public class BotToBallDistance extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ETeam	team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 */
	public BotToBallDistance(ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public List<BotDistance> calculate(AIInfoFrame curFrame)
	{
		final WorldFrame wf = curFrame.worldFrame;
		
		Map<Integer, ? extends TrackedBot> bots;
		if (team == ETeam.TIGERS)
		{
			bots = wf.tigerBots;
		} else
		{
			bots = wf.foeBots;
		}
		
		final List<BotDistance> distances = new ArrayList<BotDistance>(bots.size());
		for (TrackedBot bot : bots.values())
		{
			final float distanceToBall = AIMath.distancePP(bot.pos, wf.ball.pos);
			distances.add(new BotDistance(bot, distanceToBall));
		}
		
		Collections.sort(distances, BotDistance.ASCENDING);
		return distances;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
