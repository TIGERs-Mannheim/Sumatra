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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


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
	
	private final float	ballRadius			= AIConfig.getGeometry().getBallRadius();
	private final float	minDistToBot		= AIConfig.getMetisCalculators().getMinDistToBot();
	private final float	botRadius			= AIConfig.getGeometry().getBotRadius();
	private final float	possesionTreshold	= ballRadius + minDistToBot + botRadius;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 */
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
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		final TacticalField tacticalField = curFrame.tacticalInfo;
		
		
		boolean scoringChance;
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
			final TrackedBot ballGetter = ballGetterDist.getBot();
			final IVector2 ballPos = worldFrame.ball.getPos();
			
			boolean goalPointBetweenPosts = false;
			boolean noObstacleInTheWay = false;
			boolean ballPossession = false;
			
			IVector2 pointOnBaseline;
			
			try
			{
				pointOnBaseline = GeoMath.intersectionPoint(Line.newLine(ballGetter.getPos(), ballPos), goalLine);
				
				// check whether point on baseline is between the two goal posts
				if ((pointOnBaseline.y() < goal.getGoalPostLeft().y())
						&& (pointOnBaseline.y() > goal.getGoalPostRight().y()))
				{
					goalPointBetweenPosts = true;
				} else
				{
					goalPointBetweenPosts = false;
				}
				
			} catch (final MathException err)
			{
				goalPointBetweenPosts = false;
				pointOnBaseline = goal.getGoalCenter();
			}
			
			
			// check whether there is no obstacle in the way
			noObstacleInTheWay = GeoMath.p2pVisibility(worldFrame, ballGetter.getPos(), pointOnBaseline);
			
			// check whether the team is in ball possession
			if (ballGetterDist.getDist() <= possesionTreshold)
			{
				ballPossession = true;
			} else
			{
				ballPossession = false;
			}
			
			scoringChance = goalPointBetweenPosts && noObstacleInTheWay && ballPossession;
		} else
		{
			scoringChance = false;
		}
		
		if (team == ETeam.TIGERS)
		{
			curFrame.tacticalInfo.setTigersScoringChance(scoringChance);
		} else
		{
			curFrame.tacticalInfo.setOpponentScoringChance(scoringChance);
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
			curFrame.tacticalInfo.setTigersScoringChance(false);
		} else
		{
			curFrame.tacticalInfo.setOpponentScoringChance(false);
		}
	}
}
