/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Move a robot destination away from ball by a given distance. Distance defaults to STOP radius.
 * Also check destinations through a point checker for valid destinations
 */
public class AwayFromBallMover
{
	private static final List<Double> DIRECTIONS = generateDirections();
	private final PointChecker pointChecker = new PointChecker();
	private double minDistanceToBall = defaultMinDistanceToBall();
	
	
	public AwayFromBallMover()
	{
		pointChecker.useRuleEnforcement();
	}
	
	private static List<Double> generateDirections()
	{
		List<Double> angles = new ArrayList<>();
		double angleStep = 0.1;
		angles.add(0.0);
		for (double a = angleStep; a < AngleMath.PI; a += angleStep)
		{
			angles.add(a);
			angles.add(-a);
		}

		return angles;
	}
	
	public IVector2 findValidDest(final BaseAiFrame aiFrame, final IVector2 originalDest)
	{
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		double baseDirection = originalDest.subtractNew(ballPos).getAngle();
		double distToBallOffset = 20;
		Optional<IVector2> alteredDest = Optional.empty();
		for (int i = 0; i < 15 && !alteredDest.isPresent(); i++)
		{
			final double distToBall = minDistanceToBall + distToBallOffset;
			alteredDest = DIRECTIONS.stream()
					.map(dir -> dirToDest(ballPos, baseDirection, distToBall, dir))
					.filter(p -> pointChecker.allMatch(aiFrame, p))
					.findFirst();
			distToBallOffset += Geometry.getBotRadius() * 2;
		}
		return alteredDest.orElse(originalDest);
	}
	
	private double defaultMinDistanceToBall()
	{
		return Geometry.getBotRadius() + Geometry.getBallRadius()
				+ RuleConstraints.getStopRadius();
	}
	
	private IVector2 dirToDest(final IVector2 ballPos, final double baseDirection, final double distToBall,
			final Double dir)
	{
		return ballPos.addNew(Vector2.fromAngleLength(baseDirection + dir, distToBall));
	}
	
	public PointChecker getPointChecker()
	{
		return pointChecker;
	}
	
	
	public void setMinDistanceToBall(final double minDistanceToBall)
	{
		this.minDistanceToBall = minDistanceToBall;
	}
}