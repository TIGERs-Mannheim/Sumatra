/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.chipped.ChipBallConsultant;
import edu.tigers.sumatra.ball.trajectory.chipped.ChipBallTrajectory;
import edu.tigers.sumatra.ball.trajectory.flat.FlatBallConsultant;
import edu.tigers.sumatra.ball.trajectory.flat.FlatBallTrajectory;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Factory class for creating classes for the configured ball models.
 */
@RequiredArgsConstructor
public final class BallFactory
{
	@Getter
	private final BallParameters ballParams;


	public IBallTrajectory createTrajectoryFromState(final BallState state)
	{
		if (state.isChipped())
		{
			return ChipBallTrajectory.fromState(ballParams, state.getPos(), state.getVel(), state.getSpin());
		}

		return FlatBallTrajectory
				.fromState(ballParams, state.getPos().getXYVector(), state.getVel().getXYVector(), state.getSpin());
	}


	public IBallTrajectory createTrajectoryFromBallAtRest(final IVector2 pos)
	{
		return FlatBallTrajectory.fromKick(ballParams, pos, Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
	}


	public IBallTrajectory createTrajectoryFromRollingBall(final IVector2 pos, final IVector2 vel)
	{
		IVector2 spin = vel.multiplyNew(1.0 / ballParams.getBallRadius());

		return FlatBallTrajectory.fromKick(ballParams, pos, vel, spin);
	}


	public IBallTrajectory createTrajectoryFromKickedBallWithoutSpin(final IVector2 pos, final IVector3 vel)
	{
		if (vel.z() > 0)
		{
			return ChipBallTrajectory.fromKick(ballParams, pos, vel, Vector2f.ZERO_VECTOR);
		}

		return FlatBallTrajectory.fromKick(ballParams, pos, vel.getXYVector(), Vector2f.ZERO_VECTOR);
	}


	public IBallTrajectory createTrajectoryFromKickedBall(final IVector2 pos, final IVector3 vel, final IVector2 spin)
	{
		if (vel.z() > 0)
		{
			return ChipBallTrajectory.fromKick(ballParams, pos, vel, spin);
		}

		return FlatBallTrajectory.fromKick(ballParams, pos, vel.getXYVector(), spin);
	}


	/**
	 * Create a consultant for straight kicks with the default configured implementation
	 *
	 * @return a new ball consultant for straight kicks
	 */
	public IFlatBallConsultant createFlatConsultant()
	{
		return new FlatBallConsultant(ballParams);
	}


	/**
	 * Create a consultant for chip kicks with the default configured implementation
	 *
	 * @return a new ball consultant for chip kicks
	 */
	public IChipBallConsultant createChipConsultant()
	{
		return new ChipBallConsultant(ballParams);
	}
}
