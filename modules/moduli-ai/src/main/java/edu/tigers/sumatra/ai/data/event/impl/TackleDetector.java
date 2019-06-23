/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
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
public class TackleDetector implements IGameEventDetector
{
	
	@Override
	public GameEventFrame getActiveParticipant(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (isTackleActive(newTacticalField))
		{
			BotID tacklingTiger = newTacticalField.getBallPossession().getTigersId();
			BotID tacklingOpponent = newTacticalField.getBallPossession().getOpponentsId();
			
			return new GameEventFrame(tacklingTiger, tacklingOpponent);
		}
		
		return null;
	}
	
	
	private boolean isTackleActive(final TacticalField newTacticalField)
	{
		return newTacticalField.getBallPossession().getEBallPossession() == EBallPossession.BOTH;
	}
	
}
