/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
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
import java.util.stream.Stream;


/**
 * Process the collision of the ball with other objects like bots, goals and field border
 */
public class BallCollisionHandler
{
	private static final double GOAL_BORDER_WIDTH_MM = 20;
	@Configurable(defValue = "false", comment = "Set spin after reflecting the ball on a collision")
	private static boolean reflectSpin = false;
	@Configurable(defValue = "0", comment = "[RPM] spin speed applied if sth is sticky to the ball")
	private static double dribblingSpeed = 0.0;


	public ICollisionState process(final BallState preState, final double dt, final Collection<? extends ISimBot> bots)
	{
		var collisionObjects = generateCollisionObjects(bots);
		var state = new BallCollisionState(preState);
		var isBelowRobotHeight = preState.getPos().z() <= Geometry.getBallParameters().getMaxInterceptableHeight();

		Optional<ICollision> collision = getFirstCollision(preState, dt, collisionObjects);
		if (collision.isPresent() && (isBelowRobotHeight || collision.get().getObject().isFieldBoundary()))
		{
			state.setCollision(collision.get());
			reflect(state);
		}

		// Note on a special case:
		// If the ball touches the boundary and the robot touches the ball, there is a conflict.
		// Currently, the robots are not stopped/blocked by a ball lying next to the boundary wall.
		// If the ball is not moved, the robot will move inside the ball. This breaks the collision detection.
		// So we check for an inside collision here, after normal reflection was handled.
		// The ball is pushed inside the boundary, but can therefore be handled correctly on the robot's dribbler.
		Optional<ICollision> insideCollision = getFirstInsideCollision(preState.getPos(), collisionObjects);
		if (insideCollision.isPresent() && (isBelowRobotHeight || insideCollision.get().getObject().isFieldBoundary()))
		{
			state.setCollision(insideCollision.get());
			moveOutside(state);
			reflect(state);
		}

		impulse(state);

		return state;
	}


	private List<ICollisionObject> generateCollisionObjects(final Collection<? extends ISimBot> bots)
	{
		List<ICollisionObject> objects = new ArrayList<>();

		addBots(bots, objects);

		objects.addAll(objectsFromGoal(Geometry.getGoalOur()));
		objects.addAll(objectsFromGoal(Geometry.getGoalTheir()));

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
				Vector2f.fromY(-1),
				true));
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(maxX, -maxY),
						Vector2f.fromXY(-maxX, -maxY)),
				Vector2f.fromY(1),
				true));

		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(maxX, maxY),
						Vector2f.fromXY(maxX, -maxY)),
				Vector2f.fromX(-1),
				true));
		objects.add(new LineSegmentCollisionObject(
				Lines.segmentFromPoints(
						Vector2f.fromXY(-maxX, maxY),
						Vector2f.fromXY(-maxX, -maxY)),
				Vector2f.fromX(1),
				true));
	}


	private List<ICollisionObject> objectsFromGoal(Goal goal)
	{
		var sign = Math.signum(goal.getCenter().x());
		var x = Math.abs(goal.getCenter().x());
		var width = goal.getWidth();

		var positiveFront = Vector2.fromXY(x + Geometry.getLineWidth() / 2 + GOAL_BORDER_WIDTH_MM / 2,
				width / 2 + GOAL_BORDER_WIDTH_MM / 2).multiply(sign);
		var negativeFront = Vector2.fromXY(x + Geometry.getLineWidth() / 2 + GOAL_BORDER_WIDTH_MM / 2,
				-width / 2 - GOAL_BORDER_WIDTH_MM / 2).multiply(sign);

		var backDistance = SumatraMath.max(
				Geometry.getBoundaryWidth() - GOAL_BORDER_WIDTH_MM - Geometry.getLineWidth() / 2,
				goal.getDepth()
		);

		var backOffset = Vector2.fromX(backDistance).multiply(sign);
		var middleOffset = Vector2.fromX(goal.getDepth()).multiply(sign);

		var positiveBack = positiveFront.addNew(backOffset);
		var negativeBack = negativeFront.addNew(backOffset);

		var positiveMiddle = positiveFront.addNew(middleOffset);
		var negativeMiddle = negativeFront.addNew(middleOffset);

		return Stream.of(
						Lines.segmentFromPoints(positiveFront, positiveBack),
						Lines.segmentFromPoints(negativeFront, negativeBack),
						Lines.segmentFromPoints(positiveMiddle, negativeMiddle)
				)
				.map(segment -> Rectangle.aroundLine(segment, GOAL_BORDER_WIDTH_MM / 2))
				.map(rectangle -> new RectCollisionObject(rectangle, true))
				.map(ICollisionObject.class::cast)
				.toList();
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
			IVector3 impulse = collision.get().getObject().getImpulse();
			state.setVel(state.getVel().addNew(impulse));
		}
	}


	private void reflect(final BallCollisionState colState)
	{
		var collision = colState.getCollision().orElseThrow(IllegalStateException::new);

		double dampFactor = collision.getObject().getDampFactor();
		double dampFactorOrtho = collision.getObject().getDampFactorOrthogonal();
		if (collision.getObject().isSticky())
		{
			dampFactor = 1;
			dampFactorOrtho = 1;
		}

		if (dampFactorOrtho >= 1)
		{
			colState.setPos(Vector3.from2d(colState.getPos().getXYVector(), 0));
		}

		var ballVel = ballCollision(collision, colState.getVel(), dampFactor, dampFactorOrtho);
		colState.setVel(ballVel);

		var ballSpin = ballCollision(collision, Vector3.from2d(colState.getSpin(), 0), 0.4, 0.4);
		colState.setSpin(reflectSpin ? ballSpin.getXYVector() : Vector2f.ZERO_VECTOR);
	}


	private void moveOutside(final BallCollisionState colState)
	{
		var collision = colState.getCollision().orElseThrow(IllegalStateException::new);
		var normal = collision.getNormal();
		var sticky = collision.getObject().isSticky();

		if (sticky)
		{
			// move ball 1mm inside of obstacle
			IVector2 newPos = collision.getObject().stick(collision.getPos())
					.addNew(normal.scaleToNew(-1));
			colState.setPos(Vector3.from2d(newPos, colState.getPos().z()));
			colState.setVel(Vector3.from2d(collision.getObjectSurfaceVel(), 0));
			colState.setSpin(collision.getNormal().scaleToNew(-dribblingSpeed * AngleMath.PI_TWO / 60.0)); // RPM to rad/s
		} else
		{
			// move ball 1mm outside of obstacle
			IVector2 newPos = collision.getObject().stick(collision.getPos())
					.addNew(normal.scaleToNew(1));
			colState.setPos(Vector3.from2d(newPos, colState.getPos().z()));
			// Spin is set to be simply rolling
			colState.setSpin(colState.getVel().getXYVector().multiplyNew(1.0 / Geometry.getBallRadius()));
		}
	}


	private IVector3 ballCollision(ICollision collision, IVector3 ballVel, double dampFactor, double dampFactorOrtho)
	{
		var ballVelFlat = ballVel.getXYVector();
		var objVel = collision.getObjectSurfaceVel();
		var normal = collision.getNormal().normalizeNew();

		// Flat collision, in direction of the collision Normal
		var objVelProjected = normal.scalarProduct(objVel);
		var ballVelProjected = normal.scalarProduct(ballVelFlat);

		if (objVelProjected <= ballVelProjected)
		{
			// Untouched
			return ballVel;
		}
		// Assuming m_object >> m_ball
		// With dampFactor = 1 -> perfect inelastic collision
		//      dampFactor = 0 -> perfect elastic collision
		var finalVelProjected = objVelProjected + (objVelProjected - ballVelProjected) * (1 - dampFactor);
		var finalVel = normal.multiplyNew(finalVelProjected);

		// Flat collision, orthogonal to the collision normal
		var normalOrtho = normal.getNormalVector();
		var surfaceVelOrtho = normalOrtho.multiplyNew(dampFactorOrtho * normalOrtho.scalarProduct(objVel));
		var ballVelOrtho = normalOrtho.multiplyNew((1 - dampFactorOrtho) * normalOrtho.scalarProduct(ballVelFlat));
		var finalVelOrtho = surfaceVelOrtho.add(ballVelOrtho);

		// Collision in z direction
		var finalVelZ = (1 - dampFactorOrtho) * ballVel.z();

		return Vector3.from2d(finalVel.add(finalVelOrtho), finalVelZ);
	}
}
