/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.Getter;

import java.util.Collections;
import java.util.List;


/**
 * Common base implementation for ball trajectories.
 */
@Persistent
public abstract class ABallTrajectory implements IBallTrajectory
{
	/**
	 * Parameters for this trajectory.
	 */
	@Getter
	protected BallParameters parameters;


	/**
	 * Initial position in [mm].
	 */
	@Getter
	protected IVector3 initialPos;


	/**
	 * Kick velocity in [mm/s].
	 */
	protected IVector3 initialVel;


	/**
	 * Kick spin in [rad/s].
	 */
	@Getter
	protected IVector2 initialSpin;


	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	protected ABallTrajectory()
	{
		parameters = BallParameters.builder().build();
		initialPos = Vector3f.ZERO_VECTOR;
		initialVel = Vector3f.ZERO_VECTOR;
		initialSpin = Vector2f.ZERO_VECTOR;
	}


	/**
	 * Get the time when the ball comes to rest.
	 *
	 * @return
	 */
	public abstract double getTimeAtRest();


	/**
	 * Get the required time for the ball to travel the given distance.<br>
	 * If the distance can not be achieved, the result will be Infinity.
	 *
	 * @param distance Distance in [mm], must be positive.
	 * @return the time in [s] that is need to travel the distance, Inf if the ball stops before reaching the distance.
	 */
	protected abstract double getTimeByDistanceInMillimeters(final double distance);


	/**
	 * Get the time where the ball reaches a given velocity.<br>
	 * If <code>velocity</code> is larger than the current velocity, 0 will be returned
	 *
	 * @param velocity Velocity in [mm/s], must be positive.
	 * @return the time in [s] when the ball's velocity is smaller than or equal to the targetVelocity for the first
	 * time.
	 */
	protected abstract double getTimeByVelocityInMillimetersPerSec(final double velocity);


	@Override
	public IVector3 getInitialVel()
	{
		return initialVel.multiplyNew(0.001);
	}


	@Override
	public IVector3 getPosByTime(final double time)
	{
		return getMilliStateAtTime(time).getPos();
	}


	@Override
	public IVector3 getVelByTime(final double time)
	{
		return getMilliStateAtTime(time).getVel().multiplyNew(0.001);
	}


	@Override
	public IVector3 getAccByTime(final double time)
	{
		return getMilliStateAtTime(time).getAcc().multiplyNew(0.001);
	}


	@Override
	public IVector2 getSpinByTime(final double time)
	{
		return getMilliStateAtTime(time).getSpin();
	}


	@Override
	public IVector3 getPosByVel(final double targetVelocity)
	{
		if (getAbsVelByTime(0) < targetVelocity)
		{
			return initialPos;
		}

		double time = getTimeByVel(targetVelocity);
		return getPosByTime(time);
	}


	@Override
	public double getTimeByDist(final double travelDistance)
	{
		return getTimeByDistanceInMillimeters(travelDistance);
	}


	@Override
	public double getTimeByVel(final double targetVelocity)
	{
		return getTimeByVelocityInMillimetersPerSec(targetVelocity * 1000.0);
	}


	@Override
	public double getAbsVelByDist(final double distance)
	{
		double time = getTimeByDist(distance);
		return getAbsVelByTime(time);
	}


	@Override
	public double getAbsVelByPos(final IVector2 targetPosition)
	{
		return getAbsVelByDist(initialPos.getXYVector().distanceTo(targetPosition));
	}


	@Override
	public double getTimeByPos(final IVector2 targetPosition)
	{
		return getTimeByDist(initialPos.getXYVector().distanceTo(targetPosition));
	}


	@Override
	public double getDistByTime(final double time)
	{
		return initialPos.getXYVector().distanceTo(getPosByTime(time).getXYVector());
	}


	@Override
	public double getAbsVelByTime(final double time)
	{
		return getMilliStateAtTime(time).getVel().getLength2() * 0.001;
	}


	@Override
	public boolean isInterceptableByTime(final double time)
	{
		return getPosByTime(time).getXYZVector().z() < parameters.getMaxInterceptableHeight();
	}


	@Override
	public boolean isRollingByTime(final double time)
	{
		return getPosByTime(time).getXYZVector().z() < parameters.getMinHopHeight();
	}


	@Override
	public IHalfLine getTravelLine()
	{
		IVector2 finalPos = getPosByTime(getTimeAtRest()).getXYVector();
		return Lines.halfLineFromPoints(initialPos.getXYVector(), finalPos);
	}


	@Override
	public ILineSegment getTravelLineSegment()
	{
		IVector2 finalPos = getPosByTime(getTimeAtRest()).getXYVector();
		return Lines.segmentFromPoints(initialPos.getXYVector(), finalPos);
	}


	@Override
	public ILineSegment getTravelLineRolling()
	{
		return getTravelLineSegment();
	}


	@Override
	public List<ILineSegment> getTravelLinesInterceptable()
	{
		return Collections.singletonList(getTravelLineSegment());
	}


	@Override
	public List<IVector2> getTouchdownLocations()
	{
		return Collections.emptyList();
	}
}
