/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This behavior aims to support the offensive such that it has an possible pass receiver all the time.
 * The number ob supporter selecting this behavior is restricted.
 * In general, this behavior drives the supporter to a certain distance to the ball and tries to NOT be covered from
 * opponents.
 */
public class RepulsivePassReceiver extends ARepulsiveBehavior
{
	
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean isActive = true;
	@Configurable(comment = "Whether force field should be drawn or not", defValue = "false")
	private static boolean isDrawing = false;
	
	
	@Configurable(comment = "Optimal pass distance", defValue = "3500.0")
	private static double radiusMeanBallDistance = 3500;
	@Configurable(comment = "Minimum Number of Supporter attracted by Ball", defValue = "1")
	private static int numberPassReceiversAtTheirGoal = 1;
	@Configurable(comment = "Maximum Number of Supporter attracted by Ball", defValue = "3")
	private static int numberPassReceiversAtOwnGoal = 3;
	@Configurable(comment = "Hysteresis offset for maximum number of PassReceivers", defValue = "1000.")
	private static double hysteresisOffsetPassReceivers = 1000d;
	private int lastCalculatedNumberPassReceivers = -2;
	
	// Sigmas
	@Configurable(comment = "[mm]", defValue = "1250.0")
	private static double sigmaTeamBot = 1250;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallAttraction = 3000;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallReceiverForward = 3000;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaBehindFoeLine = 400;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaFreeLine = 400;
	
	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "-1000.0")
	private static double magnitudeTeamBot = -1000;
	@Configurable(comment = "[mm]", defValue = "4000.0")
	private static double magnitudeBallAttraction = 4000;
	@Configurable(comment = "[mm]", defValue = "-3000.0")
	private static double magnitudeBallReceiverForward = -3000;
	@Configurable(comment = "[mm]", defValue = "250.0")
	private static double magnitudeOffensiveSightAttraction = 250;
	@Configurable(comment = "[mm]", defValue = "-1000.0")
	private static double magnitudeBehindFoeLine = -1000;
	@Configurable(comment = "[mm]", defValue = "2500.0")
	private static double magnitudeFreeLine = 2500;
	@Configurable(comment = "degree", defValue = "30.0")
	private static double minAngleForFreeLine = 30;
	
	// Stores History of RepulsivePassReceivers
	private static Map<BotID, CalcViabilityInfo> botActiveCalcHistory = new HashMap<>();
	
	@Configurable(comment = "half-life period for Score Rating [ms]", defValue = "1000.0")
	private static double halflifeperiodScoreRating = 1000;
	
	private long calcTimestamp;
	
	@Configurable(comment = "Hysteresis offset for active Pass Receiver", defValue = "2000.0")
	private double activePassReceiverOffset = 2000;
	
	private int idx = 1;
	
	static
	{
		ConfigRegistration.registerClass("roles", RepulsivePassReceiver.class);
	}
	
	
	public RepulsivePassReceiver(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public double calculateViability()
	{
		int numberPassReceiver = getNumberPassReceiver();
		
		boolean passReceiver = ((SupportRole) getRole()).getCurrentSupportBots().stream()
				.sorted(Comparator.comparingDouble(this::getViabilityRatingForBot))
				.limit(numberPassReceiver)
				.anyMatch(bot -> bot.getBotId() == getRole().getBotID());
		
		getRole().getWFrame().getBots().values().stream()
				.filter(bot -> !((SupportRole) getRole()).getCurrentSupportBots().contains(bot))
				.forEach(bot -> botActiveCalcHistory.put(bot.getBotId(), new CalcViabilityInfo(false)));
		
		long deltaTime = calcDeltaTime();
		
		if (botActiveCalcHistory.get(getRole().getBotID()) == null)
		{
			botActiveCalcHistory.put(getRole().getBotID(), new CalcViabilityInfo(passReceiver));
		}
		
		// Refresh History rating factors
		botActiveCalcHistory.forEach((bot, info) -> info.nextCalcDone(deltaTime));
		
		if (passReceiver && isActive)
		{
			if ((botActiveCalcHistory.get(getRole().getBotID()) != null) &&
					!botActiveCalcHistory.get(getRole().getBotID()).isActive)
			{
				botActiveCalcHistory.put(getRole().getBotID(), new CalcViabilityInfo(true));
			}
			
			return 1;
		} else
		{
			if ((botActiveCalcHistory.get(getRole().getBotID()) != null) &&
					botActiveCalcHistory.get(getRole().getBotID()).isActive)
			{
				botActiveCalcHistory.put(getRole().getBotID(), new CalcViabilityInfo(false));
			}
			
			return 0;
		}
	}
	
	
	private int getNumberPassReceiver()
	{
		int amountAvailableSupporter = ((SupportRole) getRole()).getCurrentSupportBots().size();
		int numberPassReceiver;
		
		if (amountAvailableSupporter <= 1)
		{
			numberPassReceiver = amountAvailableSupporter;
		} else // More then one supporter available
		{
			int amountFieldParts = (numberPassReceiversAtOwnGoal - numberPassReceiversAtTheirGoal) + 1;
			double distanceToBoundary = (getRole().getBall().getPos().x() + (Geometry.getFieldLength() * 0.5d));
			double fieldPartLength = (Geometry.getFieldLength() / amountFieldParts);
			
			
			numberPassReceiver = numberPassReceiversAtOwnGoal -
					(int) (distanceToBoundary / fieldPartLength);
			
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
				numberPassReceiver = amountAvailableSupporter - 1; // One supporter should be attacker
			}
		}
		return numberPassReceiver;
	}
	
	
	private long calcDeltaTime()
	{
		long nowTimestamp = getRole().getWFrame().getTimestamp();
		long deltaTime = nowTimestamp - calcTimestamp;
		calcTimestamp = nowTimestamp;
		return deltaTime;
	}
	
	
	/**
	 * Calculates a viability score for a bot depending on distances and history of last times calculation.
	 * Higher values stand for a worse rating then lower ones. (best Value = 0)
	 * 
	 * @param bot
	 * @return Rating
	 */
	private double getViabilityRatingForBot(final ITrackedBot bot)
	{
		double score = Geometry.getFieldLength() * 0.5d;
		
		score += bot.getPos().x();
		
		CalcViabilityInfo botViabilityHistory = botActiveCalcHistory.get(bot.getBotId());
		
		if ((botViabilityHistory == null) || !botViabilityHistory.isActive)
		{
			score += activePassReceiverOffset;
		}
		
		if (botViabilityHistory != null)
		{
			score = score * botViabilityHistory.getRatingFactor();
		}
		
		return score;
		
	}
	
	
	@Override
	public boolean getIsActive()
	{
		return isActive;
	}
	
	
	@Override
	List<Force> collectForces(final ITrackedBot affectedBot, final List<ITrackedBot> supporter,
			final List<ITrackedBot> opponents)
	{
		double distToBall = getRole().getPos().distanceToSqr(getRole().getBall().getPos());
		idx = getRole().getAiFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT).stream()
				.filter(b -> getRole().getBall().getPos().distanceToSqr(b.getPos()) < distToBall)
				.collect(Collectors.toList()).size() + 1;
		
		List<Force> forces = new ArrayList<>();
		forces.addAll(getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelLinesBehindOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot));
		forces.addAll(getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(getForceRepelFromPassLine(affectedBot));
		
		forces.add(getForceStayInsideField(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getForceDesiredPassDistance());
		forces.add(getForceMoveForward(affectedBot));
		forces.add(getOffensiveSightForce(affectedBot.getPos()));
		
		forces.addAll(getForceForFreeLinesFromAttacker(affectedBot));
		
		return forces;
		
	}
	
	
	private Force getForceMoveForward(final ITrackedBot affectedBot)
	{
		return new Force(Vector2.fromXY(Geometry.getGoalOur().getCenter().x(), affectedBot.getPos().y()),
				sigmaBallReceiverForward, magnitudeBallReceiverForward);
	}
	
	
	private Force getForceDesiredPassDistance()
	{
		
		return new Force(getRole().getBall().getPos(), sigmaBallAttraction, magnitudeBallAttraction,
				radiusMeanBallDistance * idx, true);
		
	}
	
	
	private Force getOffensiveSightForce(final IVector2 currentPos)
	{
		
		Optional<BotID> offensiveBot = getRole().getAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		if (offensiveBot.isPresent()
				&& getRole().getAiFrame().getTacticalField().getSkirmishInformation().isSkirmishDetected())
		{
			ITrackedBot bot = getRole().getWFrame().getBot(offensiveBot.get());
			IVector2 sweetSpot = bot.getPos()
					.addNew(Vector2.fromAngle(bot.getOrientation()).scaleTo(radiusMeanBallDistance));
			sweetSpot = setPositionInsideAllowedArea(currentPos, sweetSpot);
			return new Force(sweetSpot, magnitudeOffensiveSightAttraction, Force.DistanceFunction.CONSTANT);
		}
		return Force.dummy();
	}
	
	
	protected List<Force> getForceRepelLinesBehindOpponentBot(final List<ITrackedBot> opponents,
			final ITrackedBot affectedBot)
	{
		List<Force> forces = new ArrayList<>();
		
		// Rays Ball - Opponent extension
		for (ITrackedBot opponent : opponents)
		{
			IVector2 lineDirVector = Vector2.fromPoints(getRole().getBall().getPos(), opponent.getPos());
			IHalfLine passSegment = Lines.halfLineFromDirection(opponent.getPos(), lineDirVector);
			IVector2 referencePoint = passSegment.closestPointOnLine(affectedBot.getPos());
			forces.add(new Force(referencePoint, sigmaBehindFoeLine, magnitudeBehindFoeLine));
		}
		
		return forces;
	}
	
	
	protected List<Force> getForceForFreeLinesFromAttacker(final ITrackedBot affectedBot)
	{
		List<Force> forces = new ArrayList<>();
		List<IArc> arcsFreeOfOpponents = getRole().getAiFrame().getTacticalField().getOffensiveShadows().stream()
				.filter(arc -> arc.getRotation() > AngleMath.deg2rad(minAngleForFreeLine))
				.collect(Collectors.toList());
		
		for (IArc arc : arcsFreeOfOpponents)
		{
			double angleBisector = arc.getStartAngle() + (arc.getRotation() * 0.5d);
			IVector2 lineDirection = Vector2.fromAngle(angleBisector);
			IHalfLine freeLine = Lines.halfLineFromDirection(arc.center(), lineDirection);
			IVector2 referencePoint = freeLine.closestPointOnLine(affectedBot.getPos());
			forces.add(new Force(referencePoint, sigmaFreeLine, magnitudeFreeLine));
		}
		
		return forces;
	}
	
	
	@Override
	boolean isDrawing()
	{
		return isDrawing;
	}
	
	
	/**
	 * Datatyp for storing last Viability Calculations
	 * and a rating Factory to scale the rating for bots
	 */
	private class CalcViabilityInfo
	{
		boolean isActive;
		double ratingFactor;
		
		
		CalcViabilityInfo(final boolean isActive)
		{
			this.isActive = isActive;
			
			if (isActive)
			{
				ratingFactor = 0d;
			} else
			{
				ratingFactor = 1d;
			}
		}
		
		
		public void nextCalcDone(final long deltaTime)
		{
			double complement = 1 - ratingFactor;
			
			complement = complement * Math.pow(0.5, deltaTime / (halflifeperiodScoreRating * 10e6d));
			
			ratingFactor = 1 - complement;
		}
		
		
		public double getRatingFactor()
		{
			return ratingFactor;
		}
		
		
		@Override
		public String toString()
		{
			return isActive + " " + ratingFactor;
		}
		
	}
	
}
