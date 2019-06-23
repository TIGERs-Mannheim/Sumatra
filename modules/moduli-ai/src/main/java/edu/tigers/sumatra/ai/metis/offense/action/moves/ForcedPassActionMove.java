/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author MarkG
 */
public class ForcedPassActionMove extends AOffensiveActionMove
{
	/**
	 * Default
	 */
	public ForcedPassActionMove()
	{
		super(EOffensiveActionMove.FORCED_PASS);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		boolean bothTouched = newTacticalField.isMixedTeamBothTouchedBall();
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() != EAIControlState.MIXED_TEAM_MODE)
		{
			bothTouched = true;
		}
		
		if (!OffensiveConstants.isForcePassWhenIndirectIsCalled())
		{
			return EActionViability.FALSE;
		}
		
		if ((newTacticalField.getGameState().isIndirectFreeForUs() || !bothTouched)
				&& isPassPossible(id, newTacticalField))
		{
			return EActionViability.TRUE;
		}
		
		if (OffensiveConstants.isAlwaysForcePass() && isPassPossible(id, newTacticalField))
		{
			return EActionViability.TRUE;
		}
		
		return EActionViability.FALSE;
	}
	
	
	@Override
	public void activateAction(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final OffensiveAction action)
	{
		IPassTarget passTarget = selectPassTarget(id, newTacticalField)
				.orElseThrow(IllegalStateException::new);
		action.setPassTarget(passTarget);
		action.setType(OffensiveAction.EOffensiveAction.PASS);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return 1;
	}
}
