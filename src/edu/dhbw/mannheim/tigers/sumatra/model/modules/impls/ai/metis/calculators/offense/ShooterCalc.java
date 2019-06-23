/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper.ShooterMemory;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;


/**
 * ShooterMemory calculates the best target in the goal for the bot that has the ball atm.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShooterCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ShooterMemory	mem;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ShooterCalc()
	{
		mem = new ShooterMemory();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		// Evaluate direct goal shot targets (using ShooterMemory)
		mem.update(wFrame, baseAiFrame, wFrame.getBall().getPos());
		ValuePoint bestDirectShotTarget = mem.getBestPoint();
		newTacticalField.setBestDirectShotTarget(bestDirectShotTarget);
		List<ValuePoint> goalValuePoints = mem.getGeneratedGoalPoints();
		newTacticalField.setGoalValuePoints(goalValuePoints);
		
		for (ValuePoint vp : goalValuePoints)
		{
			Color color = AFieldLayer.getColorByValue(vp.getValue());
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.GOAL_POINTS).add(new DrawablePoint(vp, color));
		}
		newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.GOAL_POINTS)
				.add(new DrawablePoint(bestDirectShotTarget, Color.blue));
	}
}
