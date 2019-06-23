/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.action.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author: MarkG
 */
public class DirectKickActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "This is the area around their penalty area where the ball is shot directly", defValue = "100")
	private static float defaultMarginAroundPenaltyAreaForDirectShot = 100;
	
	
	/**
	 * Default
	 */
	public DirectKickActionMove()
	{
		super(EOffensiveActionMove.DIRECT_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		final boolean shouldShootDirectly = isDirectGoalShootPossible(newTacticalField, baseAiFrame)
				|| isBallTooCloseToPenaltyArea(baseAiFrame);
		
		if (shouldShootDirectly)
		{
			return EActionViability.TRUE;
		}
		
		return EActionViability.FALSE;
	}
	
	
	private boolean isBallTooCloseToPenaltyArea(BaseAiFrame baseAiFrame)
	{
		IPenaltyArea penaltyAreaWithMargin = Geometry.getPenaltyAreaTheir()
				.withMargin(defaultMarginAroundPenaltyAreaForDirectShot);
		
		return 	penaltyAreaWithMargin
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos());
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
				* ActionMoveConstants.getViabilityMultiplierDirectKick();
	}
}
