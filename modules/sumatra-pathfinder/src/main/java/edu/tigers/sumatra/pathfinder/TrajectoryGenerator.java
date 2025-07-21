/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.DestinationForTimedPositionCalc;
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
	private static final DestinationForTimedPositionCalc OFFSET_CALC = new DestinationForTimedPositionCalc();

	//************************************************************************
	// Position Trajectory
	//************************************************************************


	public static ITrajectory<IVector2> generatePositionTrajectory(ITrackedBot bot, IVector2 dest)
	{
		IMoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), dest);
	}


	public static ITrajectory<IVector2> generatePositionTrajectory(ITrackedBot bot, IVector2 dest,
			IMoveConstraints mc)
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
	public static ITrajectory<IVector2> generatePositionTrajectory(
			IMoveConstraints mc,
			IVector2 curPos,
			IVector2 curVel,
			IVector2 dest
	)
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

	//************************************************************************
	// Rotation Trajectory
	//************************************************************************


	public static ITrajectory<Double> generateRotationTrajectory(
			ITrackedBot bot,
			double targetAngle,
			IMoveConstraints mc
	)
	{
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	public static ITrajectory<Double> generateRotationTrajectory(
			ITrackedBot bot,
			double targetAngle
	)
	{
		IMoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	private static ITrajectory<Double> generateRotationTrajectory(
			double curOrientation,
			double curAVel,
			double targetAngle,
			IMoveConstraints mc
	)
	{
		return TRAJECTORY_FACTORY.orientation((float) curOrientation, (float) targetAngle, (float) curAVel,
				(float) mc.getVelMaxW(),
				(float) mc.getAccMaxW());
	}


	public static boolean isComeToAStopFaster(ITrackedBot bot, IVector2 dest)
	{
		return isComeToAStopFaster(bot.getMoveConstraints(), bot.getPos(), bot.getVel(), dest);
	}


	public static boolean isComeToAStopFaster(MoveConstraints mc, IVector2 curPos, IVector2 curVel, IVector2 dest)
	{
		var trajWithoutComeToAStop = generatePositionTrajectory(mc, curPos, curVel, dest);
		var stateAfterComeToAStop = stateAfterComeToAStop(
				mc,
				trajWithoutComeToAStop.getPosition(0),
				trajWithoutComeToAStop.getVelocity(0)
		);

		var end = trajWithoutComeToAStop.getPosition(trajWithoutComeToAStop.getTotalTime()).multiplyNew(1e3);

		var trajAfterBrk = generatePositionTrajectory(
				mc,
				stateAfterComeToAStop.pos,
				stateAfterComeToAStop.vel,
				end
		);

		return trajAfterBrk.getTotalTime() + stateAfterComeToAStop.lookAhead + 0.01
				< trajWithoutComeToAStop.getTotalTime();
	}

	//************************************************************************
	// Overshoot Trajectory
	//************************************************************************


	public static IVector2 generateVirtualPositionToReachPointInTime(
			ITrackedBot bot,
			IVector2 dest,
			double targetTime
	)
	{
		return generateVirtualPositionToReachPointInTime(
				bot.getMoveConstraints(),
				bot.getPos(),
				bot.getVel(),
				dest,
				targetTime
		);
	}


	/**
	 * @param mc
	 * @param curPos [mm]
	 * @param curVel [m/s]
	 * @param dest   [mm]
	 * @return
	 */
	public static IVector2 generateVirtualPositionToReachPointInTime(
			MoveConstraints mc,
			IVector2 curPos,
			IVector2 curVel,
			IVector2 dest,
			double targetTime
	)
	{
		var posInM = curPos.multiplyNew(1e-3);
		var destInM = dest.multiplyNew(1e-3);
		if (mc.getPrimaryDirection().isZeroVector())
		{
			return OFFSET_CALC.destinationForBangBang2dSync(
					posInM,
					destInM,
					curVel,
					mc.getVelMax(),
					mc.getAccMax(),
					targetTime
			).multiplyNew(1e3);
		}
		return OFFSET_CALC.destinationForBangBang2dAsync(
				posInM,
				destInM,
				curVel,
				mc.getVelMax(),
				mc.getAccMax(),
				targetTime,
				mc.getPrimaryDirection()
		).multiplyNew(1e3);
	}


	public static ITrajectory<IVector2> generatePositionTrajectoryToReachPointInTime(
			ITrackedBot bot,
			IVector2 dest,
			double targetTime
	)
	{
		return generatePositionTrajectoryToReachPointInTime(
				bot.getMoveConstraints(),
				bot.getPos(),
				bot.getVel(),
				dest,
				targetTime
		);
	}


	public static ITrajectory<IVector2> generatePositionTrajectoryToReachPointInTime(
			final MoveConstraints moveConstraints,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest,
			final double targetTime
	)
	{
		// This can get optimized as an addition to the generateVirtualPositionToReachPointInTime could directly create
		// full trajectories and not only a position.
		var virtualDest = generateVirtualPositionToReachPointInTime(moveConstraints, curPos, curVel, dest, targetTime);
		return generatePositionTrajectory(moveConstraints, curPos, curVel, virtualDest);
	}


	public static boolean isComeToAStopFasterToReachPointInTime(ITrackedBot bot, IVector2 dest, double targetTime)
	{
		return isComeToAStopFasterToReachPointInTime(bot.getMoveConstraints(), bot.getPos(), bot.getVel(), dest,
				targetTime);
	}


	public static boolean isComeToAStopFasterToReachPointInTime(MoveConstraints mc, IVector2 curPos, IVector2 curVel,
			IVector2 dest, double targetTime)
	{
		var trajWithoutComeToAStop = generatePositionTrajectoryToReachPointInTime(mc, curPos, curVel, dest, targetTime);
		var stateAfterComeToAStop = stateAfterComeToAStop(
				mc,
				trajWithoutComeToAStop.getPosition(0),
				trajWithoutComeToAStop.getVelocity(0)
		);

		var trajAfterBrk = generatePositionTrajectoryToReachPointInTime(
				mc,
				stateAfterComeToAStop.pos.multiplyNew(1e3),
				stateAfterComeToAStop.vel,
				dest,
				targetTime - stateAfterComeToAStop.lookAhead
		);

		return trajAfterBrk.getTotalTime() + stateAfterComeToAStop.lookAhead + 0.01
				< trajWithoutComeToAStop.getTotalTime();
	}

	//************************************************************************
	// Private Helper
	//************************************************************************


	private static StateAfterComeToAStop stateAfterComeToAStop(MoveConstraints mc, IVector2 s0, IVector2 v0)
	{

		var lookAhead = 0.05; // 5 AI iterations
		var acc = mc.getBrkMax() * 0.9;

		var tBreak = SumatraMath.min(v0.getLength() / acc, lookAhead);
		var a0 = v0.scaleToNew(-acc);
		var v1 = a0.multiplyNew(tBreak).add(v0);
		var s1 = s0.addNew(v0.multiplyNew(tBreak)).add(a0.multiplyNew(0.5 * tBreak * tBreak)).multiply(1e3);

		return new StateAfterComeToAStop(s1, v1, lookAhead);
	}


	private record StateAfterComeToAStop(IVector2 pos, IVector2 vel, double lookAhead)
	{
	}

}
