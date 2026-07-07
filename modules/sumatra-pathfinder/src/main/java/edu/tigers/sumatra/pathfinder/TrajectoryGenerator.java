package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TimedTrajectory2D;
import edu.tigers.sumatra.trajectory.TimedTrajectoryFactory;
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
	private static final TimedTrajectoryFactory TIMED_TRAJECTORY_FACTORY = new TimedTrajectoryFactory();

	//************************************************************************
	// Position Trajectory
	//************************************************************************


	public static ITrajectory<IVector2> generatePositionTrajectory(ITrackedBot bot, IVector2 dest)
	{
		MoveConstraints mc = MoveConstraints.from(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), dest);
	}


	public static ITrajectory<IVector2> generatePositionTrajectory(
			ITrackedBot bot, IVector2 dest,
			MoveConstraints mc
	)
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
			MoveConstraints mc,
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
					mc.getAccMax()
			);
		}

		return TRAJECTORY_FACTORY.async(
				curPosM,
				destM,
				curVel,
				mc.getVelMax(),
				mc.getAccMax(),
				mc.getPrimaryDirection()
		);
	}

	//************************************************************************
	// Position Trajectory ComeToAStop Mechanism
	//************************************************************************


	public static boolean isComeToAStopFaster(ITrackedBot bot, IVector2 dest)
	{
		return isComeToAStopFaster(bot.getMoveConstraints(), bot.getPos(), bot.getVel(), dest);
	}


	public static boolean isComeToAStopFaster(MoveConstraints mc, IVector2 curPos, IVector2 curVel, IVector2 dest)
	{
		return isComeToAStopFaster(mc, generatePositionTrajectory(mc, curPos, curVel, dest));
	}


	public static boolean isComeToAStopFaster(MoveConstraints mc, ITrajectory<IVector2> trajWithoutComeToAStop)
	{
		var stateAfterComeToAStop = stateAfterComeToAStop(
				mc,
				trajWithoutComeToAStop.getPosition(0),
				trajWithoutComeToAStop.getVelocity(0)
		);

		var trajAfterBrk = generatePositionTrajectory(
				mc,
				stateAfterComeToAStop.posMM,
				stateAfterComeToAStop.vel,
				trajWithoutComeToAStop.getFinalDestination()
		);

		return trajAfterBrk.getTotalTime() + stateAfterComeToAStop.lookAhead + 0.01
				< trajWithoutComeToAStop.getTotalTime();
	}

	//************************************************************************
	// Rotation Trajectory
	//************************************************************************


	public static ITrajectory<Double> generateRotationTrajectory(
			ITrackedBot bot,
			double targetAngle,
			MoveConstraints mc
	)
	{
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	public static ITrajectory<Double> generateRotationTrajectory(
			ITrackedBot bot,
			double targetAngle
	)
	{
		MoveConstraints mc = MoveConstraints.from(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}


	private static ITrajectory<Double> generateRotationTrajectory(
			double curOrientation,
			double curAVel,
			double targetAngle,
			MoveConstraints mc
	)
	{
		return TRAJECTORY_FACTORY.orientation(
				(float) curOrientation, (float) targetAngle, (float) curAVel,
				(float) mc.getVelMaxW(),
				(float) mc.getAccMaxW()
		);
	}

	//************************************************************************
	// Overshoot Trajectory
	//************************************************************************


	public static TimedTrajectory2D generateTimedPositionTrajectory(
			ITrackedBot bot,
			IVector2 dest,
			double targetTime
	)
	{

		return generateTimedPositionTrajectory(
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
	public static TimedTrajectory2D generateTimedPositionTrajectory(
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
			return TIMED_TRAJECTORY_FACTORY.fromDestinationAndTimeSync(
					posInM,
					destInM,
					curVel,
					mc.getVelMax(),
					mc.getAccMax(),
					targetTime
			);
		}
		return TIMED_TRAJECTORY_FACTORY.fromDestinationAndTimeAsync(
				posInM,
				destInM,
				curVel,
				mc.getVelMax(),
				mc.getAccMax(),
				targetTime,
				mc.getPrimaryDirection()
		);
	}

	//************************************************************************
	// Overshoot Trajectory ComeToAStop Mechanism
	//************************************************************************


	public static boolean isComeToAStopFasterTimed(ITrackedBot bot, IVector2 dest, double targetTime)
	{
		return isComeToAStopFasterTimed(
				bot.getMoveConstraints(),
				bot.getPos(),
				bot.getVel(),
				dest,
				targetTime
		);
	}


	public static boolean isComeToAStopFasterTimed(
			MoveConstraints mc,
			IVector2 curPos,
			IVector2 curVel,
			IVector2 dest,
			double targetTime
	)
	{
		return isComeToAStopFasterTimed(mc, generateTimedPositionTrajectory(mc, curPos, curVel, dest, targetTime));
	}


	public static boolean isComeToAStopFasterTimed(MoveConstraints mc, TimedTrajectory2D trajWithoutComeToAStop)
	{
		var targetTime = trajWithoutComeToAStop.getTargetTime();
		var targetDest = trajWithoutComeToAStop.getTargetDestMM();

		var stateAfterComeToAStop = stateAfterComeToAStop(
				mc,
				trajWithoutComeToAStop.getPosition(0),
				trajWithoutComeToAStop.getVelocity(0)
		);

		var targetTimeAfterBrk = targetTime - stateAfterComeToAStop.lookAhead;
		var trajAfterBrk = generateTimedPositionTrajectory(
				mc,
				stateAfterComeToAStop.posMM,
				stateAfterComeToAStop.vel,
				targetDest,
				targetTimeAfterBrk
		);

		var distWithoutComeToAStop = targetDest.distanceTo(trajWithoutComeToAStop.getPositionMM(targetTime));
		var distAfterBrk = targetDest.distanceTo(stateAfterComeToAStop.getPositionMM(trajAfterBrk, targetTime));
		if (Math.abs(distWithoutComeToAStop - distAfterBrk) > 10)
		{
			// Significant difference detected in capability of reaching the target
			return distAfterBrk < distWithoutComeToAStop;
		}

		if (SumatraMath.isZero(distAfterBrk) && SumatraMath.isZero(distWithoutComeToAStop))
		{
			// Both trajectories can reach the destination in time -> we use same approach as none-timed trajectories
			return trajAfterBrk.getTotalTime() + stateAfterComeToAStop.lookAhead + 0.01
					< trajWithoutComeToAStop.getTotalTime();
		}

		// Both scenarios have very similar impacts to the capability to reach the position at the wanted timestamp. There are quite some mathematical instabilities  -> do not use come to a stop in this case
		return false;
	}

	//************************************************************************
	// Private Helper
	//************************************************************************


	private static StateAfterComeToAStop stateAfterComeToAStop(MoveConstraints mc, IVector2 s0, IVector2 v0)
	{
		var acc = mc.getBrkMax() * 0.9;

		var tBrake = SumatraMath.min(v0.getLength() / acc, 0.05);
		var a0 = v0.scaleToNew(-acc);
		var v1 = a0.multiplyNew(tBrake).add(v0);
		var s1 = s0.addNew(v0.multiplyNew(tBrake)).add(a0.multiplyNew(0.5 * tBrake * tBrake)).multiply(1e3);

		return new StateAfterComeToAStop(s1, v1, tBrake, s0, v0, a0);
	}


	private record StateAfterComeToAStop(
			IVector2 posMM,
			IVector2 vel,
			double lookAhead,
			IVector2 s0,
			IVector2 v0,
			IVector2 a0
	)
	{
		private IVector2 getPositionMM(ITrajectory<IVector2> trajectoryAfterBreak, double t)
		{
			if (t >= lookAhead)
			{
				return trajectoryAfterBreak.getPositionMM(t - lookAhead);
			}
			var tBrake = SumatraMath.max(t, 0);
			return s0.addNew(v0.multiplyNew(tBrake)).add(a0.multiplyNew(0.5 * tBrake * tBrake)).multiply(1e3);
		}
	}
}
