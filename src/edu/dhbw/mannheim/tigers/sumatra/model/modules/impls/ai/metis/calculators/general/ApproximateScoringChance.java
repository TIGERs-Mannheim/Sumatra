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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


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
	
	private final Vector2f	goalCenter;
	private final float		goalSize;
	
	private ETeam				team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 */
	public ApproximateScoringChance(ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
		if (team == ETeam.TIGERS)
		{
			// WE want to shoot on THEIR goal
			goal = AIConfig.getGeometry().getGoalTheir();
			
		} else
		{
			// THEY want to shoot on OUR goal
			goal = AIConfig.getGeometry().getGoalOur();
		}
		
		goalCenter = goal.getGoalCenter();
		goalSize = goal.getSize();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		Boolean approximateScoringChance = false;
		
		final IVector2 ballPos = worldFrame.ball.getPos();
		
		// set goal points
		final Vector2 leftGoalPointOne = new Vector2(goalCenter.x(), goalCenter.y() + ((goalSize / 2.0f) * 0.9f));
		final Vector2 leftGoalPointTwo = new Vector2(goalCenter.x(), goalCenter.y() + ((goalSize / 2.0f) * 0.8f));
		final Vector2 leftGoalPointThree = new Vector2(goalCenter.x(), goalCenter.y() + ((goalSize / 2.0f) * 0.2f));
		
		final Vector2 rightGoalPointOne = new Vector2(goalCenter.x(), goalCenter.y() - ((goalSize / 2.0f) * 0.9f));
		final Vector2 rightGoalPointTwo = new Vector2(goalCenter.x(), goalCenter.y() - ((goalSize / 2.0f) * 0.8f));
		final Vector2 rightGoalPointThree = new Vector2(goalCenter.x(), goalCenter.y() - ((goalSize / 2.0f) * 0.2f));
		
		// checks whether at least one of three goalPoints can be seen by the ball
		if (ballPos.y() > 0)
		{
			approximateScoringChance = GeoMath.p2pVisibility(worldFrame, ballPos, leftGoalPointOne);
			approximateScoringChance |= GeoMath.p2pVisibility(worldFrame, ballPos, leftGoalPointTwo);
			approximateScoringChance |= GeoMath.p2pVisibility(worldFrame, ballPos, leftGoalPointThree);
		} else
		{
			approximateScoringChance = GeoMath.p2pVisibility(worldFrame, ballPos, rightGoalPointOne);
			approximateScoringChance |= GeoMath.p2pVisibility(worldFrame, ballPos, rightGoalPointTwo);
			approximateScoringChance |= GeoMath.p2pVisibility(worldFrame, ballPos, rightGoalPointThree);
		}
		if (team == ETeam.TIGERS)
		{
			curFrame.tacticalInfo.setTigersApproximateScoringChance(approximateScoringChance);
		} else
		{
			curFrame.tacticalInfo.setOpponentApproximateScoringChance(approximateScoringChance);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		if (team == ETeam.TIGERS)
		{
			curFrame.tacticalInfo.setTigersApproximateScoringChance(false);
		} else
		{
			curFrame.tacticalInfo.setOpponentApproximateScoringChance(false);
		}
	}
}
