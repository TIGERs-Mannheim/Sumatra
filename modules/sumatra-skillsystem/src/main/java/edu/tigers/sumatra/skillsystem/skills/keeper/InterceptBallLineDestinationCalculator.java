/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;

import java.awt.Color;
import java.util.Comparator;


public class InterceptBallLineDestinationCalculator extends AInterceptBallDestinationCalculator
{
	@Configurable(comment = "[mm] distance between samples to find optimal intercept destination", defValue = "10")
	private static int samplesInterceptDestinationGranularity = 10;

	static
	{
		ConfigRegistration.registerClass("skills", InterceptBallLineDestinationCalculator.class);
	}


	private IVector2 findBestDestinationWithinPenArea(IVector2 fallbackPoint)
	{
		var penAreaWithMargin = Geometry.getPenaltyAreaOur().withMargin(-100);
		var ballPos = getBall().getPos();
		var ballInsidePenArea = penAreaWithMargin.isPointInShape(ballPos);
		var ballLine = Lines.lineFromDirection(ballPos, getBall().getVel());
		var pointACandidates = penAreaWithMargin.intersectPerimeterPath(ballLine);
		var pointBCandidate = Geometry.getGoalOur().getLine().intersect(ballLine).asOptional();
		if (pointACandidates.size() != 1 || pointBCandidate.isEmpty())
		{
			return fallbackPoint;
		}

		final IVector2 pointA = pointACandidates.getFirst();
		final IVector2 pointB = pointBCandidate.get();

		var searchDist = pointA.distanceTo(pointB);
		var numSamples = (int) (searchDist / samplesInterceptDestinationGranularity);
		var distBallToA = ((ballInsidePenArea) ? -1 : 1) * ballPos.distanceTo(pointA);

		var bestPoint = SumatraMath.evenDistribution1D(0, searchDist, numSamples).stream()
				.map(dist -> buildDestWithTrajectory(getBall().getVel().scaleToNew(dist), pointA, distBallToA))
				.max(DestWithTrajectory::compareTo).map(DestWithTrajectory::dest).orElse(fallbackPoint);

		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS)
				.add(new DrawableLine(pointA, pointB, Color.WHITE));
		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS)
				.add(new DrawableCircle(Circle.createCircle(bestPoint, 35), Color.GREEN));
		return bestPoint;
	}


	private DestWithTrajectory buildDestWithTrajectory(IVector2 offset, IVector2 pointA, double distBallToA)
	{
		var dest = pointA.addNew(offset);
		var ballDist = distBallToA + offset.getLength();
		var tt = ballDist > 0 ? getBall().getTrajectory().getTimeByDist(ballDist) : 0.0;
		return buildDestWithTrajectory(dest, tt);
	}


	@Override
	public IVector2 calcDestination()
	{
		if (usePrimaryDirectionsIntercept)
		{
			getMoveConstraints().setPrimaryDirection(getBall().getVel());
		} else
		{
			getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
		}
		var fallBackPoint = findPointOnBallTraj();
		var bestPoint = findBestDestinationWithinPenArea(fallBackPoint);
		bestPoint = adaptDestinationToChipKick(bestPoint);
		bestPoint = moveInterceptDestinationInsideField(bestPoint);
		return calcOverAcceleration(bestPoint);
	}


	private IVector2 adaptDestinationToChipKick(final IVector2 destination)
	{
		final var goalLine = Geometry.getGoalOur().getLine();
		return getWorldFrame().getBall().getTrajectory().getTouchdownLocations().stream()
				.filter(td -> td.x() > Geometry.getGoalOur().getCenter().x())
				.filter(td -> td.distanceTo(getPos()) < maxChipInterceptDist)
				.filter(td -> td.x() < destination.x())
				.min(Comparator.comparingDouble(goalLine::distanceToSqr)).orElse(destination);
	}


	private IVector2 moveInterceptDestinationInsideField(IVector2 destination)
	{
		return Geometry.getField().withMargin(Geometry.getBotRadius())
				.nearestPointInside(destination, getBall().getPos());
	}


	private IVector2 calcOverAcceleration(final IVector2 destination)
	{
		final double targetTime;
		if (Math.abs(destination.subtractNew(getBall().getPos()).getAngle()) > AngleMath.PI_HALF)
		{
			targetTime = getBall().getTrajectory().getTimeByPos(destination);
		} else
		{
			targetTime = 0.0;
		}
		return TrajectoryGenerator.generateVirtualPositionToReachPointInTime(getTBot(), destination, targetTime);
	}
}
