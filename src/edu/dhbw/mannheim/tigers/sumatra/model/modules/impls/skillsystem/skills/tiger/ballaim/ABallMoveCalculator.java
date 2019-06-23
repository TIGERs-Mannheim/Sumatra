/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballaim;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove.EBallMoveState;
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

	
	private float									targetOrientation	= 0.0f;
	
	protected WorldFrame							wFrame;
	/** The position of the ball, changed in {@link #update(WorldFrame, int)} */
	protected IVector2							ballPos;
	protected final EBallMoveState			state;
	protected IVector2							target;

	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * @param pidConfig
	 */
	public ABallMoveCalculator(PIDControllerConfig pidConfig, EBallMoveState state)
	{
		this.pidRotateSpeed = new PIDControllerRotateZ2(pidConfig);
		this.state = state;
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
	 * @param target
	 */
	public void update(WorldFrame wFrame, int botId, IVector2 target)
	{
		this.wFrame = wFrame;
		this.botId = botId;
		this.target = new Vector2(target);
		
		// If ball not visible, assume it is where it should be... O.o
		if (!wFrame.ball.isOnCam())
		{
			// Calculate the ideal ballPos
			Vector2 dir = new Vector2(1, 1);
			dir.turnTo(getBot().angle);
			dir.scaleTo(95);	// < startAimBallDistanceTolerance
			ballPos = dir.add(getBot().pos);
		} else
		{
			ballPos = wFrame.ball.pos;
		}
	}
	

	/**
	 * Calculates move vector.
	 * 
	 * @param deltaT
	 * @return move vector
	 */
	public abstract IVector2 calcMoveVector(double deltaT);
	

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
		if (Math.abs(pidRotateSpeed.getPreviousError()) < GetBallAndAimSkill.START_GET_BALL_ANGLE_TOLERANCE
				&& Math.abs(getBot().aVel) < AIConfig.getSkills().getRotationSpeedThreshold())
		{
			currentTurnSpeed = 0;
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
	

	/**
	 * @return bot
	 */
	protected TrackedTigerBot getBot()
	{
		return wFrame.tigerBots.get(botId);
	}

	
	public IVector2 getBallPos()
	{
		return ballPos;
	}
	

	/**
	 * TODO Malte, add comment!
	 * 
	 */
	public void calcTargetOrientation()
	{
		targetOrientation = doCalcTargetOrientation();
	}
	
	
	protected abstract float doCalcTargetOrientation();
}
