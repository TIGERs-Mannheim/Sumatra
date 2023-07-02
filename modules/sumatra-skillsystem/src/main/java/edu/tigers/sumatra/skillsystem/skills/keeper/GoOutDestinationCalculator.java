/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;

import java.util.List;
import java.util.Optional;


/**
 * Calc the position within the Penalty Area that covers the whole goal from threatPosition
 */
public class GoOutDestinationCalculator extends AKeeperDestinationCalculator
{
	@Override
	public IVector2 calcDestination()
	{

		Optional<IVector2> poleCoveringPosition = calcPositionAtPoleCoveringWholeGoal(getPosToCover());
		IVector2 targetPosition;
		if (poleCoveringPosition.isPresent())
		{
			targetPosition = poleCoveringPosition.get();
		} else
		{
			IVector2 posCoveringWholeGoal = calcPositionCoveringWholeGoal(getPosToCover());
			if (isPositionInPenaltyArea(posCoveringWholeGoal))
			{
				targetPosition = posCoveringWholeGoal;
			} else if (isTargetPositionOutsideOfField(posCoveringWholeGoal))
			{
				targetPosition = calcNearestGoalPostPosition(getPosToCover());
			} else
			{
				targetPosition = calcPositionBehindPenaltyArea(getPosToCover());
			}
		}
		return targetPosition;
	}


	private IVector2 calcPositionBehindPenaltyArea(IVector2 posToCover)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		ILine ballGoalCenter = Lines.lineFromPoints(goalCenter, posToCover);

		List<IVector2> intersectionsList = Geometry.getPenaltyAreaOur().intersectPerimeterPath(ballGoalCenter);

		IVector2 intersection = intersectionsList.stream().min(new VectorDistanceComparator(posToCover))
				.orElse(posToCover);

		double stepSize = VectorMath.distancePP(goalCenter, intersection) - ((2 * Geometry.getBallRadius())
				+ Geometry.getBotRadius());
		return LineMath.stepAlongLine(goalCenter, intersection, stepSize);
	}


	private IVector2 calcNearestGoalPostPosition(IVector2 posToCover)
	{
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		IVector2 nearestPole;
		if (posToCover.y() < 0)
		{
			nearestPole = rightPole;
		} else
		{
			nearestPole = leftPole;
		}
		return LineMath.stepAlongLine(nearestPole, Geometry.getCenter(),
				Geometry.getBotRadius() + Geometry.getBallRadius());
	}


	private boolean isTargetPositionOutsideOfField(IVector2 targetPos)
	{
		return !Geometry.getField().isPointInShape(targetPos);
	}


	private boolean isPositionInPenaltyArea(IVector2 targetPos)
	{
		return Geometry.getPenaltyAreaOur()
				.withMargin(-((2 * Geometry.getBallRadius()) + Geometry.getBotRadius())).isPointInShape(targetPos);
	}


	private IVector2 calcPositionCoveringWholeGoal(IVector2 posToCover)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		IVector2 ballGoalOrthoDirection = Lines.lineFromPoints(posToCover, goalCenter).directionVector()
				.getNormalVector();
		ILine ballGoalLineOrtho = Lines.lineFromDirection(goalCenter, ballGoalOrthoDirection);
		ILine ballLeftPose = Lines.lineFromPoints(posToCover, leftPole);
		ILine ballRightPose = Lines.lineFromPoints(posToCover, rightPole);

		double distLM = VectorMath.distancePP(goalCenter, ballGoalLineOrtho.intersect(ballLeftPose)
				.asOptional().orElseThrow(IllegalStateException::new));

		double distRM = VectorMath.distancePP(goalCenter, ballGoalLineOrtho.intersect(ballRightPose)
				.asOptional().orElseThrow(IllegalStateException::new));

		double relativeRadius = (2 * Geometry.getBotRadius() * distLM) / (distLM + distRM);

		double alpha = ballLeftPose.directionVector().angleToAbs(ballGoalOrthoDirection).orElse(0.0);
		// angle should be less than 90° = pi/2
		if ((alpha > (AngleMath.PI / 2)) && (alpha < AngleMath.PI))
		{
			alpha = AngleMath.PI - alpha;
		}

		IVector2 optimalDistanceToBallPosDirectedToGoal = LineMath.stepAlongLine(posToCover, goalCenter,
				relativeRadius * SumatraMath.tan(alpha));

		IVector2 optimalDistanceToBallPosDirectedToRightPose = ballRightPose
				.intersect(Lines.lineFromDirection(optimalDistanceToBallPosDirectedToGoal, ballGoalOrthoDirection))
				.asOptional().orElseThrow(IllegalStateException::new);

		return LineMath.stepAlongLine(optimalDistanceToBallPosDirectedToRightPose, optimalDistanceToBallPosDirectedToGoal,
				Geometry.getBotRadius());
	}


	private Optional<IVector2> calcPositionAtPoleCoveringWholeGoal(IVector2 posToCover)
	{
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		double distanceRightPole = Lines.lineFromPoints(posToCover, leftPole).distanceTo(rightPole);
		double distanceLeftPole = Lines.lineFromPoints(posToCover, rightPole).distanceTo(leftPole);

		boolean isPositionAtLeftPoleCoverGoal = distanceLeftPole <= Geometry.getBotRadius() * 2 && posToCover.y() > 0;
		boolean isPositionAtRightPoleCoverGoal = distanceRightPole <= Geometry.getBotRadius() * 2 && posToCover.y() < 0;

		Optional<IVector2> pole = Optional.empty();

		if (isPositionAtLeftPoleCoverGoal)
		{
			pole = Optional.of(leftPole);
		} else if (isPositionAtRightPoleCoverGoal)
		{
			pole = Optional.of(rightPole);
		}

		Optional<IVector2> coveringPosition = Optional.empty();
		if (pole.isPresent())
		{
			coveringPosition = Optional.of(LineMath.stepAlongLine(pole.get(), Geometry.getCenter(),
					Geometry.getBotRadius() + Geometry.getBallRadius()));
		}
		return coveringPosition;
	}
}
