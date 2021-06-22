/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;

import java.util.Optional;

import static java.lang.Math.abs;


/**
 *
 */
public class BotCollisionObject implements ICollisionObject
{
	private final CircleCollisionObject circleCollisionObject;
	private final KickerFrontLineCollisionObject lineCollision;
	private final Pose pose;
	private final IVector3 vel;
	private final double center2DribblerDist;
	private final ILine frontLine;
	private final ITriangle frontTriangle;
	private final BotID botID;


	/**
	 * @param pose
	 * @param vel
	 * @param center2DribblerDist
	 * @param botID
	 */
	public BotCollisionObject(final Pose pose, final IVector3 vel, final double center2DribblerDist, final BotID botID)
	{
		this(pose, vel, center2DribblerDist, botID, false, 0, false);
	}


	/**
	 * @param pose
	 * @param vel
	 * @param center2DribblerDist
	 * @param botID
	 * @param sticky
	 * @param kickSpeed
	 * @param chip
	 */
	public BotCollisionObject(final Pose pose, final IVector3 vel, final double center2DribblerDist, final BotID botID,
			final boolean sticky,
			final double kickSpeed, final boolean chip)
	{
		this.pose = pose;
		this.vel = vel;
		this.center2DribblerDist = center2DribblerDist;
		this.botID = botID;

		circleCollisionObject = new CircleCollisionObject(Circle.createCircle(pose.getPos(), Geometry.getBotRadius()
				+ Geometry.getBallRadius()), vel);

		double theta = SumatraMath.acos(center2DribblerDist / Geometry.getBotRadius());
		double kickWidth = (center2DribblerDist + Geometry.getBallRadius()) * SumatraMath.tan(theta) * 2.0;
		IVector2 kickCenter = pose.getPos()
				.addNew(Vector2.fromAngleLength(pose.getOrientation(), center2DribblerDist + Geometry.getBallRadius()));
		IVector2 leftBotEdge = kickCenter
				.addNew(Vector2.fromAngleLength(pose.getOrientation() + AngleMath.DEG_090_IN_RAD, kickWidth));
		IVector2 rightBotEdge = kickCenter
				.addNew(Vector2.fromAngleLength(pose.getOrientation() - AngleMath.DEG_090_IN_RAD, kickWidth));

		frontLine = Line.fromPoints(leftBotEdge, rightBotEdge);
		frontTriangle = Triangle.fromCorners(pose.getPos(), leftBotEdge, rightBotEdge);

		lineCollision = new KickerFrontLineCollisionObject(frontLine, vel, Vector2.fromAngle(pose.getOrientation()),
				botID);

		IVector3 impulse;
		if (chip)
		{
			impulse = Geometry.getBallFactory().createChipConsultant().speedToVel(pose.getOrientation(), kickSpeed);
		} else
		{
			impulse = Vector2.fromAngle(pose.getOrientation()).scaleTo(kickSpeed).getXYZVector();
		}

		if (sticky)
		{
			lineCollision.setAcc(Vector3.from2d(
					Vector2.fromAngle(pose.getOrientation()).scaleTo(-10),
					0));
		}

		lineCollision.setImpulse(impulse);
		lineCollision.setSticky(sticky);
		lineCollision.setDampFactor(1);
	}


	@Override
	public IVector3 getVel()
	{
		return vel;
	}


	private boolean isInFront(final IVector3 prePos)
	{
		double bot2PrePosAngle = prePos.getXYVector().subtractNew(pose.getPos()).getAngle(0);
		double theta = SumatraMath.acos(center2DribblerDist / Geometry.getBotRadius());
		double angleDiff = abs(AngleMath.difference(pose.getOrientation(), bot2PrePosAngle));
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
		return circleCollisionObject.getCollision(prePos, postPos);
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
				IVector2 normal = Vector2.fromAngle(pose.getOrientation());
				IVector2 colPos;
				colPos = frontLine.leadPointOf(prePos.getXYVector());
				return Optional.of(new Collision(colPos, normal, lineCollision));
			}
			return Optional.empty();
		}
		return circleCollisionObject.getInsideCollision(prePos);
	}


	@Override
	public IVector3 getImpulse(final IVector3 prePos)
	{
		return lineCollision.getImpulse(prePos);
	}


	@Override
	public BotID getBotID()
	{
		return botID;
	}
}
