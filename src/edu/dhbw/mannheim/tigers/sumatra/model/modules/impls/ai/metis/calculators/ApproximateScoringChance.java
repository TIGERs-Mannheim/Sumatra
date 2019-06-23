/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.04.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


/**
 * This calculator determines whether we or our opponents have an APPROXIMATE chance
 * to score a goal. This means the ball carrier may has to get the ball and aim
 * before a shot would be successful.
 * 
 * @author FlorianS
 * 
 */
public class ApproximateScoringChance extends ACalculator
{
	

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Goal		goal;
	
	private final Vector2f	GOAL_CENTER;
	private final float		GOAL_SIZE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ApproximateScoringChance(ETeam team)
	{
		ETeam.assertOneTeam(team);
		if (team == ETeam.TIGERS)
		{
			// WE want to shoot on THEIR goal
			goal = AIConfig.getGeometry().getGoalTheir();
			
		} else
		{
			// THEY want to shoot on OUR goal
			goal = AIConfig.getGeometry().getGoalOur();
		}
		
		GOAL_CENTER = goal.getGoalCenter();
		GOAL_SIZE = goal.getSize();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public Boolean calculate(AIInfoFrame curFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		Boolean approximateScoringChance = false;
		
		IVector2 ballPos = worldFrame.ball.pos;
		
		// set goal points
		Vector2 leftGoalPointOne = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y + GOAL_SIZE / 2.0f * 0.9f);
		Vector2 leftGoalPointTwo = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y + GOAL_SIZE / 2.0f * 0.8f);
		Vector2 leftGoalPointThree = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y + GOAL_SIZE / 2.0f * 0.2f);
		                           
		Vector2 rightGoalPointOne = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y - GOAL_SIZE / 2.0f * 0.9f);
		Vector2 rightGoalPointTwo = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y - GOAL_SIZE / 2.0f * 0.8f);
		Vector2 rightGoalPointThree = new Vector2(GOAL_CENTER.x, GOAL_CENTER.y - GOAL_SIZE / 2.0f * 0.2f);
		
		// checks whether at least one of three goalPoints can be seen by the ball
		if (ballPos.y() > 0)
		{
			approximateScoringChance = AIMath.p2pVisibility(worldFrame, ballPos, leftGoalPointOne);
			approximateScoringChance |= AIMath.p2pVisibility(worldFrame, ballPos, leftGoalPointTwo);
			approximateScoringChance |= AIMath.p2pVisibility(worldFrame, ballPos, leftGoalPointThree);
		} else
		{
			approximateScoringChance = AIMath.p2pVisibility(worldFrame, ballPos, rightGoalPointOne);
			approximateScoringChance |= AIMath.p2pVisibility(worldFrame, ballPos, rightGoalPointTwo);
			approximateScoringChance |= AIMath.p2pVisibility(worldFrame, ballPos, rightGoalPointThree);
		}
		
		return approximateScoringChance;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
