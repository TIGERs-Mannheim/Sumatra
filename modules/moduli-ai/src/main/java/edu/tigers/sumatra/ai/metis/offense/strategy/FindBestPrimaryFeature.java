/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * find the best offensive robot to interact with the ball
 */
public class FindBestPrimaryFeature extends AOffensiveStrategyFeature
{
	@Configurable(defValue = "0.6")
	private static double ballContactTimeHysteresis = 0.6;
	
	@Configurable(defValue = "1.5")
	private static double maxActivePassTargetAge = 1.5;
	
	@Configurable(defValue = "1.0")
	private static double maxActivePassTargetBonusTime = 1.0;
	
	@Configurable(defValue = "0.3", comment = "time in [s]")
	private static double addSecondOffensiveThresh = 0.3;
	
	@Configurable(defValue = "300.0")
	private static double distThresholdForNoSecondPrimaryOffensive = 300.0;
	
	@Configurable(defValue = "true")
	private static boolean assignAttacker = true;
	
	private Map<BotID, TimedPassTarget> timeOfLastActivePassTarget = new HashMap<>();
	private Map<BotID, TimedPassTarget> candidateTimeOfLastActivePassTarget = new HashMap<>();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		if (!newTacticalField.getAiInfoFromPrevFrame().getActivePassTargets().isEmpty())
		{
			candidateTimeOfLastActivePassTarget.clear();
			newTacticalField.getAiInfoFromPrevFrame().getActivePassTargets()
					.forEach(p -> candidateTimeOfLastActivePassTarget.put(p.getBotId(),
							new TimedPassTarget(getWFrame().getTimestamp(), p.getDynamicPos())));
			timeOfLastActivePassTarget.clear();
		} else
		{
			timeOfLastActivePassTarget.putAll(candidateTimeOfLastActivePassTarget);
			candidateTimeOfLastActivePassTarget.clear();
		}
		
		timeOfLastActivePassTarget.keySet().removeIf(b -> ageOfLastPassTarget(b) > maxActivePassTargetAge);
		timeOfLastActivePassTarget.keySet().removeIf(b -> !getWFrame().getBots().keySet().contains(b));
		
		clearPassTargetBonusIfBallIsSlow();
		
		final List<IDrawableShape> shapes = getTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.OFFENSIVE_FINDER);
		for (BotID botID : timeOfLastActivePassTarget.keySet())
		{
			ITrackedBot bot = getWFrame().getBot(botID);
			double age = ageOfLastPassTarget(bot.getBotId());
			String text = String.format("%.2f|%.2f", age, getPassTargetBonus(bot.getBotId()));
			shapes.add(new DrawableAnnotation(bot.getPos(), text, Color.magenta)
					.withCenterHorizontally(true)
					.withOffset(Vector2.fromY(160)));
		}
		
		if (assignAttacker
				&& newTacticalField.getBallResponsibility() == EBallResponsibility.OFFENSE)
		{
			List<BotID> primaries = findBestPrimaries(newTacticalField);
			if (!primaries.isEmpty())
			{
				strategy.setAttackerBot(primaries.get(0));
			}
			if (primaries.size() > 1 && newTacticalField.getGameState().isGameRunning())
			{
				assert primaries.get(0) != primaries.get(1);
				strategy.addDesiredBot(primaries.get(1));
			}
		}
	}
	
	
	private void clearPassTargetBonusIfBallIsSlow()
	{
		if (getWFrame().getBall().getVel().getLength2() < 0.5)
		{
			timeOfLastActivePassTarget.keySet().removeIf(b -> ageOfLastPassTarget(b) > 0.2);
		} else
		{
			timeOfLastActivePassTarget.keySet()
					.removeIf(
							b -> !getBall().getTrajectory().getTravelLine().isPointInFront(getWFrame().getBot(b).getPos()));
		}
	}
	
	
	private double ageOfLastPassTarget(final BotID botID)
	{
		TimedPassTarget timedPassTarget = timeOfLastActivePassTarget.get(botID);
		return ageOfLastPassTarget(timedPassTarget);
	}
	
	
	private double ageOfLastPassTarget(final TimedPassTarget timedPassTarget)
	{
		if (timedPassTarget == null)
		{
			return maxActivePassTargetAge;
		}
		return (getWFrame().getTimestamp() - timedPassTarget.timestamp) / 1e9;
	}
	
	
	private List<BotID> findBestPrimaries(final TacticalField newTacticalField)
	{
		Set<BotID> potentialBots = newTacticalField.getPotentialOffensiveBots();
		if (newTacticalField.getGameState().isStoppedGame())
		{
			return getPrimariesDuringStop(newTacticalField, potentialBots);
		}
		
		if (newTacticalField.isInsaneKeeper())
		{
			return Collections.singletonList(getAiFrame().getKeeperId());
		}
		
		final Map<BotID, BallInterception> ballInterceptions = new HashMap<>(newTacticalField.getBallInterceptions());
		ballInterceptions.keySet().removeIf(id -> !potentialBots.contains(id));
		List<BallInterception> bestBallInterceptionsSorted = findFastestInterceptableInterception(ballInterceptions);
		
		if (bestBallInterceptionsSorted.isEmpty())
		{
			bestBallInterceptionsSorted = findFurthestInterception(ballInterceptions);
		}
		
		if (bestBallInterceptionsSorted.isEmpty())
		{
			return Collections.emptyList();
		}
		
		return getBestPrimaries(ballInterceptions, bestBallInterceptionsSorted);
	}
	
	
	private List<BotID> getBestPrimaries(final Map<BotID, BallInterception> ballInterceptions,
			final List<BallInterception> bestBallInterceptionsSorted)
	{
		List<BotID> primaries = new ArrayList<>();
		boolean canAnyoneIntercept = ballInterceptions.values().stream().anyMatch(BallInterception::isInterceptable);
		Optional<BotID> bestPrimary = Optional.empty();
		Optional<BotID> secondBestPrimary = Optional.empty();
		List<BotID> ignoredBots = new ArrayList<>();
		for (int i = 0; i < Math.min(2, bestBallInterceptionsSorted.size()); i++)
		{
			Optional<BotID> bestBot;
			if (i > 0)
			{
				bestBot = Optional.of(bestBallInterceptionsSorted.get(i).getBotID());
			} else
			{
				bestBot = chooseCurrentRoleOverNewBot(ballInterceptions, bestBallInterceptionsSorted.get(i),
						canAnyoneIntercept, ignoredBots,
						ERole.ATTACKER, ERole.DELAYED_ATTACK, ERole.FREE_SKIRMISH, ERole.OPPONENT_INTERCEPTION);
			}
			
			if (!bestBot.isPresent())
			{
				bestBot = chooseCurrentRoleOverNewBot(ballInterceptions, bestBallInterceptionsSorted.get(i),
						canAnyoneIntercept, ignoredBots, ERole.PASS_RECEIVER);
			}
			
			if (!bestBot.isPresent())
			{
				bestBot = Optional.of(bestBallInterceptionsSorted.get(i).getBotID());
			}
			
			ignoredBots.add(bestBot.get());
			
			if (i == 0)
			{
				bestPrimary = bestBot;
			} else
			{
				secondBestPrimary = bestBot;
			}
		}
		
		bestPrimary.ifPresent(primaries::add);
		if (bestPrimary.isPresent() && secondBestPrimary.isPresent() && secondBestPrimary.get() != bestPrimary.get())
		{
			addSecondPrimaryIfReasonable(ballInterceptions, bestPrimary.get(),
					secondBestPrimary.get()).ifPresent(primaries::add);
		}
		return primaries;
	}
	
	
	private Optional<BotID> addSecondPrimaryIfReasonable(final Map<BotID, BallInterception> ballInterceptions,
			final BotID bestPrimary,
			final BotID secondBestPrimary)
	{
		BallInterception bestBotBallInterception = ballInterceptions.get(bestPrimary);
		BallInterception secondBestBotBallInterception = ballInterceptions.get(secondBestPrimary);
		
		IVector2 bestBotPos = getWFrame().getTigerBotsAvailable().getWithNull(bestPrimary).getPos();
		IVector2 secondBestBotPos = getWFrame().getTigerBotsAvailable().getWithNull(secondBestPrimary).getPos();

		if (bestBotBallInterception.getBotTarget() == null || secondBestBotBallInterception.getBotTarget() == null)
		{
			return Optional.empty();
		}

		// negative slack time means that robot reaches the intercept pos before the ball
		double firstPrimarySlackTime = getWFrame().getBall().getTrajectory()
				.getTimeByPos(bestBotBallInterception.getBotTarget())
				- bestBotBallInterception.getBallContactTime();
		
		double secondPrimarySlackTime = getWFrame().getBall().getTrajectory()
				.getTimeByPos(secondBestBotBallInterception.getBotTarget())
				- secondBestBotBallInterception.getBallContactTime();
		
		boolean isSecondPrimary = Math.abs(firstPrimarySlackTime - secondPrimarySlackTime) < addSecondOffensiveThresh
				&& bestBotPos.distanceTo(secondBestBotPos) > distThresholdForNoSecondPrimaryOffensive
				&& bestBotBallInterception.getBallContactTime() > 0.3;
		if (isSecondPrimary)
		{
			return Optional.of(secondBestPrimary);
		}
		return Optional.empty();
	}
	
	
	private List<BotID> getPrimariesDuringStop(final TacticalField newTacticalField, final Set<BotID> potentialBots)
	{
		List<BotID> primaries = new ArrayList<>();
		final Optional<BotDistance> closest = newTacticalField.getTigersToBallDist()
				.stream()
				.filter(d -> potentialBots.contains(d.getBot().getBotId()))
				.findFirst();
		
		if (!closest.isPresent())
		{
			return primaries;
		}
		
		BotID currentAttacker = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.KEEP_DIST_TO_BALL)
				.stream()
				.findAny().map(ARole::getBotID).orElse(BotID.noBot());
		ITrackedBot attacker = getWFrame().getBot(currentAttacker);
		if ((attacker != null)
				&& potentialBots.contains(currentAttacker)
				&& (attacker.getPos().distanceTo(getBall().getPos()) < (closest.get().getDist() + 500)))
		{
			primaries.add(currentAttacker);
			return primaries;
		}
		primaries.add(closest.get().getBot().getBotId());
		return primaries;
	}
	
	
	private Optional<BotID> chooseCurrentRoleOverNewBot(
			final Map<BotID, BallInterception> ballInterceptions,
			final BallInterception bestBallInterception,
			final boolean canAnyoneIntercept,
			List<BotID> ignoredCurrentRoles,
			final ERole... role)
	{
		BotID currentRole;
		
		List<BotID> roles = currentRoles(role);
		roles.removeAll(ignoredCurrentRoles);
		
		// chose fastest role
		currentRole = roles.stream()
				.min(Comparator.comparing(i -> ballInterceptions.get(i).getBallContactTime())).orElse(BotID.noBot());
		
		BallInterception currentRoleBallInterception = ballInterceptions.get(currentRole);
		if ((currentRoleBallInterception != null)
				&& (currentRoleBallInterception.isInterceptable() || !canAnyoneIntercept))
		{
			double currentBallContactTime = currentRoleBallInterception.getBallContactTime()
					- getPassTargetBonus(currentRole);
			double bestBallContactTime = bestBallInterception.getBallContactTime()
					- getPassTargetBonus(bestBallInterception.getBotID());
			double timeThatBestIsFasterThanCurrent = currentBallContactTime - bestBallContactTime;
			
			if (timeThatBestIsFasterThanCurrent < ballContactTimeHysteresis)
			{
				return Optional.of(currentRole);
			}
		}
		return Optional.empty();
	}
	
	
	private double getPassTargetBonus(final BotID currentRole)
	{
		return (1 - SumatraMath.relative(
				ageOfLastPassTarget(currentRole),
				0,
				maxActivePassTargetAge))
				* maxActivePassTargetBonusTime;
	}
	
	
	private List<BotID> currentRoles(final ERole[] role)
	{
		return getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(role)
				.stream()
				.map(ARole::getBotID)
				.collect(Collectors.toList());
	}
	
	
	private List<BallInterception> findFurthestInterception(final Map<BotID, BallInterception> ballInterceptionMap)
	{
		return ballInterceptionMap
				.values()
				.stream()
				.sorted(Comparator.comparing(i -> i.getBallContactTime() - getPassTargetBonus(i.getBotID())))
				.collect(Collectors.toList());
	}
	
	
	private List<BallInterception> findFastestInterceptableInterception(
			final Map<BotID, BallInterception> ballInterceptionMap)
	{
		return ballInterceptionMap
				.values()
				.stream()
				.filter(b -> b.isInterceptable() || timeOfLastActivePassTarget.containsKey(b.getBotID()))
				.sorted(Comparator.comparing(i -> i.getBallContactTime() - getPassTargetBonus(i.getBotID())))
				.collect(Collectors.toList());
	}
	
	private static class TimedPassTarget
	{
		long timestamp;
		DynamicPosition target;
		
		
		public TimedPassTarget(final long timestamp, final DynamicPosition target)
		{
			this.timestamp = timestamp;
			this.target = target;
		}
	}
}
