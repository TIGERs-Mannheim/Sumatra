/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class RobotCollisionShape
{
	private final IVector2 pos;
	private final double orient;
	private final double radius;
	private final double center2Dribbler;
	private final double maxCircleLoss;
	private final double maxFrontLoss;
	
	
	/**
	 * @param pos
	 * @param orient
	 * @param radius
	 * @param center2Dribbler
	 * @param maxCircleLoss
	 * @param maxFrontLoss
	 */
	public RobotCollisionShape(final IVector2 pos, final double orient, final double radius,
			final double center2Dribbler, final double maxCircleLoss,
			final double maxFrontLoss)
	{
		this.pos = pos;
		this.orient = orient;
		this.radius = radius;
		this.center2Dribbler = center2Dribbler;
		this.maxCircleLoss = maxCircleLoss;
		this.maxFrontLoss = maxFrontLoss;
	}
	
	
	/**
	 * @param ballPos Position [mm]
	 * @param ballVel Velocity [mm/s]
	 * @return
	 */
	public CollisionResult getCollision(final IVector2 ballPos, final IVector2 ballVel)
	{
		final double ballRadius = Geometry.getBallRadius();
		IVector2 ballVelUsed = ballVel;
		
		// check if outside of bot anyway (bounding circle)
		if (!BotShape.fromFullSpecification(pos, radius, center2Dribbler, orient).isPointInShape(ballPos))
		{
			return new CollisionResult(ECollisionLocation.NONE, null);
		}
		
		if (ballVelUsed.getLength2() < 10)
		{
			// ball very slow, project outside and generate new inbound vel
			ICircle botCircle = Circle.createCircle(pos, radius + ballRadius);
			
			IVector2 outside = botCircle.nearestPointOutside(ballPos);
			ballVelUsed = ballPos.subtractNew(outside);
		}
		
		ILineSegment frontLine = BotMath.getDribblerFrontLine(Vector3.from2d(pos, orient), radius + ballRadius,
				center2Dribbler + ballRadius);
		
		ILineSegment ballVelLine = Lines.segmentFromOffset(ballPos, ballVelUsed.multiplyNew(-1.0).scaleTo(radius * 5.0));
		
		Optional<IVector2> frontIntersect = frontLine.intersectSegment(ballVelLine);
		
		if (frontIntersect.isPresent())
		{
			double collisionAngle = Vector2.fromAngle(orient).multiply(-1.0).angleTo(ballVelUsed).orElse(Math.PI);
			double collisionAngleAbs = Math.abs(collisionAngle);
			if (collisionAngleAbs > AngleMath.PI_HALF)
			{
				// ball is rolling away from robot => no real front collision with reflection
				return new CollisionResult(ECollisionLocation.CIRCLE, null);
			}
			
			double outVelAbs = ballVelUsed.getLength2()
					- (ballVelUsed.getLength2() * SumatraMath.cos(collisionAngleAbs) * maxFrontLoss);
			
			Vector2 outVel = Vector2.fromAngle(orient).multiply(-1.0).turn(-collisionAngle).multiply(-1.0)
					.scaleTo(outVelAbs);
			
			return new CollisionResult(ECollisionLocation.FRONT, outVel);
		}
		
		ICircle botCircle = Circle.createCircle(pos, radius + ballRadius);
		List<IVector2> intersect = botCircle.lineIntersections(ballVelLine);
		
		if (!intersect.isEmpty())
		{
			IVector2 intersectCircle = intersect.get(0);
			double collisionAngle = Vector2.fromPoints(intersectCircle, pos).angleTo(ballVelUsed).orElse(Math.PI);
			double collisionAngleAbs = Math.abs(collisionAngle);
			if (collisionAngleAbs > AngleMath.PI_HALF)
			{
				// ball is rolling away from robot
				return new CollisionResult(ECollisionLocation.CIRCLE, null);
			}
			
			double outVelAbs = ballVelUsed.getLength2()
					- (ballVelUsed.getLength2() * SumatraMath.cos(collisionAngleAbs) * maxCircleLoss);
			
			Vector2 outVel = Vector2.fromPoints(intersectCircle, pos).turn(-collisionAngle).multiply(-1.0)
					.scaleTo(outVelAbs);
			
			return new CollisionResult(ECollisionLocation.CIRCLE, outVel);
		}
		
		// this should actually not happen
		Validate.isTrue(false);
		
		return new CollisionResult(ECollisionLocation.NONE, null);
	}
	
	
	/**
	 * Collision result.
	 */
	public static class CollisionResult
	{
		private final ECollisionLocation location;
		private final IVector2 ballReflectedVel;
		
		
		/**
		 * @param location
		 * @param ballReflectedVel
		 */
		private CollisionResult(final ECollisionLocation location,
				final IVector2 ballReflectedVel)
		{
			this.location = location;
			this.ballReflectedVel = ballReflectedVel;
		}
		
		
		public ECollisionLocation getLocation()
		{
			return location;
		}
		
		
		/**
		 * @return the ballReflectedVel
		 */
		public IVector2 getBallReflectedVel()
		{
			return ballReflectedVel;
		}
	}
	
	/**
	 * Collision location.
	 */
	public enum ECollisionLocation
	{
		NONE,
		FRONT,
		CIRCLE
	}
}
