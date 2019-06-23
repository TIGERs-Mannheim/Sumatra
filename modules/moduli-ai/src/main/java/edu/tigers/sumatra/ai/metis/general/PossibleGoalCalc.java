/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This calculator determines whether a ball was shot
 * 
 * @author Dirk Klostermann
 */
public class PossibleGoalCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * the goals from the config file
	 */
	private final Goal	goalOur		= Geometry.getGoalOur();
	private final Goal	goalTheir	= Geometry.getGoalTheir();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		EPossibleGoal isGoal = EPossibleGoal.NO_ONE;
		final IVector2 ball = wFrame.getBall().getPos();
		if (isBallPossiblyInGoal(ball, goalOur))
		{
			isGoal = EPossibleGoal.THEY;
		}
		if (isBallPossiblyInGoal(ball, goalTheir))
		{
			isGoal = EPossibleGoal.WE;
		}
		newTacticalField.setPossibleGoal(isGoal);
	}
	
	
	/**
	 * @param ball
	 * @param goal
	 * @return
	 */
	private boolean isBallPossiblyInGoal(final IVector2 ball, final Goal goal)
	{
		// if the y value of the ball is not between the both goal posts, there will not be a goal,
		// hopefully no one will place the goals on a different position
		if (!(((goal.getLeftPost().y() < ball.y()) && (goal.getRightPost().y() > ball.y())) || ((goal
				.getLeftPost().y() > ball.y()) && (goal.getRightPost().y() < ball.y()))))
		{
			return false;
		}
		// did the ball crossed the goal line?
		final double goalX = goal.getCenter().x();
		return ((goalX < 0) && ((goalX + 25) > ball.x())) || ((goalX > 0) && ((goalX - 25) < ball.x()));
	}
}
