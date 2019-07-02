/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveExecutionStatus;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.stream.Collectors;


/**
 * The attacker roles handles the ball
 */
public class AttackerRole extends ARole
{
	
	private String oldState = "";
	
	private IPenaltyArea area = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + 20);

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
		FinisherMoveState finisherMoveState = new FinisherMoveState(this);
		
		addTransition(EBallHandlingEvent.BALL_MOVES, approachBallLineState);
		addTransition(EBallHandlingEvent.BALL_MOVES_TOWARDS_ME, approachBallLineState);
		addTransition(EBallHandlingEvent.OPPONENT_BETWEEN_ME_AND_BALL, approachBallLineState);
		
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
		addTransition(EBallHandlingEvent.FOUND_SUITABLE_STRATEGY, touchKickState);
		
		addTransition(EBallHandlingEvent.FREE_KICK, singleTouchKickState);
		addTransition(EBallHandlingEvent.SWITCH_TO_RUN_UP, runUpChipKickState);
		
		addTransition(EBallHandlingEvent.START_FINISHER_MOVE, finisherMoveState);
		addTransition(EBallHandlingEvent.FINISHER_MOVE_EXECUTED, touchKickState);
		
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
		boolean switchedState = !oldState.equals(getCurrentState().getIdentifier());
		oldState = getCurrentState().getIdentifier();
		if (getPos().distanceTo(getBall().getPos()) > 200 || switchedState)
		{
			return EOffensiveExecutionStatus.GETTING_READY;
		}
		return EOffensiveExecutionStatus.IMMINENT;
	}
	
	
	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();
		
		// determine critical foe bots
		getCurrentSkill().getMoveCon().setCriticalFoeBots(
				getWFrame().getFoeBots().values().stream()
						.filter(b -> OffensiveMath.isBotCritical(b.getPos(), area))
						.map(ITrackedBot::getBotId)
						.collect(Collectors.toSet()));
	}
}
