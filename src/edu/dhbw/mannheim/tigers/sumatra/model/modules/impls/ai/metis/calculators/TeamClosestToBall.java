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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


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
	public TeamClosestToBall()
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ETeam calculate(AIInfoFrame curFrame)
	{
		TacticalField tacticalInfo = curFrame.tacticalInfo;
		BotDistance closestTiger = tacticalInfo.getTigerClosestToBall();
		BotDistance closestEnemy = tacticalInfo.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Float.MAX_VALUE, the current decision is that our opponents will !
		
		if (closestTiger.dist < closestEnemy.dist)
		{
			return ETeam.TIGERS;
		} else
		{
			return ETeam.OPPONENTS;
		}
	}
}
