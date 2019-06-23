/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * Possible action moves for the attacker
 */
public enum EOffensiveActionMove implements IInstanceableEnum
{
	/**
	 * Pass is forced in standard situations
	 */
	FORCED_PASS(new InstanceableClass(ForcedPassActionMove.class)),
	/**
	 * Redirect directly to the opponent goal
	 */
	REDIRECT_GOAL_KICK(new InstanceableClass(RedirectGoalKickActionMove.class)),
	/**
	 * Direct kick on the enemy goal
	 */
	GOAL_KICK(new InstanceableClass(GoalKickActionMove.class)),
	/**
	 * Free the ball when its dangerous and near our penalty area
	 */
	CLEARING_KICK(new InstanceableClass(ClearingKickActionMove.class)),
	/**
	 * Redirect directly to a friendly robot (pass)
	 */
	REDIRECT_PASS(new InstanceableClass(RedirectPassActionMove.class)),
	/**
	 * A standard pass to another friendly robot
	 */
	STANDARD_PASS(new InstanceableClass(StandardPassActionMove.class)),
	/**
	 * Low chance of scoring a goal, otherwise same as {@link #GOAL_KICK}
	 */
	LOW_CHANCE_GOAL_KICK(new InstanceableClass(LowChanceGoalKickActionMove.class)),
	/**
	 * Chip the ball to the enemy Half
	 */
	MOVE_BALL_TO_OPPONENT_HALF(new InstanceableClass(MoveBallToOpponentHalfActionMove.class)),
	/**
	 * Pass to some free spot on the field, no robot as pass Target
	 */
	KICK_INS_BLAUE(new InstanceableClass(KickInsBlaueActionMove.class)),
	
	;
	
	
	private final InstanceableClass clazz;
	
	
	/**
	 */
	EOffensiveActionMove(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
