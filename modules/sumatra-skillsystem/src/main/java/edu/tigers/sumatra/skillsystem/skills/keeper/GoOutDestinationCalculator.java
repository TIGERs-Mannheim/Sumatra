/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


/**
 * Calc the position within the Penalty Area that covers the whole goal from threatPosition
 */
public class GoOutDestinationCalculator extends AKeeperDestinationCalculator
{
	@Override
	public KeeperDestination calcDestination()
	{

		Optional<IVector2> poleCoveringPosition = calcPositionAtPostCoveringWholeGoal(getPosToCover());
		IVector2 targetPosition;
		Color circleColor;
		if (poleCoveringPosition.isPresent())
		{
			targetPosition = poleCoveringPosition.get();
			circleColor = Color.YELLOW;
		} else
		{
			IVector2 posCoveringWholeGoal = calcPositionCoveringWholeGoal(getPosToCover());
			if (isPositionInPenaltyArea(posCoveringWholeGoal))
			{
				circleColor = Color.GREEN;
				targetPosition = posCoveringWholeGoal;
			} else if (isTargetPositionOutsideOfField(posCoveringWholeGoal))
			{
				targetPosition = calcNearestGoalPostPosition(getPosToCover());
				circleColor = Color.BLUE;
			} else
			{
				targetPosition = calcPositionBehindPenaltyArea(getPosToCover());
				circleColor = Color.RED;
			}
		}

		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS)
				.add(new DrawableLine(getPosToCover(), Geometry.getGoalOur().getLeftPost()));
		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS)
				.add(new DrawableLine(getPosToCover(), Geometry.getGoalOur().getRightPost()));
		getShapes().get(ESkillShapesLayer.KEEPER_POSITIONING_CALCULATORS)
				.add(new DrawableCircle(targetPosition, Geometry.getBotRadius(), circleColor));

		return KeeperDestination.fromDestination(getTBot(), targetPosition);
	}


	private IVector2 calcPositionBehindPenaltyArea(IVector2 posToCover)
	{
		IVector2 goalCenter = Geometry.getGoalOur().bisection(posToCover);
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
		IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPost = Geometry.getGoalOur().getRightPost();

		IVector2 nearestPost;
		if (posToCover.y() < 0)
		{
			nearestPost = rightPost;
		} else
		{
			nearestPost = leftPost;
		}
		return LineMath.stepAlongLine(nearestPost, Geometry.getCenter(),
				Geometry.getBotRadius() + Geometry.getBallRadius());
	}


	private boolean isTargetPositionOutsideOfField(IVector2 targetPos)
	{
		return !Geometry.getField().isPointInShape(targetPos);
	}


	private boolean isPositionInPenaltyArea(IVector2 targetPos)
	{
		return Geometry.getPenaltyAreaOur()
				.withMargin(-(2 * Geometry.getBallRadius() + Geometry.getBotRadius()))
				.isPointInShape(targetPos);
	}


	private IVector2 calcPositionCoveringWholeGoal(IVector2 posToCover)
	{
		var goalCenter = Geometry.getGoalOur().bisection(posToCover);

		var ballGoalOrthoDirection = Lines.lineFromPoints(posToCover, goalCenter).directionVector()
				.getNormalVector();
		var ballGoalLineOrtho = Lines.lineFromDirection(goalCenter, ballGoalOrthoDirection);
		var ballLeftPost = Lines.halfLineFromPoints(posToCover, Geometry.getGoalOur().getLeftPost());
		var ballRightPost = Lines.halfLineFromPoints(posToCover, Geometry.getGoalOur().getRightPost());

		double distLM = VectorMath.distancePP(goalCenter, ballGoalLineOrtho.intersect(ballLeftPost)
				.asOptional().orElseThrow(IllegalStateException::new));

		double distRM = VectorMath.distancePP(goalCenter, ballGoalLineOrtho.intersect(ballRightPost)
				.asOptional().orElseThrow(IllegalStateException::new));

		double relativeRadius = (2 * Geometry.getBotRadius() * distLM) / (distLM + distRM);

		double alpha = ballLeftPost.directionVector().angleToAbs(ballGoalOrthoDirection).orElse(0.0);
		// angle should be less than 90° = pi/2
		if ((alpha > (AngleMath.PI / 2)) && (alpha < AngleMath.PI))
		{
			alpha = AngleMath.PI - alpha;
		}

		IVector2 optimalDistanceToBallPosDirectedToGoal = LineMath.stepAlongLine(posToCover, goalCenter,
				relativeRadius * SumatraMath.tan(alpha));

		IVector2 optimalDistanceToBallPosDirectedToRightPose = ballRightPost
				.intersect(Lines.lineFromDirection(optimalDistanceToBallPosDirectedToGoal, ballGoalOrthoDirection))
				.asOptional().orElseThrow(IllegalStateException::new);

		return LineMath.stepAlongLine(optimalDistanceToBallPosDirectedToRightPose, optimalDistanceToBallPosDirectedToGoal,
				Geometry.getBotRadius());
	}


	private Optional<IVector2> calcPositionAtPostCoveringWholeGoal(IVector2 posToCover)
	{
		IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPost = Geometry.getGoalOur().getRightPost();

		double distanceRightPost = Lines.lineFromPoints(posToCover, leftPost).distanceTo(rightPost);
		double distanceLeftPost = Lines.lineFromPoints(posToCover, rightPost).distanceTo(leftPost);

		boolean isPositionAtLeftPostCoverGoal = distanceLeftPost <= Geometry.getBotRadius() * 2 && posToCover.y() > 0;
		boolean isPositionAtRightPostCoverGoal = distanceRightPost <= Geometry.getBotRadius() * 2 && posToCover.y() < 0;

		Optional<IVector2> post = Optional.empty();

		if (isPositionAtLeftPostCoverGoal)
		{
			post = Optional.of(leftPost);
		} else if (isPositionAtRightPostCoverGoal)
		{
			post = Optional.of(rightPost);
		}

		Optional<IVector2> coveringPosition = Optional.empty();
		if (post.isPresent())
		{
			coveringPosition = Optional.of(LineMath.stepAlongLine(post.get(), Geometry.getCenter(),
					Geometry.getBotRadius() + Geometry.getBallRadius()));
		}
		return coveringPosition;
	}
}
