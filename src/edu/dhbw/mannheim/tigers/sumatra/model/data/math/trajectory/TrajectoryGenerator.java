/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajectoryGenerator
{
	/**
	 * @param bot
	 * @param dest
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectory(final TrackedTigerBot bot, final IVector2 dest)
	{
		return generatePositionTrajectory(bot.getBot(), bot.getPos(), bot.getVel(), dest);
	}
	
	
	/**
	 * @param bot
	 * @param curPos [mm]
	 * @param curVel
	 * @param dest [mm]
	 * @return
	 */
	public static BangBangTrajectory2D generatePositionTrajectory(final ABot bot, final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		final float maxAcc, maxBrk, maxVel;
		maxAcc = bot.getPerformance().getAccMax();
		maxBrk = bot.getPerformance().getBrkMax();
		maxVel = bot.getPerformance().getVelMax();
		return new BangBangTrajectory2D(curPos.multiplyNew(1e-3f), dest.multiplyNew(1e-3f), curVel, maxAcc, maxBrk,
				maxVel);
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
	 * @param trajXY
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectory(final TrackedTigerBot bot,
			final float targetAngle,
			final BangBangTrajectory2D trajXY)
	{
		return generateRotationTrajectory(bot.getBot(), bot.getAngle(), bot.getaVel(), targetAngle, trajXY);
	}
	
	
	/**
	 * @param bot
	 * @param curOrientation
	 * @param curAVel
	 * @param targetAngle
	 * @param trajXY
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectory(final ABot bot, final float curOrientation,
			final float curAVel,
			final float targetAngle,
			final BangBangTrajectory2D trajXY)
	{
		final float maxAcc, maxBrk, maxVel;
		maxAcc = bot.getPerformance().getAccMaxW();
		maxBrk = bot.getPerformance().getBrkMaxW();
		maxVel = bot.getPerformance().getVelMaxW();
		
		return new BangBangTrajectory1DOrient(curOrientation, targetAngle, curAVel, maxAcc, maxBrk, maxVel);
	}
	
	
	/**
	 * Generate a trajectory with zero length
	 * 
	 * @param pos
	 * @param trajXY
	 * @return
	 */
	public static BangBangTrajectory1DOrient generateRotationTrajectoryStub(final float pos,
			final BangBangTrajectory2D trajXY)
	{
		return new BangBangTrajectory1DOrient(pos, pos, 0f, 10f, 10f, 20f);
	}
}
