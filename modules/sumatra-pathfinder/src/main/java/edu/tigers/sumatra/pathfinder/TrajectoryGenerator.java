/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Generate BangBang trajectories
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrajectoryGenerator
{
	private static final BangBangTrajectoryFactory TRAJECTORY_FACTORY = new BangBangTrajectoryFactory();


	public static ITrajectory<IVector2> generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest)
	{
		IMoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), dest);
	}


	public static ITrajectory<IVector2> generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest,
			final IMoveConstraints mc)
	{
		return generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), dest);
	}


	/**
	 * @param mc
	 * @param curPos [mm]
	 * @param curVel [m/s]
	 * @param dest   [mm]
	 * @return
	 */
	public static ITrajectory<IVector2> generatePositionTrajectory(final IMoveConstraints mc,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		IVector2 curPosM = curPos.multiplyNew(1e-3f);
		IVector2 destM = dest.multiplyNew(1e-3f);

		if (mc.getPrimaryDirection().isZeroVector())
		{
			return TRAJECTORY_FACTORY.sync(
					curPosM,
					destM,
					curVel,
					mc.getVelMax(),
					mc.getAccMaxDerived());
		}

		return TRAJECTORY_FACTORY.async(
				curPosM,
				destM,
				curVel,
				mc.getVelMax(),
				mc.getAccMaxDerived(),
				mc.getPrimaryDirection());
	}


	public static ITrajectory<Double> generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle,
			final IMoveConstraints mc)
	{
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	public static ITrajectory<Double> generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle)
	{
		IMoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	private static ITrajectory<Double> generateRotationTrajectory(
			final double curOrientation,
			final double curAVel,
			final double targetAngle,
			final IMoveConstraints mc)
	{
		return TRAJECTORY_FACTORY.orientation((float) curOrientation, (float) targetAngle, (float) curAVel,
				(float) mc.getVelMaxW(),
				(float) mc.getAccMaxW());
	}
}
