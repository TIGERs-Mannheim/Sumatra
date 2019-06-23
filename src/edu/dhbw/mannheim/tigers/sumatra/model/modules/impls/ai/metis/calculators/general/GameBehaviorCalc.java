/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


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
		if (baseAiFrame.getLatestRefereeMsg() == null)
		{
			fallbackCalc(newTacticalField, baseAiFrame);
			return;
		}
		
		int goalDifference = baseAiFrame.getLatestRefereeMsg().getTeamInfoTigers().getScore()
				- baseAiFrame.getLatestRefereeMsg().getTeamInfoThem().getScore();
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
