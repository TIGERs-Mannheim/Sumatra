/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Try scoring a goal with a one touch (redirect) kick
 */
public class RedirectGoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.2")
	private static double minGoalShotChanceForTrueViability = 0.2;
	
	@Configurable(defValue = "0.05")
	private static double minGoalShotChanceForPartiallyViability = 0.05;
	
	
	public RedirectGoalKickActionMove()
	{
		super(EOffensiveActionMove.REDIRECT_GOAL_KICK);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (!ActionMoveConstants.allowGoalKick() || attackerCanNotKickOrCatchTheBall(baseAiFrame))
		{
			return EActionViability.FALSE;
		}
		
		double antiToggleAngle = 0.05;
		double antiToggleScore = 0.075;
		if (baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().containsKey(id)
				&& baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().get(id)
						.getMove() != EOffensiveActionMove.REDIRECT_GOAL_KICK)
		{
			antiToggleAngle = 0;
			antiToggleScore = 0;
		}
		
		Optional<IRatedTarget> vp = newTacticalField.getBestGoalKickTargetForBot().get(id);
		if (!vp.isPresent())
		{
			return EActionViability.FALSE;
		}
		
		boolean isGoalRedirectPossible = isBallRedirectReasonable(baseAiFrame.getWorldFrame(),
				baseAiFrame.getWorldFrame().getTiger(id).getBotKickerPos(),
				vp.get().getTarget(), antiToggleAngle);
		
		double val = vp.map(IRatedTarget::getScore).orElse(0.0);
		if (val > minGoalShotChanceForTrueViability + antiToggleScore && isGoalRedirectPossible)
		{
			return EActionViability.TRUE;
		} else if (val > minGoalShotChanceForPartiallyViability + antiToggleScore && isGoalRedirectPossible)
		{
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		final DynamicPosition target = newTacticalField.getBestGoalKickTargetForBot().get(id)
				.map(IRatedTarget::getTarget)
				.orElse(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
		final KickTarget kickTarget = new KickTarget(
				target,
				RuleConstraints.getMaxBallSpeed(),
				KickTarget.ChipPolicy.NO_CHIP);
		return createOffensiveAction(EOffensiveAction.REDIRECT, kickTarget)
				.withAllowRedirect(true);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getBestGoalKickTargetForBot().get(id).map(IRatedTarget::getScore).orElse(0.0);
	}
	
	
	/**
	 * @param wf
	 * @param kickerPos
	 * @param target
	 * @param antiToggle
	 * @return
	 */
	public boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 kickerPos, final IVector2 target,
			final double antiToggle)
	{
		return isBallRedirectReasonable(wf, wf.getBall().getPos(), kickerPos, target, antiToggle);
	}
	
	
	/**
	 * @param wf
	 * @param source
	 * @param kickerPos
	 * @param target
	 * @param antiToogle
	 * @return
	 */
	private boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 source, final IVector2 kickerPos,
			final IVector2 target, final double antiToogle)
	{
		double atC = 0;
		if (antiToogle > 0)
		{
			atC = 0.05;
		}
		double redirectAngle = OffensiveMath.getRedirectAngle(source, kickerPos, target);
		return (MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(), kickerPos) >= (0.15 - atC))
				&& (redirectAngle <= (OffensiveConstants.getMaximumReasonableRedirectAngle() + antiToogle));
	}
}
