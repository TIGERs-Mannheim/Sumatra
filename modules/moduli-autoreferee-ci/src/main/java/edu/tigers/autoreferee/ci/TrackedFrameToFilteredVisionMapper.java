/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.ci;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.referee.proto.SslGcCommon;
import edu.tigers.sumatra.referee.proto.SslGcGeometry;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import edu.tigers.sumatra.wp.proto.SslVisionDetectionTracked;

import java.util.List;


public class TrackedFrameToFilteredVisionMapper
{
	private long ballLastVisibleTimestamp;


	public FilteredVisionFrame map(SslVisionDetectionTracked.TrackedFrame trackedFrame)
	{
		long timestamp = (long) (trackedFrame.getTimestamp() * 1e9);
		return FilteredVisionFrame.builder()
				.withId((long) trackedFrame.getFrameNumber())
				.withTimestamp(timestamp)
				.withBall(mapBalls(trackedFrame.getBallsList(), timestamp))
				.withBots(mapRobots(trackedFrame.getRobotsList(), timestamp))
				.withShapeMap(new ShapeMap())
				.withKick(map(trackedFrame.getKickedBall()))
				.build();
	}


	private FilteredVisionKick map(SslVisionDetectionTracked.KickedBall kickedBall)
	{
		long kickTimestamp = (long) (kickedBall.getStartTimestamp() * 1e9);
		return FilteredVisionKick.builder()
				.withKickTimestamp(kickTimestamp)
				.withTrajectoryStartTime(kickTimestamp)
				.withKickingBot(map(kickedBall.getRobotId()))
				.withKickingBotPosition(map(kickedBall.getPos()))
				.withKickingBotOrientation(map(kickedBall.getVel()).getXYVector().getAngle())
				.withNumBallDetectionsSinceKick(100)
				.withBallTrajectory(Geometry.getBallFactory().createTrajectoryFromState(BallState.builder()
						.withPos(Vector3.from2d(map(kickedBall.getPos()), 0))
						.withVel(map(kickedBall.getVel()))
						.withAcc(Vector3.zero())
						.withSpin(Vector2.zero())
						.build()))
				.build();
	}


	private List<FilteredVisionBot> mapRobots(List<SslVisionDetectionTracked.TrackedRobot> robots, long timestamp)
	{
		return robots.stream().map(robot -> map(robot, timestamp)).toList();
	}


	private FilteredVisionBot map(SslVisionDetectionTracked.TrackedRobot robot, long timestamp)
	{
		return FilteredVisionBot.builder()
				.withBotID(map(robot.getRobotId()))
				.withTimestamp(timestamp)
				.withPos(map(robot.getPos()))
				.withVel(map(robot.getVel()))
				.withOrientation((double) robot.getOrientation())
				.withAngularVel((double) robot.getVelAngular())
				.withQuality(robot.getVisibility())
				.build();
	}


	private BotID map(SslGcCommon.RobotId robotId)
	{
		if (robotId == null)
		{
			return BotID.noBot();
		}
		return BotID.createBotId(
				robotId.getId(),
				switch (robotId.getTeam())
						{
							case BLUE -> ETeamColor.BLUE;
							case YELLOW -> ETeamColor.YELLOW;
							default -> ETeamColor.NEUTRAL;
						}
		);
	}


	private FilteredVisionBall mapBalls(List<SslVisionDetectionTracked.TrackedBall> ballsList, long timestamp)
	{
		if (ballsList.isEmpty())
		{
			return FilteredVisionBall.builder()
					.withTimestamp(timestamp)
					.withLastVisibleTimestamp(ballLastVisibleTimestamp)
					.withBallState(BallState.builder()
							.withPos(Vector3.zero())
							.withVel(Vector3.zero())
							.withAcc(Vector3.zero())
							.withSpin(Vector2.zero())
							.build())
					.build();
		}
		SslVisionDetectionTracked.TrackedBall ball = ballsList.get(0);
		ballLastVisibleTimestamp = timestamp;
		return FilteredVisionBall.builder()
				.withTimestamp(timestamp)
				.withLastVisibleTimestamp(ballLastVisibleTimestamp)
				.withBallState(map(ball))
				.build();
	}


	private BallState map(SslVisionDetectionTracked.TrackedBall ball)
	{
		return BallState.builder()
				.withPos(map(ball.getPos()))
				.withVel(map(ball.getVel()))
				.withAcc(Vector3.zero())
				.withSpin(Vector2.zero())
				.build();
	}


	private IVector3 map(SslGcGeometry.Vector3 v)
	{
		if (v == null)
		{
			return Vector3.zero();
		}
		return Vector3.fromXYZ(v.getX(), v.getY(), v.getZ()).multiply(1000);
	}


	private IVector2 map(SslGcGeometry.Vector2 v)
	{
		if (v == null)
		{
			return Vector2.zero();
		}
		return Vector2.fromXY(v.getX(), v.getY()).multiply(1000);
	}
}
