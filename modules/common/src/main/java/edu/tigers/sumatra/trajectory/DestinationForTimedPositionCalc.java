/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;

import java.util.Optional;
import java.util.function.UnaryOperator;


/**
 * Generate virtual destinations such that a position will be reached in time
 * Might also be referred as Overshooting
 */
public class DestinationForTimedPositionCalc
{
	/**
	 * @param s0         [m] start position
	 * @param s1         [m] target position
	 * @param v0         [m/s] initial velocity
	 * @param vMax       [m/s] maximum velocity
	 * @param aMax       [m/s²] maximum acceleration
	 * @param targetTime [s] target time
	 * @return [m] virtual destination
	 */
	public IVector2 destinationForBangBang2dSync(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			double vMax,
			double aMax,
			double targetTime
	)
	{
		return destinationForBangBang2D(
				s0,
				s1,
				v0,
				(float) vMax,
				(float) aMax,
				(float) targetTime,
				alpha -> alpha
		);
	}


	/**
	 * @param s0               [m] start position
	 * @param s1               [m] target position
	 * @param v0               [m/s] initial velocity
	 * @param vMax             [m/s] maximum velocity
	 * @param aMax             [m/s²] maximum acceleration
	 * @param targetTime       [s] target time
	 * @param primaryDirection Primary Moving Direction
	 * @return [m] virtual destination
	 */
	public IVector2 destinationForBangBang2dAsync(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			double vMax,
			double aMax,
			double targetTime,
			IVector2 primaryDirection
	)
	{
		var rotation = primaryDirection.getAngle();
		var startToTarget = s1.subtractNew(s0).turn(-rotation);
		var v0Rotated = v0.turnNew(-rotation);

		return destinationForBangBang2D(
				Vector2f.ZERO_VECTOR,
				startToTarget,
				v0Rotated,
				(float) vMax,
				(float) aMax,
				(float) targetTime,
				BangBangTrajectoryFactory.ALPHA_FN_ASYNC
		).turnNew(rotation).add(s0);
	}


	IVector2 destinationForBangBang2D(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			float vMax,
			float aMax,
			float targetTime,
			UnaryOperator<Float> alphaFn
	)
	{
		var v0x = (float) v0.x();
		var v0y = (float) v0.y();
		var distance = s1.subtractNew(s0);
		var distanceX = (float) distance.x();
		var distanceY = (float) distance.y();


		float inc = (float) AngleMath.PI / 8.0f;
		float alpha = (float) AngleMath.PI / 4.0f;


		TimedPos1D x = new TimedPos1D(0, 0);
		TimedPos1D y = new TimedPos1D(0, 0);

		// binary search, some iterations (fixed)
		while (inc > 1e-7)
		{
			DoubleWrapper cos = new DoubleWrapper();
			final float sA = (float) FastMath.sinAndCos(alphaFn.apply(alpha), cos);
			final float cA = (float) cos.value;

			x = getTimedPos1D(distanceX, v0x, vMax * cA, aMax * cA, targetTime);
			y = getTimedPos1D(distanceY, v0y, vMax * sA, aMax * sA, targetTime);

			double diff = Math.abs(x.time() - y.time());
			if (diff < BangBangTrajectoryFactory.SYNC_ACCURACY)
			{
				break;
			}
			if (x.time() > y.time())
			{
				alpha -= inc;
			} else
			{
				alpha += inc;
			}

			inc *= 0.5f;
		}
		return Vector2.fromXY(x.pos() + s0.x(), y.pos() + s0.y());
	}


	/**
	 * @param s    TargetPosition - StartPosition
	 * @param v0   initialVel
	 * @param vMax maximal absolute velocity
	 * @param aMax maximal absolute acceleration
	 * @param tt   TargetTime
	 * @return
	 */
	TimedPos1D getTimedPos1D(float s, float v0, float vMax, float aMax, float tt)
	{
		// Hit Windows:
		// Either our v0 is low enough that we could stop before reaching the goal target:
		//
		//  |    |----------------------------------------------------
		//  |             Direct Hit
		// -|---------------------------------------------------------> tt
		//
		// Or our v0 is so high that we will always overshoot. Is target time low enough to direct hit or do we
		// need to overshoot and recover?
		//
		//  |    |----------|                |------------------------
		//  |     Direct Hit                   Overshoot and Recover
		// -|---------------------------------------------------------> tt

		var aDec = v0 >= 0 ? -aMax : aMax;
		var sZeroVel = 0.5f * v0 * (-v0 / aDec);
		var v1Max = s >= 0 ? vMax : -vMax;

		if ((s >= 0.f) != (v0 > 0.f) // If v0 and s0 -> sT are in a different direction -> no forced overshoot
				// abs(sZeroVel) > abs(s), without this breaking is always possible -> no forced overshoot
				|| (s >= 0) == (sZeroVel < s)
				// Determine if we can be slow enough -> no forced overshoot
				|| calcSlowestDirectTime(s, v0, aMax) >= tt
		)
		{
			// We can directly hit the timed target position
			return calcFastestDirect(s, v0, v1Max, aMax, tt);
		} else
		{
			// Calculate necessary time to break to zero
			var tBreaking = Math.abs(v0 / aMax);
			// Calc the fastest overshoot by starting at sZeroVel in opposed direction with v0=0.0
			var timed = calcFastestDirect(s - sZeroVel, 0.f, -v1Max, aMax, tt - tBreaking);
			// Extend TimedPos1D to accommodate breaking
			return new TimedPos1D(timed.pos() + sZeroVel, timed.time() + tBreaking);
		}
	}


	private float calcSlowestDirectTime(
			float s,
			float v0,
			float aMax
	)
	{
		var aDec = (v0 >= 0) ? -aMax : aMax;
		var sqrt = (float) SumatraMath.sqrt(v0 * v0 + 2 * aDec * (s));
		return (v0 >= 0.f) ? ((-v0 + sqrt) / aDec) : ((-v0 - sqrt) / aDec);
	}


	/**
	 * @param s     TargetPos - InitialPos
	 * @param v0    initialVel
	 * @param v1Max maximal velocity but with the same sign (direction) as s
	 * @param aMax  maximal acceleration
	 * @param tt    TargetTime
	 * @return
	 */
	private TimedPos1D calcFastestDirect(
			float s,
			float v0,
			float v1Max,
			float aMax,
			float tt
	)
	{
		// Possible Fastest Directs:
		//  - Straight too slow
		//  - Trapezoidal too slow
		//  - Trapezoidal finishing early
		//  - Trapezoidal direct hit
		//  - Triangular too slow
		//  - Triangular finishing early
		//  - Triangular direct hit
		var aDec = v1Max >= 0 ? -aMax : aMax;
		var trapezoidal = calcFastestDirectTrapezoidal(s, v0, v1Max, aMax, aDec, tt);
		return trapezoidal.orElseGet(() -> calcFastestDirectTriangular(s, v0, v1Max, aMax, aDec, tt));
	}


	private Optional<TimedPos1D> calcFastestDirectTrapezoidal(
			float s,
			float v0,
			float v1Max,
			float aMax,
			float aDec,
			float tt
	)
	{
		// Full acceleration for s01 to reach v1Max
		var aAcc = v0 >= v1Max ? -aMax : aMax;
		var t01 = (v1Max - v0) / aAcc;
		var s01 = 0.5f * (v1Max + v0) * t01;

		if ((s >= 0.0f) == (s <= s01))
		{
			// We are not able to accel to v1Max before reaching s -> No Trapezoidal form possible
			return Optional.empty();
		}

		var s13 = s - s01;
		var t23 = -v1Max / aDec;
		var s23 = 0.5f * v1Max * t23;

		// Determining if "Trapezoidal too slow"
		// v1Max,v2    _________
		//            /|   |   |\
		//           / |   |   | \          s reached at t=t2
		//          /  |   |   |  \
		//         /   |   |   |   \
		// v0,v3  /    |   |   |    \
		//    ---|-----|---|---|-----|----------->
		//      t0    t1  tt  t2    t3
		//       |-t01-|--t12--|-t23-|
		var t12TooSlow = (s13) / v1Max;
		if (t01 + t12TooSlow >= tt)
		{
			return Optional.of(new TimedPos1D(s + s23, t01 + t12TooSlow + t23));
		}

		// Determine if "Trapezoidal finishing early"
		// v1Max,v2    _________
		//            /|       |\
		//           / |       | \          s reached at t=t3
		//          /  |       |  \
		//         /   |       |   \
		// v0,v3  /    |       |    \
		//    ---|-----|-------|-----|-----|----->
		//      t0    t1      t2    t3    tt
		//       |-t01-|--t12--|-t23-|
		var s12Early = s13 - s23;
		var t12Early = s12Early / v1Max;
		if (t12Early >= 0.0f && t01 + t12Early + t23 <= tt)
		{
			return Optional.of(new TimedPos1D(s, t01 + t12Early + t23));
		}

		// Determine if "Trapezoidal direct hit"
		// v1Max,v2     _________
		//             /|       |\
		//            / |       | \         tt = t3
		//           /  |       |  \        s reached at t=tt
		// v3       /   |       |   \
		//         /    |       |   |\
		// v0,v4  /     |       |   | \
		//    ---|------|-------|---|--|--------->
		//      t0     t1      t2  tt t4
		//       |-t01--|--t12--|t23|
		//              |----t13----|
		// https://www.wolframalpha.com/input?i=solve+v_0*t_1+%3Dv_0*t_2%2B1%2F2*a*Power%5Bt_2%2C2%5D%2Bv_1*t_3%2C+v_1+%3D+v_0%2Ba*t_2%2C+t_1%2Bt%3Dt_2%2Bt_3+for+v_1%2Ct_1%2C++t_2
		var t13 = tt - t01;
		var t23Direct = (float) SumatraMath.sqrt(2 * (s13 - t13 * v1Max) / aDec);
		var t12Direct = t13 - t23Direct;
		if (t12Direct > 0 && t23Direct < t23)
		{
			var v3 = v1Max + aDec * t23Direct;
			var t34 = -v3 / aDec;
			return Optional.of(new TimedPos1D(s + 0.5f * v3 * t34, tt + t34));
		}
		return Optional.empty();
	}


	private TimedPos1D calcFastestDirectTriangular(
			float s,
			float v0,
			float v1Max,
			float aMax,
			float aDec,
			float tt
	)
	{

		// Determining if "Straight too slow"
		// Can*t reach v1Max before reaching s, but already checked slowestDirect Time is smaller than tt (getPosition1D)
		// => we are too slow at s and only reasonable trajectory left is straight decelerating
		if ((v1Max >= 0) == (v0 >= v1Max))
		{
			var t = -v0 / aDec;
			return new TimedPos1D(0.5f * v0 * t, t);

		}
		var aAcc = -aDec;
		// Determining if "Triangular too slow"
		//
		//                  _/|\_
		//                _/  |  \_
		//              _/    |    \_
		//            _/|     |      \_        s reached at t=t1
		//          _/  |     |        \_
		//        _/    |     |          \_
		//    ---|------|-----|------------|-------->
		//      t0     tt    t1           t2
		//       |-----t01----|-----t12----|
		// https://www.wolframalpha.com/input?i=solve+s%3Dv_0*t_01%2B1%2F2*a*t_01%5E2%2C+for+t_01
		var sqrtTooSlow = (float) SumatraMath.sqrt(2 * aAcc * s + v0 * v0);
		var t01TooSLow = (v1Max >= 0.f) ? ((-v0 + sqrtTooSlow) / aAcc) : ((-v0 - sqrtTooSlow) / aAcc);
		if (t01TooSLow >= tt)
		{
			var v1TooSlow = v0 + aAcc * t01TooSLow;
			var t12TooSlow = Math.abs(v1TooSlow / aAcc);
			return new TimedPos1D(s + 0.5f * v1TooSlow * t12TooSlow, t01TooSLow + t12TooSlow);
		}

		// Determining if "Triangular finishing early"
		//
		//                  _/|\_
		//                _/  |  \_
		//              _/    |    \_
		//            _/      |      \_        s reached at t=t2
		//          _/        |        \_
		//        _/          |          \_
		//    ---|------------|------------|----|--->
		//      t0           t1           t2   tt
		//       |-----t01----|-----t12----|
		// https://www.wolframalpha.com/input?i=solve+s%3Dv_0+*+t_1+%2B+1%2F2+*+a+*+t_1%5E2+%2B+v_1+*+t_2+%2B+1%2F2+*+%28-a%29+*+t_2%5E2%2C+v_1+%3D+v_0+%2B+a+*+t_1%2C+0%3Dv_1%2B+%28-a%29+*+t_2+for+t_1%2C+v_1%2C+t_2
		var sqEarly = ((s * aAcc) + (0.5f * v0 * v0)) / (aMax * aMax);
		var t12Early = sqEarly > 0.0f ? (float) SumatraMath.sqrt(sqEarly) : 0.0f;
		var v1Early = aAcc * t12Early;
		var t01Early = (v1Early - v0) / aAcc;
		if (t01Early + t12Early <= tt)
		{
			return new TimedPos1D(s, t01Early + t12Early);
		}

		// Determining if "Triangular direct hit"
		//
		//                  _/|\_
		//                _/  |  \_
		//              _/    |    \_
		//            _/      |     |\_        s reached at t=tt
		//          _/        |     |  \_
		//        _/          |     |    \_
		//    ---|------------|-----|------|-------->
		//      t0           t1    tt     t3
		//       |-----t01----|-t12-|-t23--|
		//                    |-----t13----|
		// https://www.wolframalpha.com/input?i=solve+s+%3D+v_0+*+t_1+%2B+0.5+*+a+*+t_1+**+2+%2Bv_1*t_2+-+0.5+*+a+*+t_2**2%2C+v_1+%3D+v_0+%2B+a+*+t_1%2C+v_2%3Dv_1-a*t_2%2C+t%3Dt_1%2Bt_2+for+v_2%2Cv_1%2C+t_1%2C+t_2

		var sqDirect = (float) SumatraMath.sqrt(2 * aAcc * (aAcc * tt * tt - 2 * s + 2 * tt * v0));
		var t01Direct = tt - sqDirect / (2 * aMax);
		var v1Direct = v0 + aAcc * t01Direct;
		var t13Direct = v1Direct / aAcc;
		var s01Direct = 0.5f * (v0 + v1Direct) * t01Direct;
		var s13Direct = 0.5f * v1Direct * t13Direct;
		return new TimedPos1D(s01Direct + s13Direct, t01Direct + t13Direct);

	}


	private record TimedPos1D(float pos, float time)
	{
	}
}
