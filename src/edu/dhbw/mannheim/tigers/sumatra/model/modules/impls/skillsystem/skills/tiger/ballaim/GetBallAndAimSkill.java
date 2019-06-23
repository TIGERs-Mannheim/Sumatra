/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballaim;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove.EBallMoveState;


/**
 * Gets ball and aims at target. When ball get lost this skill gets it back!
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class GetBallAndAimSkill extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private EBallMoveState			currentState								= EBallMoveState.GET_BALL;
	private ABallMoveCalculator	moveCalculator;
	
	// ---------------- tolerances ----------------------------------------------
	
	/** Wenn der Wert überschritten ist, wird GetBall ausgewählt. */
	protected static final float	START_GET_BALL_ANGLE_TOLERANCE		= AIMath.deg2rad(30);
	/** Wenn der Wert überschritten ist, wird GetBall ausgewählt. */
	protected final float			START_GET_BALL_DISTANCE_TOLERANCE	= 120;
	/** Wenn der Wert unterschritten ist, wird AimBall ausgewählt. */
	protected final float			START_AIM_ANGLE_TOLERANCE				= AIMath.deg2rad(15);									// 25);
	/** Wenn der Wert unterschritten ist, wird AimBall ausgewählt. */
	protected final float			START_AIM_BALL_DISTANCE_TOLERANCE	= AIConfig.getTolerances().getNextToBall()
																									+ AIConfig.getGeometry().getBallRadius()
																									+ AIConfig.getGeometry().getBotRadius();	// 100;
	/** Wenn der Wert unterschritten ist, ist AimBall completed!. */
	protected final float			finishAimAngleTolerance;
	

	// ---------------- timing --------------------------------------------------
	private static final long		NEVER_PROCESSED							= -1;
	/** Stores the last {@link System#nanoTime()} {@link #calcActions()} was called, used for timing */
	private long						lastProcessed								= NEVER_PROCESSED;
	
	// --------------------------------------------------------------------------
	private final IVector2			target;
	
	
	/**
	 * @param target
	 * @param aimTolerance
	 */
	public GetBallAndAimSkill(IVector2 target, float aimTolerance)
	{
		super(ESkillName.GET_BALL_AND_AIM, ESkillGroup.MOVE);
		
		this.target = target;
		this.finishAimAngleTolerance = aimTolerance;
		this.moveCalculator = new GetBallCalculator();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// Safety check
		TrackedTigerBot bot = getBot();
		if (bot == null)
		{
			return cmds;
		}
		
		// ### Step 0: Timing
		final long now = System.nanoTime();
		final double deltaT; // [s]
		if (lastProcessed == NEVER_PROCESSED)
		{
			// Okay, first time this thing got ever called. Take the skills 'actual period'.
			// Not very precise, but better then nothing!
			deltaT = this.getPeriod() / 1e9f;
		} else
		{
			// Now we can be exact
			deltaT = (now - lastProcessed) / 1e9f;
		}
		lastProcessed = now;
		

		// ### Update state: Has been completed?
		 System.out.println("##########################");
		 System.out.println("conf: " + START_AIM_BALL_DISTANCE_TOLERANCE);
		 System.out.println("State: " + currentState);
		// Update
		moveCalculator.update(getWorldFrame(), getBot().id, target);
		changeSkillState(checkState(moveCalculator.getBallPos()));
		
		// Completed?
		if (currentState == EBallMoveState.FINISHED)
		{
			cmds.add(new TigerMotorMoveV2(new Vector2f(0, 0), 0, 0));
			complete();
			return cmds; // Jump off
		}
		
		// ### Not complete: Move and turn
		// Step 1: Calculate move vector
		final IVector2 desiredMovement;
		desiredMovement = moveCalculator.calcMoveVector(deltaT);
		
		// Step 2: Calculate orientation
		moveCalculator.calcTargetOrientation();
		
		// Step 3: Calculate rotation
		final float turnspeed = moveCalculator.calcTurnSpeed();
		
		// Step 4: Merge
		mergeMovement(desiredMovement, turnspeed, cmds);
		

		return cmds;
	}
	

	// --------------------------------------------------------------------------
	// --- state handling -------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * Changes skill state.
	 * 
	 * @param state
	 */
	private void changeSkillState(EBallMoveState state)
	{
		if (currentState != state)
		{
			switch (state)
			{
				case GET_BALL:
					moveCalculator = new GetBallCalculator();
					break;
				
				case AIM:
					moveCalculator = new AimBallCalculator();
					break;
				
			}
			currentState = state;
			moveCalculator.update(getWorldFrame(), getBot().id, target);
		}
	}
	

	/**
	 * Checks if the actual state is still correct and returns the needed
	 * state for the situation. Skill class must handle state changing.
	 * 
	 * @return actual needed state
	 */
	public EBallMoveState checkState(IVector2 ballPos)
	{
		float distanceBotBall = Math.abs(ballPos.subtractNew(getBot().pos).getLength2());
		System.out.println("dist Ball-Bot  : " + distanceBotBall);
		float targetAngle = target.subtractNew(getBot().pos).getAngle();
		float botOrientation = getBot().angle;
		
		float botBallAngle = ballPos.subtractNew(getBot().pos).getAngle();
		
		float targetBotAngleDiff = Math.abs(targetAngle - botOrientation);
		float ballBotAngleDiff = Math.abs(botBallAngle - botOrientation);
		System.out.println("targetBot-aDiff: " + AIMath.rad2deg(targetBotAngleDiff));
		System.out.println("botBall-aDiff  : " + AIMath.rad2deg(ballBotAngleDiff));
		
		if (currentState == EBallMoveState.GET_BALL)
		{
			if (distanceBotBall < START_AIM_BALL_DISTANCE_TOLERANCE && ballBotAngleDiff < START_AIM_ANGLE_TOLERANCE)
			{
				return EBallMoveState.AIM;
			} else
			{
				return EBallMoveState.GET_BALL;
			}
		} else if (currentState == EBallMoveState.AIM)
		{
			if (distanceBotBall > START_GET_BALL_DISTANCE_TOLERANCE || ballBotAngleDiff > START_GET_BALL_ANGLE_TOLERANCE)
			{
				return EBallMoveState.GET_BALL;
			} else
			{
				// completed?
				if (targetBotAngleDiff < finishAimAngleTolerance)
				{
					return EBallMoveState.FINISHED;
				}
				return EBallMoveState.AIM;
			}
		} else
		{
			// System.out.println("UNKNOW STATE!!!!!!!!");
			return currentState;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- merge ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Merges movement.
	 * 
	 * @param move [m/s]
	 * @param rotation [rad/s]
	 * @param cmds
	 */
	private void mergeMovement(IVector2 move, float rotation, ArrayList<ACommand> cmds)
	{
		// Create command
		cmds.add(new TigerMotorMoveV2(move, 0, rotation));
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		GetBallAndAimSkill move = (GetBallAndAimSkill) newSkill;
		return target.equals(move.target);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
