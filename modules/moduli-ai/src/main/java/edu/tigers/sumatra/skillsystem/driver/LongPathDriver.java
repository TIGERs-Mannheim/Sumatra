/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2015
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.Vector3f;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author kisle
 */
public class LongPathDriver extends HermiteSplinePathDriver
{
	@Configurable(comment = "Driving Angels")
	private static Double[]	lookAngles						= { 0.0, AngleMath.PI };
																		
																		
	private final IVector3	destination;
									
									
	@Configurable(comment = "Time to Start Rotation")
	private static double	startTimeFinalOrientation	= 0.1;
																		
	@Configurable(comment = "Time to Start Rotation")
	private static double	minSplineTime					= 1.0;
																		
	@Configurable(comment = "time [s]")
	private static double	positionMoveLookAhead		= 0.3;
																		
																		
	static
	{
		ConfigRegistration.registerClass("skills", LongPathDriver.class);
	}
	
	
	/**
	 * @param bot
	 * @param path
	 */
	public LongPathDriver(final ITrackedBot bot, final IPath path)
	{
		super(bot, path);
		destination = new Vector3(path.getEnd(), path.getTargetOrientation());
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		final double nextOrientation;
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
		double ct = (wFrame.getTimestamp() - getStartTime()) / 1e9;
		if ((getSpline().getTotalTime() - ct) > startTimeFinalOrientation)
		{
			nextOrientation = AngleMath.normalizeAngle(rotateAngle + smallestAngle + bot2Target.getAngle());
		} else
		{
			nextOrientation = destination.z();
		}
		return new Vector3f(super.getNextDestination(bot, wFrame).getXYVector(), nextOrientation);
	}
}
