/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event.impl;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.event.GameEventFrame;
import edu.tigers.sumatra.ai.data.event.IGameEventDetector;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class BallPossessionDetector implements IGameEventDetector
{
	
	/**
	 * The active participant in this case is the ball that is touching the ball when the ball possession is a singular
	 * event
	 */
	@Override
	public GameEventFrame getActiveParticipant(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		EBallPossession activePossession = newTacticalField.getBallPossession().getEBallPossession();
		
		BotID involvedBot;
		
		switch (activePossession)
		{
			case WE:
				involvedBot = newTacticalField.getBallPossession().getTigersId();
				break;
			case THEY:
				involvedBot = newTacticalField.getBallPossession().getOpponentsId();
				break;
			default:
				return null;
				
				
		}
		
		return new GameEventFrame(involvedBot);
	}
}
