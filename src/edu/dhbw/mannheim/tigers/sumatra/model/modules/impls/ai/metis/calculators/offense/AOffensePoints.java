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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.PointCloud;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * @author GuntherB, FlorianS
 */
public abstract class AOffensePoints extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public final int				memorySize;
	/** */
	public final int				triesPerCycle;
	
	/** */
	public List<PointCloud>		bestClouds;
	
	private static Vector2f		theirGoalCenter		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	private static float			goalSize					= AIConfig.getGeometry().getGoalTheir().getSize();
	
	private static Vector2f		leftGoalPointOne		= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			+ ((goalSize / 2.0f) * 0.9f));
	private static Vector2f		leftGoalPointTwo		= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			+ ((goalSize / 2.0f) * 0.8f));
	private static Vector2f		leftGoalPointThree	= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			+ ((goalSize / 2.0f) * 0.2f));
	
	private static Vector2f		rightGoalPointOne		= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			- ((goalSize / 2.0f) * 0.9f));
	private static Vector2f		rightGoalPointTwo		= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			- ((goalSize / 2.0f) * 0.8f));
	private static Vector2f		rightGoalPointThree	= new Vector2f(theirGoalCenter.x(), theirGoalCenter.y()
																			- ((goalSize / 2.0f) * 0.2f));
	// these values are not tested in reality
	private static final float	CRITICAL_DISTANCE		= 3500;
	private static final float	CRITICAL_ANGLE			= 90;
	
	
	private static final float	STARTING_DISTANCE		= 10000000f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param memorysize
	 * @param triesPerCycle
	 * 
	 */
	public AOffensePoints(int memorysize, int triesPerCycle)
	{
		memorySize = memorysize;
		this.triesPerCycle = triesPerCycle;
		
		bestClouds = new ArrayList<PointCloud>();
		
		for (int c = 0; c < memorySize; c++)
		{
			bestClouds.add(initClouds());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		
		// clouds from last cycle
		for (final PointCloud cloud : bestClouds)
		{
			cloud.updateCloud(worldFrame);
			// re-evaluate existing clouds
			cloud.setValue(evaluateCloud(cloud, worldFrame));
		}
		
		Collections.sort(bestClouds);
		
		// generate and pseudo-instertsort new clouds
		// new points
		for (int counter = 0; counter < triesPerCycle; counter++)
		{
			final PointCloud newCloud = generateNewCloud(worldFrame);
			newCloud.setValue(evaluateCloud(newCloud, worldFrame));
			
			int index = 0;
			for (final PointCloud cloud : bestClouds)
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
		
		final ArrayList<ValuePoint> bestMasterPoints = new ArrayList<ValuePoint>();
		for (final PointCloud cloud : bestClouds)
		{
			bestMasterPoints.add(new ValuePoint(cloud.getMasterPoint()));
		}
		setInfoInTacticalField(curFrame, bestMasterPoints);
	}
	
	
	protected abstract void setInfoInTacticalField(AIInfoFrame curFrame, List<ValuePoint> points);
	
	
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
	 * @param cloud
	 * @param wf
	 * 
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateMinimumDistances(PointCloud cloud, WorldFrame wf)
	{
		final int numberOfLines = 3;
		float averageDistanceSum = 0.0f;
		float factor = 0;
		
		// updating and evaluating average distance per point
		final ValuePoint currentPoint = cloud.getMasterPoint();
		
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
		{
			factor = 0;
		} else
		{
			factor = 1 - (averageDistanceSum / CRITICAL_DISTANCE);
		}
		
		return factor;
	}
	
	
	/**
	 * finds the minimum distance on a single line
	 * @return a multiplier between 0.0f and 1.0f
	 */
	private static float evaluateSingleLine(ValuePoint start, IVector2 end, WorldFrame wf)
	{
		
		// float distanceStartEnd = AIMath.distancePP(start, end);
		float minimumDistance = STARTING_DISTANCE;
		
		for (final TrackedBot bot : wf.foeBots.values())
		{
			// float distanceBotStart = AIMath.distancePP(bot, start);
			// float distanceBotEnd = AIMath.distancePP(bot, end);
			// if (!(distanceStartEnd < distanceBotStart || distanceStartEnd < distanceBotEnd))
			// {
			// only check those bots that possibly can be in between singlePoint and goalPoint
			final float distanceBotLine = GeoMath.distancePL(bot.getPos(), start, end);
			if (distanceBotLine < minimumDistance)
			{
				minimumDistance = distanceBotLine;
			}
			// }
		}
		
		if (minimumDistance > (STARTING_DISTANCE - 1f))
		{
			// can't compare floats to equal, so this will evaluate whether it has changed
			// returns 0, because this can't be trusted
			return 0;
		}
		
		
		return minimumDistance;
	}
	
	
	/**
	 * determines whether the ball can be seen from a point
	 * @param cloud
	 * @param wf
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateBallVisibility(PointCloud cloud, WorldFrame wf)
	{
		final IVector2 ballPos = wf.ball.getPos();
		float factor = 0;
		BotID id;
		final List<BotID> tigerIds = new ArrayList<BotID>();
		
		
		/*
		 * get all our bots ids
		 * all our bots shall be ignored from p2pVisibility because otherwise
		 * a certain bot negates its own visibility condition
		 */
		for (final TrackedBot currentBot : wf.tigerBotsVisible.values())
		{
			id = currentBot.getId();
			tigerIds.add(id);
		}
		
		// evaluating ball visibility
		final ValuePoint currentPoint = cloud.getMasterPoint();
		
		for (final BotID ignoredId : tigerIds)
		{
			if (GeoMath.p2pVisibility(wf, currentPoint, ballPos, cloud.getRaySize(), ignoredId))
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
	 * @param cloud
	 * @param wf
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateGoalVisibility(PointCloud cloud, WorldFrame wf)
	{
		float factor = 0;
		BotID id;
		final List<BotID> tigerIds = new ArrayList<BotID>();
		
		final Goal goal = AIConfig.getGeometry().getGoalTheir();
		final Vector2f goalPostLeft = goal.getGoalPostLeft();
		final Vector2f goalPostRight = goal.getGoalPostRight();
		final Vector2 distanceToPost = new Vector2(0, 50);
		
		final Vector2 masterPoint = cloud.getMasterPoint();
		
		/*
		 * get all our bots ids
		 * all our bots shall be ignored from p2pVisibility because otherwise
		 * a certain bot negates its own visibility condition
		 */
		for (final TrackedBot currentBot : wf.tigerBotsVisible.values())
		{
			id = currentBot.getId();
			tigerIds.add(id);
		}
		
		// check whether the dribbler's destination is on the left or the right side
		final Vector2 goalPoint;
		if (masterPoint.y > 0)
		{
			goalPoint = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPoint = goalPostRight.addNew(distanceToPost);
		}
		
		// evaluating goal visibility
		final ValuePoint currentPoint = cloud.getMasterPoint();
		
		for (final BotID ignoredId : tigerIds)
		{
			if (GeoMath.p2pVisibility(wf, currentPoint, goalPoint, cloud.getRaySize(), ignoredId))
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
	 * @param cloud
	 * @param wf
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluateAngle(PointCloud cloud, WorldFrame wf)
	{
		final Goal goal = AIConfig.getGeometry().getGoalTheir();
		final IVector2 postLeft = goal.getGoalPostLeft();
		final IVector2 postRight = goal.getGoalPostRight();
		final IVector2 ballPos = wf.ball.getPos();
		final IVector2 goalPoint;
		
		float factor = 0;
		
		// evaluating angles
		
		final ValuePoint currentPoint = cloud.getMasterPoint();
		// goal post closest to point is taken as a reference
		if (currentPoint.y > 0)
		{
			goalPoint = postLeft;
		} else
		{
			goalPoint = postRight;
		}
		
		// angles between x-axis and line point-ball respectively point-goal
		final float anglePointBall = GeoMath.angleBetweenXAxisAndLine(currentPoint, ballPos);
		final float anglePointGoal = GeoMath.angleBetweenXAxisAndLine(currentPoint, goalPoint);
		
		final float angleDifference = Math.abs(anglePointBall - anglePointGoal);
		
		if (angleDifference > CRITICAL_ANGLE)
		{
			factor += 0;
		} else
		{
			factor += 1 - (angleDifference / CRITICAL_ANGLE);
		}
		
		return factor;
	}
	
	
	/**
	 * determines and evaluates whether a point is still closer to our goal than the ball
	 * @param cloud
	 * @param wf
	 * @return a multiplier between 0.0f and 1.0f
	 */
	public static float evaluatePositionValidity(PointCloud cloud, WorldFrame wf)
	{
		final IVector2 masterPoint = cloud.getMasterPoint();
		float factor = 0.0f;
		
		// compare masterPoint's and ball's x-coordinate
		if (masterPoint.x() < wf.ball.getPos().x())
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
	 * @param cloud
	 * @return a multiplier between 1.00 and 1.4
	 */
	public static float evaluateEvolution(PointCloud cloud)
	{
		// value * 1.1; 1.2; 1.4; 1.5 to weigh evolution size
		return 1.00f + (0.1f * cloud.getEvolution());
	}
	
	
	/**
	 * 
	 * @param cloud
	 * @return a multiplier between 1.00 and 2.00
	 */
	public static float evaluateLifetime(PointCloud cloud)
	{
		return 1.0f;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
