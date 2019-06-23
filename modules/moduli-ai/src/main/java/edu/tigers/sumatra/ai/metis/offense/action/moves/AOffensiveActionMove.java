/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Base class for all action moves
 */
public abstract class AOffensiveActionMove
{
	private final EOffensiveActionMove move;
	
	private double viabilityScore;
	
	
	protected AOffensiveActionMove(final EOffensiveActionMove move)
	{
		this.move = move;
	}
	
	
	public abstract EActionViability isActionViable(BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame);
	
	
	public abstract OffensiveAction activateAction(BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame);
	
	
	public EOffensiveActionMove getMove()
	{
		return move;
	}
	
	
	protected boolean isPassPossible(final BotID botID, final TacticalField newTacticalField)
	{
		return selectPassTarget(botID, newTacticalField).isPresent();
	}
	
	
	/**
	 * selectPassTarget:
	 * determines which target to pass to.
	 *
	 * @param botID
	 * @param newTacticalField
	 * @return a valid pass target, if present
	 */
	protected Optional<IPassTarget> selectPassTarget(final BotID botID, final TacticalField newTacticalField)
	{
		return newTacticalField.getPassTargetsRanked().stream()
				.filter(passTarget -> !passTarget.getBotId().equals(botID))
				.findFirst();
	}
	
	
	protected boolean isLowScoringChanceDirectGoalShootPossible(final TacticalField newTacticalField)
	{
		return newTacticalField.getBestGoalKickTarget() != null;
	}
	
	
	protected DynamicPosition getBestShootTarget(final TacticalField newTacticalField)
	{
		return newTacticalField.getBestGoalKickTarget()
				.map(IRatedTarget::getTarget).orElse(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	protected abstract double calcViabilityScore(BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame);
	
	
	protected OffensiveAction createOffensiveAction(EOffensiveAction action, KickTarget kickTarget)
	{
		return new OffensiveAction(getMove(), getViabilityScore(), action, kickTarget);
	}
	
	
	/**
	 * @param id
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public void calcViability(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, double adjustedValue)
	{
		viabilityScore = Math.min(1, Math.max(0, calcViabilityScore(id, newTacticalField, baseAiFrame) * adjustedValue));
	}
	
	
	protected boolean attackerCanNotKickOrCatchTheBall(BaseAiFrame aiFrame)
	{
		return aiFrame.getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(a -> (AttackerRole) a)
				.noneMatch(AttackerRole::canKickOrCatchTheBall);
	}
	
	
	public double getViabilityScore()
	{
		return viabilityScore;
	}
}
