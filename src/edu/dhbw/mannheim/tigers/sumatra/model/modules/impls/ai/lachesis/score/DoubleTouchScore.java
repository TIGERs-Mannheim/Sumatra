/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.ETeamSpecRefCmd;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * Scores if the bot is not allowed to touch the ball
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class DoubleTouchScore extends AScore
{
	private EState	state								= EState.INACTIVE;
	private long	lastRefereeCmdId				= -1;
	private BotID	botNotAllowedToTouchBall	= BotID.createBotId();
	
	private enum EState
	{
		WAIT_FIRST,
		WAIT_SECOND,
		INACTIVE
	}
	
	
	@Override
	protected ScoreResult doCalcScore(final TrackedTigerBot tiger, final ARole role, final MetisAiFrame frame)
	{
		// in some situations, bots are not allowed to touch the ball more than twice
		boolean botNotAllowed2TouchBall = tiger.getId().equals(getBotNotAllowedToTouchBall(frame));
		if ((role.getType() == ERole.OFFENSIVE) && botNotAllowed2TouchBall)
		{
			return new ScoreResult(EUsefulness.BAD);
		}
		return ScoreResult.defaultResult();
	}
	
	
	private BotID getBotNotAllowedToTouchBall(final MetisAiFrame curFrame)
	{
		IRecordFrame preAiFrame = curFrame.getPrevFrame();
		
		
		// keep, if nothing happens
		BotID curBotLastTouchedBall = curFrame.getTacticalField().getBotLastTouchedBall();
		BotID preBotLastTouchedBall = preAiFrame.getTacticalField().getBotLastTouchedBall();
		
		// catch special case: no referee msg received yet at all
		if (curFrame.getLatestRefereeMsg() == null)
		{
			return BotID.createBotId();
		}
		
		// check for a relevant message
		if ((state == EState.INACTIVE)
				&& (lastRefereeCmdId != curFrame.getLatestRefereeMsg().getCommandCounter())
				&& ((curFrame.getLatestRefereeMsg().getTeamSpecRefCmd() == ETeamSpecRefCmd.DirectFreeKickTigers) || (curFrame
						.getLatestRefereeMsg().getTeamSpecRefCmd() == ETeamSpecRefCmd.KickOffTigers)))
		{
			// activated
			state = EState.WAIT_FIRST;
			// do not react on the same referee msg again
			lastRefereeCmdId = curFrame.getLatestRefereeMsg().getCommandCounter();
		}
		
		if (state == EState.WAIT_SECOND)
		{
			// if botLastTouchedBall changed
			if (!curBotLastTouchedBall.equals(preBotLastTouchedBall))
			{
				botNotAllowedToTouchBall = BotID.createBotId();
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
