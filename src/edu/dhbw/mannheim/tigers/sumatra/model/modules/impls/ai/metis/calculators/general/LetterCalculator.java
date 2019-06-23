/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ELetter;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculates path points for several letters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LetterCalculator extends ACalculator
{
	@Configurable
	private static double	scaleFactor	= 2000;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		ArrayList<IVector2> iList = new ArrayList<IVector2>(0);
		ArrayList<IVector2> lList = new ArrayList<IVector2>(0);
		ArrayList<IVector2> yList = new ArrayList<IVector2>(0);
		
		int invertFactor = baseAiFrame.getWorldFrame().isInverted() ? -1 : 1;
		
		iList.add(new Vector2(0d, 1d * invertFactor * scaleFactor));
		iList.add(new Vector2(0d, -1d * invertFactor * scaleFactor));
		
		lList.add(new Vector2(-0.5 * invertFactor * scaleFactor, 1 * invertFactor * scaleFactor));
		lList.add(new Vector2(-0.5 * invertFactor * scaleFactor, -1 * invertFactor * scaleFactor));
		lList.add(new Vector2(0.5 * invertFactor * scaleFactor, -1 * invertFactor * scaleFactor));
		
		yList.add(new Vector2(-0.5 * invertFactor * scaleFactor, 1 * invertFactor * scaleFactor));
		yList.add(new Vector2(0 * invertFactor * scaleFactor, (1 - (2 / 3f)) * invertFactor * scaleFactor));
		yList.add(new Vector2(0.5 * invertFactor * scaleFactor, 1 * invertFactor * scaleFactor));
		yList.add(new Vector2(0 * invertFactor * scaleFactor, (1 - (2 / 3f)) * invertFactor * scaleFactor));
		yList.add(new Vector2(0 * invertFactor * scaleFactor, -1 * invertFactor * scaleFactor));
		
		newTacticalField.getLetters().put(ELetter.I, iList);
		newTacticalField.getLetters().put(ELetter.L, lList);
		newTacticalField.getLetters().put(ELetter.Y, yList);
	}
	
}
