/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Generator for hermite splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SplineGenerator
{
	@Configurable(comment = "Vel [m/s] - Max linear velocity to use for spline generation")
	private static float	maxLinearVelocity			= 2.5f;
	@Configurable(comment = "Acc [m/s^2] - Max linear acceleration to use for spline generation")
	private static float	maxLinearAcceleration	= 2.5f;
	
	@Configurable(comment = "Vel [rad/s] - Max rotation velocity to use for spline generation")
	private static float	maxRotateVelocity			= 10;
	@Configurable(comment = "Vel [rad/s^2] - Max rotation acceleration to use for spline generation")
	private static float	maxRotateAcceleration	= 15;
	
	
	@Configurable(comment = "Points on a path with a combined angle*distance score below this value will be removed")
	private static float	pathReductionScore		= 0.0f;
	
	
	/**
	 */
	public SplineGenerator()
	{
	}
	
	
	/**
	 * @param botType
	 */
	public SplineGenerator(final EBotType botType)
	{
		// parameter not needed atm, but constructor kept for compatibility
	}
	
	
	/**
	 * @return
	 */
	public SplineTrajectoryGenerator createDefaultGenerator()
	{
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(maxLinearVelocity, maxLinearAcceleration);
		gen.setReducePathScore(pathReductionScore);
		gen.setRotationTrajParams(maxRotateVelocity, maxRotateAcceleration);
		return gen;
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param bot
	 * @param nodes on the path including destination and excluding current position
	 * @param finalOrientation
	 * @param speedLimit max velocity to use
	 * @return
	 */
	public SplinePair3D createSpline(final TrackedTigerBot bot, final List<IVector2> nodes,
			final float finalOrientation, final float speedLimit)
	{
		SplineTrajectoryGenerator gen = createDefaultGenerator();
		if (speedLimit > 0)
		{
			gen.setMaxVelocity(speedLimit);
		}
		return createSpline(bot, nodes, finalOrientation, gen);
	}
	
	
	/**
	 * @param bot
	 * @param nodes
	 * @param finalOrientation
	 * @param gen
	 * @return
	 */
	public SplinePair3D createSpline(final TrackedTigerBot bot, final List<IVector2> nodes,
			final float finalOrientation,
			final SplineTrajectoryGenerator gen)
	{
		List<IVector2> nodesMeters = new ArrayList<IVector2>(nodes.size() + 1);
		nodesMeters.add(convertAIVector2SplineNode(bot.getPos()));
		
		for (IVector2 vec : nodes)
		{
			nodesMeters.add(convertAIVector2SplineNode(vec));
		}
		
		return gen.create(nodesMeters, bot.getVel(), AVector2.ZERO_VECTOR,
				convertAIAngle2SplineOrientation(bot.getAngle()),
				convertAIAngle2SplineOrientation(finalOrientation), bot.getaVel(), 0f);
	}
	
	
	private IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	private float convertAIAngle2SplineOrientation(final float angle)
	{
		return angle;
	}
	
	
	/**
	 * @return the maxLinearVelocity
	 */
	public static float getMaxLinearVelocity()
	{
		return maxLinearVelocity;
	}
	
	
	/**
	 * @return the maxLinearAcceleration
	 */
	public static float getMaxLinearAcceleration()
	{
		return maxLinearAcceleration;
	}
	
	
	/**
	 * @return the maxRotateVelocity
	 */
	public static float getMaxRotateVelocity()
	{
		return maxRotateVelocity;
	}
	
	
	/**
	 * @return the maxRotateAcceleration
	 */
	public static float getMaxRotateAcceleration()
	{
		return maxRotateAcceleration;
	}
}
