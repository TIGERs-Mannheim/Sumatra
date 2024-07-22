/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * This behaviour is one of the standard supportive behaviours and should bring the supporter to a good attacking
 * position without
 * interfering with each other.
 * As a ARepulsiveBehavior, it calculates the destinations through different forces emitted by different objects or
 * geometries.
 * In general, this behavior drives the supporter towards the goal, with respect to the opponents position.
 */
public class AttackerRepulsiveBehavior extends ARepulsiveBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "Whether force field should be drawn or not", defValue = "false")
	private static boolean drawing = false;

	// Force sigmas
	@Configurable(comment = "[mm]", defValue = "1000.0")
	private static double sigmaGoalRepel = 1000;
	@Configurable(comment = "[mm]", defValue = "800.0")
	private static double sigmaGoalAttraction = 200;
	@Configurable(comment = "[mm]", defValue = "800.0")
	private static double sigmaPenaltyAreaAttraction = 200;
	@Configurable(comment = "[mm]", defValue = "800.0")
	private static double sigmaAngleRangePoints = 200;

	// Force magnitudes
	@Configurable(comment = "[mm]", defValue = "-3000.0")
	private static double magnitudeGoalRepel = -3000;
	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double magnitudeGoalAttraction = 3000;
	@Configurable(comment = "[mm]", defValue = "2000.0")
	private static double magnitudePenaltyAreaAttraction = 2000;
	@Configurable(comment = "[mm]", defValue = "2000.0")
	private static double magnitudeAngleRangePoints = 2000;

	// special goal force variables
	@Configurable(comment = "The target circle of the supporters", defValue = "2500.0")
	private static double radiusGoal = 2500;

	private final Supplier<List<IVector2>> supportiveGoalPositions;
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<List<IArc>> offensiveShadows;
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;


	public AttackerRepulsiveBehavior(
			Supplier<List<IVector2>> supportiveGoalPositions,
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions,
			Supplier<List<IArc>> offensiveShadows
	)
	{
		super(Color.red, desiredBots);
		this.offensiveActions = offensiveActions;
		this.desiredBots = desiredBots;
		this.offensiveShadows = offensiveShadows;
		this.supportiveGoalPositions = supportiveGoalPositions;
	}


	@Override
	public double getViability(BotID botID)
	{
		return 1;
	}


	@Override
	protected List<Force> collectForces(
			BotState affectedBot,
			Collection<ITrackedBot> supporter,
			Collection<ITrackedBot> opponents
	)
	{
		var forceGenerator = new RepulsivePassReceiverForceGenerator(
				this.offensiveShadows.get(), this.offensiveActions.get(), this.desiredBots.get(), getWFrame());
		List<Force> forces = new ArrayList<>();
		forces.add(forceGenerator.getForceStayInsideField(affectedBot));
		forces.add(forceGenerator.getForceRepelFromBall());
		forces.add(getForceAttractToPenaltyArea(affectedBot));
		forces.addAll(forceGenerator.getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(forceGenerator.getForceRepelFromTeamBot(supporter, affectedBot));
		forces.addAll(getForceAttractGoalCircle());
		forces.addAll(forceGenerator.getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(forceGenerator.getForceRepelFromPassLine(affectedBot));
		forces.addAll(getAngleRangePositionForce());
		return forces;
	}


	private List<Force> getAngleRangePositionForce()
	{
		return supportiveGoalPositions.get().stream()
				.map(p -> new Force(p, sigmaAngleRangePoints, magnitudeAngleRangePoints))
				.toList();
	}


	private Force getForceAttractToPenaltyArea(BotState affectedBot)
	{
		IPenaltyArea penaltyArea = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 1.75);
		IVector2 referencePoint = penaltyArea.projectPointOnToPenaltyAreaBorder(affectedBot.getPos());

		return new Force(referencePoint, sigmaPenaltyAreaAttraction, magnitudePenaltyAreaAttraction);
	}


	private List<Force> getForceAttractGoalCircle()
	{
		Vector2f goalCenter = Geometry.getGoalTheir().getCenter();
		return List.of(
				new Force(goalCenter, sigmaGoalAttraction, magnitudeGoalAttraction, radiusGoal, true),
				new Force(goalCenter, sigmaGoalRepel, magnitudeGoalRepel),
				new Force(Geometry.getGoalOur().getCenter(), sigmaGoalRepel, magnitudeGoalRepel)
		);
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
