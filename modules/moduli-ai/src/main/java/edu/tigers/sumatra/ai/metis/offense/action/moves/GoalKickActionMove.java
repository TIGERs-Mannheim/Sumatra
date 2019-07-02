/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Kick directly on the opponent goal
 */
public class GoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "This is the area around their penalty area where the ball is shot directly", defValue = "100")
	private static float defaultMarginAroundPenaltyAreaForDirectShot = 100;
	
	@Configurable(defValue = "0.1")
	private static double minGoalChanceForPartiallyViability = 0.1;
	
	static
	{
		ConfigRegistration.registerClass("metis", GoalKickActionMove.class);
	}
	
	
	public GoalKickActionMove()
	{
		super(EOffensiveActionMove.GOAL_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IVector2 origin = calcShotOrigin(id, baseAiFrame);
		
		double ratedTargetScore = calcAndRateTarget(baseAiFrame, origin).map(IRatedTarget::getScore).orElse(0.0);
		if (ActionMoveConstants.allowGoalKick() && ratedTargetScore > OffensiveConstants.getMinBotShouldDoGoalShotScore())
		{
			return EActionViability.TRUE;
		}
		
		if (isBallTooCloseToPenaltyArea(baseAiFrame) && ActionMoveConstants.allowGoalKick())
		{
			return EActionViability.TRUE;
		}
		
		if (ratedTargetScore > minGoalChanceForPartiallyViability)
		{
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	private boolean isBallTooCloseToPenaltyArea(BaseAiFrame baseAiFrame)
	{
		IPenaltyArea penaltyAreaWithMargin = Geometry.getPenaltyAreaTheir()
				.withMargin(defaultMarginAroundPenaltyAreaForDirectShot);
		
		return penaltyAreaWithMargin
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos());
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
		double score = target.map(IRatedTarget::getScore).orElse(0.0);
		return score * ActionMoveConstants.getViabilityMultiplierDirectKick();
	}
}
