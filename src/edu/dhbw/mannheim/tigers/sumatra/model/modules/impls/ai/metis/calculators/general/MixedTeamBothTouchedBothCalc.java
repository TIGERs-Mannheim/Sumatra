/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * Calculator for the rule of Mixed team challenge that goals are only scored if both subteams of one team has touched
 * the ball.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class MixedTeamBothTouchedBothCalc extends ACalculator
{
	private boolean	tigersTouched	= false;
	private boolean	partnerTouched	= false;
	
	
	/**
	 */
	public MixedTeamBothTouchedBothCalc()
	{
	}
	
	
	/**
	 */
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		boolean bothTouched = false;
		// Rule has to be applied in on running stage with no interrupts
		if (newTacticalField.getGameState() == EGameState.RUNNING)
		{
			
			List<BotID> partnerBots = new ArrayList<BotID>(6);
			Set<BotID> tigerBots = baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet();
			
			for (BotID visBot : baseAiFrame.getWorldFrame().getTigerBotsVisible().keySet())
			{
				if (!tigerBots.contains(visBot))
				{
					partnerBots.add(visBot);
				}
			}
			
			BotID botTouch = newTacticalField.getBotTouchedBall();
			// Checks who has touched ball from tigers or partners and save flags
			if (partnerBots.contains(botTouch))
			{
				partnerTouched = true;
			} else if (tigerBots.contains(botTouch))
			{
				tigersTouched = true;
			} else if (baseAiFrame.getWorldFrame().getFoeBots().keySet().contains(botTouch))
			{
				partnerTouched = false;
				tigersTouched = false;
			}
			
			// checks if both flags are true
			if (tigersTouched && partnerTouched)
			{
				bothTouched = true;
			} else
			{
				bothTouched = false;
			}
			
			
		} else
		{
			// Not in Running state. Reset flags
			tigersTouched = false;
			partnerTouched = false;
			bothTouched = false;
		}
		newTacticalField.setMixedTeamBothTouchedBall(bothTouched);
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setMixedTeamBothTouchedBall(false);
	}
}
