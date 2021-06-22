/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.goal;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.function.Supplier;


/**
 * This calculator determines whether a goal may be scored. It does not consider if this was a valid goal as announced
 * by the referee
 */
@RequiredArgsConstructor
public class PossibleGoalCalc extends ACalculator
{
	private final Supplier<BallLeftFieldPosition> ballLeftFieldPositionSupplier;

	@Getter
	private EPossibleGoal possibleGoal;


	@Override
	public boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isRunning() && ballLeftFieldPositionSupplier.get() != null;
	}


	@Override
	protected void reset()
	{
		possibleGoal = EPossibleGoal.NO_ONE;
	}


	@Override
	public void doCalc()
	{
		if (ballLeftFieldPositionSupplier.get().getType() == BallLeftFieldPosition.EBallLeftFieldType.GOAL)
		{
			possibleGoal = ballLeftFieldPositionSupplier.get().getPosition().getPos().x() < 0
					? EPossibleGoal.THEY
					: EPossibleGoal.WE;
		} else
		{
			possibleGoal = EPossibleGoal.NO_ONE;
		}

		if (possibleGoal == EPossibleGoal.WE)
		{
			getShapes(EAiShapesLayer.AI_POSSIBLE_GOAL).add(
					new DrawableCircle(
							Circle.createCircle(ballLeftFieldPositionSupplier.get().getPosition().getPos(), 120),
							Color.GREEN));
		} else if (possibleGoal == EPossibleGoal.THEY)
		{
			getShapes(EAiShapesLayer.AI_POSSIBLE_GOAL).add(
					new DrawableCircle(
							Circle.createCircle(ballLeftFieldPositionSupplier.get().getPosition().getPos(), 130),
							Color.RED));
		}
	}
}
