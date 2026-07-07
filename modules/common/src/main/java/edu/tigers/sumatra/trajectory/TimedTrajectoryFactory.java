package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;
import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.function.UnaryOperator;


/**
 * Generate virtual destinations such that a position will be reached in time
 * Might also be referred as Overshooting
 */
public class TimedTrajectoryFactory
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
	public TimedTrajectory2D fromDestinationAndTimeSync(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			double vMax,
			double aMax,
			double targetTime
	)
	{
		var partialTimed = fromDestinationAndTime(
				s0,
				s1,
				v0,
				(float) vMax,
				(float) aMax,
				(float) targetTime,
				alpha -> alpha
		);
		return new TimedTrajectory2D(
				partialTimed.trajectory2D,
				s1,
				targetTime,
				partialTimed.timeAtTargetX,
				partialTimed.timeAtTargetY
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
	public TimedTrajectory2D fromDestinationAndTimeAsync(
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


		var partialTimed = fromDestinationAndTime(
				Vector2f.ZERO_VECTOR,
				startToTarget,
				v0Rotated,
				(float) vMax,
				(float) aMax,
				(float) targetTime,
				BangBangTrajectoryFactory.ALPHA_FN_ASYNC
		);

		return new TimedTrajectory2D(
				new BangBangTrajectory2DAsync(partialTimed.trajectory2D, s0, rotation),
				s1,
				targetTime,
				partialTimed.timeAtTargetX,
				partialTimed.timeAtTargetY
		);
	}


	PartialTimedTrajectory2D fromDestinationAndTime(
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
		float sinusAlpha = 1;
		float cosinAlpha = 0;


		TimedPos1D x = new TimedPos1D(0, 0, 0);
		TimedPos1D y = new TimedPos1D(0, 0, 0);

		// binary search, some iterations (fixed)
		while (inc > 1e-7)
		{
			DoubleWrapper cos = new DoubleWrapper();
			sinusAlpha = (float) FastMath.sinAndCos(alphaFn.apply(alpha), cos);
			cosinAlpha = (float) cos.value;

			x = getTimedPos1D(distanceX, v0x, vMax * cosinAlpha, aMax * cosinAlpha, targetTime);
			y = getTimedPos1D(distanceY, v0y, vMax * sinusAlpha, aMax * sinusAlpha, targetTime);

			double diff = Math.abs(x.totalTime() - y.totalTime());
			if (diff < BangBangTrajectoryFactory.SYNC_ACCURACY)
			{
				break;
			}
			if (x.totalTime() > y.totalTime())
			{
				alpha -= inc;
			} else
			{
				alpha += inc;
			}

			inc *= 0.5f;
		}

		float virtualDestX = (float) (x.pos() + s0.x());
		float virtualDestY = (float) (y.pos() + s0.y());

		BangBangTrajectory1D xTraj = new BangBangTrajectory1D();
		BangBangTrajectory1D yTraj = new BangBangTrajectory1D();

		xTraj.generate((float) s0.x(), virtualDestX, v0x, vMax * cosinAlpha, aMax * cosinAlpha);
		yTraj.generate((float) s0.y(), virtualDestY, v0y, vMax * sinusAlpha, aMax * sinusAlpha);

		return new PartialTimedTrajectory2D(
				new BangBangTrajectory2D(xTraj, yTraj),
				x.timeAtTarget(),
				y.timeAtTarget()
		);
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
		// Simplify logic by ensuring s >= 0
		if (s < 0)
		{
			return getTimedPos1D(-s, -v0, vMax, aMax, tt).flip();
		}

		float tBreaking;
		float sZeroVel;
		// Ensure v0 is always >= 0
		if (v0 < 0)
		{
			// We are moving away from the goal -> recover from overshoot
			tBreaking = -(v0 / aMax);
			sZeroVel = 0.5f * v0 * tBreaking;
			// new s is ensured to be above 0 (sZeroVel < 0) -> use calcFastestDirect immediately
			return calcFastestDirect(s - sZeroVel, 0, vMax, aMax, tt - tBreaking, Float.POSITIVE_INFINITY).add(
					sZeroVel, tBreaking);
		} else
		{
			tBreaking = v0 / aMax;
			sZeroVel = 0.5f * v0 * tBreaking;
		}

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

		// Calculate necessary time to break to zero

		float slowestDirect = Float.POSITIVE_INFINITY;
		if (sZeroVel < s
				// Determine if we can be slow enough -> no forced overshoot
				|| (slowestDirect = calcSlowestDirectTime(s, v0, aMax)) >= tt
		)
		{
			// sZeroVel > s, brake distance is small enough -> no forced overshoot
			// We can directly hit the timed target position
			return calcFastestDirect(s, v0, vMax, aMax, tt, slowestDirect);
		}

		// Calc the fastest overshoot by starting at sZeroVel in opposed direction with v0=0.0
		return calcFastestDirect(sZeroVel - s, 0, vMax, aMax, tt - tBreaking, Float.POSITIVE_INFINITY)
				.flip()
				.add(sZeroVel, tBreaking);
	}


	/**
	 * 4 Assumptions:
	 * s >= 0
	 * v0 >= 0
	 * aMax >= 0
	 * s too short to brake from v0 to s with aMax
	 */
	private float calcSlowestDirectTime(
			float s,
			float v0,
			float aMax
	)
	{
		var sqrt = (float) SumatraMath.sqrt(v0 * v0 - 2 * aMax * s);
		return (v0 - sqrt) / aMax;
	}


	/**
	 * @param s    TargetPos - InitialPos
	 * @param v0   initialVel
	 * @param vMax maximal velocity but with the same sign (direction) as s
	 * @param aMax maximal acceleration
	 * @param tt   TargetTime
	 * @return
	 */
	private TimedPos1D calcFastestDirect(
			float s,
			float v0,
			float vMax,
			float aMax,
			float tt,
			float slowestDirectTime
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
		var trapezoidal = calcFastestDirectTrapezoidal(s, v0, vMax, aMax, tt);
		return trapezoidal.orElseGet(() -> calcFastestDirectTriangular(s, v0, vMax, aMax, tt, slowestDirectTime));
	}


	private Optional<TimedPos1D> calcFastestDirectTrapezoidal(
			float s,
			float v0,
			float vMax,
			float aMax,
			float tt
	)
	{
		// Full acceleration for s01 to reach vMax
		var aAcc = v0 >= vMax ? -aMax : aMax;
		var t01 = (vMax - v0) / aAcc;
		var s01 = 0.5f * (vMax + v0) * t01;

		if (s <= s01)
		{
			// We are not able to accelerate/deccelerate to vMax before reaching s -> No Trapezoidal form possible
			return Optional.empty();
		}

		var s13 = s - s01;
		var t23 = vMax / aMax;
		var s23 = 0.5f * vMax * t23;

		// Determining if "Trapezoidal too slow"
		// vMax,v2     _________
		//            /|   |   |\
		//           / |   |   | \          s reached at t=t2
		//          /  |   |   |  \
		//         /   |   |   |   \
		// v0,v3  /    |   |   |    \
		//    ---|-----|---|---|-----|----------->
		//      t0    t1  tt  t2    t3
		//       |-t01-|--t12--|-t23-|
		var t12TooSlow = (s13) / vMax;
		if (t01 + t12TooSlow >= tt)
		{
			return Optional.of(new TimedPos1D(s + s23, t01 + t12TooSlow + t23, t01 + t12TooSlow));
		}

		// Determine if "Trapezoidal finishing early"
		// vMax,v2     _________
		//            /|       |\
		//           / |       | \          s reached at t=t3
		//          /  |       |  \
		//         /   |       |   \
		// v0,v3  /    |       |    \
		//    ---|-----|-------|-----|-----|----->
		//      t0    t1      t2    t3    tt
		//       |-t01-|--t12--|-t23-|
		var s12Early = SumatraMath.clampToZeroIfSlightlyNegative(s13 - s23);
		var t12Early = s12Early / vMax;
		if (t12Early >= 0.0f && t01 + t12Early + t23 <= tt)
		{
			return Optional.of(new TimedPos1D(s, t01 + t12Early + t23, t01 + t12Early + t23));
		}

		// Determine if "Trapezoidal direct hit"
		// vMax,v2      _________
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
		var t23Direct = (float) SumatraMath.sqrt(2 * (s13 - t13 * vMax) / -aMax);
		var t12Direct = t13 - t23Direct;
		if (t12Direct > 0 && t23Direct < t23)
		{
			var v3 = vMax + -aMax * t23Direct;
			var t34 = v3 / aMax;
			return Optional.of(new TimedPos1D(s + 0.5f * v3 * t34, tt + t34, tt));
		}
		return Optional.empty();
	}


	private TimedPos1D calcFastestDirectTriangular(
			float s,
			float v0,
			float vMax,
			float aMax,
			float tt,
			float slowestDirectTime
	)
	{
		// Determining if "Straight too slow"
		// Can't slow down to vMax before reaching s (otherwise trapezoidal)
		// Already checked slowestDirectTime is bigger than tt in getPosition1D() (otherwise overshoot recovery will be triggered in getPosition1D(), and v0 is 0)
		// => we are not reaching s in time, although we are still faster than vMax when reaching s. Only reasonable trajectory left is straight decelerating
		if (v0 >= vMax)
		{
			var t = v0 / aMax;
			var timeAtTarget = slowestDirectTime;
			Validate.isTrue(
					SumatraMath.isEqual(-0.5f * aMax * timeAtTarget * timeAtTarget + v0 * timeAtTarget, s),
					"s=%.20f, v0=%.20f, vMax=%.20f, aMax%.20f, tt=%.20f", s, v0, vMax, aMax, tt
			);
			return new TimedPos1D(0.5f * v0 * t, t, timeAtTarget);
		}
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
		var sqrtTooSlow = (float) SumatraMath.sqrt(2 * aMax * s + v0 * v0);
		var t01TooSLow = (-v0 + sqrtTooSlow) / aMax;
		if (t01TooSLow >= tt)
		{
			var v1TooSlow = v0 + aMax * t01TooSLow;
			var t12TooSlow = Math.abs(v1TooSlow / aMax);
			return new TimedPos1D(s + 0.5f * v1TooSlow * t12TooSlow, t01TooSLow + t12TooSlow, t01TooSLow);
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
		var sqEarly = ((s * aMax) + (0.5f * v0 * v0)) / (aMax * aMax);
		var t12Early = sqEarly > 0.0f ? (float) SumatraMath.sqrt(sqEarly) : 0.0f;
		var v1Early = aMax * t12Early;
		var t01Early = (v1Early - v0) / aMax;
		if (t01Early + t12Early <= tt)
		{
			return new TimedPos1D(s, t01Early + t12Early, t01Early + t12Early);
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

		var sqDirect = (float) SumatraMath.sqrt(2 * aMax * (aMax * tt * tt - 2 * s + 2 * tt * v0));
		var t01Direct = tt - sqDirect / (2 * aMax);
		var v1Direct = v0 + aMax * t01Direct;
		var t13Direct = v1Direct / aMax;
		var s01Direct = 0.5f * (v0 + v1Direct) * t01Direct;
		var s13Direct = 0.5f * v1Direct * t13Direct;
		return new TimedPos1D(s01Direct + s13Direct, t01Direct + t13Direct, tt);

	}


	record TimedPos1D(float pos, float totalTime, float timeAtTarget)
	{
		TimedPos1D flip()
		{
			return new TimedPos1D(-pos, totalTime, timeAtTarget);
		}


		TimedPos1D add(float pos, float extraTime)
		{
			return new TimedPos1D(this.pos + pos, this.totalTime + extraTime, this.timeAtTarget + extraTime);
		}
	}

	record PartialTimedTrajectory2D(BangBangTrajectory2D trajectory2D, double timeAtTargetX, double timeAtTargetY)
	{}
}
