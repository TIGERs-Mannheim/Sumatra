/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.ETeamSpecRefCmd;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * Calculator for checking for situations, where a bot is not allowed to touch the ball a second time.
 * This is especially true in a free kick situation.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BotNotAllowedToTouchBall extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// Logger
	private EState	state					= EState.INACTIVE;
	private long	lastRefereeCmdId	= -1;
	
	private enum EState
	{
		WAIT_FIRST,
		WAIT_SECOND,
		INACTIVE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calculate botNotAllowedToTouchBall
	 * 
	 * @param curFrame
	 * @param preFrame
	 */
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// keep, if nothing happens
		BotID botNotAllowedToTouchBall = preFrame.tacticalInfo.getBotNotAllowedToTouchBall();
		BotID curBotLastTouchedBall = curFrame.tacticalInfo.getBotLastTouchedBall();
		BotID preBotLastTouchedBall = preFrame.tacticalInfo.getBotLastTouchedBall();
		
		// catch special case: no referee msg received yet at all
		if (curFrame.refereeMsgCached == null)
		{
			curFrame.tacticalInfo.setBotNotAllowedToTouchBall(botNotAllowedToTouchBall);
			return;
		}
		
		// check for a relevant message
		if ((state == EState.INACTIVE)
				&& (lastRefereeCmdId != curFrame.refereeMsgCached.getCommandCounter())
				&& ((curFrame.refereeMsgCached.getTeamSpecRefCmd() == ETeamSpecRefCmd.DirectFreeKickTigers) || (curFrame.refereeMsgCached
						.getTeamSpecRefCmd() == ETeamSpecRefCmd.KickOffTigers)))
		{
			// activated
			state = EState.WAIT_FIRST;
			// do not react on the same referee msg again
			lastRefereeCmdId = curFrame.refereeMsgCached.getCommandCounter();
		}
		
		if (state == EState.WAIT_SECOND)
		{
			// if botLastTouchedBall changed
			if (!curBotLastTouchedBall.equals(preBotLastTouchedBall))
			{
				botNotAllowedToTouchBall = new BotID();
				state = EState.INACTIVE;
			}
		}
		
		if (state == EState.WAIT_FIRST)
		{
			// if botLastTouchedBall changed
			if (!curBotLastTouchedBall.equals(preBotLastTouchedBall))
			{
				botNotAllowedToTouchBall = new BotID(curBotLastTouchedBall);
				state = EState.WAIT_SECOND;
			}
			
		}
		
		curFrame.tacticalInfo.setBotNotAllowedToTouchBall(botNotAllowedToTouchBall);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setBotNotAllowedToTouchBall(new BotID());
	}
}
