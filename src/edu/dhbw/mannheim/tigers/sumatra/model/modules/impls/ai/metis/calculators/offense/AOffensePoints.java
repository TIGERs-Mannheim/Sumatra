/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s):
 * GuntherB
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PointCloud;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;


/**
 * @author GuntherB, FlorianS
 * 
 */
public abstract class AOffensePoints extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	public final int				MEMORYSIZE;
	public final int				TRIES_PER_CYCLE;
	
	public List<PointCloud>		bestClouds;
	
	private static Vector2f		theirGoalCenter		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	private static float			goalSize					= AIConfig.getGeometry().getGoalTheir().getSize();
	
	private static Vector2f		leftGoalPointOne		= new Vector2f(theirGoalCenter.x, theirGoalCenter.y + goalSize
																			/ 2.0f * 0.9f);
	private static Vector2f		leftGoalPointTwo		= new Vector2f(theirGoalCenter.x, theirGoalCenter.y + goalSize
																			/ 2.0f * 0.8f);
	private static Vector2f		leftGoalPointThree	= new Vector2f(theirGoalCenter.x, theirGoalCenter.y + goalSize
																			/ 2.0f * 0.2f);
	
	private static Vector2f		rightGoalPointOne		= new Vector2f(theirGoalCenter.x, theirGoalCenter.y - goalSize
																			/ 2.0f * 0.9f);
	private static Vector2f		rightGoalPointTwo		= new Vector2f(theirGoalCenter.x, theirGoalCenter.y - goalSize
																			/ 2.0f * 0.8f);
	private static Vector2f		rightGoalPointThree	= new Vector2f(theirGoalCenter.x, theirGoalCenter.y - goalSize
																			/ 2.0f * 0.2f);
	
	private static final float	CRITICAL_DISTANCE		= 3500;
	private static final float	CRITICAL_ANGLE			= 90;																	// TODO Flo:
																																				// find out
																																				// the value
																																				// for real
																																				// field/sim
																																				

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AOffensePoints(int memorysize, int triesPerCycle)
	{
		MEMORYSIZE = memorysize;
		TRIES_PER_CYCLE = triesPerCycle;
		
		bestClouds = new ArrayList<PointCloud>();
		
		for (int c = 0; c < MEMORYSIZE; c++)
		{
			bestClouds.add(initClouds());
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public List<ValuePoint> calculate(AIInfoFrame curFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		
		// clouds from last cycle
		for (PointCloud cloud : bestClouds)
		{
			cloud.updateCloud(worldFrame);
			// re-evaluate existing clouds
			cloud.setValue(evaluateCloud(cloud, worldFrame));
		}
		
		Collections.sort(bestClouds);
		
		// generate and pseudo-instertsort new clouds
		// new points
		for (int counter = 0; counter < TRIES_PER_CYCLE; counter++)
		{
			PointCloud newCloud = generateNewCloud(worldFrame);
			newCloud.setValue(evaluateCloud(newCloud, worldFrame));
			
			int index = 0;
			for (PointCloud cloud : bestClouds)
			{
				if (newCloud.getValue() > cloud.getValue())
				{
					break;
				}
				index++;
			}
			
			// insert at given index and delete last element
			bestClouds.add(index, newCloud);
			bestClouds.remove(bestClouds.size() - 1);
		}
		
		ArrayList<ValuePoint> bestMasterPoints = new ArrayList<ValuePoint>();
		for (PointCloud cloud : bestClouds)
		{
			bestMasterPoints.add(new ValuePoint(cloud.getMasterPoint()));
		}
		return bestMasterPoints;
	}
	

	protected abstract PointCloud generateNewCloud(WorldFrame worldFrame);
	

	private PointCloud initClouds()
	{
		return new PointCloud(new ValuePoint(0, 0));
	}
	

	/** override in childclass, no abstract so it can be called in a static way */
	protected abstract float evaluateCloud(PointCloud cloud, WorldFrame worldFrame);
	

	// ----------------------------------------------------------------
	// ----------------- evaluation -----------------------------------
	// ----------------------------------------------------------------
	
	/**
	 * This method will connect every point of the cloud to 3 goalPoints and find each minimum distance
	 * of these lines to enemy bots.
	 * 
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateMinimumDistances(PointCloud cloud, WorldFrame wf)
	{
		int numberOfLines = 3;
		float averageDistanceSum = 0.0f;
		float factor = 0;
		
		// updating and evaluating average distance per point
		ValuePoint currentPoint = cloud.getMasterPoint();
		
		if (currentPoint.y > 0)
		{
			averageDistanceSum += evaluateSingleLine(currentPoint, leftGoalPointOne, wf);
			averageDistanceSum += evaluateSingleLine(currentPoint, leftGoalPointTwo, wf);
			averageDistanceSum += evaluateSingleLine(currentPoint, leftGoalPointThree, wf);
			
		} else
		{
			averageDistanceSum += evaluateSingleLine(currentPoint, rightGoalPointOne, wf);
			averageDistanceSum += evaluateSingleLine(currentPoint, rightGoalPointTwo, wf);
			averageDistanceSum += evaluateSingleLine(currentPoint, rightGoalPointThree, wf);
		}
		
		averageDistanceSum /= numberOfLines;
		
		if (averageDistanceSum > CRITICAL_DISTANCE)
			factor = 0;
		else
			factor = 1 - averageDistanceSum / CRITICAL_DISTANCE;
		
		return factor;
	}
	

	/**
	 * finds the minimum distance on a single line
	 * @return a multiplier between 0.0f and 1.0f
	 */
	private static float evaluateSingleLine(ValuePoint start, IVector2 end, WorldFrame wf)
	{
		final float STARTING_DISTANCE = 10000000f;
		
		// float distanceStartEnd = AIMath.distancePP(start, end);
		float minimumDistance = STARTING_DISTANCE;
		
		for (TrackedBot bot : wf.foeBots.values())
		{
			// float distanceBotStart = AIMath.distancePP(bot, start);
			// float distanceBotEnd = AIMath.distancePP(bot, end);
			// if (!(distanceStartEnd < distanceBotStart || distanceStartEnd < distanceBotEnd))
			// {
			// only check those bots that possibly can be in between singlePoint and goalPoint
			float distanceBotLine = AIMath.distancePL(bot.pos, start, end);
			if (distanceBotLine < minimumDistance)
			{
				minimumDistance = distanceBotLine;
			}
			// }
		}
		
		if (minimumDistance > STARTING_DISTANCE - 1f)
		{ // can't compare floats to equal, so this will evaluate whether it has changed
			return 0; // returns 0, because this can't be trusted
		}
		

		return minimumDistance;
	}
	

	/**
	 * determines whether the ball can be seen from a point
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateBallVisibility(PointCloud cloud, WorldFrame wf)
	{
		Vector2f ballPos = wf.ball.pos;
		float factor = 0;
		int id = 0;
		List<Integer> tigerIds = new ArrayList<Integer>();
		

		/*
		 * get all our bots ids
		 * all our bots shall be ignored from p2pVisibility because otherwise
		 * a certain bot negates its own visibility condition
		 */
		for (TrackedBot currentBot : wf.tigerBots.values())
		{
			id = currentBot.id;
			tigerIds.add(id);
		}
		
		// evaluating ball visibility
		ValuePoint currentPoint = cloud.getMasterPoint();
		
		for (Integer ignoredId : tigerIds)
		{
			if (AIMath.p2pVisibility(wf, currentPoint, ballPos, cloud.getRaySize(), ignoredId))
			{
				factor += 1;
			} else
			{
				factor += 0;
			}
		}
		
		factor /= tigerIds.size();
		return factor;
	}
	

	/**
	 * determines whether the ball can be seen from a point
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateGoalVisibility(PointCloud cloud, WorldFrame wf)
	{
		float factor = 0;
		int id = 0;
		List<Integer> tigerIds = new ArrayList<Integer>();
		
		Goal goal = AIConfig.getGeometry().getGoalTheir();
		Vector2f goalPostLeft = goal.getGoalPostLeft();
		Vector2f goalPostRight = goal.getGoalPostRight();
		Vector2 distanceToPost = new Vector2(0, 50);
		
		Vector2 goalPoint = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 masterPoint = cloud.getMasterPoint();
		
		/*
		 * get all our bots ids
		 * all our bots shall be ignored from p2pVisibility because otherwise
		 * a certain bot negates its own visibility condition
		 */
		for (TrackedBot currentBot : wf.tigerBots.values())
		{
			id = currentBot.id;
			tigerIds.add(id);
		}
		
		// check whether the dribbler's destination is on the left or the right side
		if (masterPoint.y > 0)
		{
			goalPoint = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPoint = goalPostRight.addNew(distanceToPost);
		}
		
		// evaluating goal visibility
		ValuePoint currentPoint = cloud.getMasterPoint();
		
		for (Integer ignoredId : tigerIds)
		{
			if (AIMath.p2pVisibility(wf, currentPoint, goalPoint, cloud.getRaySize(), ignoredId))
			{
				factor += 1;
			} else
			{
				factor += 0;
			}
			
		}
		
		factor /= tigerIds.size();
		return factor;
	}
	

	/**
	 * determines and evaluates the angle between receiving and shooting
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateAngle(PointCloud cloud, WorldFrame wf)
	{
		Goal goal = AIConfig.getGeometry().getGoalTheir();
		Vector2f postLeft = goal.getGoalPostLeft();
		Vector2f postRight = goal.getGoalPostRight();
		Vector2f ballPos = wf.ball.pos;
		IVector2 goalPoint = AIConfig.INIT_VECTOR;
		
		float factor = 0;
		
		// evaluating angles
		
		ValuePoint currentPoint = cloud.getMasterPoint();
		// goal post closest to point is taken as a reference
		if (currentPoint.y > 0)
		{
			goalPoint = postLeft;
		} else
		{
			goalPoint = postRight;
		}
		
		// angles between x-axis and line point-ball respectively point-goal
		float anglePointBall = AIMath.angleBetweenXAxisAndLine(currentPoint, ballPos);
		float anglePointGoal = AIMath.angleBetweenXAxisAndLine(currentPoint, goalPoint);
		
		float angleDifference = Math.abs(anglePointBall - anglePointGoal);
		
		if (angleDifference > CRITICAL_ANGLE)
		{
			factor += 0;
		} else
		{
			factor += 1 - angleDifference / CRITICAL_ANGLE;
		}
		
		return factor;
	}
	

	/**
	 * determines and evaluates whether a point is still closer to our goal than the ball
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluatePositionValidity(PointCloud cloud, WorldFrame wf)
	{
		Vector2 masterPoint = cloud.getMasterPoint();
		float factor = 0.0f;
		
		// compare masterPoint's and ball's x-coordinate
		if (masterPoint.x < wf.ball.pos.x)
		{
			factor = 1.0f;
		} else
		{
			factor = 0.0f;
		}
		
		return factor;
	}
	

	/**
	 * 
	 * @return a multiplier between 1.00 and 1.4
	 */
	public static float evaluateEvolution(PointCloud cloud)
	{
		// value * 1.1; 1.2; 1.4; 1.5 to weigh evolution size
		return 1.00f + 0.1f * cloud.getEvolution();
	}
	

	/**
	 * 
	 * @return a multiplier between 1.00 and 2.00
	 */
	public static float evaluateLifetime(PointCloud cloud)
	{
		// return (float) (1.00 + 0.1 * cloud.getEvolution());
		
		// TODO GuntherB doSomethinUseful
		return 1.0f;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
