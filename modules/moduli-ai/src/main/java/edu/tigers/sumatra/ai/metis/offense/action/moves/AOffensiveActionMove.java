/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.finisher.IFinisherMove;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.util.Optional;


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
	
	
	protected boolean isPassPossible(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		return selectPassTarget(botID, newTacticalField, baseAiFrame).isPresent();
	}
	
	
	/**
	 * selectPassTarget:
	 * determines which target to pass to.
	 *
	 * @param botID
	 * @param newTacticalField
	 * @return a valid pass target, if present
	 */
	protected Optional<IRatedPassTarget> selectPassTarget(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getRatedPassTargetsRanked().stream()
				.filter(passTarget -> !passTarget.getBotId().equals(botID))
				.filter(e -> isTargetApproachAngleAccesible(newTacticalField, botID, e,
						baseAiFrame.getWorldFrame().getBall().getPos()))
				.findFirst();
	}
	
	
	private boolean isTargetApproachAngleAccesible(final TacticalField newTacticalField, final BotID botID,
			final IRatedPassTarget passTarget, final IVector2 ballPos)
	{
		double angle = ballPos.subtractNew(passTarget.getPos()).getAngle();
		if (!OffensiveMath.isAngleAccessible(newTacticalField.getUnaccessibleBallAngles().get(botID), angle))
		{
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(passTarget.getPos(), 50),
					new Color(255, 0, 13, 109));
			dc.setFill(true);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(dc);
			return false;
		}
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(passTarget.getPos(), 50),
				new Color(0, 255, 1, 109));
		dc.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(dc);
		return true;
	}
	
	
	protected boolean isLowScoringChanceDirectGoalShootPossible(final TacticalField newTacticalField)
	{
		return newTacticalField.getBestGoalKickTarget().isPresent();
	}
	
	
	protected IVector2 calcShotOrigin(final BotID id, final BaseAiFrame baseAiFrame)
	{
		IVector2 origin = baseAiFrame.getWorldFrame().getBall().getPos();
		if (!attackerCanNotKickOrCatchTheBall(baseAiFrame, id))
		{
			// if bot is catching ball, then set origin to its kicker pos
			origin = baseAiFrame.getWorldFrame().getBot(id).getBotKickerPos();
		}
		return origin;
	}
	
	
	protected Optional<IRatedTarget> calcAndRateTarget(BaseAiFrame baseAiFrame, IVector2 origin)
	{
		final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(baseAiFrame.getWorldFrame().getFoeBots().values());
		rater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		return rater.rate(origin);
	}
	
	
	protected Optional<IRatedTarget> calcAndRateTarget(BaseAiFrame baseAiFrame, IVector2 origin, double timeToKick)
	{
		final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(baseAiFrame.getWorldFrame().getFoeBots().values());
		rater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		rater.setTimeToKick(timeToKick);
		return rater.rate(origin);
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
	
	
	protected OffensiveAction createProtectOffensiveAction(KickTarget protectTarget)
	{
		return new OffensiveAction(EOffensiveActionMove.PROTECT_MOVE, EOffensiveAction.PROTECT, protectTarget);
	}
	
	
	protected OffensiveAction createOffensiveAction(EOffensiveAction action, IFinisherMove finisher,
			KickTarget kickTarget)
	{
		return new OffensiveAction(getMove(), getViabilityScore(), action, kickTarget, finisher);
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
	
	
	protected boolean attackerCanNotKickOrCatchTheBall(BaseAiFrame aiFrame, BotID id)
	{
		return aiFrame.getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(a -> (AttackerRole) a)
				.filter(e -> e.getBotID().equals(id))
				.noneMatch(AttackerRole::canKickOrCatchTheBall);
	}
	
	
	public double getViabilityScore()
	{
		return viabilityScore;
	}
}
