/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;


/**
 * Directly kick on the opponent goal even when the chance is low
 */
public class LowChanceGoalKickActionMove extends AOffensiveActionMove
{
	public LowChanceGoalKickActionMove()
	{
		super(EOffensiveActionMove.LOW_CHANCE_GOAL_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (ActionMoveConstants.allowGoalKick()
				&& isLowScoringChanceDirectGoalShootPossible(newTacticalField))
		{
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		final KickTarget kickTarget = new KickTarget(getBestShootTarget(newTacticalField),
				RuleConstraints.getMaxBallSpeed(),
				KickTarget.ChipPolicy.NO_CHIP);
		return createOffensiveAction(EOffensiveAction.GOAL_SHOT, kickTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getBestGoalKickTarget().map(IRatedTarget::getScore).orElse(0.0)
				* ActionMoveConstants.getViabilityMultiplierLowChanceDirectKick();
	}
}
