/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;


/**
 * Kick directly on the opponent goal
 */
public class GoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "This is the area around their penalty area where the ball is shot directly", defValue = "100")
	private static float defaultMarginAroundPenaltyAreaForDirectShot = 100;
	
	
	public GoalKickActionMove()
	{
		super(EOffensiveActionMove.GOAL_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		final boolean goalKick = OffensiveMath.attackerCanScoreGoal(newTacticalField)
				|| isBallTooCloseToPenaltyArea(baseAiFrame);
		
		if (ActionMoveConstants.allowGoalKick() && goalKick)
		{
			return EActionViability.TRUE;
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
		KickTarget kickTarget = new KickTarget(getBestShootTarget(newTacticalField), RuleConstraints.getMaxBallSpeed(),
				KickTarget.ChipPolicy.NO_CHIP);
		return createOffensiveAction(EOffensiveAction.GOAL_SHOT, kickTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getBestGoalKickTarget().map(IRatedTarget::getScore).orElse(0.0)
				* ActionMoveConstants.getViabilityMultiplierDirectKick();
	}
}
