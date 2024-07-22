/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.util.Optional;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrackedBot extends ITrackedObject, IExportable
{
	@Override
	ITrackedBot mirrored();


	/**
	 * @param t
	 * @return
	 */
	IVector2 getPosByTime(double t);


	/**
	 * @param t
	 * @return
	 */
	IVector2 getVelByTime(double t);


	/**
	 * @param t
	 * @return
	 */
	double getAngleByTime(double t);


	/**
	 * @return the ball contact information
	 */
	BallContact getBallContact();


	/**
	 * @return
	 */
	double getCenter2DribblerDist();


	/**
	 * @return the bot shape of this bot
	 */
	IBotShape getBotShape();


	/**
	 * Calculates the position of the dribbler/kicker of the given bot.
	 * Use this position for ball receivers, etc.
	 *
	 * @return
	 */
	IVector2 getBotKickerPos();


	/**
	 * Calculates the position of the dribbler/kicker of the given bot plus some margin like the ball radius.
	 *
	 * @param margin added to the center2Dribbler distance
	 * @return
	 */
	IVector2 getBotKickerPos(double margin);


	/**
	 * @return id of the bot
	 */
	BotID getBotId();


	/**
	 * @return
	 */
	ETeamColor getTeamColor();


	/**
	 * @return the angle [rad]
	 */
	double getOrientation();


	/**
	 * @return the aVel [rad/s]
	 */
	double getAngularVel();


	/**
	 * @return
	 */
	RobotInfo getRobotInfo();


	/**
	 * @param t
	 * @return
	 */
	IVector2 getBotKickerPosByTime(double t);

	/**
	 * Calculates the position of the dribbler/kicker of the given bot plus some margin like the ball radius.
	 *
	 * @param t
	 * @param margin added to the center2Dribbler distance
	 * @return
	 */
	IVector2 getBotKickerPosByTime(double t, double margin);


	/**
	 * @return
	 */
	MoveConstraints getMoveConstraints();


	/**
	 * @return the pose of the destination based on the current trajectory, if available
	 */
	Optional<Pose> getDestinationPose();


	/**
	 * @return the current robot state (internal, filtered or buffered)
	 */
	State getBotState();


	/**
	 * @return the current robot state as reported by the vision filter
	 */
	Optional<State> getFilteredState();

	/**
	 * @return the current trajectory, if present
	 */
	Optional<ITrajectory<IVector3>> getCurrentTrajectory();

	/**
	 * @return the current movement status of the robot
	 */
	boolean isMalFunctioning();
}
