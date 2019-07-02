/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ids.BotID;


/**
 * Force a pass (for example in standard situations)
 */
public class ForcedPassActionMove extends AOffensiveActionMove
{
	public ForcedPassActionMove()
	{
		super(EOffensiveActionMove.FORCED_PASS);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (!OffensiveConstants.isForcePassWhenIndirectIsCalled())
		{
			return EActionViability.FALSE;
		}
		
		if ((newTacticalField.getGameState().isIndirectFreeForUs())
				&& isPassPossible(id, newTacticalField, baseAiFrame))
		{
			return EActionViability.TRUE;
		}
		
		if (OffensiveConstants.isAlwaysForcePass() && isPassPossible(id, newTacticalField, baseAiFrame))
		{
			return EActionViability.TRUE;
		}
		
		return EActionViability.FALSE;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IRatedPassTarget passTarget = selectPassTarget(id, newTacticalField, baseAiFrame)
				.orElseThrow(IllegalStateException::new);
		final KickTarget kickTarget = KickTarget.pass(passTarget.getDynamicPos(),
				OffensiveConstants.getMaxPassEndVelRedirect(), KickTarget.ChipPolicy.ALLOW_CHIP);
		return createOffensiveAction(EOffensiveAction.PASS, kickTarget)
				.withPassTarget(passTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return 1;
	}
}
