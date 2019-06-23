/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.dynamics.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CollisionHandler
{
	private final List<ICollisionObject> collisionObjects = new ArrayList<>();
	
	
	/**
	 * @param object
	 */
	public void addObject(final ICollisionObject object)
	{
		collisionObjects.add(object);
	}
	
	
	private Optional<ICollision> getFirstCollision(final IState preState, final double dt)
	{
		for (ICollisionObject object : collisionObjects)
		{
			IVector3 vel = preState.getVel().subtractNew(object.getVel());
			IVector3 postPos = preState.getPos().addNew(vel.multiplyNew(1000 * dt));
			Optional<ICollision> collision = object.getCollision(preState.getPos(), postPos);
			if (collision.isPresent())
			{
				return collision;
			}
		}
		return Optional.empty();
	}
	
	
	private Optional<ICollision> getFirstInsideCollision(final IVector3 postPos)
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
	
	
	/**
	 * @param preState
	 * @param dt
	 * @return
	 */
	public ICollisionState process(final IState preState, final double dt)
	{
		BallCollisionState state = new BallCollisionState(preState);
		if (preState.getPos().z() > Geometry.getBallParameters().getMaxInterceptableHeight())
		{
			return state;
		}
		Optional<ICollision> collision = getFirstCollision(preState, dt);
		if (collision.isPresent())
		{
			state.setCollision(collision.orElse(null));
			reflect(state);
		} else
		{
			Optional<ICollision> insideCollision = getFirstInsideCollision(preState.getPos());
			if (insideCollision.isPresent())
			{
				state.setCollision(insideCollision.orElse(null));
				moveOutside(state);
			}
		}
		impulse(state);
		
		return state;
	}
	
	
	private void impulse(final BallCollisionState state)
	{
		if (state.getCollision().isPresent())
		{
			IVector3 impulse = state.getCollision().get().getObject().getImpulse(state.getPos());
			state.setVel(state.getVel().addNew(impulse));
		}
	}
	
	
	private void reflect(final BallCollisionState colState)
	{
		ICollision collision = colState.getCollision().orElseThrow(IllegalStateException::new);
		IVector2 curVel = colState.getVel().getXYVector();
		IVector3 objectVel = collision.getObjectVel();
		IVector2 colNormal = collision.getNormal();
		IVector2 relativeVel = curVel.subtractNew(objectVel.getXYVector());
		
		double dampFactor = collision.getObject().getDampFactor();
		if (collision.getObject().isSticky())
		{
			dampFactor = 1;
		}
		IVector2 outVel = ballCollision(relativeVel, colNormal, dampFactor);
		colState.setVel(Vector3.from2d(outVel, colState.getVel().z()));
	}
	
	
	private void moveOutside(final BallCollisionState colState)
	{
		ICollision collision = colState.getCollision().orElseThrow(IllegalStateException::new);
		IVector2 objectVel = collision.getObjectVel().getXYVector();
		IVector2 normal = collision.getNormal();
		boolean sticky = collision.getObject().isSticky();
		
		// move ball 1mm outside of obstacle
		IVector2 newPos = collision.getPos()
				.addNew(collision.getNormal().scaleToNew(sticky ? -1 : 1));
		colState.setPos(Vector3.from2d(newPos, colState.getPos().z()));
		
		// set ball vel to object vel (e.g. robot pushes ball)
		double newAbsVel = objectVel.getLength2();
		double colAngle = objectVel.angleToAbs(normal).orElse(0.0);
		double relVel = colAngle / AngleMath.PI_HALF;
		newAbsVel *= 1 - Math.min(1, relVel);
		colState.setVel(Vector3.from2d(normal.scaleToNew(newAbsVel),
				colState.getVel().z()));
	}
	
	
	private IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal, final double dampFactor)
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
		return collisionNormal.turnNew(velAngleDiff).scaleTo(
				ballVel.getLength2() * (1 - dampFactor));
	}
}
