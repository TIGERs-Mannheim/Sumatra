/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;


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
		Optional<IPassTarget> passTarget = selectPassTarget(id, newTacticalField);
		return passTarget.map(iPassTarget -> EActionViability.PARTIALLY).orElse(EActionViability.FALSE);
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IPassTarget passTarget = selectPassTarget(id, newTacticalField)
				.orElseThrow(IllegalStateException::new);
		final KickTarget kickTarget = new KickTarget(passTarget.getDynamicTarget(),
				OffensiveConstants.getMaxPassEndVelRedirect(), KickTarget.ChipPolicy.ALLOW_CHIP);
		return createOffensiveAction(EOffensiveAction.PASS, kickTarget)
				.withPassTarget(passTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return selectPassTarget(id, newTacticalField).map(IPassTarget::getScore).orElse(0.0)
				* ActionMoveConstants.getViabilityMultiplierStandardPass();
	}
}
