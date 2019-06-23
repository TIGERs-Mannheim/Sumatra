/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.math.kick.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author: MarkG
 */
public abstract class AOffensiveActionMove
{
	
	protected static final Logger			log	= Logger
			.getLogger(AOffensiveActionMove.class.getName());
	
	private final EOffensiveActionMove	move;
	
	private double								viabilityScore;
	
	
	/**
	 * @param move
	 */
	protected AOffensiveActionMove(final EOffensiveActionMove move)
	{
		this.move = move;
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @param action
	 * @param id
	 * @return
	 */
	public abstract EActionViability isActionViable(BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame,
			OffensiveAction action);
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @param action
	 * @param id
	 */
	public abstract void activateAction(BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			OffensiveAction action);
	
	
	/**
	 * @return EOffenisveStrategyFeature
	 */
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
	
	
	protected boolean isDirectGoalShootPossible(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getBestDirectShotTarget() != null
				&& OffensiveMath.willBotShoot(baseAiFrame.getWorldFrame());
	}
	
	
	protected double getDirectShootScore(final WorldFrame wf)
	{
		Optional<ValuePoint> target = BestDirectShotBallPossessingBot.getBestShot(Geometry.getGoalTheir(),
				wf.getBall().getPos(), new ArrayList<>(wf.getFoeBots().values()));
		
		return target.orElse(new ValuePoint(DefenseMath.getBisectionGoal(wf.getBall().getPos()), 0.0)).getValue();
	}
	
	
	protected boolean isLowScoringChanceDirectGoalShootPossible(final TacticalField newTacticalField)
	{
		return newTacticalField.getBestDirectShotTarget() != null;
	}
	
	
	protected DynamicPosition getBestShootTarget(final TacticalField newTacticalField)
	{
		IVector2 target = newTacticalField.getBestDirectShotTarget();
		if (target == null)
		{
			target = Geometry.getGoalTheir().getCenter();
		}
		return new DynamicPosition(target);
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	protected abstract double calcViabilityScore(BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame);
	
	
	/**
	 * @param id
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public void calcViability(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		viabilityScore = calcViabilityScore(id, newTacticalField, baseAiFrame);
	}
	
	
	public double getViabilityScore()
	{
		return viabilityScore;
	}
}
