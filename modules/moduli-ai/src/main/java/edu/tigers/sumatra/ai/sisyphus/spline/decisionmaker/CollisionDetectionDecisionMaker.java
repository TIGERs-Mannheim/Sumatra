/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.spline.decisionmaker;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.FieldInformation;
import edu.tigers.sumatra.ai.sisyphus.spline.EDecision;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * checks if the bot will crash if it follows the current spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class CollisionDetectionDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	
	private static final Logger	log										= Logger
																								.getLogger(CollisionDetectionDecisionMaker.class
																										.getName());
																										
	private static int				ignoreMultipleCollisions			= 1;
	private int							ignoreMultipleCollisionsCounter	= ignoreMultipleCollisions;
	private boolean					wasLastCollision						= false;
																						
	@Configurable(comment = "Offset time [s] appended to startTime")
	private static double			timeOffsetStart						= 0.1;
	@Configurable(comment = "Offset time [s] prepended to endTime")
	private static double			timeOffsetEnd							= 0.1;
	@Configurable(comment = "Time length [s] to check for collisions")
	private static double			timeLength								= 1.0;
																						
	@Configurable(comment = "step size of iterations done by the collision detection (started at the position of the bot)")
	private static double			timeStepSize							= 0.1;
	@Configurable(comment = "distance from path to obstacles to fire a collision detection")
	private static double			safetyDistance							= 0;
																						
																						
	static
	{
		ConfigRegistration.registerClass("sisyphus", CollisionDetectionDecisionMaker.class);
	}
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ITrajectory<IVector3> oldSpline,
			final ITrajectory<IVector3> newSpline,
			final List<IDrawableShape> shapes, final double curTime)
	{
		FieldInformation fieldInfo = localPathFinderInput.getFieldInfo();
		ITrackedBot bot = fieldInfo.getwFrame().getBot(localPathFinderInput.getBotId());
		
		double tCurrent = TrajectoryMath.timeNearest2Point(oldSpline, bot.getPos());
		// double tCurrent = oldSpline.getCurrentTime();
		double tStart = tCurrent + timeOffsetStart;
		double tEndMax = oldSpline.getTotalTime() - timeOffsetEnd;
		double tEnd = tStart + timeLength;
		tStart = Math.min(tStart, tEndMax);
		tEnd = Math.min(tEnd, tEndMax);
		
		for (double t = tStart; t < tEnd; t += timeStepSize)
		{
			IVector2 p = oldSpline.getPositionMM(t).getXYVector();
			
			fieldInfo.fillBotPosList(t);
			// fieldInfo.getMoveCon().setGoalPostObstacle(false);
			IVector2 collisionPoint = fieldInfo.isPointOKPP(p, safetyDistance, true);
			if (collisionPoint != null)
			{
				shapes.add(new DrawablePoint(p, Color.blue));
				shapes.add(new DrawableCircle(new Circle(collisionPoint, 50), Color.orange));
				return trueIfLastWasNotCollision("New Spline - Collision imminent @ t = " + t, localPathFinderInput);
			}
			shapes.add(new DrawablePoint(p, Color.cyan));
		}
		fieldInfo.fillBotPosList(0);
		wasLastCollision = false;
		ignoreMultipleCollisionsCounter = ignoreMultipleCollisions;
		return EDecision.NO_VIOLATION;
	}
	
	
	private EDecision trueIfLastWasNotCollision(final String logMessage, final PathFinderInput localPathFinderInput)
	{
		if (wasLastCollision && (ignoreMultipleCollisionsCounter != 0))
		{
			ignoreMultipleCollisionsCounter--;
		} else
		{
			log.trace(logMessage);
			wasLastCollision = true;
			ignoreMultipleCollisionsCounter = ignoreMultipleCollisions;
			return EDecision.COLLISION_AHEAD;
		}
		return EDecision.NO_VIOLATION;
	}
}
