/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveRedirectorMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Perform a one touch (redirect) pass
 */
public class RedirectPassActionMove extends AOffensiveActionMove
{
	
	@Configurable(defValue = "0.2")
	private static double minReceiverGoalChance = 0.2;
	
	private OffensiveRedirectorMath redirectorMath = new OffensiveRedirectorMath();
	
	private IPassTarget target = null;
	
	private double redirectPassViability = 0;
	
	static
	{
		ConfigRegistration.registerClass("metis", RedirectPassActionMove.class);
	}
	
	
	public RedirectPassActionMove()
	{
		super(EOffensiveActionMove.REDIRECT_PASS);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (attackerCanNotKickOrCatchTheBall(baseAiFrame))
		{
			return EActionViability.FALSE;
		}
		
		IPassTarget oldTarget = null;
		if (baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().containsKey(id)
				&& baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().get(id)
						.getMove() == EOffensiveActionMove.REDIRECT_PASS)
		{
			oldTarget = baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().get(id)
					.getPassTarget().orElse(null);
		}
		
		redirectPassViability = 0;
		Optional<IPassTarget> oTarget = calcPotentialPassTarget(id, newTacticalField, baseAiFrame, oldTarget);
		if (oTarget.isPresent() && oTarget.get().getGoalKickScore() > minReceiverGoalChance)
		{
			target = oTarget.get();
			redirectPassViability = target.getGoalKickScore();
			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		final KickTarget kickTarget = new KickTarget(target.getDynamicTarget(),
				OffensiveConstants.getMaxPassEndVelRedirect(), KickTarget.ChipPolicy.ALLOW_CHIP);
		return createOffensiveAction(EOffensiveAction.REDIRECT, kickTarget)
				.withPassTarget(target)
				.withAllowRedirect(true);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return redirectPassViability;
	}
	
	
	private Optional<IPassTarget> calcPotentialPassTarget(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, IPassTarget oldDoublePassTarget)
	{
		Map<BotID, ITrackedBot> botMap = OffensiveMath
				.getPotentialOffensiveBotMap(newTacticalField, baseAiFrame)
				.entrySet().stream()
				.filter(entry -> entry.getKey() != id)
				.filter(entry -> entry.getKey().isBot())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return redirectorMath.calcBestRedirectPassTarget(baseAiFrame.getWorldFrame(), botMap,
				baseAiFrame.getWorldFrame().getTiger(id),
				newTacticalField, oldDoublePassTarget, baseAiFrame);
	}
}
