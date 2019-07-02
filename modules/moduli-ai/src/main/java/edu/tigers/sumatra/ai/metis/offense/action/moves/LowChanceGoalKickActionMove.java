/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Directly kick on the opponent goal even when the chance is low
 */
public class LowChanceGoalKickActionMove extends AOffensiveActionMove
{
	private static final double MIN_LOW_SCORE_CHANCE = 0.02;

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
		Optional<IRatedTarget> target = calcAndRateTarget(baseAiFrame, calcShotOrigin(id, baseAiFrame));
		DynamicPosition dTarget = target.map(IRatedTarget::getTarget)
				.orElse(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
		KickTarget kickTarget = KickTarget.goalShot(dTarget);
		return createOffensiveAction(EOffensiveAction.GOAL_SHOT, kickTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 shootOrigin = calcShotOrigin(id, baseAiFrame);
		double timeToKick = DefenseMath.calculateTDeflectEnemyGoal(baseAiFrame.getWorldFrame().getBot(id).getPos(),
				shootOrigin);
		Optional<IRatedTarget> target = calcAndRateTarget(baseAiFrame, shootOrigin, timeToKick);
		double score = target.map(IRatedTarget::getScore).orElse(0.0) * ActionMoveConstants.getViabilityMultiplierDirectKick();
		if (Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()*2 + Geometry.getBallRadius()*2)
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
		{
			score = Math.max(MIN_LOW_SCORE_CHANCE, score);
		}
		return score;
	}
}
