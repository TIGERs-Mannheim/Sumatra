/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.prediction.IChipBallConsultant;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrackedBall extends ITrackedObject
{
	@Override
	ITrackedBall mirrored();
	
	
	/**
	 * Get theoretical RPM based on ball velocity
	 * <a href="http://www.endmemo.com/physics/rpmlinear.php">http://www.endmemo.com/physics/rpmlinear.php</a>
	 *
	 * @return
	 */
	double getRpm();
	
	
	/**
	 * @return
	 */
	IVector3 getPos3();
	
	
	/**
	 * @return
	 */
	IVector3 getVel3();
	
	
	/**
	 * @return
	 */
	IVector3 getAcc3();
	
	
	/**
	 * @return true, if the ball is detected by any camera
	 */
	boolean isOnCam();
	
	
	/**
	 * @return the height of the ball
	 */
	double getHeight();
	
	
	/**
	 * Get timestamp when this ball was last seen on a camera.
	 * 
	 * @return
	 */
	long getLastVisibleTimestamp();
	
	
	/**
	 * Get the velocity where the ball switches to rolling.
	 * 
	 * @return
	 */
	double getvSwitchToRoll();
	
	
	/**
	 * Is this a chipped ball?<br/>
	 * This does not necessarily mean it is currently in the air!
	 * 
	 * @return
	 */
	boolean isChipped();
	
	
	/**
	 * @return the current ball state
	 */
	BallTrajectoryState getState();
	
	
	/**
	 * @return the trajectory of the ball that returns the ball state at any time
	 */
	IBallTrajectory getTrajectory();
	
	
	/**
	 * @return the consultant for straight balls
	 */
	IStraightBallConsultant getStraightConsultant();
	
	
	/**
	 * @return the consulatant for chip balls
	 */
	IChipBallConsultant getChipConsultant();
}
