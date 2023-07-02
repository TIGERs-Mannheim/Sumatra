/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.botshape;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Data
@RequiredArgsConstructor(staticName = "fromFullSpecification")
public class BotShape implements IBotShape
{
	private final IVector2 position;
	private final double radius;
	private final double center2Dribbler;
	private final double orientation;


	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 *
	 * @param botPos
	 * @param orientation
	 * @param center2Dribbler
	 * @return kick position of a bot that is located at botPos
	 */
	public static IVector2 getKickerCenterPos(final IVector2 botPos, final double orientation,
			final double center2Dribbler)
	{
		return botPos.addNew(Vector2.fromAngle(orientation).scaleTo(center2Dribbler));
	}


	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 *
	 * @param pose
	 * @param center2Dribbler
	 * @return kick position of a bot that is located at botPos
	 */
	public static IVector2 getKickerCenterPos(final Pose pose,
			final double center2Dribbler)
	{
		return getKickerCenterPos(pose.getPos(), pose.getOrientation(), center2Dribbler);
	}


	/**
	 * Calculates the position of the robot's center based on the given kicer position, orientation and
	 * distance from center to dribbler
	 *
	 * @param kickerPos
	 * @param orientation
	 * @param center2Dribbler
	 * @return the bot center position
	 */
	public static IVector2 getCenterFromKickerPos(final IVector2 kickerPos, final double orientation,
			final double center2Dribbler)
	{
		return kickerPos.addNew(Vector2.fromAngle(orientation).multiply(-center2Dribbler));
	}


	@Override
	public IBotShape mirror()
	{
		return new BotShape(position.multiplyNew(-1), radius, center2Dribbler, orientation + AngleMath.PI);
	}


	@Override
	public double radius()
	{
		return radius;
	}


	@Override
	public IVector2 center()
	{
		return position;
	}


	@Override
	public IVector2 getKickerCenterPos()
	{
		return getKickerCenterPos(position, orientation, center2Dribbler);
	}


	@Override
	public ILineSegment getKickerLine()
	{
		double orient2CornerAngle = SumatraMath.acos(center2Dribbler / radius);

		IVector2 p1 = position.addNew(Vector2.fromAngle(orientation + orient2CornerAngle).scaleTo(radius));
		IVector2 p2 = position.addNew(Vector2.fromAngle(orientation - orient2CornerAngle).scaleTo(radius));

		return Lines.segmentFromPoints(p1, p2);
	}


	@Override
	public boolean isPointInKickerZone(final IVector2 point, final double zoneLength)
	{
		return isPointInKickerZone(point, zoneLength, getKickerWidth());
	}


	@Override
	public boolean isPointInKickerZone(final IVector2 point, final double zoneLength, final double zoneWidth)
	{
		var orientLine = Lines.halfLineFromDirection(position, Vector2.fromAngle(orientation));
		var leadPoint = orientLine.toLine().closestPointOnPath(point);

		if (!orientLine.isPointInFront(leadPoint))
		{
			return false;
		}

		double distBotToLeadPoint = position.distanceTo(leadPoint);

		if ((distBotToLeadPoint < center2Dribbler) || (distBotToLeadPoint > (center2Dribbler + zoneLength)))
		{
			return false;
		}

		double distPointToLeadPoint = point.distanceTo(leadPoint);

		return distPointToLeadPoint <= (zoneWidth * 0.5);
	}


	@Override
	public double getKickerWidth()
	{
		double orient2CornerAngle = SumatraMath.acos(center2Dribbler / radius);
		return SumatraMath.sin(orient2CornerAngle) * radius * 2.0;
	}


	@Override
	public double getCenter2Dribbler()
	{
		return center2Dribbler;
	}


	@Override
	public double getOrientation()
	{
		return orientation;
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		if (position.distanceTo(point) > radius + IPath.LINE_MARGIN)
		{
			return false;
		}
		// check if point is not in front of kicker.
		// Use radius as kickerZone as maximum value here (in case of center2Dribbler==0)
		return !isPointInKickerZone(point, radius) || getKickerLine().isPointOnPath(point);
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		double r = radius;
		double alpha = SumatraMath.acos(center2Dribbler / r);
		double startAngleRad = orientation - alpha;
		double angleExtent = 2 * alpha - AngleMath.PI_TWO;

		return List.of(
				Arc.createArc(position, radius, startAngleRad, angleExtent),
				getKickerLine()
		);
	}


	@Override
	public IBotShape withMargin(final double margin)
	{
		double factor = (margin + radius) / radius;

		return new BotShape(position, radius * factor, center2Dribbler * factor, orientation);
	}
}
