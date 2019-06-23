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


/**
 * TODO osteinbrecher, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author osteinbrecher
 * 
 */
public class BallMoveV2 extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private EBallMoveState			actualSkillState	= EBallMoveState.GET_BALL;
	private ABallMoveCalculator	moveCalculator;
	
	// ---------------- geometry -------------------------------------------------
	
	// TODO Oliver, move parameters in config
	// public static final float GET_BALL_DISTANCE = 100;
	// public static final float MOVE_BALL_DISTANCE = 150;
	//
	//
	// public static final float TURN_ANGLE = AIMath.rad(1); // 8);
	// public static final float MOVE_ANGLE = AIMath.rad(30);
	
	// ---------------- timing ---------------------------------------------------
	private static final long		NEVER_PROCESSED	= -1;
	/** Stores the last {@link System#nanoTime()} {@link #calcActions()} was called, used for timing */
	private long						lastProcessed		= NEVER_PROCESSED;
	
	// --------------------------------------------------------------------------
	// private final TrackedPosition trackedBallPosition;
	private final IVector2			destination;
	private final IVector2			target;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * @param dest
	 */
	public BallMoveV2(IVector2 dest)
	{
		super(ESkillName.BALL_MOVE_V2, ESkillGroup.MOVE);
		
		this.destination = dest;
		this.target = null;
		// this.trackedBallPosition = new TrackedPosition(TrackedPosition.BALL_ID);
		this.moveCalculator = new GetBall(EBallMoveState.TURN);
		
	}
	

	/**
	 * @param dest
	 */
	public BallMoveV2(IVector2 dest, IVector2 target)
	{
		super(ESkillName.BALL_MOVE_V2, ESkillGroup.MOVE);
		
		this.destination = dest;
		this.target = target;
		// this.trackedBallPosition = new TrackedPosition(TrackedPosition.BALL_ID);
		this.moveCalculator = new GetBall(EBallMoveState.TURN);
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
		
		// Step 0: Timing
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
		
		// System.out.println("State:" + actualSkillState);
		
		moveCalculator.update(getWorldFrame(), getBot().id);
		changeSkillState(moveCalculator.checkState());
		
		// System.out.println("STATE " + actualSkillState);
		// System.out.println("completet? " + moveCalculator.isCompleted());
		
		if (target == null || actualSkillState != EBallMoveState.AIM)
		{
			moveCalculator.updateTargetOrientation(calcTargetOrientation(getWorldFrame().ball.pos));
			
		} else
		{
			moveCalculator.updateTargetOrientation(calcTargetOrientation(target));
		}
		

		// Step 1: Calculate move vector
		final IVector2 desiredMovement;
		desiredMovement = moveCalculator.calcMoveVector(deltaT);
		

		// Step 2: Calculate rotation
		final float turnspeed = moveCalculator.calcTurnSpeed();
		
		// Step 3: Merge
		mergeMovement(desiredMovement, turnspeed, cmds);
		

		// Completed?
		if (isSubCompleted())
		{
			cmds.add(new TigerMotorMoveV2(new Vector2f(0, 0), 0, 0));
			complete();
		}
		

		return cmds;
	}
	

	/**
	 * Calculates target orientation between bot an position
	 * 
	 * @return target angle
	 */
	private float calcTargetOrientation(IVector2 pos)
	{
		// trackedBallPosition.update(getWorldFrame());
		// if (trackedBallPosition.posUpdated())
		// {
		// // Calc difference
		// return AIMath.angleBetweenXAxisAndLine(getBot().pos, trackedBallPosition.getPos());
		// } else
		// {
		// // Target was not found, simply keep the current angle
		// return getBot().angle;
		// }
		
		return AIMath.angleBetweenXAxisAndLine(getBot().pos, pos);
		
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
		if (actualSkillState != state)
		{
			switch (state)
			{
				case GET_BALL:
					actualSkillState = state;
					moveCalculator = new GetBall(EBallMoveState.TURN);
					break;
				
				case TURN:
					actualSkillState = state;
					moveCalculator = new TurnBall(target);
					break;
				
				case MOVE:
					actualSkillState = state;
					if (target == null)
					{
						moveCalculator = new MoveBall(destination, getSisyphus(), false);
					} else
					{
						moveCalculator = new MoveBall(destination, getSisyphus(), true);
					}
					
					break;
				
				case AIM:
					actualSkillState = state;
					moveCalculator = new TurnBall(target);
					break;
				
			}
			
			moveCalculator.update(getWorldFrame(), getBot().id);
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
		BallMoveV2 move = (BallMoveV2) newSkill;
		return destination.equals(move.destination, AIConfig.getTolerances().getPositioning());
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean isSubCompleted()
	{
		if (target != null)
		{
			if (actualSkillState == EBallMoveState.MOVE && moveCalculator.isCompleted())
			{
				return true;
			}
		} else
		{
			if (actualSkillState == EBallMoveState.AIM && moveCalculator.isCompleted())
			{
				return true;
			}
		}
		
		return false;
	}
	
}
