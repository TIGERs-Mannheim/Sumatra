/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
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
	@Configurable(comment = "Number of Supporter attracted by Ball", defValue = "2")
	private static int numberSupporterAffectedByBallForce = 2;
	
	// Sigmas
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallAttraction = 3000;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaBallReceiverForward = 3000;
	
	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "4000.0")
	private static double magnitudeBallAttraction = 4000;
	@Configurable(comment = "[mm]", defValue = "-3000.0")
	private static double magnitudeBallReceiverForward = -3000;
	@Configurable(comment = "[mm]", defValue = "250")
	private static double magnitudeOffensiveSightAttraction = 250;
	
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
		boolean passReceiver = ((SupportRole) getRole()).getCurrentSupportBots().stream()
				.sorted(Comparator.comparingDouble(b -> b.getPos().distanceToSqr(getRole().getBall().getPos())))
				.limit(numberSupporterAffectedByBallForce)
				.anyMatch(b -> b.getBotId() == getRole().getBotID());
		
		if (passReceiver && isActive)
		{
			return 1;
		}
		return 0;
	}
	
	
	@Override
	List<Force> collectForces(final ITrackedBot affectedBot, final List<ITrackedBot> supporter,
			final List<ITrackedBot> opponents)
	{
		List<Force> forces = new ArrayList<>();
        forces.addAll(getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelFromTeamBot(supporter, affectedBot));
		forces.addAll(getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(getForceRepelFromPassLine(affectedBot));
		
		forces.add(getForceStayInsideField(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getForceDesiredPassDistance());
		forces.add(getForceMoveForward(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getOffensiveSightForce());

		return forces;
		
	}
	
	
	private Force getForceMoveForward(ITrackedBot affectedBot)
	{
		return new Force(Vector2.fromXY(Geometry.getGoalOur().getCenter().x(), affectedBot.getPos().y()),
				sigmaBallReceiverForward, magnitudeBallReceiverForward);
	}
	
	
	private Force getForceDesiredPassDistance()
	{
		return new Force(getRole().getBall().getPos(), sigmaBallAttraction, magnitudeBallAttraction,
				radiusMeanBallDistance, true);
	}

	private Force getOffensiveSightForce(){

		Optional<BotID> offensiveBot = getRole().getAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		if(offensiveBot.isPresent() && getRole().getAiFrame().getTacticalField().getSkirmishInformation().isSkirmishDetected()){
			ITrackedBot bot = getRole().getWFrame().getBot(offensiveBot.get());
			IVector2 sweetSpot = bot.getPos().addNew(Vector2.fromAngle(bot.getOrientation()).scaleTo(radiusMeanBallDistance));
			sweetSpot = setPositionInsideAllowedArea(sweetSpot);
			return new Force(sweetSpot, magnitudeOffensiveSightAttraction, Force.DistanceFunction.CONSTANT);
		}
		return Force.dummy();
	}

	
	@Override
	boolean isDrawing()
	{
		return isDrawing;
	}
	
}
