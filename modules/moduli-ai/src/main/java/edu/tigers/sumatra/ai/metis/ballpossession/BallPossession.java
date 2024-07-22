/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.ballpossession;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Contains the EBallPossession and the id of the bot, who has got the ball
 */
@Persistent
@Value
@AllArgsConstructor
public class BallPossession
{
	EBallPossession eBallPossession;
	EBallControl opponentBallControl;
	BotID tigersId;
	BotID opponentsId;


	public BallPossession()
	{
		eBallPossession = EBallPossession.NO_ONE;
		tigersId = BotID.noBot();
		opponentsId = BotID.noBot();
		opponentBallControl = EBallControl.NONE;
	}
}
