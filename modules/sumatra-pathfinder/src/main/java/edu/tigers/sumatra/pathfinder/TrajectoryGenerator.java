/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DAsync;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Generate BangBang trajectories
 */
public final class TrajectoryGenerator
{
	private static final double MAX_LUT_POS = 18.0;
	private static final double MAX_LUT_VEL = 8.0;
	private static final int LUT_TRIGGER_LIMIT = 200_000;
	
	private static final TrajectoryBooster BOOSTER;
	
	@Configurable(comment = "Use trajectory booster, which uses a LUT too boost generation - warning: accuracy is too low! Note: Needs a Sumatra restart.", defValue = "false")
	private static boolean useTrajectoryBooster = false;
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", TrajectoryGenerator.class);
		
		if (useTrajectoryBooster)
		{
			BOOSTER = new TrajectoryBooster(MAX_LUT_POS, MAX_LUT_VEL, LUT_TRIGGER_LIMIT);
		} else
		{
			BOOSTER = null;
		}
	}
	
	
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
		IVector2 curPosM = curPos.multiplyNew(1e-3f);
		IVector2 destM = dest.multiplyNew(1e-3f);
		IVector2 limitedVel = curVel;
		if (limitedVel.getLength2() > moveConstraints.getVelMax())
		{
			limitedVel = curVel.scaleToNew(moveConstraints.getVelMax());
		}
		
		if (moveConstraints.getPrimaryDirection().isZeroVector())
		{
			if (BOOSTER != null)
			{
				Optional<BangBangTrajectory2D> boostedTraj = BOOSTER.query(moveConstraints, curPosM, limitedVel, destM);
				if (boostedTraj.isPresent())
				{
					return boostedTraj.get();
				}
			}
			
			return new BangBangTrajectory2D(
					curPosM,
					destM,
					limitedVel,
					moveConstraints.getVelMax(),
					moveConstraints.getAccMax());
		}
		
		return new BangBangTrajectory2DAsync(
				curPosM,
				destM,
				limitedVel,
				moveConstraints.getVelMax(),
				moveConstraints.getAccMax(),
				moveConstraints.getPrimaryDirection());
	}
	
	
	public static BangBangTrajectory1DOrient generateRotationTrajectory(
			final ITrackedBot bot,
			final double targetAngle,
			final MoveConstraints moveConstraints)
	{
		return generateRotationTrajectory(bot.getOrientation(), bot.getAngularVel(), targetAngle, moveConstraints);
	}
	
	
	private static BangBangTrajectory1DOrient generateRotationTrajectory(
			final double curOrientation,
			final double curAVel,
			final double targetAngle,
			final MoveConstraints moveConstraints)
	{
		return new BangBangTrajectory1DOrient((float) curOrientation, (float) targetAngle, (float) curAVel,
				(float) moveConstraints.getVelMaxW(),
				(float) moveConstraints.getAccMaxW());
	}
	
	
	/**
	 * Generate a trajectory with zero length
	 * 
	 * @param pos
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectoryStub(final double pos)
	{
		return new BangBangTrajectory1DOrient((float) pos, (float) pos, 0f, 10f, 10f);
	}
}
