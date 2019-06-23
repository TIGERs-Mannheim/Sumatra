/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis.score;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.ETeamSpecRefCmd;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Scores if the bot is not allowed to touch the ball
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class DoubleTouchScore extends AScore
{
	private EState	state								= EState.INACTIVE;
	private long	lastRefereeCmdId				= -1;
	private BotID	botNotAllowedToTouchBall	= BotID.get();
	
	private enum EState
	{
		WAIT_FIRST,
		WAIT_SECOND,
		INACTIVE
	}
	
	
	@Override
	protected ScoreResult doCalcScore(final ITrackedBot tiger, final ARole role, final MetisAiFrame frame)
	{
		// in some situations, bots are not allowed to touch the ball more than twice
		boolean botNotAllowed2TouchBall = tiger.getBotId().equals(getBotNotAllowedToTouchBall(frame));
		if ((role.getType() == ERole.OFFENSIVE) && botNotAllowed2TouchBall)
		{
			return new ScoreResult(EUsefulness.BAD);
		}
		return ScoreResult.defaultResult();
	}
	
	
	private BotID getBotNotAllowedToTouchBall(final MetisAiFrame curFrame)
	{
		MetisAiFrame preAiFrame = curFrame.getPrevFrame();
		
		
		// keep, if nothing happens
		BotID curBotLastTouchedBall = curFrame.getTacticalField().getBotLastTouchedBall();
		BotID preBotLastTouchedBall = preAiFrame.getTacticalField().getBotLastTouchedBall();
		
		// catch special case: no referee msg received yet at all
		if (curFrame.getRefereeMsg() == null)
		{
			return BotID.get();
		}
		
		// check for a relevant message
		if ((state == EState.INACTIVE)
				&& (lastRefereeCmdId != curFrame.getRefereeMsg().getCommandCounter())
				&& ((curFrame.getRefereeMsg().getTeamSpecRefCmd() == ETeamSpecRefCmd.DirectFreeKickTigers) || (curFrame
						.getRefereeMsg().getTeamSpecRefCmd() == ETeamSpecRefCmd.KickOffTigers)))
		{
			// activated
			state = EState.WAIT_FIRST;
			// do not react on the same referee msg again
			lastRefereeCmdId = curFrame.getRefereeMsg().getCommandCounter();
		}
		
		if (state == EState.WAIT_SECOND)
		{
			// if botLastTouchedBall changed
			if (!curBotLastTouchedBall.equals(preBotLastTouchedBall))
			{
				botNotAllowedToTouchBall = BotID.get();
				state = EState.INACTIVE;
			}
		}
		
		if (state == EState.WAIT_FIRST)
		{
			// if botLastTouchedBall changed
			if (!curBotLastTouchedBall.equals(preBotLastTouchedBall))
			{
				botNotAllowedToTouchBall = curBotLastTouchedBall;
				state = EState.WAIT_SECOND;
			}
			
		}
		
		return botNotAllowedToTouchBall;
	}
}
