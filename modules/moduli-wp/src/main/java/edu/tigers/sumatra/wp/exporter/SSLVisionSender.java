/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslDetection;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldCircularArc;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.Vector2f;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;


/**
 * Send out SSL vision frames based on WorldFrames
 */
@Log4j2
public class SSLVisionSender extends AModule implements IWorldFrameObserver
{
	private static final double GEOMETRY_BROADCAST_INTERVAL = 3;

	private MulticastUDPTransmitter transmitter;
	private int frameNumber = 0;
	private long tLastGeometrySent = 0;


	@Override
	public void startModule()
	{
		String address = getSubnodeConfiguration().getString("address", "224.5.23.2");
		int port = getSubnodeConfiguration().getInt("port", 11006);
		transmitter = new MulticastUDPTransmitter(address, port);

		String nifName = getSubnodeConfiguration().getString("interface", null);
		if (nifName != null)
		{
			log.info("Publishing vision packets to {}:{} ({})", address, port, nifName);
			transmitter.connectTo(nifName);
		} else
		{
			log.info("Publishing vision packets to {}:{} (all interfaces)", address, port);
			transmitter.connectToAllInterfaces();
		}

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{

		SSL_WrapperPacket.Builder wrapper = SSL_WrapperPacket.newBuilder();

		SSL_DetectionFrame.Builder frame = SSL_DetectionFrame.newBuilder();
		frame.setCameraId(0);
		frame.setFrameNumber(frameNumber++);
		double timestamp = wFrameWrapper.getTimestamp() / 1e9;
		frame.setTCapture(timestamp);
		frame.setTSent(timestamp);

		SSL_DetectionBall.Builder ball = SSL_DetectionBall.newBuilder();
		ball.setConfidence(1);
		ball.setX((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().x());
		ball.setY((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().y());
		ball.setZ((float) wFrameWrapper.getSimpleWorldFrame().getBall().getPos3().z());
		ball.setPixelX(0);
		ball.setPixelY(0);
		frame.addBalls(ball);

		for (ITrackedBot bot : wFrameWrapper.getSimpleWorldFrame().getBots().values())
		{
			MessagesRobocupSslDetection.SSL_DetectionRobot.Builder sslBot = MessagesRobocupSslDetection.SSL_DetectionRobot
					.newBuilder();
			sslBot.setConfidence(1);
			sslBot.setRobotId(bot.getBotId().getNumber());
			sslBot.setX((float) bot.getPos().x());
			sslBot.setY((float) bot.getPos().y());
			sslBot.setOrientation((float) bot.getOrientation());
			sslBot.setHeight(150);
			sslBot.setPixelX(0);
			sslBot.setPixelY(0);

			if (bot.getBotId().getTeamColor() == ETeamColor.YELLOW)
			{
				frame.addRobotsYellow(sslBot);
			} else
			{
				frame.addRobotsBlue(sslBot);
			}
		}
		wrapper.setDetection(frame);

		if ((wFrameWrapper.getTimestamp() - tLastGeometrySent) / 1e9 > GEOMETRY_BROADCAST_INTERVAL)
		{
			wrapper.setGeometry(createGeometryMessage());
			tLastGeometrySent = wFrameWrapper.getTimestamp();
		}

		transmitter.send(wrapper.build().toByteArray());
	}


	private SSL_GeometryData.Builder createGeometryMessage()
	{
		SSL_GeometryData.Builder geometry = SSL_GeometryData.newBuilder();
		SSL_GeometryFieldSize.Builder field = SSL_GeometryFieldSize.newBuilder();
		field.setBoundaryWidth((int) Geometry.getBoundaryWidth());
		field.setFieldLength((int) Geometry.getFieldLength());
		field.setFieldWidth((int) Geometry.getFieldWidth());
		field.setGoalDepth((int) Geometry.getGoalOur().getDepth());
		field.setGoalWidth((int) Geometry.getGoalOur().getWidth());

		SSL_FieldLineSegment.Builder penAreaLineStretchLeft = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pl1 = Vector2f.newBuilder();
		pl1.setX((float) Geometry.getPenaltyAreaTheir().getNegCorner().x());
		pl1.setY((float) Geometry.getPenaltyAreaTheir().getNegCorner().y());
		Vector2f.Builder pl2 = Vector2f.newBuilder();
		pl2.setX((float) Geometry.getPenaltyAreaTheir().getPosCorner().x());
		pl2.setY((float) Geometry.getPenaltyAreaTheir().getPosCorner().y());
		penAreaLineStretchLeft.setP1(pl1);
		penAreaLineStretchLeft.setP2(pl2);
		penAreaLineStretchLeft.setThickness(10);
		penAreaLineStretchLeft.setName("LeftPenaltyStretch");
		field.addFieldLines(penAreaLineStretchLeft);

		SSL_FieldLineSegment.Builder penAreaLineStretchRight = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pr1 = Vector2f.newBuilder();
		pr1.setX((float) Geometry.getPenaltyAreaOur().getNegCorner().x());
		pr1.setY((float) Geometry.getPenaltyAreaOur().getNegCorner().y());
		Vector2f.Builder pr2 = Vector2f.newBuilder();
		pr2.setX((float) Geometry.getPenaltyAreaOur().getPosCorner().x());
		pr2.setY((float) Geometry.getPenaltyAreaOur().getPosCorner().y());
		penAreaLineStretchRight.setP1(pr1);
		penAreaLineStretchRight.setP2(pr2);
		penAreaLineStretchRight.setThickness(10);
		penAreaLineStretchRight.setName("RightPenaltyStretch");
		field.addFieldLines(penAreaLineStretchRight);

		Geometry.getPenaltyAreaOur().getRectangle().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);

		Geometry.getPenaltyAreaTheir().getRectangle().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);

		Geometry.getField().getEdges().stream()
				.map(Lines::segmentFromLine)
				.map(l -> createLineSegment("", l))
				.forEach(field::addFieldLines);
		field.addFieldLines(createLineSegment("HalfwayLine",
				Lines.segmentFromPoints(
						Vector2.fromY(Geometry.getFieldWidth() / 2),
						Vector2.fromY(-Geometry.getFieldWidth() / 2))));
		field.addFieldLines(createLineSegment("CenterLine",
				Lines.segmentFromPoints(
						Vector2.fromX(Geometry.getFieldLength() / 2),
						Vector2.fromX(-Geometry.getFieldLength() / 2))));

		SSL_FieldCircularArc.Builder centerCircle = SSL_FieldCircularArc.newBuilder();
		centerCircle.setCenter(Vector2f.newBuilder().setX(0).setY(0).build());
		centerCircle.setA1(0);
		centerCircle.setA2((float) AngleMath.PI_TWO);
		centerCircle.setRadius((float) Geometry.getCenterCircle().radius());
		centerCircle.setThickness(10);
		centerCircle.setName("CenterCircle");
		field.addFieldArcs(centerCircle);

		geometry.setField(field);

		geometry.setModels(MessagesRobocupSslGeometry.SSL_GeometryModels.newBuilder()
				.setStraightTwoPhase(MessagesRobocupSslGeometry.SSL_BallModelStraightTwoPhase.newBuilder()
						.setAccSlide(Geometry.getBallParameters().getAccSlide())
						.setAccRoll(Geometry.getBallParameters().getAccRoll())
						.setKSwitch(Geometry.getBallParameters().getKSwitch())
						.build())
				.setChipFixedLoss(MessagesRobocupSslGeometry.SSL_BallModelChipFixedLoss.newBuilder()
						.setDampingXyFirstHop(Geometry.getBallParameters().getChipDampingXYFirstHop())
						.setDampingXyOtherHops(Geometry.getBallParameters().getChipDampingXYOtherHops())
						.setDampingZ(Geometry.getBallParameters().getChipDampingZ())
						.build())
				.build());
		return geometry;
	}


	private SSL_FieldLineSegment.Builder createLineSegment(final String name, final ILineSegment line)
	{
		SSL_FieldLineSegment.Builder lineSegment = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pr1 = Vector2f.newBuilder();
		pr1.setX((float) line.getStart().x());
		pr1.setY((float) line.getStart().y());
		Vector2f.Builder pr2 = Vector2f.newBuilder();
		pr2.setX((float) line.getEnd().x());
		pr2.setY((float) line.getEnd().y());
		lineSegment.setP1(pr1);
		lineSegment.setP2(pr2);
		lineSegment.setThickness(10);
		lineSegment.setName(name);
		return lineSegment;
	}
}
