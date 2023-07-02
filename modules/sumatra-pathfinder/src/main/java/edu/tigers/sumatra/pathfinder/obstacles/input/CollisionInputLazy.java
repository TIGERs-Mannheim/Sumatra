/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles.input;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CollisionInputLazy implements CollisionInput
{
	private IVector2 robotPos;
	private IVector2 robotVel;
	private IVector2 robotAcc;
	private Boolean accelerating;
	private Double extraMargin;

	private final ITrajectory<IVector2> trajectory;
	private final double timeOffset;


	@Override
	public boolean accelerating()
	{
		if (accelerating == null)
		{
			accelerating = CollisionInput.super.accelerating();
		}
		return accelerating;
	}


	@Override
	public IVector2 getRobotPos()
	{
		if (robotPos == null)
		{
			robotPos = trajectory.getPositionMM(timeOffset);
		}
		return robotPos;
	}


	@Override
	public IVector2 getRobotVel()
	{
		if (robotVel == null)
		{
			robotVel = trajectory.getVelocity(timeOffset);
		}
		return robotVel;
	}


	@Override
	public IVector2 getRobotAcc()
	{
		if (robotAcc == null)
		{
			robotAcc = trajectory.getAcceleration(timeOffset);
		}
		return robotAcc;
	}


	@Override
	public double getTimeOffset()
	{
		return timeOffset;
	}


	@Override
	public double getExtraMargin()
	{
		if (extraMargin == null)
		{
			extraMargin = CollisionInput.super.getExtraMargin();
		}
		return extraMargin;
	}
}
