/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This behaviour is one of the standard supportive behaviours and should bring the supporter to a good attacking
 * position without
 * interfering with each other.
 * As a ARepulsiveBehavior, it calculates the destinations through different forces emitted by different objects or
 * geometries.
 * In general, this behavior drives the supporter towards the goal, with respect to the opponents position.
 */
public class RepulsiveAttacker extends ARepulsiveBehavior
{
	
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean isActive = true;
	
	@Configurable(comment = "Whether force field should be drawn or not", defValue = "false")
	private static boolean isDrawing = false;
	
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
	
	static
	{
		ConfigRegistration.registerClass("roles", RepulsiveAttacker.class);
	}
	
	
	public RepulsiveAttacker(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public double calculateViability()
	{
		if (isActive)
		{
			return 1;
		}
		return 0;
	}
	
	
	@Override
	public boolean getIsActive()
	{
		return RepulsiveAttacker.isActive;
	}
	
	
	@Override
	protected List<Force> collectForces(ITrackedBot affectedBot, List<ITrackedBot> supporter,
			List<ITrackedBot> opponents)
	{
		List<Force> forces = new ArrayList<>();
		forces.add(getForceStayInsideField(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getForceAttractToPenaltyArea(affectedBot));
		forces.addAll(getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelFromTeamBot(supporter, affectedBot));
		forces.addAll(getForceAttractGoalCircle());
		forces.addAll(getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(getForceRepelFromPassLine(affectedBot));
		forces.addAll(getAngleRangePositionForce());
		return forces;
	}
	
	
	public static double getRadiusGoal()
	{
		return radiusGoal;
	}
	
	
	private List<Force> getAngleRangePositionForce()
	{
		return getRole().getAiFrame().getTacticalField().getSupportiveGoalPositions()
				.stream().map(p -> new Force(p, sigmaAngleRangePoints, magnitudeAngleRangePoints))
				.collect(Collectors.toList());
		
	}
	
	
	private Force getForceAttractToPenaltyArea(ITrackedBot affectedBot)
	{
		IVector2 referencePoint = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 1.75)
				.projectPointOnToPenaltyAreaBorder(affectedBot.getPos());
		return new Force(referencePoint, sigmaPenaltyAreaAttraction, magnitudePenaltyAreaAttraction);
	}
	
	
	private List<Force> getForceAttractGoalCircle()
	{
		List<Force> forceList = new ArrayList<>();
		forceList.add(new Force(Geometry.getGoalTheir().getCenter(), sigmaGoalAttraction, magnitudeGoalAttraction,
				radiusGoal, true));
		
		forceList.add(new Force(Geometry.getGoalTheir().getCenter(), sigmaGoalRepel, magnitudeGoalRepel));
		forceList.add(new Force(Geometry.getGoalOur().getCenter(), sigmaGoalRepel, magnitudeGoalRepel));
		return forceList;
	}
	
	
	@Override
	boolean isDrawing()
	{
		return isDrawing;
	}
}
