/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.ball.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CollisionHandler
{
	private static final double				BALL_DAMP_KICKER_FACTOR	= 0.66;
	
	private final List<ICollisionObject>	collisionObjects			= new ArrayList<>();
	private final List<IImpulseObject>		impulseObjects				= new ArrayList<>();
	
	private boolean								complexCollision			= false;
	
	
	/**
	 * 
	 */
	public CollisionHandler()
	{
	}
	
	
	/**
	 * @param complexCollision
	 */
	public CollisionHandler(final boolean complexCollision)
	{
		this.complexCollision = complexCollision;
	}
	
	
	/**
	 * @param object
	 */
	public void addObject(final ICollisionObject object)
	{
		collisionObjects.add(object);
	}
	
	
	/**
	 * @param object
	 */
	public void addImpulseObject(final IImpulseObject object)
	{
		impulseObjects.add(object);
	}
	
	
	/**
	 * @param prePos
	 * @param postPos
	 * @return
	 */
	public Optional<ICollision> getFirstCollision(final IVector3 prePos, final IVector3 postPos)
	{
		for (ICollisionObject object : collisionObjects)
		{
			Optional<ICollision> collision = object.getCollision(prePos, postPos);
			if (collision.isPresent())
			{
				return collision;
			}
		}
		return Optional.empty();
	}
	
	
	private IVector3 getCollisionPos(final ICollisionState state)
	{
		IVector3 ballPos;
		if (state.getCollision().isPresent())
		{
			ballPos = new Vector3(state.getCollision().get().getPos(), state.getPos().z());
		} else
		{
			ballPos = state.getPos();
		}
		return ballPos;
	}
	
	
	/**
	 * @param state
	 * @return
	 */
	public IVector3 getImpulse(final ICollisionState state)
	{
		IVector3 ballPos = getCollisionPos(state);
		Vector3 impulse = new Vector3();
		for (IImpulseObject obj : impulseObjects)
		{
			impulse.add(obj.getImpulse(ballPos));
		}
		return impulse;
	}
	
	
	/**
	 * @param state
	 * @return
	 */
	public IVector3 getTorque(final IState state)
	{
		Vector3 impulse = new Vector3();
		for (IImpulseObject obj : impulseObjects)
		{
			impulse.add(obj.getTorqueAcc(state));
		}
		return impulse;
	}
	
	
	/**
	 * @param preState
	 * @param postState
	 * @return
	 */
	public ICollisionState process(final IState preState, final IState postState)
	{
		BallCollisionState state = new BallCollisionState(postState);
		Optional<ICollision> collision = getFirstCollision(preState.getPos(), postState.getPos());
		if (collision.isPresent())
		{
			state.setCollision(collision);
			state = reflect(state);
		}
		
		return state;
	}
	
	
	private BallCollisionState reflect(final ICollisionState colState)
	{
		BallCollisionState newState = new BallCollisionState(colState);
		
		IVector2 curVel = colState.getVel().getXYVector();
		IVector2 objectVel = colState.getCollision().get().getObjectVel();
		IVector2 colNormal = colState.getCollision().get().getNormal();
		
		IVector2 outVel = ballCollision(curVel, colNormal);
		
		if (complexCollision)
		{
			double hitBallAngle = AngleMath.PI;
			if (!curVel.isZeroVector())
			{
				hitBallAngle = GeoMath.angleBetweenVectorAndVector(curVel, objectVel);
			}
			double hitAngle = GeoMath.angleBetweenVectorAndVector(colNormal, objectVel);
			double relAngle = Math.max(0, (-hitAngle / AngleMath.PI_HALF) + 1);
			double hitVelAbs = Math.max(0, objectVel.getLength() - curVel.getLength()) * relAngle;
			IVector2 hitVel = colNormal.scaleToNew(hitVelAbs);
			if ((hitBallAngle > AngleMath.PI_HALF))
			{
				// bot hits ball
				outVel = outVel.addNew(hitVel);
			}
			
			if ((hitBallAngle < AngleMath.PI_HALF) && (outVel.getLength() < hitVel.getLength()))
			{
				// bot touches/pushes ball, scale towards hitvel
				outVel = outVel.addNew(hitVel).scaleTo(hitVel.getLength());
			}
		}
		
		if (outVel.isZeroVector())
		{
			// move ball slowly outside
			// do NOT change the position directly! this causes a jump which results in high velocities!
			IVector2 colPos = colState.getCollision().get().getPos();
			IVector2 curPos = colState.getPos().getXYVector();
			IVector2 corDir = colPos.subtractNew(curPos);
			IVector2 corVel = corDir.scaleToNew((0.2 * Math.min(50, corDir.getLength())) / 50);
			outVel = outVel.addNew(corVel);
		}
		
		newState.setVel(new Vector3(outVel, newState.getVel().z()));
		newState.setAcc(AVector3.ZERO_VECTOR);
		
		if (complexCollision)
		{
			double distOverObs = GeoMath.distancePP(colState.getPos().getXYVector(),
					colState.getCollision().get().getPos());
			IVector2 corPos = colState.getCollision().get().getPos().addNew(outVel.scaleToNew(distOverObs));
			newState.setPos(new Vector3(corPos, colState.getPos().z()));
		}
		
		return newState;
	}
	
	
	private IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal)
	{
		if (ballVel.isZeroVector())
		{
			return ballVel;
		}
		double angleNormalColl = GeoMath.angleBetweenVectorAndVector(ballVel, collisionNormal);
		if (angleNormalColl < AngleMath.PI_HALF)
		{
			return ballVel;
		}
		double relDamp = Math.max(0, (angleNormalColl / AngleMath.PI_HALF) - 1);
		double damp = relDamp * BALL_DAMP_KICKER_FACTOR;
		double velInfAngle = ballVel.getAngle();
		if (angleNormalColl > AngleMath.PI_HALF)
		{
			velInfAngle = AngleMath.normalizeAngle(ballVel.getAngle() + AngleMath.PI);
		}
		double velAngleDiff = AngleMath.getShortestRotation(velInfAngle, collisionNormal.getAngle());
		IVector2 outVel = new Vector2(collisionNormal).turn(velAngleDiff).scaleTo(
				ballVel.getLength2() * (1 - damp));
		return outVel;
	}
}
