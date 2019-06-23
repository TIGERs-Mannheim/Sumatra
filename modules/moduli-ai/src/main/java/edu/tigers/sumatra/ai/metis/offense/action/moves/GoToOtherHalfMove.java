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
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author: MarkG
 */
public class GoToOtherHalfMove extends AOffensiveActionMove
{
	
	/**
	 * Default
	 */
	public GoToOtherHalfMove()
	{
		super(EOffensiveActionMove.GO_TO_OTHER_HALF);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().values().stream()
				.anyMatch(a -> Geometry.getFieldHalfTheir().isPointInShape(a.getPos())))
		{
			return EActionViability.FALSE;
		}
		return EActionViability.PARTIALLY;
	}
	
	
	@Override
	public void activateAction(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final OffensiveAction action)
	{
		action.setType(OffensiveAction.EOffensiveAction.CLEARING_KICK);
		action.setDirectShotAndClearingTarget(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return ActionMoveConstants.getDefaultGoToOtherHalfViability()
				* ActionMoveConstants.getViabilityMultiplierGoToOtherHalf();
	}
	
}
