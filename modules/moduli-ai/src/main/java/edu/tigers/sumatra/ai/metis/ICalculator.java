/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public interface ICalculator
{
	
	/**
	 * Specifies if the doCalc method has to be executed
	 *
	 * @param tacticalField
	 * @param aiFrame
	 * @return
	 */
	default boolean isCalculationNecessary(TacticalField tacticalField, BaseAiFrame aiFrame)
	{
		return true;
	}
	
	
	/**
	 * This function should be used to analyze something.
	 *
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	void doCalc(TacticalField newTacticalField, BaseAiFrame baseAiFrame);
	
}
