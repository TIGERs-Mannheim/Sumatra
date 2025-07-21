/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.planarcurve.IPlanarCurveProvider;

import java.util.List;


/**
 * Interface for ball trajectories.
 */
public interface IBallTrajectory extends IMirrorable<IBallTrajectory>, IPlanarCurveProvider, IEuclideanDistance
{
	/**
	 * Get ball parameters for this trajectory.
	 *
	 * @return
	 */
	BallParameters getParameters();

	/**
	 * Get initial position in [mm].
	 *
	 * @return
	 */
	IVector3 getInitialPos();

	/**
	 * Get initial velocity in [m/s].
	 *
	 * @return
	 */
	IVector3 getInitialVel();

	/**
	 * Get initial spin in [rad/s].
	 *
	 * @return
	 */
	IVector2 getInitialSpin();

	/**
	 * Get the state at the specified time in [s] after initial state in milli-units.
	 *
	 * @param time in [s]
	 * @return
	 */
	BallState getMilliStateAtTime(final double time);


	/**
	 * Get the position for a given time
	 *
	 * @param time in [s]
	 * @return the position after <code>time</code> seconds in [mm]
	 */
	IVector3 getPosByTime(double time);


	/**
	 * Get the velocity for a given time
	 *
	 * @param time in [s]
	 * @return the velocity after <code>time</code> seconds in [m/s]
	 */
	IVector3 getVelByTime(double time);


	/**
	 * Get the acceleration for a given time
	 *
	 * @param time in [s]
	 * @return the acceleration after <code>time</code> seconds in [m/s^2]
	 */
	IVector3 getAccByTime(double time);


	/**
	 * Get ball spin for a given time.
	 *
	 * @param time in [s]
	 * @return ball spin in [rad/s], positive spin corresponds to positive linear velocity
	 */
	IVector2 getSpinByTime(double time);


	/**
	 * Get the position for a given target velocity.<br>
	 * If <code>targetVelocity</code> is larger than the current velocity, the current position will be returned.
	 *
	 * @param targetVelocity in [m/s]
	 * @return the position in [mm] when the ball's velocity is smaller than or equal to the targetVelocity for the first time.
	 */
	IVector3 getPosByVel(double targetVelocity);


	/**
	 * Get the required time for the ball to travel the given distance.<br>
	 * If the distance can not be achieved, the result will be Infinity.
	 * If the ball trajectory is curved, the distance is measured from the initial location, not along the path.
	 *
	 * @param travelDistance in [mm]
	 * @return the time in [s] that is need to travel the distance, Inf if the ball stops before reaching the distance.
	 */
	double getTimeByDist(double travelDistance);


	/**
	 * Get the time where the ball reaches a given velocity.<br>
	 * If <code>targetVelocity</code> is larger than the current velocity, 0 will be returned
	 *
	 * @param targetVelocity in [m/s]
	 * @return the time in [s] when the ball's velocity is smaller than or equal to the targetVelocity for the first
	 * time.
	 */
	double getTimeByVel(double targetVelocity);


	/**
	 * Get the velocity after the ball traveled the given distance.
	 *
	 * @param distance in [mm]
	 * @return the velocity in [m/s] after the ball traveled the given distance. 0 if the ball can not travel that far.
	 */
	double getAbsVelByDist(double distance);


	/**
	 * Get the velocity of the ball when reaching given position.<br>
	 * If the position can not be reached, the velocity will be zero.<br>
	 * This method assumes that the position is on the ball traveling line!
	 *
	 * @param targetPosition in [mm]
	 * @return the future velocity in [m/s] of the ball when reaching given position, or 0 if it can not be reached
	 */
	double getAbsVelByPos(IVector2 targetPosition);


	/**
	 * Get time to reach given position.<br>
	 * If the position can not be reached, the time will be Inf.<br>
	 * This method assumes that the position is on the ball traveling line!
	 *
	 * @param targetPosition in [mm]
	 * @return the time in [s] required to reach the given target position, Inf if it can not be reached.
	 */
	double getTimeByPos(IVector2 targetPosition);


	/**
	 * Get the travel distance for given time.
	 *
	 * @param time in [s]
	 * @return the distance in [mm] traveled in given time.
	 */
	double getDistByTime(double time);


	/**
	 * Get the absolute velocity over ground (2D) in given time.
	 *
	 * @param time in [s]
	 * @return the velocity in [m/s] after given time.
	 */
	double getAbsVelByTime(double time);


	/**
	 * True, if ball is below robot height at given time.
	 *
	 * @param time [s]
	 * @return true, if ball is below robot height
	 */
	boolean isInterceptableByTime(final double time);


	/**
	 * True, if the ball is rolling on the ground.
	 *
	 * @param time [s]
	 * @return true, if ball is rolling
	 */
	boolean isRollingByTime(final double time);


	/**
	 * @return a halfLine from current ball pos to ball end pos,
	 * note that this may not be the actual travel line for curved shots
	 */
	IHalfLine getTravelLine();


	/**
	 * @return a line from current ball pos to ball end pos,
	 * note that this may not be the actual travel line for curved shots
	 */
	ILineSegment getTravelLineSegment();

	/**
	 * @return all lines from current ball pos to ball end pos;
	 * the lines are an approximation for curved shots
	 */
	default List<ILineSegment> getTravelLineSegments()
	{
		return List.of(getTravelLineSegment());
	}


	/**
	 * Same as getTravelLine() for a flat ball.
	 *
	 * @return a line from the point where the ball is rolling on the ground to end pos
	 */
	ILineSegment getTravelLineRolling();


	/**
	 * Same as getTravelLine() for a flat ball.
	 *
	 * @return all lines from the point where the ball is rolling on the ground to end pos;
	 * the lines are an approximation for curved shots
	 */
	default List<ILineSegment> getTravelLinesRolling()
	{
		return List.of(getTravelLineRolling());
	}

	/**
	 * @return all segments of the travel line where the ball is interceptable (below robot height)
	 */
	List<ILineSegment> getTravelLinesInterceptableByRobot();


	/**
	 * @return all segments of the travel line where the ball is interceptable (below maximumHeight)
	 */
	List<ILineSegment> getTravelLinesInterceptableBelow(double maximumHeight);


	/**
	 * Get touchdown locations of a chipped ball.
	 *
	 * @return locations where the ball touches ground
	 */
	List<IVector2> getTouchdownLocations();

	/**
	 * Find the closest point on this ball trajectory for the given point.
	 *
	 * @param point some point
	 * @return the closest point on this trajectory
	 */
	IVector2 closestPointToRolling(IVector2 point);

	/**
	 * Find the closest point on this ball trajectory for the given point.
	 *
	 * @param point some point
	 * @return the closest point on this trajectory
	 */
	IVector2 closestPointToBelow(IVector2 point, double maximumHeight);

	/**
	 * Return a new trajectory where the initialPos is adjusted so that the position after time equals posNow.
	 *
	 * @param posNow
	 * @param time
	 * @return
	 */
	IBallTrajectory withAdjustedInitialPos(final IVector3 posNow, final double time);

	/**
	 * Return a new trajectory with new ball parameters.
	 *
	 * @param ballParameters
	 * @return
	 */
	IBallTrajectory withBallParameters(final BallParameters ballParameters);
}
