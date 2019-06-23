/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.07.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This {@link ACalculator} implementation calculates the distances of each bot to the ball, sorts the results and
 * stores them to {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField#getTigersToBallDist()} or
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField#getEnemiesToBallDist()} resp.
 * 
 * @author Gero
 */
public class BotToBallDistanceCalc extends ACalculator
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
	public BotToBallDistanceCalc(final ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		IBotIDMap<? extends TrackedBot> bots = getBots(wFrame);
		
		
		final List<BotDistance> distances = new ArrayList<BotDistance>(bots.size());
		for (TrackedBot bot : bots.values())
		{
			final float distanceToBall = GeoMath.distancePP(bot.getPos(), wFrame.ball.getPos());
			distances.add(new BotDistance(bot, distanceToBall));
		}
		
		Collections.sort(distances, BotDistance.ASCENDING);
		
		if (team == ETeam.TIGERS)
		{
			newTacticalField.setTigersToBallDist(distances);
		} else
		{
			newTacticalField.setEnemiesToBallDist(distances);
		}
		
	}
	
	
	private BotIDMapConst<? extends TrackedBot> getBots(final WorldFrame wf)
	{
		if (team == ETeam.TIGERS)
		{
			return wf.tigerBotsVisible;
		}
		return wf.foeBots;
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final List<BotDistance> distances = new ArrayList<BotDistance>(0);
		
		if (team == ETeam.TIGERS)
		{
			newTacticalField.setTigersToBallDist(distances);
		} else
		{
			newTacticalField.setEnemiesToBallDist(distances);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
