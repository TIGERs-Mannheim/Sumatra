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
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;

import java.util.Optional;


/**
 * @author: MarkG
 */
public class StandardPassActionMove extends AOffensiveActionMove
{
	/**
	 * Default
	 */
	public StandardPassActionMove()
	{
		super(EOffensiveActionMove.CLEARING_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		Optional<IPassTarget> passTarget = selectPassTarget(id, newTacticalField);
		return passTarget.map(iPassTarget -> EActionViability.PARTIALLY).orElse(EActionViability.FALSE);
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
		return selectPassTarget(id, newTacticalField).map(IPassTarget::getScore).orElse(0.0)
				* ActionMoveConstants.getViabilityMultiplierStandardPass();
	}
}
