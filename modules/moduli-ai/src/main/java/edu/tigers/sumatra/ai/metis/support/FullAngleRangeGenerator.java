/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;


public class FullAngleRangeGenerator
{
	private final IVector2 center;
	private final List<MovingRobot> consideredBtos;
	private List<AngleRange> coveredAngleRanges = new ArrayList<>();
	private List<AngleRange> uncoveredAngleRanges = new ArrayList<>();
	private List<AngleRange> consolidatedCoveredAngleRanges = new ArrayList<>();

	private double kickSpeed = RuleConstraints.getMaxBallSpeed();
	private IStraightBallConsultant ballConsultant;


	public FullAngleRangeGenerator(List<MovingRobot> bots, IVector2 center, IStraightBallConsultant ballConsultant)
	{
		this.center = center;
		this.consideredBtos = bots;
		this.ballConsultant = ballConsultant;
	}


	public List<AngleRange> getCoveredAngleRanges()
	{
		if (coveredAngleRanges.isEmpty())
		{
			calcCoveredAngleRanges();
		}
		return coveredAngleRanges;

	}


	private void calcCoveredAngleRanges()
	{
		for (MovingRobot movingRobot : consideredBtos)
		{
			double distBotToStart = movingRobot.getPos().distanceTo(center);
			final double timeToKick = 0;
			double timeBallToBot = ballConsultant.getTimeForKick(distBotToStart, kickSpeed) + timeToKick;
			final double timeForBotToReact = 0.5;
			double horizon = Math.max(0, timeBallToBot - timeForBotToReact);
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
			coveredAngleRanges.add(new AngleRange(rightAngle, leftAngle));
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
			uncoveredAngleRanges.add(new AngleRange(-Math.PI+0.01, Math.PI-0.01));
			return;
		}
		double lastLeft = getConsolidatedCoveredAngleRanges().get(getConsolidatedCoveredAngleRanges().size() - 1)
				.getLeftAngle();

		for (AngleRange ar : getConsolidatedCoveredAngleRanges())
		{
			if (ar.getRightAngle() > lastLeft)
			{
				uncoveredAngleRanges.add(new AngleRange(lastLeft, ar.getRightAngle()));
			}
			lastLeft = ar.getLeftAngle();
		}
		double firstRightAngle = getConsolidatedCoveredAngleRanges().get(0)
				.getRightAngle();
		if (firstRightAngle < lastLeft)
		{
			firstRightAngle += Math.PI * 2;
		}
		double left = Math.max(lastLeft, firstRightAngle);
		double right = Math.min(lastLeft, firstRightAngle);

		uncoveredAngleRanges.add(new AngleRange(right, left));
	}


	public List<AngleRange> getConsolidatedCoveredAngleRanges()
	{
		if (consolidatedCoveredAngleRanges.isEmpty())
		{
			calcConsolidatedCoveredAngleRanges();
		}
		return coveredAngleRanges;
	}


	private void calcConsolidatedCoveredAngleRanges()
	{
		getCoveredAngleRanges().sort(Comparator.comparingDouble(AngleRange::getRightAngle));
		if (getCoveredAngleRanges().size() <= 1)
		{
			consolidatedCoveredAngleRanges = getCoveredAngleRanges();
			return;
		}
		double tmpRightAngle = getCoveredAngleRanges().get(0).getRightAngle();
		double tmpLeftAngle = getCoveredAngleRanges().get(0).getLeftAngle();
		for (AngleRange ar : getCoveredAngleRanges())
		{
			if (ar.getRightAngle() > tmpLeftAngle)
			{
				consolidatedCoveredAngleRanges.add(new AngleRange(tmpRightAngle, tmpLeftAngle));
				tmpRightAngle = ar.getRightAngle();
			}
			tmpLeftAngle = Math.max(ar.getLeftAngle(), tmpLeftAngle);
		}

		if (consolidatedCoveredAngleRanges.isEmpty())
		{
			double leftAngle = getCoveredAngleRanges().stream().map(AngleRange::getLeftAngle).max(Double::compareTo)
					.orElse(0.);
			double rightAngle = getCoveredAngleRanges().stream().map(AngleRange::getRightAngle).min(Double::compareTo)
					.orElse(0.);
			consolidatedCoveredAngleRanges.add(new AngleRange(rightAngle, leftAngle));
		} else if (Math.abs(tmpLeftAngle- consolidatedCoveredAngleRanges.get(consolidatedCoveredAngleRanges.size() - 1)
				.getLeftAngle())<0.001)
		{
			consolidatedCoveredAngleRanges.add(new AngleRange(tmpRightAngle, tmpLeftAngle));
		}

		if (consolidatedCoveredAngleRanges.size() >= 2)
		{
			AngleRange first = consolidatedCoveredAngleRanges.get(0);
			AngleRange last = consolidatedCoveredAngleRanges.get(consolidatedCoveredAngleRanges.size() - 1);
			if (first.getRightAngle() > last.getLeftAngle())
			{
				consolidatedCoveredAngleRanges.remove(first);
				consolidatedCoveredAngleRanges.remove(last);

				consolidatedCoveredAngleRanges.add(new AngleRange(first.getLeftAngle(), last.getRightAngle()));
			}
		}
	}


	public void drawAngleRanges(List<AngleRange> ranges, double radius, List<IDrawableShape> shapes, Color color)
	{
		for (AngleRange angleRange : ranges)
		{
			IDrawableShape arc = new DrawableArc(
					Arc.createArc(center, radius, angleRange.getRightAngle(),
							angleRange.getLeftAngle() - angleRange.getRightAngle()),
					color);
			arc.setFill(true);
			shapes.add(arc);
		}
	}
}
