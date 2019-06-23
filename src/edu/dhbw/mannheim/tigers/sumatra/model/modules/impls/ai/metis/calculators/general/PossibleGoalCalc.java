/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 03.09.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


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
	private final Goal	goalOur		= AIConfig.getGeometry().getGoalOur();
	private final Goal	goalTheir	= AIConfig.getGeometry().getGoalTheir();
	
	
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
		if (!(((goal.getGoalPostLeft().y() < ball.y()) && (goal.getGoalPostRight().y() > ball.y())) || ((goal
				.getGoalPostLeft().y() > ball.y()) && (goal.getGoalPostRight().y() < ball.y()))))
		{
			return false;
		}
		// did the ball crossed the goal line?
		final float goalX = goal.getGoalCenter().x();
		if (((goalX < 0) && ((goalX + 25) > ball.x())) || ((goalX > 0) && ((goalX - 25) < ball.x())))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setPossibleGoal(EPossibleGoal.NO_ONE);
	}
}
