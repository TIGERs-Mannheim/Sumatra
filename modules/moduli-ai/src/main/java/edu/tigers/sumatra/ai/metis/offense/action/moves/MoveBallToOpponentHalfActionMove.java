/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Get the ball to the opponent half by chipping it forward
 */
public class MoveBallToOpponentHalfActionMove extends AOffensiveActionMove
{
	public MoveBallToOpponentHalfActionMove()
	{
		super(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().values().stream()
				.anyMatch(a -> Geometry.getFieldHalfTheir().isPointInShape(a.getPos())))
		{
			return EActionViability.FALSE;
		}
		return EActionViability.PARTIALLY;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		// assuming that we can not reach the opponent goal anyway, we just kick with max speed
		IVector2 target = Geometry.getGoalTheir().getCenter();
		double goalWidth = Geometry.getGoalTheir().getWidth()
				* Math.signum(baseAiFrame.getWorldFrame().getBall().getPos().y());
		IVector2 centerToGoalPost = Geometry.getGoalTheir().getRightPost().subtractNew(target);
		target = target.addNew(centerToGoalPost.scaleToNew(goalWidth / 2.5));
		final KickTarget kickTarget = KickTarget.pass(new DynamicPosition(target, 0.6),
				RuleConstraints.getMaxBallSpeed(), KickTarget.ChipPolicy.ALLOW_CHIP);
		return createOffensiveAction(EOffensiveAction.CLEARING_KICK, kickTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return ActionMoveConstants.getDefaultGoToOtherHalfViability()
				* ActionMoveConstants.getViabilityMultiplierGoToOtherHalf();
	}
	
}
