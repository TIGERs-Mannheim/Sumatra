/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
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

	@Configurable(comment = "Optimal pass distance", defValue = "2000.0")
	private static double radiusMeanBallDistance = 2000.0;
	@Configurable(comment = "Minimum Number of Supporter attracted by Ball", defValue = "1")
	private static int numberPassReceiversAtTheirGoal = 1;
	@Configurable(comment = "Maximum Number of Supporter attracted by Ball", defValue = "3")
	private static int numberPassReceiversAtOwnGoal = 3;
	@Configurable(comment = "Hysteresis offset for maximum number of PassReceivers", defValue = "1000.")
	private static double hysteresisOffsetPassReceivers = 1000d;
	// Sigmas
	@Configurable(comment = "[mm]", defValue = "1500.0")
	private static double sigmaTeamBot = 1500;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallAttraction = 3000;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallReceiverForward = 3000;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaBehindOpponentLine = 400;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaFreeLine = 400;
	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "-1500.0")
	private static double magnitudeTeamBot = -1500;
	@Configurable(comment = "[mm]", defValue = "2000.0")
	private static double magnitudeBallAttraction = 2000;
	@Configurable(comment = "[mm]", defValue = "-5000.0")
	private static double magnitudeBallReceiverForward = -5000.0;
	@Configurable(comment = "[mm]", defValue = "250.0")
	private static double magnitudeOffensiveSightAttraction = 250;
	@Configurable(comment = "[mm]", defValue = "-1000.0")
	private static double magnitudeBehindOpponentLine = -1000;
	@Configurable(comment = "[mm]", defValue = "2500.0")
	private static double magnitudeFreeLine = 2500;
	@Configurable(comment = "degree", defValue = "30.0")
	private static double minAngleForFreeLine = 30;
	@Configurable(comment = "half-life period for Score Rating [s]", defValue = "1.0")
	private static double halfLifePeriodScoreRating = 1;

	@Configurable(comment = "Hysteresis offset for inactive Pass Receiver", defValue = "2000.0")
	private static double inactivePassReceiverOffset = 2000;

	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<List<IArc>> offensiveShadows;

	private final Map<BotID, CalcViabilityInfo> viabilityHistory = new HashMap<>();
	private int lastCalculatedNumberPassReceivers = -2;


	public PassReceiverRepulsiveBehavior(
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions,
			Supplier<OffensiveStrategy> offensiveStrategy,
			Supplier<SkirmishInformation> skirmishInformation,
			Supplier<List<IArc>> offensiveShadows
	)
	{
		super(Color.cyan, desiredBots, offensiveActions);
		this.desiredBots = desiredBots;
		this.offensiveStrategy = offensiveStrategy;
		this.skirmishInformation = skirmishInformation;
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

		lastCalculatedNumberPassReceivers = numberPassReceiver;

		if (numberPassReceiver >= amountAvailableSupporter)
		{
			return amountAvailableSupporter - 1; // One supporter should be attacker
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
		double distToBallSqr = affectedBot.getPos().distanceToSqr(getWFrame().getBall().getPos());
		int idx = (int) desiredBots.get().get(EPlay.SUPPORT).stream()
				.map(id -> getWFrame().getBot(id))
				.filter(bot -> getWFrame().getBall().getPos().distanceToSqr(bot.getPos()) < distToBallSqr)
				.count() + 1;

		List<Force> forces = new ArrayList<>();
		forces.addAll(getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelLinesBehindOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot));
		forces.addAll(getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(getForceRepelFromPassLine(affectedBot));

		forces.add(getForceStayInsideField(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getForceDesiredPassDistance(idx));
		forces.add(getForceMoveForward(affectedBot));
		forces.add(getOffensiveSightForce(affectedBot.getPos()));

		forces.addAll(getForceForFreeLinesFromAttacker(affectedBot));

		return forces;
	}


	private Force getForceMoveForward(final BotState affectedBot)
	{
		Vector2 position = Vector2.fromXY(Geometry.getGoalOur().getCenter().x(), affectedBot.getPos().y());
		return new Force(position, sigmaBallReceiverForward, magnitudeBallReceiverForward);
	}


	private Force getForceDesiredPassDistance(int idx)
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		double mean = radiusMeanBallDistance * idx;
		return new Force(ballPos, sigmaBallAttraction, magnitudeBallAttraction, mean, true);
	}


	private Force getOffensiveSightForce(final IVector2 currentPos)
	{
		if (skirmishInformation.get().getStrategy() == ESkirmishStrategy.NONE)
		{
			return Force.dummy();
		}

		return offensiveStrategy.get().getAttackerBot()
				.map(id -> getWFrame().getBot(id))
				.map(bot -> forceForOffensiveBot(currentPos, bot))
				.orElseGet(Force::dummy);
	}


	private Force forceForOffensiveBot(IVector2 currentPos, ITrackedBot offensiveBot)
	{
		Vector2 direction = Vector2.fromAngle(offensiveBot.getOrientation()).scaleTo(radiusMeanBallDistance);
		IVector2 sweetSpot = setPositionInsideAllowedArea(currentPos, offensiveBot.getPos().addNew(direction));
		return new Force(sweetSpot, magnitudeOffensiveSightAttraction, DistanceFunction.CONSTANT);
	}


	private List<Force> getForceRepelLinesBehindOpponentBot(Collection<ITrackedBot> opponents, BotState affectedBot)
	{
		return opponents.stream()
				.map(bot -> getForceForOpponent(affectedBot, bot))
				.toList();
	}


	private Force getForceForOpponent(BotState affectedBot, ITrackedBot opponent)
	{
		IVector2 lineDirVector = Vector2.fromPoints(getWFrame().getBall().getPos(), opponent.getPos());
		IHalfLine passSegment = Lines.halfLineFromDirection(opponent.getPos(), lineDirVector);
		IVector2 referencePoint = passSegment.closestPointOnPath(affectedBot.getPos());
		return new Force(referencePoint, sigmaBehindOpponentLine, magnitudeBehindOpponentLine);
	}


	private List<Force> getForceForFreeLinesFromAttacker(final BotState affectedBot)
	{
		return offensiveShadows.get().stream()
				.filter(arc -> arc.getRotation() > AngleMath.deg2rad(minAngleForFreeLine))
				.map(arc -> getForceForArc(affectedBot, arc))
				.toList();
	}


	private Force getForceForArc(BotState affectedBot, IArc arc)
	{
		double angleBisector = arc.getStartAngle() + (arc.getRotation() * 0.5d);
		IVector2 lineDirection = Vector2.fromAngle(angleBisector);
		IHalfLine freeLine = Lines.halfLineFromDirection(arc.center(), lineDirection);
		IVector2 referencePoint = freeLine.closestPointOnPath(affectedBot.getPos());
		return new Force(referencePoint, sigmaFreeLine, magnitudeFreeLine);
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

