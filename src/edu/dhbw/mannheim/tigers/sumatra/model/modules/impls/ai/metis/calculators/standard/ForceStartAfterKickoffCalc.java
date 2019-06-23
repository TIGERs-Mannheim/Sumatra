/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.standard;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * Calculates if this is a force start after a kickoff
 */
public class ForceStartAfterKickoffCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean	forceStartAfterKickoffEnemies;
	private boolean	kickOff;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ForceStartAfterKickoffCalc()
	{
		forceStartAfterKickoffEnemies = false;
		kickOff = false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		if (curFrame.refereeMsgCached == null)
		{
			return;
		}
		if ((curFrame.refereeMsgCached.getCommand() == Command.PREPARE_KICKOFF_BLUE)
				|| (curFrame.refereeMsgCached.getCommand() == Command.PREPARE_KICKOFF_YELLOW))
		{
			kickOff = true;
		} else if ((curFrame.refereeMsgCached.getCommand() == Command.FORCE_START))
		{
			if (kickOff)
			{
				forceStartAfterKickoffEnemies = true;
			} else
			{
				forceStartAfterKickoffEnemies = false;
			}
			kickOff = false;
		} else
		{
			kickOff = false;
			forceStartAfterKickoffEnemies = false;
		}
		curFrame.tacticalInfo.setForceStartAfterKickoffEnemies(forceStartAfterKickoffEnemies);
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
