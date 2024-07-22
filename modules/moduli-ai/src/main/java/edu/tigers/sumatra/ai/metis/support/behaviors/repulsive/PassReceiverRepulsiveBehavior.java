/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This behavior aims to support the offensive such that it has an possible pass receiver all the time.
 * The number of supporters selecting this behavior is restricted.
 * In general, this behavior drives the supporter to a certain distance to the ball and tries to NOT be covered from
 * opponents.
 */
public class PassReceiverRepulsiveBehavior extends ARepulsiveBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;
	@Configurable(comment = "Whether force field should be drawn or not", defValue = "false")
	private static boolean drawing = false;
	@Configurable(comment = "Minimum Number of Supporter attracted by Ball", defValue = "1")
	private static int numberPassReceiversAtTheirGoal = 1;
	@Configurable(comment = "Maximum Number of Supporter attracted by Ball", defValue = "3")
	private static int numberPassReceiversAtOwnGoal = 3;
	@Configurable(comment = "Hysteresis offset for maximum number of PassReceivers", defValue = "1000.")
	private static double hysteresisOffsetPassReceivers = 1000d;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallReceiverForward = 3000;
	@Configurable(comment = "[mm]", defValue = "-5000.0")
	private static double magnitudeBallReceiverForward = -5000.0;
	@Configurable(comment = "half-life period for Score Rating [s]", defValue = "1.0")
	private static double halfLifePeriodScoreRating = 1;

	@Configurable(comment = "Hysteresis offset for inactive Pass Receiver", defValue = "2000.0")
	private static double inactivePassReceiverOffset = 2000;

	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<List<IArc>> offensiveShadows;
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Map<BotID, CalcViabilityInfo> viabilityHistory = new HashMap<>();
	private int lastCalculatedNumberPassReceivers = -2;


	public PassReceiverRepulsiveBehavior(
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions,
			Supplier<List<IArc>> offensiveShadows
	)
	{
		super(Color.cyan, desiredBots);
		this.offensiveActions = offensiveActions;
		this.desiredBots = desiredBots;
		this.offensiveShadows = offensiveShadows;
	}


	@Override
	public double getViability(BotID botID)
	{
		var currentSupporters = desiredBots.get().get(EPlay.SUPPORT).stream()
				.map(id -> getWFrame().getBot(id))
				.collect(Collectors.toSet());
		int numberPassReceiver = getNumberPassReceiver(currentSupporters);

		boolean activate = currentSupporters.stream()
				.sorted(Comparator.comparingDouble(this::getInverseViabilityRatingForBot))
				.limit(numberPassReceiver)
				.anyMatch(bot -> bot.getBotId().equals(botID));

		if (activate)
		{
			viabilityHistory.computeIfAbsent(botID, id -> new CalcViabilityInfo()).update();
			return 1;
		}
		viabilityHistory.remove(botID);
		return 0;
	}


	private int getNumberPassReceiver(Set<ITrackedBot> currentSupporters)
	{
		int amountAvailableSupporter = currentSupporters.size();

		if (amountAvailableSupporter <= 1)
		{
			return amountAvailableSupporter;
		}

		// More then one supporter available
		int amountFieldParts = (numberPassReceiversAtOwnGoal - numberPassReceiversAtTheirGoal) + 1;
		double distanceToBoundary = (getWFrame().getBall().getPos().x() + (Geometry.getFieldLength() * 0.5d));
		distanceToBoundary = SumatraMath.cap(distanceToBoundary, 0, Geometry.getFieldLength());
		int numberPassReceiver = getNumberPassReceiver(amountFieldParts, distanceToBoundary);

		lastCalculatedNumberPassReceivers = numberPassReceiver;
		if (numberPassReceiver >= amountAvailableSupporter)
		{
			return amountAvailableSupporter - 1; // One supporter should be attacker
		}
		return numberPassReceiver;
	}


	private int getNumberPassReceiver(int amountFieldParts, double distanceToBoundary)
	{
		double fieldPartLength = (Geometry.getFieldLength() / amountFieldParts);

		int numberPassReceiver = numberPassReceiversAtOwnGoal - (int) (distanceToBoundary / fieldPartLength);

		// if hysteresisOffset is not transcended, lastCalculatedNumber is taken
		if ((lastCalculatedNumberPassReceivers == (numberPassReceiver + 1)) &&
				((distanceToBoundary
						- ((int) (distanceToBoundary / fieldPartLength)
						* fieldPartLength)) < hysteresisOffsetPassReceivers))
		{
			numberPassReceiver = lastCalculatedNumberPassReceivers;
		}
		if ((lastCalculatedNumberPassReceivers == (numberPassReceiver - 1)) &&
				((distanceToBoundary - (((int) (distanceToBoundary / fieldPartLength) + 1)
						* fieldPartLength)) > -hysteresisOffsetPassReceivers))
		{
			numberPassReceiver = lastCalculatedNumberPassReceivers;
		}
		return numberPassReceiver;
	}


	/**
	 * Calculates a viability score for a bot depending on distances and history of last times calculation.
	 * Higher values stand for a worse rating then lower ones. (best Value = 0)
	 *
	 * @param bot
	 * @return Rating
	 */
	private double getInverseViabilityRatingForBot(final ITrackedBot bot)
	{
		double baseScore = Geometry.getFieldLength() * 0.5 + bot.getPos().x();

		CalcViabilityInfo botViabilityHistory = viabilityHistory.get(bot.getBotId());

		if (botViabilityHistory == null)
		{
			return baseScore + inactivePassReceiverOffset;
		}
		return baseScore * botViabilityHistory.ratingFactor;
	}


	@Override
	List<Force> collectForces(
			BotState affectedBot,
			Collection<ITrackedBot> supporter,
			Collection<ITrackedBot> opponents
	)
	{
		RepulsivePassReceiverForceGenerator forceGenerator = new RepulsivePassReceiverForceGenerator(
				this.offensiveShadows.get(), this.offensiveActions.get(), desiredBots.get(), getWFrame());
		var forces = forceGenerator.getRepulsivePassReceiverForces(getWFrame(), affectedBot, supporter, opponents);
		forces.add(getForceMoveForward(affectedBot));
		return forces;
	}


	private Force getForceMoveForward(final BotState affectedBot)
	{
		Vector2 position = Vector2.fromXY(Geometry.getGoalOur().getCenter().x(), affectedBot.getPos().y());
		return new Force(position, sigmaBallReceiverForward, magnitudeBallReceiverForward);
	}


	/**
	 * Datatyp for storing last Viability Calculations
	 * and a factor to scale the rating for the bots
	 */
	public class CalcViabilityInfo
	{
		double ratingFactor;
		private long lastTimestamp;


		CalcViabilityInfo()
		{
			this.ratingFactor = 1;
		}


		public void update()
		{
			double dt = (getWFrame().getTimestamp() - lastTimestamp) / 1e9;
			lastTimestamp = getWFrame().getTimestamp();

			double complement = 1 - ratingFactor;
			double adaptedComplement = complement * Math.pow(0.5, dt / halfLifePeriodScoreRating);
			ratingFactor = 1 - adaptedComplement;
		}
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}


	@Override
	boolean isDrawing()
	{
		return drawing;
	}
}

