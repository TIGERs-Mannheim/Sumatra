/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This calculator determines which Team is closer to the ball.
 * 
 * @author FlorianS, Gero
 */
public class TeamClosestToBall extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TeamClosestToBall()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final TacticalField tacticalInfo = curFrame.tacticalInfo;
		final BotDistance closestTiger = tacticalInfo.getTigerClosestToBall();
		final BotDistance closestEnemy = tacticalInfo.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Float.MAX_VALUE, the current decision is that our opponents will !
		
		ETeam closestTeam;
		if (closestTiger.getDist() < closestEnemy.getDist())
		{
			closestTeam = ETeam.TIGERS;
		} else
		{
			closestTeam = ETeam.OPPONENTS;
		}
		curFrame.tacticalInfo.setTeamClosestToBall(closestTeam);
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setTeamClosestToBall(ETeam.UNKNOWN);
	}
}
