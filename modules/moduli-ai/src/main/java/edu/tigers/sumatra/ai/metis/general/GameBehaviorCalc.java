/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.data.EGameBehavior;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;


/**
 * Calculator to switch between GameBehaviours
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class GameBehaviorCalc extends ACalculator
{
	
	/**
	 */
	public GameBehaviorCalc()
	{
	}
	
	
	/**
	 * TODO after RoboCup 2015: more intelligent GameBevahior detector by considering strength of us and strength of foes
	 * (with kicks on goals, score, time left, etc.)
	 */
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getRefereeMsg() == null)
		{
			fallbackCalc(newTacticalField, baseAiFrame);
			return;
		}
		
		int goalDifference = baseAiFrame.getRefereeMsg().getTeamInfoTigers().getScore()
				- baseAiFrame.getRefereeMsg().getTeamInfoThem().getScore();
		EGameBehavior behavior = newTacticalField.getGameBehavior();
		
		if (goalDifference <= -5)
		{
			behavior = EGameBehavior.DEFENSIVE;
		} else
		{
			behavior = EGameBehavior.OFFENSIVE;
		}
		newTacticalField.setGameBehavior(behavior);
		
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setGameBehavior(EGameBehavior.OFFENSIVE);
	}
}
