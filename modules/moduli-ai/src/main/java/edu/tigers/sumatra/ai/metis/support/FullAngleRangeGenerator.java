/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ball.trajectory.IFlatBallConsultant;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RequiredArgsConstructor
public class FullAngleRangeGenerator
{
	private final List<IMovingRobot> consideredBots;
	private final IVector2 center;
	private final IFlatBallConsultant ballConsultant;
	private final double maxHorizon;
	private List<AngleRange> coveredAngleRanges = new ArrayList<>();
	private List<AngleRange> uncoveredAngleRanges = new ArrayList<>();
	private List<AngleRange> consolidatedCoveredAngleRanges = new ArrayList<>();

	private double kickSpeed = RuleConstraints.getMaxKickSpeed();


	private List<AngleRange> getCoveredAngleRanges()
	{
		if (coveredAngleRanges.isEmpty())
		{
			calcCoveredAngleRanges();
		}
		return coveredAngleRanges;

	}


	private void calcCoveredAngleRanges()
	{
		for (IMovingRobot movingRobot : consideredBots)
		{
			double distBotToStart = movingRobot.getPos().distanceTo(center);
			final double timeToKick = 0;
			double timeBallToBot = ballConsultant.getTimeForKick(distBotToStart, kickSpeed) + timeToKick;
			final double timeForBotToReact = 0.5;
			double horizon = Math.min(maxHorizon, Math.max(0, timeBallToBot - timeForBotToReact));
			ICircle obstacleCircle = movingRobot.getMovingHorizon(horizon);
			List<IVector2> intersections = obstacleCircle.tangentialIntersections(center);

			IVector2 first = intersections.get(0).subtractNew(center);
			IVector2 second = intersections.get(1).subtractNew(center);

			double rightAngle;
			double leftAngle;
			if (first.angleTo(second).orElse(0.0) > 0)
			{
				rightAngle = first.getAngle();
				leftAngle = second.getAngle();
			} else
			{
				leftAngle = first.getAngle();
				rightAngle = second.getAngle();
			}

			if (rightAngle > leftAngle)
			{
				rightAngle -= 2 * Math.PI;
			}
			coveredAngleRanges.add(AngleRange.fromAngles(rightAngle, leftAngle));
		}
	}


	public List<AngleRange> getUncoveredAngleRanges()
	{
		if (uncoveredAngleRanges.isEmpty())
		{
			calcUncoveredRanges();
		}
		return uncoveredAngleRanges;
	}


	private void calcUncoveredRanges()
	{
		if (getConsolidatedCoveredAngleRanges().isEmpty())
		{
			uncoveredAngleRanges.add(AngleRange.fromAngles(-Math.PI + 0.01, Math.PI - 0.01));
			return;
		}
		double lastLeft = getConsolidatedCoveredAngleRanges().getLast()
				.getLeft();

		for (AngleRange ar : getConsolidatedCoveredAngleRanges())
		{
			if (ar.getRight() > lastLeft)
			{
				uncoveredAngleRanges.add(AngleRange.fromAngles(lastLeft, ar.getRight()));
			}
			lastLeft = ar.getLeft();
		}
		double firstRightAngle = getConsolidatedCoveredAngleRanges().getFirst()
				.getRight();
		if (firstRightAngle < lastLeft)
		{
			firstRightAngle += Math.PI * 2;
		}
		double left = Math.max(lastLeft, firstRightAngle);
		double right = Math.min(lastLeft, firstRightAngle);

		uncoveredAngleRanges.add(AngleRange.fromAngles(right, left));
	}


	private List<AngleRange> getConsolidatedCoveredAngleRanges()
	{
		if (consolidatedCoveredAngleRanges.isEmpty())
		{
			calcConsolidatedCoveredAngleRanges();
		}
		return coveredAngleRanges;
	}


	private void calcConsolidatedCoveredAngleRanges()
	{
		getCoveredAngleRanges().sort(Comparator.comparingDouble(AngleRange::getRight));
		if (getCoveredAngleRanges().size() <= 1)
		{
			consolidatedCoveredAngleRanges = getCoveredAngleRanges();
			return;
		}
		double tmpRightAngle = getCoveredAngleRanges().getFirst().getRight();
		double tmpLeftAngle = getCoveredAngleRanges().getFirst().getLeft();
		for (AngleRange ar : getCoveredAngleRanges())
		{
			if (ar.getRight() > tmpLeftAngle)
			{
				consolidatedCoveredAngleRanges.add(AngleRange.fromAngles(tmpRightAngle, tmpLeftAngle));
				tmpRightAngle = ar.getRight();
			}
			tmpLeftAngle = Math.max(ar.getLeft(), tmpLeftAngle);
		}

		if (consolidatedCoveredAngleRanges.isEmpty())
		{
			double leftAngle = getCoveredAngleRanges().stream().map(AngleRange::getLeft).max(Double::compareTo)
					.orElse(0.);
			double rightAngle = getCoveredAngleRanges().stream().map(AngleRange::getRight).min(Double::compareTo)
					.orElse(0.);
			consolidatedCoveredAngleRanges.add(AngleRange.fromAngles(rightAngle, leftAngle));
		} else if (Math.abs(tmpLeftAngle - consolidatedCoveredAngleRanges.getLast()
				.getLeft()) < 0.001)
		{
			consolidatedCoveredAngleRanges.add(AngleRange.fromAngles(tmpRightAngle, tmpLeftAngle));
		}

		if (consolidatedCoveredAngleRanges.size() >= 2)
		{
			AngleRange first = consolidatedCoveredAngleRanges.getFirst();
			AngleRange last = consolidatedCoveredAngleRanges.getLast();
			if (first.getRight() > last.getLeft())
			{
				consolidatedCoveredAngleRanges.remove(first);
				consolidatedCoveredAngleRanges.remove(last);

				consolidatedCoveredAngleRanges.add(AngleRange.fromAngles(first.getLeft(), last.getRight()));
			}
		}
	}
}
