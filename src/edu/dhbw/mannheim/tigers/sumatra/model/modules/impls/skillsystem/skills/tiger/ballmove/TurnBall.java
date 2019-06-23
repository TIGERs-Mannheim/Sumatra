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
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;


/**
 * Calculates turn speed and move vector for turning with ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class TurnBall extends ABallMoveCalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private float				aimVelocity	= 0.0f;
	
	/** radius for turn around ball */
	private final float		radius;
	
	/** [rad] */
	private float				angleDiff;
	private final IVector2	target;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target The target the bot should look at
	 */
	public TurnBall(IVector2 target)
	{
		super(new PIDControllerConfig(0.05f, 0, 0, 0.25f));
		
		this.radius = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() - 20;
		this.target = target;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(WorldFrame wFrame, int botId)
	{
		super.update(wFrame, botId);
		
		float currentAngle = getBot().angle; // aballPos.subtractNew(botPos).getAngle();
		float targetAngle = target.subtractNew(getBot().pos).getAngle();
		

		aimVelocity = pidRotateSpeed.process(currentAngle, targetAngle, 0);
		// System.out.println("AimVel   : " + aimVelocity);
		angleDiff = Math.abs(currentAngle - targetAngle);
		// System.out.println("AngleDiff: " + AIMath.deg(angleDiff));
		// System.out.println("CurA: " + AIMath.deg(currentAngle));
		// System.out.println("TarA: " + AIMath.deg(targetAngle));
		// System.out.println("Targ: " + target);
	}
	

	@Override
	protected EBallMoveState doCheckState()
	{
		 float currentAngle = getBot().pos.getAngle();
		 float targetAngle = target.getAngle();
		 float diffAngle = Math.abs(currentAngle - targetAngle);
		
		 System.out.println("diffAngle " + AIMath.rad2deg(diffAngle));
		
		if (AIMath.isZero(angleDiff, angleTolerance))
		{
			return EBallMoveState.MOVE;
		} else
		{
			return EBallMoveState.TURN;
		}
	}
	

	@Override
	public IVector2 calcMoveVector(double deltaT)
	{
		if (AIMath.isZero(angleDiff, AIConfig.getTolerances().getAiming())
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			moveCompleted = true;
			return Vector2.ZERO_VECTOR;
			
		} else
		{
			return new Vector2(); // aimVelocity, 0);
		}
		
	}
	

	@Override
	public float calcTurnSpeed()
	{
		if (AIMath.isZero(angleDiff, AIConfig.getTolerances().getAiming())
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			rotateCompleted = true;
			return 0;
		} else
		{
			return aimVelocity / (radius / 1000);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

}
