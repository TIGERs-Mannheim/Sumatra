/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.sim.ISimBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Process the collision of the ball with other objects like bots, goals and field border
 */
public class BallCollisionHandler
{
	@Configurable(defValue = "false")
	private static boolean enableSpin = false;
	
	public ICollisionState process(final BallState preState, final double dt, final Collection<? extends ISimBot> bots)
	{
		final List<ICollisionObject> collisionObjects = generateCollisionObjects(bots);
		final BallCollisionState state = new BallCollisionState(preState);
		if (preState.getPos().z() > Geometry.getBallParameters().getMaxInterceptableHeight())
		{
			return state;
		}
		Optional<ICollision> collision = getFirstCollision(preState, dt, collisionObjects);
		if (collision.isPresent())
		{
			state.setCollision(collision.orElse(null));
			reflect(state);
		} else
		{
			Optional<ICollision> insideCollision = getFirstInsideCollision(preState.getPos(), collisionObjects);
			if (insideCollision.isPresent())
			{
				state.setCollision(insideCollision.orElse(null));
				moveOutside(state);
			}
		}
		impulse(state);

		return state;
	}


	private List<ICollisionObject> generateCollisionObjects(final Collection<? extends ISimBot> bots)
	{
		List<ICollisionObject> objects = new ArrayList<>();

		addBots(bots, objects);

		addOurGoal(objects);

		addTheirGoal(objects);

		addFieldBoundary(objects);

		return objects;
	}


	private void addFieldBoundary(final List<ICollisionObject> objects)
	{
		double maxX = Geometry.getFieldLength() / 2 + Geometry.getBoundaryLength() - Geometry.getBallRadius();
		double maxY = Geometry.getFieldWidth() / 2 + Geometry.getBoundaryWidth() - Geometry.getBallRadius();

		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(maxX, maxY),
						Vector2f.fromXY(-maxX, maxY)),
				Vector2f.fromY(-1)));
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(maxX, -maxY),
						Vector2f.fromXY(-maxX, -maxY)),
				Vector2f.fromY(1)));

		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(maxX, maxY),
						Vector2f.fromXY(maxX, -maxY)),
				Vector2f.fromX(-1)));
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(-maxX, maxY),
						Vector2f.fromXY(-maxX, -maxY)),
				Vector2f.fromX(1)));
	}


	private void addTheirGoal(final List<ICollisionObject> objects)
	{
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Geometry.getGoalTheir().getLeftPost()
								.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth() - Geometry.getBallRadius(), 0)),
						Geometry.getGoalTheir().getRightPost()
								.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth() - Geometry.getBallRadius(), 0))),
				Vector2f.fromX(-1)));
		objects.add(new RectCollisionObject(
				Rectangle.aroundLine(
						Geometry.getGoalTheir().getLeftPost(),
						Geometry.getGoalTheir().getLeftPost()
								.addNew(Vector2f.fromX(Geometry.getGoalTheir().getDepth())),
						Geometry.getBallRadius())));
		objects.add(new RectCollisionObject(
				Rectangle.aroundLine(
						Geometry.getGoalTheir().getRightPost(),
						Geometry.getGoalTheir().getRightPost()
								.addNew(Vector2f.fromX(Geometry.getGoalTheir().getDepth())),
						Geometry.getBallRadius())));
	}


	private void addOurGoal(final List<ICollisionObject> objects)
	{
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Geometry.getGoalOur().getLeftPost()
								.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() + Geometry.getBallRadius(), 0)),
						Geometry.getGoalOur().getRightPost()
								.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() + Geometry.getBallRadius(), 0))),
				Vector2f.fromX(1)));
		objects.add(new RectCollisionObject(
				Rectangle.aroundLine(
						Geometry.getGoalOur().getLeftPost(),
						Geometry.getGoalOur().getLeftPost()
								.addNew(Vector2f.fromX(-Geometry.getGoalOur().getDepth())),
						Geometry.getBallRadius())));
		objects.add(new RectCollisionObject(
				Rectangle.aroundLine(
						Geometry.getGoalOur().getRightPost(),
						Geometry.getGoalOur().getRightPost()
								.addNew(Vector2f.fromX(-Geometry.getGoalOur().getDepth())),
						Geometry.getBallRadius())));
	}


	private void addBots(final Collection<? extends ISimBot> bots, final List<ICollisionObject> objects)
	{
		bots.stream()
				.map(info -> new BotCollisionObject(
						info.getState().getPose(),
						info.getState().getVel(),
						info.getCenter2DribblerDist(),
						info.getBotId(),
						info.getAction().getDribbleRpm() > 0,
						info.getAction().isDisarm() ? 0 : info.getAction().getKickSpeed(),
						info.getAction().isChip()))
				.forEach(objects::add);
	}


	private Optional<ICollision> getFirstCollision(final BallState preState, final double dt,
			final List<ICollisionObject> collisionObjects)
	{
		for (ICollisionObject object : collisionObjects)
		{
			IVector3 vel = preState.getVel().subtractNew(object.getVel());
			IVector3 postPos = preState.getPos().addNew(vel.multiplyNew(dt));
			Optional<ICollision> collision = object.getCollision(preState.getPos(), postPos);
			if (collision.isPresent())
			{
				return collision;
			}
		}
		return Optional.empty();
	}


	private Optional<ICollision> getFirstInsideCollision(final IVector3 postPos,
			final List<ICollisionObject> collisionObjects)
	{
		for (ICollisionObject object : collisionObjects)
		{
			Optional<ICollision> collision = object.getInsideCollision(postPos);
			if (collision.isPresent())
			{
				return collision;
			}
		}
		return Optional.empty();
	}


	private void impulse(final BallCollisionState state)
	{
		final Optional<ICollision> collision = state.getCollision();
		if (collision.isPresent())
		{
			IVector3 impulse = collision.get().getObject().getImpulse(state.getPos());
			state.setVel(state.getVel().addNew(impulse));
		}
	}


	private void reflect(final BallCollisionState colState)
	{
		ICollision collision = colState.getCollision().orElseThrow(IllegalStateException::new);
		IVector2 curVel = colState.getVel().getXYVector();
		IVector2 colNormal = collision.getNormal();

		double dampFactor = collision.getObject().getDampFactor();
		if (collision.getObject().isSticky())
		{
			dampFactor = 1;
		}
		IVector2 relativeVel = curVel.multiplyNew(1 - dampFactor);
		IVector2 outVel = ballCollision(relativeVel, colNormal);
		colState.setVel(Vector3.from2d(outVel, colState.getVel().z()));

		dampFactor = 0.4;
		relativeVel = colState.getSpin().multiplyNew(1 - dampFactor);
		outVel = ballCollision(relativeVel, colNormal);
		colState.setSpin(enableSpin ? outVel : Vector2f.ZERO_VECTOR);
	}


	private void moveOutside(final BallCollisionState colState)
	{
		ICollision collision = colState.getCollision().orElseThrow(IllegalStateException::new);
		IVector2 objectVel = collision.getObjectVel().getXYVector();
		IVector2 normal = collision.getNormal();
		boolean sticky = collision.getObject().isSticky();

		// move ball 1mm outside of obstacle
		IVector2 newPos = collision.getObject().stick(collision.getPos())
				.addNew(collision.getNormal().scaleToNew(sticky ? -1 : 1));
		colState.setPos(Vector3.from2d(newPos, colState.getPos().z()));

		// set ball vel to object vel (e.g. robot pushes ball)
		double newAbsVel = objectVel.getLength2();
		double colAngle = objectVel.angleToAbs(normal).orElse(0.0);
		double relVel = colAngle / AngleMath.PI_HALF;
		newAbsVel *= 1 - Math.min(1, relVel);
		colState.setVel(Vector3.from2d(normal.scaleToNew(newAbsVel),
				colState.getVel().z()));
		colState.setSpin(colState.getVel().getXYVector().multiplyNew(1.0 / Geometry.getBallRadius()));
	}


	private IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal)
	{
		if (ballVel.isZeroVector())
		{
			return ballVel;
		}
		double angleNormalColl = ballVel.angleToAbs(collisionNormal).orElse(0.0);
		if (angleNormalColl < AngleMath.PI_HALF)
		{
			return ballVel;
		}
		double velInfAngle = ballVel.getAngle();
		if (angleNormalColl > AngleMath.PI_HALF)
		{
			velInfAngle = AngleMath.normalizeAngle(ballVel.getAngle() + AngleMath.PI);
		}
		double velAngleDiff = AngleMath.difference(collisionNormal.getAngle(), velInfAngle);
		return collisionNormal.turnNew(velAngleDiff).scaleTo(ballVel.getLength2());
	}
}
