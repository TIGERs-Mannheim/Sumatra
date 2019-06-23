/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.action.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author: MarkG
 */
public class LowChanceDirectKickActionMove extends AOffensiveActionMove
{
	/**
	 * Default
	 */
	public LowChanceDirectKickActionMove()
	{
		super(EOffensiveActionMove.LOW_CHANCE_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		if (isLowScoringChanceDirectGoalShootPossible(newTacticalField))
		{
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	@Override
	public void activateAction(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final OffensiveAction action)
	{
		action.setType(OffensiveAction.EOffensiveAction.GOAL_SHOT);
		action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return getDirectShootScore(baseAiFrame.getWorldFrame())
				* ActionMoveConstants.getViabilityMultiplierLowChanceDirectKick();
	}
}
