/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.decisionMaker;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Collision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.IUpdateSplineDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * checks if the bot will crash if it follows the current spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class CollisionDetectionDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log					= Logger.getLogger(CollisionDetectionDecisionMaker.class.getName());
	
	private int							avoidedCollisions	= 0;
	
	private boolean					wasLastCollision	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath)
	{
		if (oldPath.isOld())
		{
			avoidedCollisions = 0;
		}
		
		if (oldPath.isRambo())
		{
			return trueIfLastWasNotCollision("New Spline - Collision detection, RAMBO ("
					+ localPathFinderInput.getBotId().getNumber() + ")", localPathFinderInput);
		}
		
		FieldInformation fieldInfo = localPathFinderInput.getFieldInfo();
		
		if (oldPath.getHermiteSpline() == null)
		{
			return trueIfLastWasNotCollision("New Spline - Collision detection, spline == null", localPathFinderInput);
		}
		float totalTime = oldPath.getHermiteSpline().getPositionTrajectory().getTotalTime()
				- AIConfig.getOptimization().getIgnoreLastSeconds();
		float t = localPathFinderInput.getCurrentTimeOnSpline();
		
		for (int i = 0; (i < AIConfig.getOptimization().getCollisionIterationsMaximum()) && (t < totalTime); i++, t += AIConfig
				.getOptimization().getCollisionIterationsStepSize())
		{
			IVector2 p = DistanceUnit.METERS.toMillimeters(oldPath.getHermiteSpline().getPositionTrajectory()
					.getPosition(t));
			if (t < AIConfig.getOptimization().getIgnoreFirstSeconds())
			{
				continue;
			}
			fieldInfo.changeBotsInListToTimeInFuture(t);
			if (!fieldInfo.isPointOK(p))
			{
				// we found a collision in the old path
				oldPath.setFirstCollisionAt(new Collision(t, p));
				// but we set it also to the new path, so it is shown even if the path changes
				newPath.setFirstCollisionAt(new Collision(t, p));
				return trueIfLastWasNotCollision("New Spline - Collision imminent @ t = " + t + ", this is the "
						+ avoidedCollisions + ". collision in serie", localPathFinderInput);
			}
		}
		fieldInfo.changeBotsInListToTimeInFuture(0);
		avoidedCollisions = 0;
		wasLastCollision = false;
		return EDecision.NO_VIOLATION;
	}
	
	
	private EDecision trueIfLastWasNotCollision(String logMessage, PathFinderInput localPathFinderInput)
	{
		avoidedCollisions++;
		localPathFinderInput.setAvoidedCollisionsSeries(avoidedCollisions);
		if (!wasLastCollision)
		{
			log.trace(logMessage);
			wasLastCollision = true;
			return EDecision.COLLISION_AHEAD;
		}
		return EDecision.NO_VIOLATION;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
