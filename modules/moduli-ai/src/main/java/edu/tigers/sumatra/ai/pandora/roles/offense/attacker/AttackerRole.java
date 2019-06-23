/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveExecutionStatus;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;


/**
 * The attacker roles handles the ball
 */
public class AttackerRole extends ARole
{
	
	
	public AttackerRole()
	{
		super(ERole.ATTACKER);
		
		ApproachBallLineState approachBallLineState = new ApproachBallLineState(this);
		ApproachAndStopBallState approachAndStopBallSkill = new ApproachAndStopBallState(this);
		ApproachBallState approachBallState = new ApproachBallState(this);
		ReceiveBallState receiveBallState = new ReceiveBallState(this);
		RedirectBallState redirectBallState = new RedirectBallState(this);
		TouchKickState touchKickState = new TouchKickState(this);
		ProtectBallState protectBallState = new ProtectBallState(this);
		SingleTouchKickState singleTouchKickState = new SingleTouchKickState(this);
		RunUpChipKickState runUpChipKickState = new RunUpChipKickState(this);
		
		addTransition(EBallHandlingEvent.BALL_MOVES, approachBallLineState);
		addTransition(EBallHandlingEvent.BALL_MOVES_TOWARDS_ME, approachBallLineState);
		
		addTransition(EBallHandlingEvent.BALL_MOVES_AWAY_FROM_ME, approachAndStopBallSkill);
		addTransition(EBallHandlingEvent.BALL_KICKED, approachAndStopBallSkill);
		
		addTransition(EBallHandlingEvent.BALL_LINE_APPROACHED, receiveBallState);
		addTransition(EBallHandlingEvent.BALL_NOT_REDIRECTABLE, receiveBallState);
		addTransition(EBallHandlingEvent.SWITCH_TO_REDIRECT, redirectBallState);
		
		addTransition(EBallHandlingEvent.BALL_STOPPED_BY_BOT, touchKickState);
		addTransition(EBallHandlingEvent.BALL_RECEIVED, touchKickState);
		addTransition(EBallHandlingEvent.BALL_APPROACHED, touchKickState);
		
		addTransition(EBallHandlingEvent.BALL_STOPPED_MOVING, approachBallState);
		addTransition(EBallHandlingEvent.BALL_NOT_REDIRECTED, approachBallState);
		addTransition(EBallHandlingEvent.BALL_NOT_RECEIVED, approachBallState);
		addTransition(EBallHandlingEvent.BALL_LOST, approachBallState);
		
		addTransition(EBallHandlingEvent.BALL_POSSESSION_THREATENED, protectBallState);
		addTransition(EBallHandlingEvent.BALL_POSSESSION_SAVE, touchKickState);
		
		addTransition(EBallHandlingEvent.FREE_KICK, singleTouchKickState);
		addTransition(EBallHandlingEvent.SWITCH_TO_RUN_UP, runUpChipKickState);
		
		setInitialState(approachBallLineState);
	}
	
	
	public boolean canKickOrCatchTheBall()
	{
		Class<?> state = getCurrentState().getClass();
		return state.equals(ApproachBallLineState.class)
				|| state.equals(ReceiveBallState.class)
				|| state.equals(RedirectBallState.class);
	}
	
	
	public EOffensiveExecutionStatus getExecutionStatus()
	{
		if (getPos().distanceTo(getBall().getPos()) > 200)
		{
			return EOffensiveExecutionStatus.GETTING_READY;
		}
		return EOffensiveExecutionStatus.IMMINENT;
	}
}
