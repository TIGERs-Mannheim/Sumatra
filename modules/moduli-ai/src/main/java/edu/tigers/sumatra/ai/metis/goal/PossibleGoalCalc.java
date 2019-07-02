/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.goal;

import java.awt.Color;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;


/**
 * This calculator determines whether a goal may be scored. It does not consider if this was a valid goal as announced
 * by the referee
 * 
 * @author Dirk Klostermann
 */
public class PossibleGoalCalc extends ACalculator
{
	@Override
	public boolean isCalculationNecessary(final TacticalField newTacticalField, final BaseAiFrame aiFrame)
	{
		return aiFrame.getGamestate().isRunning() && newTacticalField.getBallLeftFieldPos() != null;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.getBallLeftFieldPos().getType() == BallLeftFieldPosition.EBallLeftFieldType.GOAL)
		{
			newTacticalField.setPossibleGoal(
					newTacticalField.getBallLeftFieldPos().getPosition().getPos().x() < 0
							? EPossibleGoal.THEY
							: EPossibleGoal.WE);
		} else
		{
			newTacticalField.setPossibleGoal(EPossibleGoal.NO_ONE);
		}
		
		if (newTacticalField.getPossibleGoal() == EPossibleGoal.WE)
		{
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_POSSIBLE_GOAL).add(
					new DrawableCircle(
							Circle.createCircle(newTacticalField.getBallLeftFieldPos().getPosition().getPos(), 120),
							Color.GREEN));
		} else if (newTacticalField.getPossibleGoal() == EPossibleGoal.THEY)
		{
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_POSSIBLE_GOAL).add(
					new DrawableCircle(
							Circle.createCircle(newTacticalField.getBallLeftFieldPos().getPosition().getPos(), 130),
							Color.RED));
		}
	}
}
