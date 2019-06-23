/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;


import java.util.List;

import org.apache.commons.lang.Validate;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkill;


/**
 * tries to intercept ball
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class InterceptionSkill extends AMoveSkill
{
	private IVector2 nearestEnemyBotPos = Vector2.fromXY(0, 0);
	private IVector2 lastValidDestination = null;
	
	@Configurable(defValue = "150.0")
	private static double interceptionSkillSecurityDist = 150;
	
	private double distanceToBall = getStdDist();
	
	
	/**
	 * Interception skill, tries to intercept enemy standards.
	 */
	public InterceptionSkill()
	{
		super(ESkill.INTERCEPTION);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		IVector2 dest = calcDefendingDestination();
		double targetAngle = getBall().getPos().subtractNew(getPos()).getAngle();
		
		setTargetPose(dest, targetAngle, getMoveCon().getMoveConstraints());
	}
	
	
	private IVector2 calcDefendingDestination()
	{
		IVector2 destination;
		IVector2 intersectPoint;
		
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 botToBall = ballPos.subtractNew(nearestEnemyBotPos).normalizeNew();
		
		intersectPoint = ballPos.addNew(botToBall.multiplyNew(distanceToBall));
		
		destination = intersectPoint;
		
		// keep stop distance
		ICircle forbiddenCircle = Circle.createCircle(ballPos, RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius());
		ICircle driveCircle = Circle.createCircle(ballPos, RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius() + interceptionSkillSecurityDist);
		
		if (isMoveTargetValid(destination)
				&& forbiddenCircle.isIntersectingWithLineSegment(Line.fromPoints(getPos(), destination)))
		{
			List<IVector2> circleIntersections = driveCircle.lineIntersections(Line.fromDirection(getPos(), ballPos));
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
				&& !Geometry.getPenaltyAreaOur().isPointInShape(destination, marginPenalty)
				&& !Geometry.getPenaltyAreaTheir().isPointInShape(destination, marginPenalty);
	}
	
	
	/**
	 * @param nearestEnemyBot
	 */
	public void setNearestEnemyBotPos(final IVector2 nearestEnemyBot)
	{
		Validate.notNull(nearestEnemyBot);
		nearestEnemyBotPos = nearestEnemyBot;
	}
	
	
	public IVector2 getLastValidDestination()
	{
		return lastValidDestination;
	}
	
	
	public void setDistanceToBall(final double distanceToBall)
	{
		this.distanceToBall = distanceToBall;
	}
	
	
	public static double getStdDist()
	{
		return RuleConstraints.getStopRadius()
				+ Geometry.getBotRadius() + interceptionSkillSecurityDist;
	}
}
