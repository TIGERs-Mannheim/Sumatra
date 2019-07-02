/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ids.BotID;

import java.util.Optional;


/**
 * Perform a standard pass
 */
public class StandardPassActionMove extends AOffensiveActionMove
{
	public StandardPassActionMove()
	{
		super(EOffensiveActionMove.STANDARD_PASS);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.getAvailableAttackers() < 2)
		{
			// no pass allowed if we have less than 2 offensive robots available
			return EActionViability.FALSE;
		}
		Optional<IRatedPassTarget> passTarget = selectPassTarget(id, newTacticalField, baseAiFrame);
		return passTarget.map(iPassTarget -> EActionViability.PARTIALLY).orElse(EActionViability.FALSE);
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
		return selectPassTarget(id, newTacticalField, baseAiFrame).map(IRatedPassTarget::getScore).orElse(0.0)
				* ActionMoveConstants.getViabilityMultiplierStandardPass();
	}
}
