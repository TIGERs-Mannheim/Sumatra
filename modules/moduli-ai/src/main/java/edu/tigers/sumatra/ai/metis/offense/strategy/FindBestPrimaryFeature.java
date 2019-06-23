/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * find the best offensive robot to interact with the ball
 */
public class FindBestPrimaryFeature extends AOffensiveStrategyFeature
{
	@Configurable(defValue = "0.6")
	private static double ballContactTimeHysteresis = 0.6;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", FindBestPrimaryFeature.class);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		findBestPrimary(newTacticalField).ifPresent(strategy::setAttackerBot);
	}
	
	
	private Optional<BotID> findBestPrimary(final TacticalField newTacticalField)
	{
		if (newTacticalField.getGameState().isStoppedGame())
		{
			Set<BotID> potentialBots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, getAiFrame()).keySet();
			final Optional<BotDistance> closest = newTacticalField.getTigersToBallDist()
					.stream()
					.filter(d -> potentialBots.contains(d.getBot().getBotId()))
					.findFirst();
			
			if (!closest.isPresent())
			{
				return Optional.empty();
			}
			
			BotID currentAttacker = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.KEEP_DIST_TO_BALL)
					.stream()
					.findAny().map(ARole::getBotID).orElse(BotID.noBot());
			ITrackedBot attacker = getWFrame().getBot(currentAttacker);
			if (attacker != null
					&& potentialBots.contains(currentAttacker)
					&& attacker.getPos().distanceTo(getBall().getPos()) < closest.get().getDist() + 500)
			{
				return Optional.of(currentAttacker);
			}
			return Optional.of(closest.get().getBot().getBotId());
		}
		
		if (OffensiveMath.isKeeperInsane(getBall(), getAiFrame().getGamestate()))
		{
			return Optional.of(getAiFrame().getKeeperId());
		}
		
		final Map<BotID, BallInterception> ballInterceptions = newTacticalField.getBallInterceptions();
		BallInterception bestBallInterception = findFastestInterceptableInterception(ballInterceptions)
				.orElseGet(() -> findFurthestInterception(ballInterceptions).orElse(null));
		
		if (bestBallInterception == null)
		{
			return Optional.empty();
		}
		
		return Optional.of(
				chooseCurrentRoleOverNewBot(ballInterceptions, bestBallInterception,
						ERole.ATTACKER, ERole.DELAYED_ATTACK, ERole.FREE_SKIRMISH, ERole.OPPONENT_INTERCEPTION)
								.orElseGet(
										() -> chooseCurrentRoleOverNewBot(ballInterceptions, bestBallInterception,
												ERole.PASS_RECEIVER)
														.orElse(bestBallInterception.getBotID())));
	}
	
	
	private Optional<BotID> chooseCurrentRoleOverNewBot(final Map<BotID, BallInterception> ballInterceptions,
			final BallInterception bestBallInterception,
			final ERole... role)
	{
		BotID currentRole = currentRole(role).orElse(BotID.noBot());
		BallInterception currentRoleBallInterception = ballInterceptions.get(currentRole);
		if (currentRoleBallInterception != null
				&& currentRoleBallInterception.isInterceptable())
		{
			double absDist = Math.abs(currentRoleBallInterception.getBallContactTime()
					- bestBallInterception.getBallContactTime());
			if (absDist < ballContactTimeHysteresis)
			{
				return Optional.of(currentRole);
			}
		}
		return Optional.empty();
	}
	
	
	private Optional<BotID> currentRole(ERole[] role)
	{
		return getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(role)
				.stream()
				.findAny()
				.map(ARole::getBotID);
	}
	
	
	private Optional<BallInterception> findFurthestInterception(final Map<BotID, BallInterception> ballInterceptionMap)
	{
		return ballInterceptionMap
				.values()
				.stream()
				.min(Comparator.comparing(BallInterception::getBallContactTime));
	}
	
	
	private Optional<BallInterception> findFastestInterceptableInterception(
			final Map<BotID, BallInterception> ballInterceptionMap)
	{
		return ballInterceptionMap
				.values()
				.stream()
				.filter(BallInterception::isInterceptable)
				.min(Comparator.comparing(BallInterception::getBallContactTime));
	}
}
