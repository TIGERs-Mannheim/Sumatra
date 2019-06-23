/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajectoryGenerator
{
	/**
	 * 
	 */
	public TrajectoryGenerator()
	{
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @return
	 */
	public BangBangTrajectory2D generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest)
	{
		return generatePositionTrajectory(bot.getBot().getMoveConstraints(), bot.getPos(), bot.getVel(), dest);
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param moveConstraints
	 * @return
	 */
	public BangBangTrajectory2D generatePositionTrajectory(final ITrackedBot bot, final IVector2 dest,
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
	public BangBangTrajectory2D generatePositionTrajectory(final MoveConstraints moveConstraints, final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		return new BangBangTrajectory2D(
				curPos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				curVel,
				moveConstraints.getAccMax(),
				moveConstraints.getAccMax(),
				moveConstraints.getVelMax());
	}
	
	
	/**
	 * Generate a trajectory with zero length
	 * 
	 * @param pos
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectoryStub(final IVector2 pos)
	{
		return new BangBangTrajectory2D(pos.multiplyNew(1e-3f), pos.multiplyNew(1e-3f), AVector2.ZERO_VECTOR, 2, 2, 2);
	}
	
	
	/**
	 * @param bot
	 * @param targetAngle
	 * @param maxAcc
	 * @param maxVel
	 * @return
	 */
	public BangBangTrajectory1DOrient generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle,
			final double maxAcc,
			final double maxVel)
	{
		return generateRotationTrajectory(bot.getAngle(), bot.getaVel(), targetAngle, maxAcc, maxVel);
	}
	
	
	/**
	 * @param curOrientation
	 * @param curAVel
	 * @param targetAngle
	 * @param maxAcc
	 * @param maxVel
	 * @return
	 */
	public BangBangTrajectory1DOrient generateRotationTrajectory(
			final double curOrientation,
			final double curAVel,
			final double targetAngle,
			final double maxAcc,
			final double maxVel)
	{
		return new BangBangTrajectory1DOrient(curOrientation, targetAngle, curAVel, maxAcc, maxAcc, maxVel);
	}
	
	
	/**
	 * Generate a trajectory with zero length
	 * 
	 * @param pos
	 * @param trajXY
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectoryStub(final double pos,
			final BangBangTrajectory2D trajXY)
	{
		return new BangBangTrajectory1DOrient(pos, pos, 0, 10, 10, 20);
	}
}
