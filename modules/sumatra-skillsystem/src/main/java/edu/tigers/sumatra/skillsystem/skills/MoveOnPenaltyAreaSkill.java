/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.IDefensePenArea;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.PenAreaFactory;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author JonasH
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class MoveOnPenaltyAreaSkill extends AMoveSkill
{
	private IVector2 destination;
	private IDefensePenArea extendedPenArea;
	
	@Configurable(comment = "For destinations further away than this value, intermediate destinations will be generated", defValue = "600.0")
	private static double breakDistance = 600.0;
	
	@Configurable(defValue = "190.0")
	private static double minDistToOpponent = 190;
	
	
	/**
	 * @param distanceToPenArea
	 */
	public MoveOnPenaltyAreaSkill(final double distanceToPenArea)
	{
		super(ESkill.MOVE_ON_PENALTY_AREA);
		
		extendedPenArea = PenAreaFactory.buildWithMargin(distanceToPenArea);
		
		IState moveOnPenAreaState = new MoveOnPenaltyAreaState();
		setInitialState(moveOnPenAreaState);
	}
	
	
	/**
	 * Setting destination
	 *
	 * @param destination
	 */
	public void updateDestination(final IVector2 destination)
	{
		this.destination = destination;
	}
	
	
	/**
	 * Creates new ExtendedPenaltyArea with extended Radius
	 *
	 * @param distanceToPenArea
	 */
	public void updateDistanceToPenArea(final double distanceToPenArea)
	{
		extendedPenArea = PenAreaFactory.buildWithMargin(distanceToPenArea);
	}
	
	
	private class MoveOnPenaltyAreaState extends MoveToState
	{
		protected MoveOnPenaltyAreaState()
		{
			super(MoveOnPenaltyAreaSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			
			getMoveCon().setBallObstacle(false);
			getMoveCon().setOurBotsObstacle(false);
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			IVector2 targetPos = findDestination();
			
			getMoveCon().updateDestination(targetPos);
			getMoveCon().updateTargetAngle(getAngleByOurGoal(targetPos));
		}
		
		
		private double getAngleByOurGoal(final IVector2 targetPos)
		{
			IVector2 projectedPoint = extendedPenArea.projectPointOnPenaltyAreaLine(targetPos);
			return Vector2.fromPoints(Geometry.getGoalOur().getCenter(), projectedPoint).getAngle();
		}
		
		
		private Optional<ITrackedBot> nearOpponent(final IVector2 intermediatePos)
		{
			return getWorldFrame().getFoeBots().values().stream()
					.filter(t -> t.getPos().distanceTo(intermediatePos) < minDistToOpponent)
					.min(Comparator.comparingDouble(t -> t.getPos().distanceTo(intermediatePos)));
		}
		
		
		private IVector2 findDestination()
		{
			final IVector2 destinationProjection = extendedPenArea.projectPointOnPenaltyAreaLine(destination);
			final IVector2 positionProjection = extendedPenArea.projectPointOnPenaltyAreaLine(getPos());
			
			double lengthToDestination = extendedPenArea.lengthToPointOnPenArea(destinationProjection);
			double lengthToPos = extendedPenArea.lengthToPointOnPenArea(positionProjection);
			final double distanceOnPenAreaBorder = lengthToDestination - lengthToPos;
			
			if (Math.abs(distanceOnPenAreaBorder) < breakDistance)
			{
				return nearOpponent(destination).map(ITrackedBot::getPos).map(this::validFinalDestination)
						.orElse(destination);
			}
			
			final double stepLength = Math.signum(distanceOnPenAreaBorder) * (breakDistance * 0.75);
			final IVector2 intermediatePos = extendedPenArea.stepAlongPenArea(positionProjection, stepLength);
			return validIntermediateDestination(intermediatePos);
		}
		
		
		private IVector2 validIntermediateDestination(final IVector2 intermediatePos)
		{
			Optional<ITrackedBot> nearestOpponent = nearOpponent(intermediatePos);
			if (nearestOpponent.isPresent())
			{
				return movePosAway(intermediatePos, nearestOpponent.get().getPos(), minDistToOpponent);
			}
			
			return intermediatePos;
		}
		
		
		private IVector2 validFinalDestination(final IVector2 obstacle)
		{
			IVector2 protectFromBallDest = LineMath.stepAlongLine(obstacle, getBall().getPos(), minDistToOpponent);
			if (extendedPenArea.isPointInShape(protectFromBallDest))
			{
				// disturb opponent
				double pushingDist = Geometry.getBotRadius();
				List<IVector2> points = new ArrayList<>(2);
				points.add(extendedPenArea.stepAlongPenArea(obstacle, pushingDist));
				points.add(extendedPenArea.stepAlongPenArea(obstacle, -pushingDist));
				return getPos().nearestTo(points);
			}
			return protectFromBallDest;
		}
		
		
		private IVector2 movePosAway(final IVector2 pos, final IVector2 obstacle, double margin)
		{
			ILine posToGoal = Line.fromPoints(Geometry.getGoalOur().getCenter(), pos);
			IVector2 dir = posToGoal.directionVector();
			
			ILine posToCircleBorder = Line.fromDirection(pos, dir);
			ICircle forbiddenArea = Circle.createCircle(obstacle, margin);
			
			return forbiddenArea.lineSegmentIntersections(posToCircleBorder).stream()
					.max(Comparator.comparingDouble(v -> v.distanceTo(Geometry.getGoalOur().getCenter()))).orElse(pos);
		}
	}
	
	
	public static double getMinDistToOpponent()
	{
		return minDistToOpponent;
	}
}
