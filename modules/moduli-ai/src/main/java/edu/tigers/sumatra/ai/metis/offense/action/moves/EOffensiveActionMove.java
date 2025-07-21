/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Possible action moves for the attacker
 */
@Getter
@AllArgsConstructor
public enum EOffensiveActionMove implements IInstanceableEnum
{
	/**
	 * Redirect directly to the opponent goal
	 */
	REDIRECT_GOAL_KICK(new InstanceableClass<>(RedirectGoalKickActionMove.class)),
	/**
	 * Direct kick on the opponent goal
	 */
	GOAL_KICK(new InstanceableClass<>(GoalKickActionMove.class)),
	/**
	 * A standard pass to another friendly robot
	 */
	STANDARD_PASS(new InstanceableClass<>(StandardPassActionMove.class)),
	/**
	 * Direct kick on the opponent goal
	 */
	FINISHER(new InstanceableClass<>(FinisherActionMove.class)),
	/**
	 * Pass is forced in standard situations
	 */
	FORCED_PASS(new InstanceableClass<>(ForcedPassActionMove.class)),
	/**
	 * Low chance of scoring a goal, otherwise same as {@link #GOAL_KICK}
	 */
	LOW_CHANCE_GOAL_KICK(new InstanceableClass<>(LowChanceGoalKickActionMove.class)),
	/**
	 * Chip the ball to the opponent Half
	 */
	MOVE_BALL_TO_OPPONENT_HALF(new InstanceableClass<>(MoveBallToOpponentHalfActionMove.class)),
	/**
	 * Receive the ball
	 */
	RECEIVE_BALL(new InstanceableClass<>(ReceiveBallActionMove.class)),
	/**
	 * If theres nothing good to do, then protect the ball
	 */
	PROTECT_MOVE(new InstanceableClass<>(ProtectActionMove.class)),

	;


	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
