/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.ball.collision.BotCollision;
import edu.tigers.sumatra.wp.ball.collision.BotDribbleImpulse;
import edu.tigers.sumatra.wp.ball.collision.BotKickImpuls;
import edu.tigers.sumatra.wp.ball.collision.CollisionHandler;
import edu.tigers.sumatra.wp.ball.collision.ICollisionState;
import edu.tigers.sumatra.wp.ball.collision.LineCollision;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionModel implements IBallCollisionModel
{
	private boolean complexCollision = false;
	
	
	/**
	 * 
	 */
	public BallCollisionModel()
	{
	}
	
	
	/**
	 * @param complexCollision
	 */
	public BallCollisionModel(final boolean complexCollision)
	{
		this.complexCollision = complexCollision;
	}
	
	
	/**
	 * @param state
	 * @param newState
	 * @param dt
	 * @param context
	 * @return
	 */
	@Override
	public ICollisionState processCollision(final IState state, final IState newState, final double dt,
			final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addCollisionObjects(ch, context);
		
		ICollisionState nextState = ch.process(state, newState);
		
		return nextState;
	}
	
	
	protected void addCollisionObjects(final CollisionHandler ch, final MotionContext context)
	{
		ILine goalLine1 = Line.newLine(
				Geometry.getGoalOur().getGoalPostLeft()
						.addNew(new Vector2(-Geometry.getGoalDepth() + Geometry.getBallRadius(), 0)),
				Geometry.getGoalOur().getGoalPostRight()
						.addNew(new Vector2(-Geometry.getGoalDepth() + Geometry.getBallRadius(), 0)));
		ch.addObject(new LineCollision(goalLine1, AVector2.ZERO_VECTOR, AVector2.X_AXIS));
		ILine goalLine2 = Line.newLine(
				Geometry.getGoalTheir().getGoalPostLeft()
						.addNew(new Vector2(Geometry.getGoalDepth() - Geometry.getBallRadius(), 0)),
				Geometry.getGoalTheir().getGoalPostRight()
						.addNew(new Vector2(Geometry.getGoalDepth() - Geometry.getBallRadius(), 0)));
		ch.addObject(new LineCollision(goalLine2, AVector2.ZERO_VECTOR, AVector2.X_AXIS.multiplyNew(-1)));
		
		context.getBots().values().stream()
				.map(info -> new BotCollision(info.getPos(), info.getVel(), info.getCenter2DribblerDist()))
				.forEach(bc -> ch.addObject(bc));
	}
	
	
	@Override
	public IVector3 getImpulse(final ICollisionState state, final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addImpulseObjects(ch, context);
		
		return ch.getImpulse(state);
	}
	
	
	@Override
	public IVector3 getTorqueAcc(final IState state, final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addTorqueObjects(ch, context);
		
		return ch.getTorque(state);
	}
	
	
	protected void addImpulseObjects(final CollisionHandler ch, final MotionContext context)
	{
		for (BotInfo info : context.getBots().values())
		{
			if (info.getKickSpeed() > 0)
			{
				Vector3 kickVel = new Vector3();
				kickVel.set(new Vector2(info.getPos().z()).scaleTo(info.getKickSpeed()), 0);
				if (info.isChip())
				{
					kickVel.set(2, info.getKickSpeed());
				}
				ch.addImpulseObject(new BotKickImpuls(info.getPos(), info.getCenter2DribblerDist(), kickVel));
			}
		}
	}
	
	
	protected void addTorqueObjects(final CollisionHandler ch, final MotionContext context)
	{
		for (BotInfo info : context.getBots().values())
		{
			if (info.getDribbleRpm() > 0)
			{
				ch.addImpulseObject(
						new BotDribbleImpulse(info.getPos(), info.getCenter2DribblerDist()));
			}
		}
	}
}
