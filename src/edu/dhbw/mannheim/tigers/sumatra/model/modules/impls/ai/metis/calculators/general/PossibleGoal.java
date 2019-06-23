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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This calculator determines whether a ball was shot
 * 
 * @author Dirk Klostermann
 */
public class PossibleGoal extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	// private static final Logger log = Logger.getLogger(PossibleGoal.class.getName());
	
	/**
	 * the goals from the config file
	 */
	private final Goal	goalOur		= AIConfig.getGeometry().getGoalOur();
	private final Goal	goalTheir	= AIConfig.getGeometry().getGoalTheir();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PossibleGoal()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This function calculate the ball possession, the id of the bot with the ball and return the result
	 * @param curFrame
	 * @param preFrame
	 */
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		EPossibleGoal isGoal = EPossibleGoal.NO_ONE;
		final IVector2 ball = curFrame.worldFrame.ball.getPos();
		if (isBallPossiblyInGoal(ball, goalOur))
		{
			isGoal = EPossibleGoal.THEY;
		}
		if (isBallPossiblyInGoal(ball, goalTheir))
		{
			isGoal = EPossibleGoal.WE;
		}
		curFrame.tacticalInfo.setPossibleGoal(isGoal);
	}
	
	
	/**
	 * @param ball
	 * @param goal
	 * @return
	 */
	private boolean isBallPossiblyInGoal(IVector2 ball, Goal goal)
	{
		// if the y value of the ball is not between the both goal posts, there will not be a goal,
		// hopefully no one will place the goals on a different position
		if (!(((goal.getGoalPostLeft().y() < ball.y()) && (goal.getGoalPostRight().y() > ball.y())) || ((goal
				.getGoalPostLeft().y() > ball.y()) && (goal.getGoalPostRight().y() < ball.y()))))
		{
			return false;
		}
		// did the ball crassed the goal line?
		final float goalX = goal.getGoalCenter().x();
		if (((goalX < 0) && ((goalX + 25) > ball.x())) || ((goalX > 0) && ((goalX - 25) < ball.x())))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setPossibleGoal(EPossibleGoal.NO_ONE);
	}
}
