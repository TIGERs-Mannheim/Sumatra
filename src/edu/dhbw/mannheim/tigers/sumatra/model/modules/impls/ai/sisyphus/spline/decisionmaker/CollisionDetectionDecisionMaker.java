/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SplineMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static float				timeOffsetStart						= 0.1f;
	@Configurable(comment = "Offset time [s] prepended to endTime")
	private static float				timeOffsetEnd							= 0.1f;
	@Configurable(comment = "Time length [s] to check for collisions")
	private static float				timeLength								= 1.0f;
	
	@Configurable(comment = "step size of iterations done by the collision detection (started at the position of the bot)")
	private static float				timeStepSize							= 0.1f;
	@Configurable(comment = "distance from path to obstacles to fire a collision detection")
	private static float				safetyDistance							= 0;
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline,
			final List<IDrawableShape> shapes)
	{
		FieldInformation fieldInfo = localPathFinderInput.getFieldInfo();
		TrackedTigerBot bot = fieldInfo.getwFrame().getBot(localPathFinderInput.getBotId());
		
		float tCurrent = SplineMath.timeNearest2Point(oldSpline, bot.getPos());
		// float tCurrent = oldSpline.getCurrentTime();
		float tStart = tCurrent + timeOffsetStart;
		float tEndMax = oldSpline.getTotalTime() - timeOffsetEnd;
		float tEnd = tStart + timeLength;
		tStart = Math.min(tStart, tEndMax);
		tEnd = Math.min(tEnd, tEndMax);
		
		for (float t = tStart; t < tEnd; t += timeStepSize)
		{
			IVector2 p = oldSpline.getPositionByTime(t).getXYVector();
			
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
