/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import static java.lang.Math.abs;

import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingConsultant;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCollision implements ICollisionObject
{
	private final CircleCollision				circleCollision;
	private final SingleSidedLineCollision	lineCollision;
	private final IVector3						pos;
	private final IVector3						vel;
	private final double							center2DribblerDist;
	private final ILine							frontLine;
	private final ITriangle						frontTriangle;
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param center2DribblerDist
	 */
	public BotCollision(final IVector3 pos, final IVector3 vel, final double center2DribblerDist)
	{
		this(pos, vel, center2DribblerDist, false, 0, false);
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param center2DribblerDist
	 * @param sticky
	 * @param kickSpeed
	 * @param chip
	 */
	public BotCollision(final IVector3 pos, final IVector3 vel, final double center2DribblerDist, final boolean sticky,
			final double kickSpeed, final boolean chip)
	{
		this.pos = pos;
		this.vel = vel;
		this.center2DribblerDist = center2DribblerDist;
		
		circleCollision = new CircleCollision(Circle.createCircle(pos.getXYVector(), Geometry.getBotRadius()
				+ Geometry.getBallRadius()), vel);
		
		double theta = Math.acos((center2DribblerDist + Geometry.getBallRadius())
				/ (Geometry.getBotRadius() + Geometry.getBallRadius()));
		IVector2 leftBotEdge = pos.getXYVector()
				.addNew(Vector2.fromAngle(pos.z() - theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
		IVector2 rightBotEdge = pos.getXYVector()
				.addNew(Vector2.fromAngle(pos.z() + theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
		frontLine = Line.fromPoints(leftBotEdge, rightBotEdge);
		frontTriangle = Triangle.fromCorners(pos.getXYVector(), leftBotEdge, rightBotEdge);
		
		lineCollision = new SingleSidedLineCollision(frontLine, vel, Vector2.fromAngle(pos.z()));
		
		IVector3 impulse;
		if (chip)
		{
			IVector2 kickVector = new FixedLossPlusRollingConsultant(new FixedLossPlusRollingParameters())
					.absoluteKickVelToVector(kickSpeed);
			impulse = Vector3.from2d(Vector2.fromAngle(pos.z()).scaleTo(kickVector.x()), kickVector.y());
		} else
		{
			impulse = Vector2.fromAngle(pos.z()).scaleTo(kickSpeed).getXYZVector();
		}
		
		lineCollision.setImpulse(impulse);
		lineCollision.setSticky(sticky);
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	private boolean isInFront(final IVector3 prePos)
	{
		double bot2PrePosAngle = prePos.getXYVector().subtractNew(pos).getAngle(0);
		double theta = Math.acos((center2DribblerDist + Geometry.getBallRadius())
				/ (Geometry.getBotRadius() + Geometry.getBallRadius()));
		double angleDiff = abs(AngleMath.difference(pos.z(), bot2PrePosAngle));
		return angleDiff < theta;
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		if (isInFront(prePos))
		{
			return lineCollision.getCollision(prePos, postPos);
		}
		// ball is NOT in front of kicker -> collision is on circle
		return circleCollision.getCollision(prePos, postPos);
	}
	
	
	@Override
	public Optional<ICollision> getInsideCollision(final IVector3 prePos)
	{
		if (isInFront(prePos))
		{
			double margin = lineCollision.isSticky() && lineCollision.getImpulse(prePos).getXYVector().isZeroVector() ? 10
					: 0;
			if (frontTriangle.isPointInShape(prePos.getXYVector(), margin))
			{
				IVector2 normal = Vector2.fromAngle(pos.z());
				IVector2 colPos;
				if (lineCollision.isSticky())
				{
					colPos = pos.getXYVector().addNew(normal.scaleToNew(center2DribblerDist + Geometry.getBallRadius()));
				} else
				{
					colPos = frontLine.leadPointOf(prePos.getXYVector());
				}
				Collision collision = new Collision(colPos, normal, this);
				return Optional.of(collision);
			}
			return Optional.empty();
		}
		return circleCollision.getInsideCollision(prePos);
	}
	
	
	@Override
	public IVector3 getImpulse(final IVector3 prePos)
	{
		return lineCollision.getImpulse(prePos);
	}
}
