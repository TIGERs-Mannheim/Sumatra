/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamFieldArc;
import edu.tigers.sumatra.cam.data.CamFieldLine;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldCircularArc;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldShapeType;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.Vector2f;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.complex.Quaternion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Translate geometry data from protobuf message to our format
 */
@Log4j2
public class SSLVisionCamGeometryTranslator
{
	private static final double DEFAULT_CENTER_CIRCLE_RADIUS = 500.0;


	public CamGeometry fromProtobuf(final SSL_GeometryData geometryData)
	{
		Map<Integer, CamCalibration> calibrations = geometryData.getCalibList().stream()
				.map(this::fromProtobuf)
				.collect(Collectors.toMap(CamCalibration::getCameraId, c -> c));

		return CamGeometry.builder()
				.fieldSize(fromProtobuf(geometryData.getField()))
				.ballModels(geometryData.getModels())
				.cameraCalibrations(calibrations)
				.build();
	}


	public SSL_GeometryData toProtobuf(CamGeometry camGeometry)
	{
		List<SSL_GeometryCameraCalibration> calibrations = camGeometry.getCameraCalibrations().values().stream()
				.map(this::toProtobuf)
				.toList();
		return SSL_GeometryData.newBuilder()
				.setModels(camGeometry.getBallModels())
				.setField(toProtobuf(camGeometry.getFieldSize()))
				.addAllCalib(calibrations)
				.build();
	}


	private CamFieldSize fromProtobuf(SSL_GeometryFieldSize field)
	{
		List<CamFieldLine> fieldLines = field.getFieldLinesList().stream().map(this::fromProtobuf)
				.toList();
		List<CamFieldArc> fieldArcs = field.getFieldArcsList().stream().map(this::fromProtobuf)
				.toList();
		return CamFieldSize.builder()
				.fieldLength(field.getFieldLength())
				.fieldWidth(field.getFieldWidth())
				.goalWidth(field.getGoalWidth())
				.goalDepth(field.getGoalDepth())
				.boundaryWidth(field.getBoundaryWidth())
				.fieldLines(fieldLines)
				.fieldArcs(fieldArcs)
				.penaltyAreaDepth(field.hasPenaltyAreaDepth() ?
						(double) field.getPenaltyAreaDepth() :
						penaltyAreaDepthFromLineSegments(fieldLines))
				.penaltyAreaWidth(field.hasPenaltyAreaWidth() ?
						(double) field.getPenaltyAreaWidth() :
						penaltyAreaWidthFromLineSegments(fieldLines))
				.centerCircleRadius(field.hasCenterCircleRadius() ?
						(double) field.getCenterCircleRadius() :
						centerCircleRadiusFromArcs(fieldArcs))
				.lineThickness(field.hasLineThickness() ? field.getLineThickness() : 10)
				.goalCenterToPenaltyMark(field.hasGoalCenterToPenaltyMark() ? field.getGoalCenterToPenaltyMark() : 8000)
				.goalHeight(field.hasGoalHeight() ? field.getGoalHeight() : 155)
				.ballRadius(field.hasBallRadius() ? field.getBallRadius() : 21.5)
				.robotRadius(field.hasMaxRobotRadius() ? field.getMaxRobotRadius() : 90)
				.build();
	}


	private SSL_GeometryFieldSize toProtobuf(CamFieldSize field)
	{
		return SSL_GeometryFieldSize.newBuilder()
				.setFieldLength((int) field.getFieldLength())
				.setFieldWidth((int) field.getFieldWidth())
				.setGoalWidth((int) field.getGoalWidth())
				.setGoalDepth((int) field.getGoalDepth())
				.setBoundaryWidth((int) field.getBoundaryWidth())
				.addAllFieldLines(field.getFieldLines().stream().map(this::toProtobuf).toList())
				.addAllFieldArcs(field.getFieldArcs().stream().map(this::toProtobuf).toList())
				.setPenaltyAreaDepth((int) field.getPenaltyAreaDepth())
				.setPenaltyAreaWidth((int) field.getPenaltyAreaWidth())
				.setCenterCircleRadius((int) field.getCenterCircleRadius())
				.setLineThickness((int) field.getLineThickness())
				.setGoalCenterToPenaltyMark((int) field.getGoalCenterToPenaltyMark())
				.setGoalHeight((int) field.getGoalHeight())
				.setBallRadius((float) field.getBallRadius())
				.setMaxRobotRadius((float) field.getRobotRadius())
				.build();
	}


	private CamFieldLine fromProtobuf(SSL_FieldLineSegment lineSegment)
	{
		IVector2 p1 = Vector2.fromXY(lineSegment.getP1().getX(), lineSegment.getP1().getY());
		IVector2 p2 = Vector2.fromXY(lineSegment.getP2().getX(), lineSegment.getP2().getY());
		var line = Lines.segmentFromPoints(p1, p2);
		return new CamFieldLine(lineSegment.getName(), lineSegment.getType(), lineSegment.getThickness(), line);
	}


	private SSL_FieldLineSegment toProtobuf(CamFieldLine lineSegment)
	{
		IVector2 p1 = lineSegment.getLine().getPathStart();
		IVector2 p2 = lineSegment.getLine().getPathEnd();
		return SSL_FieldLineSegment.newBuilder()
				.setName(lineSegment.getName())
				.setThickness((float) lineSegment.getThickness())
				.setType(lineSegment.getType())
				.setP1(Vector2f.newBuilder()
						.setX((float) p1.x())
						.setY((float) p1.y())
						.build())
				.setP2(Vector2f.newBuilder()
						.setX((float) p2.x())
						.setY((float) p2.y())
						.build())
				.build();
	}


	private CamFieldArc fromProtobuf(SSL_FieldCircularArc arc)
	{
		var center = Vector2.fromXY(arc.getCenter().getX(), arc.getCenter().getY());
		var circle = Arc.createArc(center, arc.getRadius(), arc.getA1(), arc.getA2() - arc.getA1());
		return new CamFieldArc(arc.getName(), arc.getType(), arc.getThickness(), circle);
	}


	private SSL_FieldCircularArc toProtobuf(CamFieldArc camFieldArc)
	{
		IArc arc = camFieldArc.getArc();
		return SSL_FieldCircularArc.newBuilder()
				.setName(camFieldArc.getName())
				.setThickness((float) camFieldArc.getThickness())
				.setType(camFieldArc.getType())
				.setCenter(Vector2f.newBuilder()
						.setX((float) arc.center().x())
						.setY((float) arc.center().y())
						.build())
				.setRadius((float) arc.radius())
				.setA1((float) arc.getStartAngle())
				.setA2((float) (arc.getStartAngle() + arc.getRotation()))
				.build();
	}


	private CamCalibration fromProtobuf(SSL_GeometryCameraCalibration cc)
	{
		return CamCalibration.builder()
				.cameraId(cc.getCameraId())
				.focalLength(cc.getFocalLength())
				.principalPoint(Vector2.fromXY(cc.getPrincipalPointX(), cc.getPrincipalPointY()))
				.distortion(cc.getDistortion())
				.rotationQuaternion(new Quaternion(cc.getQ3(), cc.getQ0(), cc.getQ1(), cc.getQ2()))
				.translation(Vector3.fromXYZ(cc.getTx(), cc.getTy(), cc.getTz()))
				.cameraPosition(
						Vector3.fromXYZ(
								cc.getDerivedCameraWorldTx(),
								cc.getDerivedCameraWorldTy(),
								cc.getDerivedCameraWorldTz()
						)
				)
				.build();
	}


	private SSL_GeometryCameraCalibration toProtobuf(CamCalibration cc)
	{
		return SSL_GeometryCameraCalibration.newBuilder()
				.setCameraId(cc.getCameraId())
				.setFocalLength((float) cc.getFocalLength())
				.setPrincipalPointX((float) cc.getPrincipalPoint().x())
				.setPrincipalPointY((float) cc.getPrincipalPoint().y())
				.setDistortion((float) cc.getDistortion())
				.setQ0((float) cc.getRotationQuaternion().getQ3())
				.setQ1((float) cc.getRotationQuaternion().getQ0())
				.setQ2((float) cc.getRotationQuaternion().getQ1())
				.setQ3((float) cc.getRotationQuaternion().getQ2())
				.setTx((float) cc.getTranslation().x())
				.setTy((float) cc.getTranslation().y())
				.setTz((float) cc.getTranslation().z())
				.setDerivedCameraWorldTx((float) cc.getCameraPosition().x())
				.setDerivedCameraWorldTy((float) cc.getCameraPosition().y())
				.setDerivedCameraWorldTz((float) cc.getCameraPosition().z())
				.build();
	}


	private double penaltyAreaWidthFromLineSegments(List<CamFieldLine> lines)
	{
		Set<SSL_FieldShapeType> penaltyStretchTypes = Set.of(
				SSL_FieldShapeType.LeftPenaltyStretch,
				SSL_FieldShapeType.RightPenaltyStretch
		);
		return uniqueLengthFromSegments(lines, penaltyStretchTypes);
	}


	private double penaltyAreaDepthFromLineSegments(List<CamFieldLine> lines)
	{
		Set<SSL_FieldShapeType> penaltyStretchTypes = Set.of(
				SSL_FieldShapeType.LeftFieldLeftPenaltyStretch,
				SSL_FieldShapeType.LeftFieldRightPenaltyStretch,
				SSL_FieldShapeType.RightFieldLeftPenaltyStretch,
				SSL_FieldShapeType.RightFieldRightPenaltyStretch
		);
		return uniqueLengthFromSegments(lines, penaltyStretchTypes);
	}


	private double uniqueLengthFromSegments(List<CamFieldLine> lines, Set<SSL_FieldShapeType> penaltyStretchTypes)
	{
		var penaltyStretchTypeNames = penaltyStretchTypes.stream()
				.map(SSL_FieldShapeType::name)
				.collect(Collectors.toUnmodifiableSet());

		List<CamFieldLine> penaltyStretches = lines.stream()
				.filter(l -> penaltyStretchTypes.contains(l.getType()) || penaltyStretchTypeNames.contains(l.getName()))
				.toList();

		if (penaltyStretches.size() != penaltyStretchTypes.size())
		{
			log.warn("Expected {} penalty stretches, but found: {}", penaltyStretchTypes.size(), penaltyStretches);
			return 0;
		}

		// calculate length of penalty area front line
		List<Double> lengths = penaltyStretches.stream()
				.map(CamFieldLine::getLine)
				.map(ILineSegment::directionVector)
				.map(IVector::getLength2)
				.distinct()
				.toList();

		if (lengths.size() != 1)
		{
			log.warn("Penalty stretch line lengths are not unique: {}", lengths);
			return 0;
		}
		return lengths.get(0);
	}


	private double centerCircleRadiusFromArcs(List<CamFieldArc> arcs)
	{
		return arcs.stream()
				.filter(a -> a.getType() == MessagesRobocupSslGeometry.SSL_FieldShapeType.CenterCircle)
				.findFirst()
				.map(camFieldArc -> camFieldArc.getArc().radius())
				.orElse(DEFAULT_CENTER_CIRCLE_RADIUS);
	}
}
