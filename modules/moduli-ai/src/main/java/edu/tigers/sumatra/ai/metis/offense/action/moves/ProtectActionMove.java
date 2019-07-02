/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Protect The ball
 */
public class ProtectActionMove extends AOffensiveActionMove
{
	private static final double MIN_PROTECT_SCORE = 0.01;

	public ProtectActionMove()
	{
		super(EOffensiveActionMove.PROTECT_MOVE);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		return EActionViability.PARTIALLY;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IVector2 protectTarget = Geometry.getGoalTheir().getCenter();
		if (newTacticalField.getEnemyClosestToBall().getBot() != null)
		{
			protectTarget = newTacticalField.getEnemyClosestToBall().getBot().getPos();
		}
		KickTarget target = KickTarget.pass(new DynamicPosition(protectTarget), 2.0, KickTarget.ChipPolicy.NO_CHIP);
		return createProtectOffensiveAction(target);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return MIN_PROTECT_SCORE;
	}
}
