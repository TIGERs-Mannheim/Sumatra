/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.metis.testutils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class TacticalFieldTestUtils
{
	/**
	 * Creates a tactical field that can be used to handle events of ball possession
	 * 
	 * @param eBallPossession The ball Possession that should be initialized
	 * @return A TacticalField that can be used to handle ball possession events
	 */
	public static TacticalField mockTacticalFieldJustWithBallPossessionForSide(
			final EBallPossession eBallPossession)
	{
		TacticalField newTacticalField = mock(TacticalField.class);
		
		BotID botID = BotID.createBotId(0, ETeamColor.BLUE);
		
		BallPossession ballPossession = mock(BallPossession.class);
		when(ballPossession.getEBallPossession()).thenReturn(eBallPossession);
		when(ballPossession.getTigersId()).thenReturn(botID);
		
		if (eBallPossession == EBallPossession.BOTH)
		{
			BotID opponentID = BotID.createBotId(0, ETeamColor.YELLOW);
			when(ballPossession.getOpponentsId()).thenReturn(opponentID);
		}
		
		when(newTacticalField.getBallPossession()).thenReturn(ballPossession);
		
		return newTacticalField;
	}
	
	
	/**
	 * Creates a tactical field with a ball possession for the given bot.
	 * This bot is always in our team.
	 * 
	 * @param possessingBot The bot that is possessing the ball
	 * @return A TacticalField for the usage of BallPossession
	 */
	public static TacticalField mockTacticalFieldJustWithPossessingBot(final BotID possessingBot)
	{
		TacticalField newTacticalField = mock(TacticalField.class);
		
		BallPossession ballPossession = mock(BallPossession.class);
		
		when(ballPossession.getTigersId()).thenReturn(possessingBot);
		when(ballPossession.getEBallPossession()).thenReturn(EBallPossession.WE);
		
		when(newTacticalField.getBallPossession()).thenReturn(ballPossession);
		
		return newTacticalField;
	}
}
