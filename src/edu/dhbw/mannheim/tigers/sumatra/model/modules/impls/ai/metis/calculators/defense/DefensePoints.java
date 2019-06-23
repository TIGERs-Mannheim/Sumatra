/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense;


import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.distancePL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


/**
 * This is a calculator for estimate dangerous points for defender positioning.
 * It implement the system for placing defenders of team odens.
 * (See their TDP 2010 p.10 f)
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class DefensePoints extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float							BOT_RADIUS						= AIConfig.getGeometry().getBotRadius();
	private final float							BALL_RADIUS						= AIConfig.getGeometry().getBallRadius();
	private final Goal							goal								= AIConfig.getGeometry().getGoalOur();
	
	// --- technical specifications ---
	private static final float					MAX_ACCELERATION				= (float) AIConfig.getGeneral()
																									.getMaxAcceleration() / 1000f;
	/** TODO Oliver, fix delay time ai needs to react on something */
	private static final float					TIME_DELAY						= 0.0f;
	private final float							MAX_SHOT_VELOCITY				= AIConfig.getGeneral().getMaxShootVelocity();
	private final float							MAX_PASS_VELOCITY				= AIConfig.getGeneral().getMaxPassVeloctiy();
	
	// --- analyzing specifications ---
	private final int								NUMBER_THREAD_POINTS			= AIConfig.getCalculators()
																									.getNumberOfThreadPoints();
	/** the length between two points */
	private final float							POINT_STEP_SIZE				= goal.getSize() / NUMBER_THREAD_POINTS;
	private final float							DIRECT_SHOOT_QUANTIFIER		= AIConfig.getCalculators()
																									.getDirectShootQuantifier();
	private final float							INDIRECT_SHOOT_QUANTIFIER	= AIConfig.getCalculators()
																									.getIndirectShootQuantifier();
	

	private Vector2								ballPosition					= null;
	private Vector2								keeperPosition					= null;
	

	private final List<Vector2>				dangerousPointsDirect		= new ArrayList<Vector2>();
	// private final Map<Vector2, TrackedBot> dangerousPointsIndirect = new HashMap<Vector2, TrackedBot>();
	private final Map<TrackedBot, Vector2>	dangerousPointsIndirect		= new HashMap<TrackedBot, Vector2>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DefensePoints()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public List<ValuePoint> calculate(AIInfoFrame curFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		dangerousPointsDirect.clear();
		dangerousPointsIndirect.clear();
		ballPosition = new Vector2(worldFrame.ball.pos.x, worldFrame.ball.pos.y);
		
		// this is necessary for testing play with fewer then 5 bots
		TrackedTigerBot keeper = worldFrame.tigerBots.get(AIConfig.getGeneral().getKeeperId());
		if (keeper != null)
		{
			keeperPosition = new Vector2(keeper.pos);
			

			Vector2 threadPoint = new Vector2(goal.getGoalCenter().x, -goal.getSize() / 2);
			
			float distance = -2;
			float movingRadius = -1;
			
			for (int i = 0; i < NUMBER_THREAD_POINTS; i++)
			{
				threadPoint.y += POINT_STEP_SIZE;
				// distance = AIMath.distancePP(threadPoint, keeperPosition);
				distance = AIMath.distancePL(keeperPosition, threadPoint, ballPosition) / 1000;
				
				// direct shot
				movingRadius = getMovingRadius(getTimeDirectShot(threadPoint));
				if (distance >= movingRadius)
				{
					dangerousPointsDirect.add(new Vector2(threadPoint));
				}
				
				// indirect shot
				for (Entry<Integer, TrackedBot> entry : worldFrame.foeBots.entrySet())
				{
					TrackedBot bot = entry.getValue();
					
					movingRadius = getMovingRadius(getTimeIndirectShot(threadPoint, bot.pos));
					if (distance <= movingRadius)
					{
						dangerousPointsIndirect.put(bot, new Vector2(threadPoint));
					}
				}
				
			}
		}
		
		return sortedDangerousPoints();
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This functions sorts the estimated dangerous points an returns them in a
	 * descending order starting with the most dangerous point.
	 * 
	 * @return List of sorted dangerous points
	 */
	private List<ValuePoint> sortedDangerousPoints()
	{
		List<ValuePoint> sortedPoints = new ArrayList<ValuePoint>();
		
		for (Vector2 goalPoint : dangerousPointsDirect)
		{
			float threatValue = 0;
			
			if (!isPointDirectProtected(goalPoint))
			{
				threatValue = calculateThreadValueDirect(goalPoint, ballPosition);
			}
			
			if (dangerousPointsIndirect.containsValue(goalPoint))
			{
				for (Entry<TrackedBot, Vector2> entry : dangerousPointsIndirect.entrySet())
				{
					Vector2 entryGoalPoint = entry.getValue();
					TrackedBot passingBot = entry.getKey();
					
					if (goalPoint.equals(entryGoalPoint))
					{
						threatValue += calculateThreadValueIndirect(goalPoint, passingBot.pos);
					}
					
				}
				
			}
			
			sortedPoints.add(new ValuePoint(goalPoint, threatValue));
		}
		
		Collections.sort(sortedPoints);
		
		return sortedPoints;
	}
	

	/**
	 * 
	 * Calculates the thread value for a goal point which can be direct attacked.
	 * 
	 * @param goalPoint
	 * @param ballpos
	 * @return thread value
	 */
	private float calculateThreadValueDirect(Vector2 goalPoint, Vector2 ballpos)
	{
		float ballGoalDistance = AIMath.distancePP(goalPoint, ballpos);
		return (DIRECT_SHOOT_QUANTIFIER * 1 / ballGoalDistance) * 1000;
	}
	

	/**
	 * 
	 * Calculates the thread value for a goal point which can be indirect attacked.
	 * 
	 * @param goalPoint
	 * @param passingBot
	 * @return thread value
	 */
	private float calculateThreadValueIndirect(Vector2 goalPoint, AVector2 passingBot)
	{
		float passerGoalDistance = AIMath.distancePP(passingBot, goalPoint);
		return (INDIRECT_SHOOT_QUANTIFIER * 1 / passerGoalDistance) * 1000;
	}
	

	/**
	 * 
	 * This function estimates if a goal point is directly protected by the goal keeper.
	 * 
	 * @param goalPoint which should be proofed
	 * @return true when goalPoint is protected by keeper
	 */
	private boolean isPointDirectProtected(Vector2 goalPoint)
	{
		float newDist = distancePL(keeperPosition, goalPoint, ballPosition);
		
		if (newDist < BALL_RADIUS + BOT_RADIUS)
		{
			return true;
		}
		
		return false;
	}
	

	/**
	 * 
	 * This function calculates the time a direct shot needs to hit a point within the goal mouth.
	 * 
	 * @param goalPoint a point within the mouth of the goal
	 * @return time
	 */
	private float getTimeDirectShot(Vector2 goalPoint)
	{
		return ((AIMath.distancePP(ballPosition, goalPoint) * 1 / 1000) / MAX_SHOT_VELOCITY) - TIME_DELAY;
	}
	

	/**
	 * 
	 * This function calculates the time a indirect shot needs to hit a point within the goal mouth.
	 * 
	 * @param goalPoint a point within the mouth of the goal
	 * @param bot the bot which gets a pass and shots on the goal
	 * @return time
	 */
	private float getTimeIndirectShot(Vector2 goalPoint, IVector2 bot)
	{
		Vector2 passToBot = new Vector2(bot.x(), bot.y());
		
		float passDelay = ((AIMath.distancePP(ballPosition, passToBot) * 1 / 1000) / MAX_PASS_VELOCITY);
		float shotDelay = ((AIMath.distancePP(passToBot, goalPoint) * 1 / 1000) / MAX_SHOT_VELOCITY);
		
		return (passDelay + shotDelay); // time delay is included in shotDelay
	}
	

	/**
	 * 
	 * This function returns the moving radius a bot can cover in a specific time.
	 * 
	 * @param time to move
	 * @return radius
	 */
	private float getMovingRadius(float time)
	{
		return (float) (0.5f * MAX_ACCELERATION * time * time);
	}
	

}
