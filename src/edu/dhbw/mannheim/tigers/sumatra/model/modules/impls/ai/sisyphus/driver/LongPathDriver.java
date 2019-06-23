/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2015
 * Author(s): kisle
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author kisle
 */
public class LongPathDriver extends HermiteSplinePathDriver
{
	
	@Configurable(comment = "Driving Angels")
	private static Float[]	lookAngles						= { 0.0f, AngleMath.PI };
	
	
	private IVector3			destination;
	
	
	@Configurable(comment = "Time to Start Rotation")
	private static float		startTimeFinalOrientation	= 0.1f;
	
	@Configurable(comment = "Time to Start Rotation")
	private static float		minSplineTime					= 1.0f;
	
	@Configurable(comment = "time [s]")
	private static float		positionMoveLookAhead		= 0.3f;
	
	
	/**
	 * @param bot
	 * @param path
	 * @param maxSpeed
	 * @param forcedEndTime
	 */
	public LongPathDriver(final TrackedTigerBot bot, final IPath path, final float maxSpeed,
			final float forcedEndTime)
	{
		super(bot, path, maxSpeed, forcedEndTime);
		destination = new Vector3(path.getEnd(), path.getTargetOrientation());
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		final float nextOrientation;
		IVector3 nextDest = super.getNextDestination(bot, wFrame);
		IVector2 targetPosition = nextDest.getXYVector();
		IVector2 bot2Target = targetPosition.subtractNew(bot.getPos());
		double rotateAngle = AngleMath.normalizeAngle(GeoMath.angleBetweenVectorAndVectorWithNegative(
				targetPosition.subtractNew(bot.getPos()), new Vector2(bot.getAngle())));
		
		if (getSpline().getTotalTime() < minSplineTime)
		{
			return nextDest;
		}
		
		double smallestAngle = Integer.MAX_VALUE;
		double diff = Integer.MAX_VALUE;
		
		for (double lookAngle : lookAngles)
		{
			if (Math.abs(rotateAngle - lookAngle) < diff)
			{
				diff = Math.abs(rotateAngle - lookAngle);
				smallestAngle = lookAngle;
			}
		}
		
		// Final Rotation?
		if ((getSpline().getTotalTime() - getSpline().getCurrentTime()) > startTimeFinalOrientation)
		{
			nextOrientation = AngleMath.normalizeAngle((float) (rotateAngle + smallestAngle + bot2Target.getAngle()));
		} else
		{
			nextOrientation = destination.z();
		}
		return new Vector3f(super.getNextDestination(bot, wFrame).getXYVector(), nextOrientation);
	}
}
