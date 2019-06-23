/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;


import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkill;

import java.util.List;


/**
 * tries to intercept ball
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class InterceptionSkill extends BlockSkill
{
	private IVector2 nearestEnemyBotPos = null;
	private IVector2 lastValidDestination = null;
	
	@Configurable()
	private static double interceptionSkillSecurityDist = 150;
	
	private static double stdDist = Geometry.getBotToBallDistanceStop()
			+ Geometry.getBotRadius() + interceptionSkillSecurityDist;
	
	private double distanceToBall = stdDist;
	
	
	/**
	 * Interception skill, tries to intercept enemy standards.
	 */
	public InterceptionSkill()
	{
		super(ESkill.INTERCEPTION);
	}
	
	
	@Override
	protected IVector2 calcDefendingDestination()
	{
		if (nearestEnemyBotPos == null)
		{
			nearestEnemyBotPos = Vector2.fromXY(0, 0);
		}
		
		IVector2 destination;
		boolean overAccelerationNecessary = false;
		IVector2 intersectPoint;
		
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 botToBall = ballPos.subtractNew(nearestEnemyBotPos).normalizeNew();
		
		intersectPoint = ballPos.addNew(botToBall.multiplyNew(distanceToBall));
		
		destination = intersectPoint;
		
		double distance = VectorMath.distancePP(destination, getPos());
		
		// if we are already blocking the ball we can do the fine tuning: position on the exact shooting line and
		if (distance < (Geometry.getBotRadius() / 2.0))
		{
			overAccelerationNecessary = false;
		}
		
		if (overAccelerationNecessary)
		{
			destination = getAccelerationTarget(getWorldFrame().getTiger(getBot().getBotId()), destination);
		}
		
		// keep stop distance
		ICircle forbiddenCircle = Circle.createCircle(ballPos, Geometry.getBotToBallDistanceStop()
				+ Geometry.getBotRadius());
		ICircle driveCircle = Circle.createCircle(ballPos, Geometry.getBotToBallDistanceStop()
				+ Geometry.getBotRadius() + interceptionSkillSecurityDist);
		
		if (isMoveTargetValid(destination)
				&& forbiddenCircle.isIntersectingWithLineSegment(Line.fromPoints(getPos(), destination)))
		{
			List<IVector2> circleIntersections = driveCircle.lineIntersections(Line.fromDirection(getPos(), ballPos));
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
				nextCirclePoint = Vector2.zero();
			}
			
			IVector2 wayLeftTarget = CircleMath.stepAlongCircle(nextCirclePoint, ballPos, -AngleMath.PI / 8);
			IVector2 wayRightTarget = CircleMath.stepAlongCircle(nextCirclePoint, ballPos, AngleMath.PI / 8);
			
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
			
			if (takeLeft)
			{
				destination = wayLeftTarget;
			} else
			{
				destination = wayRightTarget;
			}
			
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
		return stdDist;
	}
	
	
}
