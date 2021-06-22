/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcCommon;
import edu.tigers.sumatra.referee.proto.SslGcGeometry;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.proto.SslVisionDetectionTracked;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class TrackerPacketGenerator
{
	private final String sourceName;
	private static final Set<SslVisionDetectionTracked.Capability> CAPABILITIES = new HashSet<>();
	private int frameNumber = 0;
	private final String uuid = UUID.randomUUID().toString();

	static
	{
		CAPABILITIES.add(SslVisionDetectionTracked.Capability.CAPABILITY_DETECT_FLYING_BALLS);
		CAPABILITIES.add(SslVisionDetectionTracked.Capability.CAPABILITY_DETECT_KICKED_BALLS);
	}


	public SslVisionWrapperTracked.TrackerWrapperPacket generate(final SimpleWorldFrame swf)
	{
		SslVisionDetectionTracked.TrackedFrame.Builder frame = SslVisionDetectionTracked.TrackedFrame.newBuilder();
		frame.setFrameNumber(frameNumber++);
		frame.setTimestamp(buildTimestamp(swf.getTimestamp()));
		frame.addBalls(buildBall(swf.getBall()));
		frame.addAllRobots(buildRobots(swf.getBots().values()));
		swf.getKickFitState()
				.map(s -> buildKickEvent(swf, s))
				.ifPresent(frame::setKickedBall);
		frame.addAllCapabilities(CAPABILITIES);
		SslVisionWrapperTracked.TrackerWrapperPacket.Builder wrapper = SslVisionWrapperTracked.TrackerWrapperPacket
				.newBuilder();
		wrapper.setUuid(uuid);
		wrapper.setSourceName(sourceName);
		wrapper.setTrackedFrame(frame);
		return wrapper.build();
	}


	private SslVisionDetectionTracked.KickedBall buildKickEvent(final SimpleWorldFrame wFrame,
			final BallKickFitState ballKickFitState)
	{
		final IVector2 stopPos = wFrame.getBall().getTrajectory().getPosByVel(0.0).getXYVector();
		final double time2Stop = wFrame.getBall().getTrajectory().getTimeByPos(stopPos);
		final long stopTimestamp = wFrame.getTimestamp() + (Double.isFinite(time2Stop) ? ((long) (time2Stop * 1e9)) : 0);
		final SslVisionDetectionTracked.KickedBall.Builder kickedBall = SslVisionDetectionTracked.KickedBall
				.newBuilder()
				.setPos(buildVector2(ballKickFitState.getKickPos().multiplyNew(1e-3)))
				.setVel(buildVector3(ballKickFitState.getKickVel()))
				.setStartTimestamp(buildTimestamp(ballKickFitState.getKickTimestamp()))
				.setStopTimestamp(buildTimestamp(stopTimestamp))
				.setStopPos(buildVector2(stopPos.multiplyNew(1e-3)));

		wFrame.getKickEvent()
				.map(IKickEvent::getKickingBot)
				.map(this::buildRobotId)
				.ifPresent(kickedBall::setRobotId);
		return kickedBall.build();
	}


	private Iterable<? extends SslVisionDetectionTracked.TrackedRobot> buildRobots(
			final Collection<ITrackedBot> values)
	{
		return values.stream().map(this::buildRobot).collect(Collectors.toList());
	}


	private SslVisionDetectionTracked.TrackedRobot buildRobot(final ITrackedBot bot)
	{
		return SslVisionDetectionTracked.TrackedRobot.newBuilder()
				.setRobotId(buildRobotId(bot.getBotId()))
				.setPos(buildVector2(bot.getPos().multiplyNew(1e-3)))
				.setOrientation((float) bot.getOrientation())
				.setVel(buildVector2(bot.getVel()))
				.setVelAngular((float) bot.getAngularVel())
				.setVisibility((float) bot.getQuality())
				.build();
	}


	private SslGcCommon.RobotId buildRobotId(final BotID botId)
	{
		return SslGcCommon.RobotId.newBuilder()
				.setId(botId.getNumber())
				.setTeam(GcEventFactory.map(botId.getTeamColor()))
				.build();
	}


	private SslVisionDetectionTracked.TrackedBall buildBall(final ITrackedBall ball)
	{
		return SslVisionDetectionTracked.TrackedBall
				.newBuilder()
				.setPos(buildVector3(ball.getPos3().multiplyNew(1e-3)))
				.setVel(buildVector3(ball.getVel3()))
				.setVisibility((float) ball.getQuality())
				.build();
	}


	private SslGcGeometry.Vector3 buildVector3(final IVector3 pos3)
	{
		return SslGcGeometry.Vector3.newBuilder()
				.setX((float) pos3.x())
				.setY((float) pos3.y())
				.setZ((float) pos3.z())
				.build();
	}


	private SslGcGeometry.Vector2 buildVector2(final IVector2 pos2)
	{
		return SslGcGeometry.Vector2.newBuilder()
				.setX((float) pos2.x())
				.setY((float) pos2.y())
				.build();
	}


	private double buildTimestamp(long timestamp)
	{
		return timestamp / 1e9;
	}
}
