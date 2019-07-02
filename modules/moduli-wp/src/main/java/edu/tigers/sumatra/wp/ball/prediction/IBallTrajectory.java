/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.prediction;

import java.util.List;

import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.planarcurve.IPlanarCurveProvider;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBallTrajectory extends IMirrorable<IBallTrajectory>, IPlanarCurveProvider
{
	/**
	 * Get the position for a given time
	 *
	 * @param time in [s]
	 * @return the position after <code>time</code> seconds
	 */
	IVector getPosByTime(double time);
	
	
	/**
	 * Get the velocity for a given time
	 *
	 * @param time in [s]
	 * @return the velocity after <code>time</code> seconds
	 */
	IVector getVelByTime(double time);
	
	
	/**
	 * Get the acceleration for a given time
	 *
	 * @param time in [s]
	 * @return the acceleration after <code>time</code> seconds
	 */
	IVector getAccByTime(double time);
	
	
	/**
	 * Get ball spin for a given time.
	 * 
	 * @param time in [s]
	 * @return ball spin, forward/topspin is positive, backspin is negative
	 */
	double getSpinByTime(double time);
	
	
	/**
	 * Get the position for a given target velocity.<br>
	 * If <code>targetVelocity</code> is larger than the current velocity, the current position will be returned.
	 *
	 * @param targetVelocity in [m/s]
	 * @return the position when the ball's velocity is smaller than or equal to the targetVelocity for the first time.
	 */
	IVector getPosByVel(double targetVelocity);
	
	
	/**
	 * Get the required time for the ball to travel the given distance.<br>
	 * If the distance can not be achieved, the result will be Infinity.
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
	 *         time.
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
	 * @return the future velocity of the ball when reaching given position, or 0 if it can not be reached
	 */
	double getAbsVelByPos(IVector2 targetPosition);
	
	
	/**
	 * Get time to reach given position.<br>
	 * If the position can not be reached, the velocity will be zero.<br>
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
	 * Get the velocity in given time.
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
	 * @return true, if ballis rolling
	 */
	boolean isRollingByTime(final double time);
	
	
	/**
	 * @return a halfLine from current ball pos to ball end pos
	 */
	IHalfLine getTravelLine();
	
	
	/**
	 * @return a line from current ball pos to ball end pos
	 */
	ILineSegment getTravelLineSegment();
	
	
	/**
	 * Same as getTravelLine() for a flat ball.
	 * 
	 * @return a line from the point where the ball is rolling on the ground to end pos
	 */
	ILineSegment getTravelLineRolling();
	
	
	/**
	 * @return all segments of the travel line where the ball is interceptable (below robot height)
	 */
	List<ILineSegment> getTravelLinesInterceptable();
	
	
	/**
	 * Get touchdown locations of a chipped ball.
	 * 
	 * @return locations where the ball touches ground
	 */
	List<IVector2> getTouchdownLocations();
}
