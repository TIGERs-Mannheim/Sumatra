/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DAsync;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajectoryGenerator
{
	/**
	 * 
	 */
	private TrajectoryGenerator()
	{
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest)
	{
		MoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generatePositionTrajectory(mc, bot.getPos(), bot.getVel(), dest);
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param moveConstraints
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest,
			final MoveConstraints moveConstraints)
	{
		return generatePositionTrajectory(moveConstraints, bot.getPos(), bot.getVel(), dest);
	}
	
	
	/**
	 * @param moveConstraints
	 * @param curPos [mm]
	 * @param curVel
	 * @param dest [mm]
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectory(final MoveConstraints moveConstraints,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		if (moveConstraints.getPrimaryDirection().isZeroVector())
		{
			return new BangBangTrajectory2D(
					curPos.multiplyNew(1e-3f),
					dest.multiplyNew(1e-3f),
					curVel,
					moveConstraints.getVelMax(),
					moveConstraints.getAccMax());
		}
		
		return new BangBangTrajectory2DAsync(
				curPos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				curVel,
				moveConstraints.getVelMax(),
				moveConstraints.getAccMax(),
				moveConstraints.getPrimaryDirection());
	}
	
	
	/**
	 * @param bot
	 * @param targetAngle
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle)
	{
		MoveConstraints mc = new MoveConstraints(bot.getRobotInfo().getBotParams().getMovementLimits());
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, mc);
	}
	
	
	/**
	 * @param bot
	 * @param targetAngle
	 * @param moveConstraints
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle,
			final MoveConstraints moveConstraints)
	{
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, moveConstraints);
	}
	
	
	/**
	 * @param curOrientation
	 * @param curAVel
	 * @param targetAngle
	 * @param moveConstraints
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectory(
			final double curOrientation,
			final double curAVel,
			final double targetAngle,
			final MoveConstraints moveConstraints)
	{
		return new BangBangTrajectory1DOrient(curOrientation, targetAngle, curAVel, moveConstraints.getVelMaxW(),
				moveConstraints.getAccMaxW());
	}
	
	
	/**
	 * Generate a trajectory with zero length
	 * 
	 * @param pos
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectoryStub(final double pos)
	{
		return new BangBangTrajectory1DOrient(pos, pos, 0, 10, 10);
	}
}
