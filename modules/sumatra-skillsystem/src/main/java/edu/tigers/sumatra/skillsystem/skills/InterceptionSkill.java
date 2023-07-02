/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import lombok.NonNull;
import lombok.Setter;

import java.awt.Color;
import java.util.List;


/**
 * Intercept the ball during opponent free kicks.
 */
public class InterceptionSkill extends AMoveSkill
{
	@Configurable(defValue = "150.0")
	private static double interceptionSkillSecurityDist = 150;

	@Setter
	@NonNull
	private IVector2 nearestOpponentBotPos = Vector2.fromXY(0, 0);
	@Setter
	private double distanceToBall = getStdDist();

	private IVector2 lastValidDestination = null;


	public static double getStdDist()
	{
		return RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius() + interceptionSkillSecurityDist;
	}


	@Override
	protected void doUpdate()
	{
		IVector2 dest = calcDefendingDestination();
		double targetAngle = getBall().getPos().subtractNew(getPos()).getAngle();

		setTargetPose(dest, targetAngle, defaultMoveConstraints());
		getShapes().get(ESkillShapesLayer.KEEPER).add(new DrawableCircle(dest, 25, Color.CYAN));
	}


	private IVector2 calcDefendingDestination()
	{
		IVector2 destination;
		IVector2 intersectPoint;

		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 botToBall = ballPos.subtractNew(nearestOpponentBotPos).normalizeNew();

		intersectPoint = ballPos.addNew(botToBall.multiplyNew(distanceToBall));

		destination = intersectPoint;

		// keep stop distance
		ICircle forbiddenCircle = Circle.createCircle(ballPos, RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius());
		ICircle driveCircle = Circle.createCircle(ballPos, RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius() + interceptionSkillSecurityDist);

		if (isMoveTargetValid(destination)
				&& !forbiddenCircle.intersectPerimeterPath(Lines.segmentFromPoints(getPos(), destination)).isEmpty())
		{
			DrawableLine drl = new DrawableLine(Lines.segmentFromOffset(getPos(), ballPos.subtractNew(getPos())),
					Color.CYAN);
			getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(drl);
			List<IVector2> circleIntersections = driveCircle
					.intersectPerimeterPath(Lines.lineFromDirection(getPos(), ballPos.subtractNew(getPos())));
			IVector2 nextCirclePoint = getNextCirclePoint(circleIntersections);

			IVector2 wayLeftTarget = CircleMath.stepAlongCircle(nextCirclePoint, ballPos, -AngleMath.PI / 8);
			IVector2 wayRightTarget = CircleMath.stepAlongCircle(nextCirclePoint, ballPos, AngleMath.PI / 8);

			boolean takeLeft = shouldTakeLeft(destination, ballPos, nextCirclePoint, wayLeftTarget, wayRightTarget);
			destination = takeLeft ? wayLeftTarget : wayRightTarget;
		}

		if (isMoveTargetValid(destination))
		{
			lastValidDestination = destination;
			return destination;
		}
		if (lastValidDestination == null)
		{
			lastValidDestination = getPos();
		}
		return lastValidDestination;
	}


	private boolean shouldTakeLeft(final IVector2 destination, final IVector2 ballPos, final IVector2 nextCirclePoint,
			final IVector2 wayLeftTarget, final IVector2 wayRightTarget)
	{
		boolean takeLeft = destination.subtractNew(wayLeftTarget).getLength() < destination
				.subtractNew(wayRightTarget)
				.getLength();

		double angle = destination.subtractNew(ballPos).angleToAbs(getPos().subtractNew(ballPos)).orElse(0.0);
		for (int i = 0; i < 16; i++)
		{
			if (!isMoveTargetValid(
					CircleMath.stepAlongCircle(nextCirclePoint, ballPos, ((takeLeft ? -1 : 1) * angle) / 16)))
			{
				takeLeft = !takeLeft;
				break;
			}
		}
		return takeLeft;
	}


	private IVector2 getNextCirclePoint(final List<IVector2> circleIntersections)
	{
		IVector2 nextCirclePoint;
		if ((circleIntersections.size() >= 2) &&
				(circleIntersections.get(0).subtractNew(getPos()).getLength() > circleIntersections.get(1)
						.subtractNew(getPos()).getLength()))
		{
			nextCirclePoint = circleIntersections.get(1);
		} else if (!circleIntersections.isEmpty())
		{
			nextCirclePoint = circleIntersections.get(0);
		} else
		{
			nextCirclePoint = Vector2f.ZERO_VECTOR;
		}
		return nextCirclePoint;
	}


	private boolean isMoveTargetValid(final IVector2 destination)
	{
		double marginPenalty = 350;
		return Geometry.getField().isPointInShape(destination)
				&& !Geometry.getPenaltyAreaOur().withMargin(marginPenalty).isPointInShape(destination)
				&& !Geometry.getPenaltyAreaTheir().withMargin(marginPenalty).isPointInShape(destination);
	}
}
