/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.cam.proto.SslVisionDetection;
import edu.tigers.sumatra.cam.proto.SslVisionDetection.SSL_DetectionBall;
import edu.tigers.sumatra.cam.proto.SslVisionDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_FieldCircularArc;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_FieldShapeType;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_GeometryData;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.Vector2f;
import edu.tigers.sumatra.cam.proto.SslVisionWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * Send out SSL vision frames based on WorldFrames
 */
@Log4j2
public class SSLVisionSender extends AModule implements IWorldFrameObserver
{
	private static final double GEOMETRY_BROADCAST_INTERVAL = 3;
	private static final int LINE_THICKNESS = 10;

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
			SslVisionDetection.SSL_DetectionRobot.Builder sslBot = SslVisionDetection.SSL_DetectionRobot
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
		field.setFieldLength((int) Geometry.getFieldLength());
		field.setFieldWidth((int) Geometry.getFieldWidth());
		field.setGoalWidth((int) Geometry.getGoalOur().getWidth());
		field.setGoalDepth((int) Geometry.getGoalOur().getDepth());
		field.setBoundaryWidth((int) Geometry.getBoundaryWidth());
		field.setPenaltyAreaDepth((int) Geometry.getPenaltyAreaDepth());
		field.setPenaltyAreaWidth((int) Geometry.getPenaltyAreaWidth());
		field.setCenterCircleRadius((int) Geometry.getCenterCircle().radius());
		field.setLineThickness(LINE_THICKNESS);
		field.setGoalCenterToPenaltyMark((int) Geometry.getGoalCenterToPenaltyMark());
		field.setGoalHeight((int) Geometry.getGoalHeight());
		field.setBallRadius((int) Geometry.getBallRadius());
		field.setMaxRobotRadius((int) Geometry.getBotRadius());

		field.addAllFieldLines(createPenAreas());
		field.addAllFieldLines(createFieldBorder());

		SSL_FieldCircularArc.Builder centerCircle = SSL_FieldCircularArc.newBuilder();
		centerCircle.setCenter(Vector2f.newBuilder().setX(0).setY(0).build());
		centerCircle.setA1(0);
		centerCircle.setA2((float) AngleMath.PI_TWO);
		centerCircle.setRadius((float) Geometry.getCenterCircle().radius());
		centerCircle.setThickness(LINE_THICKNESS);
		centerCircle.setName(SSL_FieldShapeType.CenterCircle.name());
		centerCircle.setType(SSL_FieldShapeType.CenterCircle);
		field.addFieldArcs(centerCircle);

		geometry.setField(field);

		geometry.setModels(SslVisionGeometry.SSL_GeometryModels.newBuilder()
				.setStraightTwoPhase(SslVisionGeometry.SSL_BallModelStraightTwoPhase.newBuilder()
						.setAccSlide(Geometry.getBallParameters().getAccSlide())
						.setAccRoll(Geometry.getBallParameters().getAccRoll())
						.setKSwitch(Geometry.getBallParameters().getKSwitch())
						.build())
				.setChipFixedLoss(SslVisionGeometry.SSL_BallModelChipFixedLoss.newBuilder()
						.setDampingXyFirstHop(Geometry.getBallParameters().getChipDampingXYFirstHop())
						.setDampingXyOtherHops(Geometry.getBallParameters().getChipDampingXYOtherHops())
						.setDampingZ(Geometry.getBallParameters().getChipDampingZ())
						.build())
				.build());
		return geometry;
	}


	private List<SSL_FieldLineSegment> createPenAreas()
	{
		return List.of(
				createLineSegment(
						SSL_FieldShapeType.LeftPenaltyStretch,
						Lines.segmentFromPoints(
								Geometry.getPenaltyAreaTheir().getNegCorner(),
								Geometry.getPenaltyAreaTheir().getPosCorner()
						)
				).build(),
				createLineSegment(
						SSL_FieldShapeType.LeftFieldLeftPenaltyStretch,
						Lines.segmentFromPoints(
								Vector2.fromXY(
										Geometry.getPenaltyAreaTheir().getGoalCenter().x(),
										Geometry.getPenaltyAreaTheir().getPosCorner().y()
								),
								Geometry.getPenaltyAreaTheir().getPosCorner())
				).build(),
				createLineSegment(
						SSL_FieldShapeType.LeftFieldRightPenaltyStretch,
						Lines.segmentFromPoints(
								Vector2.fromXY(
										Geometry.getPenaltyAreaTheir().getGoalCenter().x(),
										Geometry.getPenaltyAreaTheir().getNegCorner().y()
								),
								Geometry.getPenaltyAreaTheir().getNegCorner())
				).build(),
				createLineSegment(
						SSL_FieldShapeType.RightPenaltyStretch,
						Lines.segmentFromPoints(
								Geometry.getPenaltyAreaOur().getNegCorner(),
								Geometry.getPenaltyAreaOur().getPosCorner()
						)
				).build(),
				createLineSegment(
						SSL_FieldShapeType.RightFieldLeftPenaltyStretch,
						Lines.segmentFromPoints(
								Vector2.fromXY(
										Geometry.getPenaltyAreaOur().getGoalCenter().x(),
										Geometry.getPenaltyAreaOur().getPosCorner().y()
								),
								Geometry.getPenaltyAreaOur().getPosCorner())
				).build(),
				createLineSegment(
						SSL_FieldShapeType.RightFieldRightPenaltyStretch,
						Lines.segmentFromPoints(
								Vector2.fromXY(
										Geometry.getPenaltyAreaOur().getGoalCenter().x(),
										Geometry.getPenaltyAreaOur().getNegCorner().y()
								),
								Geometry.getPenaltyAreaOur().getNegCorner())
				).build()
		);
	}


	private List<SSL_FieldLineSegment> createFieldBorder()
	{
		return List.of(
				createLineSegment(
						SSL_FieldShapeType.TopTouchLine,
						Lines.segmentFromPoints(
								Vector2.fromXY(Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2),
								Vector2.fromXY(-Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2)
						)
				).build(),

				createLineSegment(
						SSL_FieldShapeType.BottomTouchLine,
						Lines.segmentFromPoints(
								Vector2.fromXY(Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2),
								Vector2.fromXY(-Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2)
						)
				).build(),

				createLineSegment(
						SSL_FieldShapeType.LeftGoalLine,
						Lines.segmentFromPoints(
								Vector2.fromXY(-Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2),
								Vector2.fromXY(-Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2)
						)
				).build(),

				createLineSegment(
						SSL_FieldShapeType.RightGoalLine,
						Lines.segmentFromPoints(
								Vector2.fromXY(Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2),
								Vector2.fromXY(Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2)
						)
				).build(),

				createLineSegment(
						SSL_FieldShapeType.HalfwayLine,
						Lines.segmentFromPoints(
								Vector2.fromY(Geometry.getFieldWidth() / 2),
								Vector2.fromY(-Geometry.getFieldWidth() / 2)
						)
				).build(),

				createLineSegment(
						SSL_FieldShapeType.CenterLine,
						Lines.segmentFromPoints(
								Vector2.fromX(Geometry.getFieldLength() / 2),
								Vector2.fromX(-Geometry.getFieldLength() / 2)
						)
				).build()
		);
	}


	private SSL_FieldLineSegment.Builder createLineSegment(final SSL_FieldShapeType type, final ILineSegment line)
	{
		SSL_FieldLineSegment.Builder lineSegment = SSL_FieldLineSegment.newBuilder();
		Vector2f.Builder pr1 = Vector2f.newBuilder();
		pr1.setX((float) line.getPathStart().x());
		pr1.setY((float) line.getPathStart().y());
		Vector2f.Builder pr2 = Vector2f.newBuilder();
		pr2.setX((float) line.getPathEnd().x());
		pr2.setY((float) line.getPathEnd().y());
		lineSegment.setP1(pr1);
		lineSegment.setP2(pr2);
		lineSegment.setThickness(LINE_THICKNESS);
		lineSegment.setName(type.name());
		lineSegment.setType(type);
		return lineSegment;
	}
}
