/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.util.BallLeftFieldCalculator;


/**
 * Save the moment, the ball left the field
 */
public class BallLeftFieldCalc extends ACalculator
{
	private final BallLeftFieldCalculator ballLeftFieldCalculator = new BallLeftFieldCalculator();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setBallLeftFieldPos(ballLeftFieldCalculator.process(getWFrame()));
		
		if (newTacticalField.getBallLeftFieldPos() != null)
		{
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_LEFT_FIELD).add(
					new DrawableCircle(
							Circle.createCircle(newTacticalField.getBallLeftFieldPos().getPosition().getPos(), 100)));
		}
	}
}
