/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s):
 * FlorianS
 * TobiasK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


/**
 * This calculator determines whether we or our opponents have a DIRECT chance
 * to score a goal. This means the ball carrier is only a kick move away from
 * scoring a goal.
 * 
 * @author FlorianS, Gero
 */
public class ScoringChance extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Goal	goal;
	private final Line	goalLine;
	private final ETeam	team;
	
	private final float	BALL_RADIUS				= AIConfig.getGeometry().getBallRadius();
	private final float	MIN_DIST_TO_BOT		= AIConfig.getCalculators().getMinDistToBot();
	private final float	BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float	POSSESSION_THRESHOLD	= BALL_RADIUS + MIN_DIST_TO_BOT + BOT_RADIUS;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ScoringChance(ETeam team)
	{
		ETeam.assertOneTeam(team);
		this.team = team;
		
		// set goal and goal line
		if (team == ETeam.TIGERS)
		{
			goal = AIConfig.getGeometry().getGoalTheir();
			goalLine = new Line(AIConfig.getGeometry().getGoalLineTheir());
		} else
		{
			goal = AIConfig.getGeometry().getGoalOur();
			goalLine = new Line(AIConfig.getGeometry().getGoalLineOur());
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public Boolean calculate(AIInfoFrame curFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		final TacticalField tacticalField = curFrame.tacticalInfo;
		
		
		Boolean scoringChance = false;
		final BotDistance ballGetterDist;
		
		if (team == ETeam.TIGERS)
		{
			ballGetterDist = tacticalField.getTigerClosestToBall();
		} else
		{
			ballGetterDist = tacticalField.getEnemyClosestToBall();
		}
		
		
		if (ballGetterDist != BotDistance.NULL_BOT_DISTANCE)
		{
			final TrackedBot ballGetter = ballGetterDist.bot;
			final Vector2f ballPos = worldFrame.ball.pos;
			
			boolean goalPointBetweenPosts = false;
			boolean noObstacleInTheWay = false;
			boolean ballPossession = false;
			
			IVector2 pointOnBaseline;
			
			try
			{
				pointOnBaseline = AIMath.intersectionPoint(Line.newLine(ballGetter.pos, ballPos), goalLine);
				
				// check whether point on baseline is between the two goal posts
				if (pointOnBaseline.y() < goal.getGoalPostLeft().y && pointOnBaseline.y() > goal.getGoalPostRight().y)
				{
					goalPointBetweenPosts = true;
				} else
				{
					goalPointBetweenPosts = false;
				}
			
			} catch (MathException err)
			{
				goalPointBetweenPosts = false;
				pointOnBaseline = goal.getGoalCenter();
			}		

			
			// check whether there is no obstacle in the way
			noObstacleInTheWay = AIMath.p2pVisibility(worldFrame, ballGetter.pos, pointOnBaseline);
			
			// check whether the team is in ball possession
			if (ballGetterDist.dist <= POSSESSION_THRESHOLD)
			{
				ballPossession = true;
			} else
			{
				ballPossession = false;
			}
			
			scoringChance = goalPointBetweenPosts && noObstacleInTheWay && ballPossession;
		} else
		{
			return false;
		}
		
		return scoringChance;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
