/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball;

import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;


/**
 * Data structure for the ball state at a certain time.
 * <br>
 * <b>WARNING: Units of this class are [mm], [mm/s] ([rad/s]), [mm/s^2] !!!</b>
 */
@Value
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BallState implements IMirrorable<BallState>
{
	/**
	 * Position in [mm]
	 */
	@NonNull
	IVector3 pos;

	/**
	 * Velocity in [mm/s]
	 */
	@NonNull
	IVector3 vel;

	/**
	 * Acceleration in [mm/s^2]
	 */
	@NonNull
	IVector3 acc;

	/**
	 * The spin of the ball in [rad/s], positive spin corresponds to positive linear velocity
	 */
	@NonNull
	IVector2 spin;


	/**
	 * Create an empty default state.
	 */
	public BallState()
	{
		pos = Vector3f.ZERO_VECTOR;
		vel = Vector3f.ZERO_VECTOR;
		acc = Vector3f.ZERO_VECTOR;
		spin = Vector2f.ZERO_VECTOR;
	}


	@Override
	public BallState mirrored()
	{
		return toBuilder()
				.withPos(Vector3.from2d(pos.getXYVector().multiplyNew(-1), pos.getXYZVector().z()))
				.withVel(Vector3.from2d(vel.getXYVector().multiplyNew(-1), vel.getXYZVector().z()))
				.withAcc(Vector3.from2d(acc.getXYVector().multiplyNew(-1), acc.getXYZVector().z()))
				.withSpin(spin.multiplyNew(-1))
				.build();
	}


	/**
	 * Is this a chipped ball?
	 * Note that an originally chipped ball enters rolling state at some point and even though it was chipped it
	 * won't be reported as such by this method.
	 *
	 * @return
	 */
	public boolean isChipped()
	{
		return pos.z() > 0 || !SumatraMath.isZero(vel.z()) || !SumatraMath.isZero(acc.z());
	}


	/**
	 * Is this ball rolling based on its spin and velocity?
	 *
	 * @param ballRadius in [mm]
	 * @return
	 */
	public boolean isRolling(double ballRadius)
	{
		// compute relative velocity of ball to ground surface, if ball is rolling this is close to zero
		IVector2 contactVelocity = vel.getXYVector().subtractNew(spin.multiplyNew(ballRadius));

		return contactVelocity.getLength2() < 0.01;
	}
}
