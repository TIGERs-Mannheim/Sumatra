/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Collision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * checks if the bot will crash if it follows the current spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class CollisionDetectionDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log										= Logger
																								.getLogger(CollisionDetectionDecisionMaker.class
																										.getName());
	
	private static int				ignoreMultipleCollisions			= 1;
	private int							ignoreMultipleCollisionsCounter	= ignoreMultipleCollisions;
	private boolean					wasLastCollision						= false;
	
	@Configurable(comment = "the first MM of the spline are ignored by the collision detection. So it is possible to drive away from another bot or ball")
	private static float				ignoreFirstMillimeters				= 100;
	@Configurable(comment = "the last MM of the spline are ignored by the collision detection. So it is possible to drive to another bot or ball")
	private static float				ignoreLastMillimeters				= 100;
	@Configurable(comment = "maximum amount of iterations done by the collision detection (started at the position of the bot)")
	private static float				collisionIterationsMaximum			= 10;
	@Configurable(comment = "step size of iterations done by the collision detection (started at the position of the bot)")
	private static float				collisionIterationsStepSize		= 0.1f;
	@Configurable(comment = "distance from path to obstacles to fire a collision detection")
	private static float				collisionSafetyDistance				= -90;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final Path oldPath, final Path newPath)
	{
		FieldInformation fieldInfo = localPathFinderInput.getFieldInfo();
		
		if (oldPath.getHermiteSpline() == null)
		{
			return trueIfLastWasNotCollision("New Spline - Collision detection, spline == null", localPathFinderInput);
		}
		HermiteSplineTrajectory2D pt = oldPath.getHermiteSpline().getPositionTrajectory();
		
		// calculate the first and last seconds which should be ignored
		float ignoredFirstSeconds = pt.lengthToTime(ignoreFirstMillimeters);
		float lastMeters = DistanceUnit.METERS.toMillimeters(pt.getLength()) - ignoreLastMillimeters;
		if (lastMeters < 0)
		{
			lastMeters = DistanceUnit.METERS.toMillimeters(pt.getLength());
		}
		float ignoredLastSeconds = pt.lengthToTime(lastMeters);
		
		float totalTime = oldPath.getHermiteSpline().getPositionTrajectory().getTotalTime() - ignoredLastSeconds;
		float t = oldPath.getHermiteSpline().getTrajectoryTime();
		
		for (int i = 0; (i < collisionIterationsMaximum) && (t < totalTime); i++, t += collisionIterationsStepSize)
		{
			if ((t - oldPath.getHermiteSpline().getTrajectoryTime()) < ignoredFirstSeconds)
			{
				continue;
			}
			IVector2 p = DistanceUnit.METERS.toMillimeters(oldPath.getHermiteSpline().getPositionTrajectory()
					.getPosition(t));
			
			fieldInfo.fillBotPosList(t);
			fieldInfo.getMoveCon().setGoalPostObstacle(false);
			if (!fieldInfo.isPointOK(p, collisionSafetyDistance, true))
			{
				// we found a collision in the old path
				oldPath.setFirstCollisionAt(new Collision(t, p));
				// but we set it also to the new path, so it is shown even if the path changes
				newPath.setFirstCollisionAt(new Collision(t, p));
				return trueIfLastWasNotCollision("New Spline - Collision imminent @ t = " + t, localPathFinderInput);
			}
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
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
