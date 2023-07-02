/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;


public class InterceptBallCurveDestinationCalculator extends AInterceptBallDestinationCalculator
{
	@Configurable(comment = "[s] distance between samples to find optimal intercept destination", defValue = "0.03")
	private static double samplesInterceptDestinationGranularity = 0.03;

	static
	{
		ConfigRegistration.registerClass("skills", InterceptBallCurveDestinationCalculator.class);
	}


	private TimedPos findBestDestinationWithinPenArea(IVector2 fallbackPoint)
	{
		var ballCurve = getBall().getTrajectory().getPlanarCurve();
		var penAreaWithMargin = Geometry.getPenaltyAreaOur().withMargin(-100);
		var ballPos = getBall().getPos();
		var ballInsidePenArea = penAreaWithMargin.isPointInShape(ballPos);

		var intersections = penAreaWithMargin.getRectangle().getEdges().stream()
				.map(ballCurve::getIntersectionTimesWithLine).flatMap(Collection::stream).toList();

		if (intersections.size() != 2 && (intersections.size() != 1 && !ballInsidePenArea))
		{
			return new TimedPos(fallbackPoint, Optional.empty());
		}
		final double timeA;
		final double timeB;
		if (intersections.size() == 1)
		{
			timeA = 0;
			timeB = intersections.get(0);
		} else
		{
			if (Geometry.getGoalOur().getLine().distanceTo(ballCurve.getPos(intersections.get(0))) < 1e-3)
			{
				timeA = intersections.get(1);
				timeB = intersections.get(0);
			} else if (Geometry.getGoalOur().getLine().distanceTo(ballCurve.getPos(intersections.get(1))) < 1e-3)
			{
				timeA = intersections.get(0);
				timeB = intersections.get(1);
			} else
			{
				return new TimedPos(fallbackPoint, Optional.empty());
			}
		}

		var searchTime = timeB - timeA;
		var numSamples = (int) (searchTime / samplesInterceptDestinationGranularity);

		var bestPoint = SumatraMath.evenDistribution1D(timeA, timeB, numSamples).stream()
				.map(tt -> buildDestWithTrajectory(ballCurve.getPos(tt), tt)).max(DestWithTrajectory::compareTo)
				.map(DestWithTrajectory::dest).orElse(fallbackPoint);

		getShapes().get(ESkillShapesLayer.KEEPER)
				.add(new DrawablePlanarCurve(ballCurve.restrictToStart(timeA).restrictToEnd(timeB)));
		getShapes().get(ESkillShapesLayer.KEEPER)
				.add(new DrawableCircle(Circle.createCircle(bestPoint, 35), Color.GREEN));
		return new TimedPos(fallbackPoint, Optional.empty());
	}


	@Override
	public IVector2 calcDestination()
	{
		var fallBackPoint = findPointOnBallTraj();
		var bestPoint = findBestDestinationWithinPenArea(fallBackPoint);
		bestPoint = adaptDestinationToChipKick(bestPoint);
		bestPoint = moveInterceptDestinationInsideField(bestPoint);
		return calcOverAcceleration(bestPoint);
	}


	private TimedPos adaptDestinationToChipKick(final TimedPos destination)
	{
		final var goalLine = Geometry.getGoalOur().getLine();
		return getWorldFrame().getBall().getTrajectory().getTouchdownLocations().stream()
				.filter(td -> td.x() > Geometry.getGoalOur().getCenter().x())
				.filter(td -> td.distanceTo(getPos()) < maxChipInterceptDist)
				.min(Comparator.comparingDouble(goalLine::distanceToSqr)).map(pos -> new TimedPos(pos, Optional.empty()))
				.orElse(destination);
	}


	private TimedPos moveInterceptDestinationInsideField(TimedPos destination)
	{
		var pointInside = Geometry.getField().withMargin(Geometry.getBotRadius())
				.nearestPointInside(destination.pos(), getBall().getPos());
		if (pointInside.isCloseTo(destination.pos()))
		{
			return destination;
		} else
		{
			return new TimedPos(pointInside, Optional.empty());
		}
	}


	private IVector2 calcOverAcceleration(final TimedPos timedPos)
	{
		double targetTime;
		if (timedPos.time.isPresent())
		{
			targetTime = timedPos.time.get();
		} else
		{
			if (Math.abs(timedPos.pos.subtractNew(getBall().getPos()).getAngle()) > AngleMath.PI_HALF)
			{
				targetTime = getBall().getTrajectory().getTimeByPos(timedPos.pos);
			} else
			{
				targetTime = 0.0;
			}
		}
		return TrajectoryGenerator.generateVirtualPositionToReachPointInTime(getTBot(), getMoveConstraints(),
				timedPos.pos, targetTime);
	}


	private record TimedPos(IVector2 pos, Optional<Double> time)
	{
	}
}
