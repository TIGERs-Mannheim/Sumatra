/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerRotateZ2;


/**
 * Super class for calculating turn speed and move vector for movement with ball.
 * 
 * <b> Perform {@link #update(WorldFrame, int)} before calculating speed and vector ! </b>
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class ABallMoveCalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected int									botId;
	
	private final float							MAX_VELOCITY		= 1.0f;
	private final float							MAX_BRAKING_DIST	= 0.8f;
	
	protected final PIDControllerRotateZ2	pidRotateSpeed;
	
	protected boolean								moveCompleted		= false;
	protected boolean								rotateCompleted	= false;
	

	protected float								distanceTolerance	= 150;
	protected float								angleTolerance		= AIMath.deg2rad(30);
	
	protected float								targetOrientation	= 0.0f;
	
	protected WorldFrame							wFrame;
	/** The position of the ball, changed in {@link #update(WorldFrame, int)} */
	protected IVector2							ballPos;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * @param pidConfig
	 */
	public ABallMoveCalculator(PIDControllerConfig pidConfig)
	{
		this.pidRotateSpeed = new PIDControllerRotateZ2(pidConfig);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * Perform an update of this which is necessary before calculating
	 * turn speed and move vector! Also update {@link #ballPos}, which may be different to {@link #wFrame#ballPos}!!!
	 * 
	 * @param wFrame
	 * @param botId
	 */
	public void update(WorldFrame wFrame, int botId)
	{
		this.wFrame = wFrame;
		this.botId = botId;
		
//		 // If ball not visible, assume it is where it should be... O.o
//		 if (wFrame.ball.isOnCam())
//		 System.out.println("#####");
//		 if (!wFrame.ball.isOnCam())
//		 {
//		 // Calculate the ideal ballPos
//		 Vector2 dir = new Vector2(1, 1);
//		 dir.turnTo(getBot().angle);
//		 dir.scaleTo(BallMoveV2.GET_BALL_DISTANCE + (BallMoveV2.MOVE_BALL_DISTANCE - BallMoveV2.GET_BALL_DISTANCE) / 2);
//		 ballPos = dir.add(getBot().pos);
//		 // System.out.println(ballPos);
//		 } else
//		 {
		 ballPos = wFrame.ball.pos;
		 
		// else
		// {
		// // Calculate the ideal ballPos
		// Vector2 dir = new Vector2(1, 1);
		// dir.turnTo(getBot().angle);
		// dir.scaleTo(BallMoveV2.GET_BALL_DISTANCE + (BallMoveV2.MOVE_BALL_DISTANCE - BallMoveV2.GET_BALL_DISTANCE) / 2);
		// ballPos = dir.add(getBot().pos);
		// System.out.println("Faked the ball! =)");
		// }
	}
	

	/**
	 * Calculates move vector.
	 * 
	 * @param deltaT
	 * @return move vector
	 */
	public abstract IVector2 calcMoveVector(double deltaT);
	

	/**
	 * Checks if the actual state is still correct and returns the needed
	 * state for the situation. Skill class must handle state changing.
	 * 
	 * @return actual needed state
	 */
	public EBallMoveState checkState()
	{
		// System.out.println(wFrame.ball.pos);
		float distanceBotBall = wFrame.ball.pos.subtractNew(getBot().pos).getLength2();
		 System.out.println("distance BB: " + distanceBotBall);
		
		float angleBallBot = AIMath.normalizeAngle(getBot().angle
				- AIMath.angleBetweenXAxisAndLine(getBot().pos, wFrame.ball.pos));
		
		if (distanceBotBall > distanceTolerance || angleBallBot > angleTolerance)
		{
			return EBallMoveState.GET_BALL;
		} else
		{
			return doCheckState();
		}
	}
	

	/**
	 * See {@link ABallMoveCalculator}{@link #checkState()}
	 * 
	 * @return EBallMoveState , skill state
	 */
	protected abstract EBallMoveState doCheckState();
	

	/**
	 * Calculates turn speed.
	 * 
	 * @param wFrame
	 * @return turn speed.
	 */
	public float calcTurnSpeed()
	{
		float currentTurnSpeed = 0;
		
		// Get turn-speed from PID-controller
		currentTurnSpeed = pidRotateSpeed.process(getBot().angle, targetOrientation, 0);
		
		// complete when angle error and turn speed are small enough
		if (Math.abs(pidRotateSpeed.getPreviousError()) < angleTolerance
				&& Math.abs(getBot().aVel) < AIConfig.getSkills().getRotationSpeedThreshold())
		{
			currentTurnSpeed = 0;
			rotateCompleted = true;
		}
		
		return currentTurnSpeed;
	}
	

	/**
	 * calculates the appropriate velocity dependent on the actual velocity
	 * with a linear acceleration
	 * 
	 * @param distance to next pathpoint
	 * @param curvature the paths curvature; 0 if a straight line
	 * @param deltaT
	 * @return
	 */
	protected float calcVelocity(float distance, float curvature, double deltaT)
	{
		float currentVelocity = getBot().vel.getLength2();
		float disiredVelocity;
		float slope = 1.0f; // old value 8
		
		if (distance <= MAX_BRAKING_DIST)
		{
			disiredVelocity = MAX_VELOCITY * distance / MAX_BRAKING_DIST;
		} else
		{
			disiredVelocity = MAX_VELOCITY - Math.abs(curvature * 20);// TODO DanielW scaling, prevent negative; into
																							// config
		}
		
		float t0 = currentVelocity / slope;
		float t = (float) (t0 + deltaT * 5);
		float nextVelocity = t * slope;
		
		if (nextVelocity > disiredVelocity)
		{
			nextVelocity = disiredVelocity;
		}
		return nextVelocity;
	}
	

	/**
	 * Updates the target orientation.
	 * 
	 * @param angle
	 */
	public void updateTargetOrientation(float angle)
	{
		this.targetOrientation = angle;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public boolean isCompleted()
	{
		return moveCompleted && rotateCompleted;
	}
	

	/**
	 * @return bot
	 */
	protected TrackedTigerBot getBot()
	{
		return wFrame.tigerBots.get(botId);
	}
	
}
