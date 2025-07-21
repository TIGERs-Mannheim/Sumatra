/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.util.List;


public abstract class AInterceptBallDestinationCalculator extends AKeeperDestinationCalculator
{
	@Configurable(comment = "The radius to try intercepting the chip-kicked ball within", defValue = "500.0")
	protected static double maxChipInterceptDist = 500.0;

	static
	{
		ConfigRegistration.registerClass("skills", AInterceptBallDestinationCalculator.class);
	}


	protected DestWithTrajectory buildDestWithTrajectory(IVector2 dest, double tt)
	{
		var trajectory = TrajectoryGenerator.generatePositionTrajectoryToReachPointInTime(getTBot(), dest, tt);
		var posAtTt = trajectory.getPositionMM(tt);
		var distAtTt = posAtTt.distanceTo(dest);
		var velAtTt = trajectory.getVelocity(tt).getLength();
		var timeLeftAtTt = tt - trajectory.getTotalTime();
		var canReach = distAtTt < Geometry.getBotRadius();
		var canReachCompletely = canReach && velAtTt < 1e-3;
		var distanceToGoalLine = Geometry.getGoalOur().getLineSegment().distanceTo(dest);
		var isCloseToGoalLine = distanceToGoalLine < Geometry.getBotRadius() * 3;
		return new DestWithTrajectory(dest, trajectory, distAtTt, velAtTt, timeLeftAtTt, distanceToGoalLine, canReach,
				canReachCompletely, isCloseToGoalLine);
	}


	protected IVector2 findPointOnBallTraj()
	{

		IVector2 leadPoint = Lines.lineFromDirection(getBall().getPos(), getBall().getVel()).closestPointOnPath(getPos());
		if (getBall().isChipped())
		{
			IVector2 nearestTouchdown = getNearestChipTouchdown();
			if ((nearestTouchdown != null) && (nearestTouchdown.distanceTo(getPos()) < maxChipInterceptDist))
			{
				// Get some distance between touchdown and bot
				double distance = Geometry.getBotRadius() + Geometry.getBallRadius();
				IVector2 direction = nearestTouchdown.subtractNew(getBall().getPos()).scaleTo(distance);

				leadPoint = nearestTouchdown.addNew(direction);
			}
		}
		return leadPoint;
	}


	private IVector2 getNearestChipTouchdown()
	{

		ITrackedBall ball = getWorldFrame().getBall();
		List<IVector2> touchdowns = ball.getTrajectory().getTouchdownLocations();

		IVector2 nearestPoint = null;
		double min = -1;
		for (IVector2 td : touchdowns)
		{
			double dist = td.distanceTo(getPos());

			if ((min < 0) || ((dist < min) && (td.x() > Geometry.getGoalOur().getCenter().x())))
			{
				min = dist;
				nearestPoint = td;
			}
		}

		return nearestPoint;
	}


	protected record DestWithTrajectory(
			IVector2 dest,
			ITrajectory<IVector2> trajectory,
			double distAtTt,
			double velAtTt,
			double timeLeftAtTt,
			double distanceToGoalLine,
			boolean canReach,
			boolean canReachCompletely,
			boolean isCloseToGoalLine
	) implements Comparable<DestWithTrajectory>
	{
		/**
		 * Compare such that the "highest" object is the best object
		 *
		 * @param other
		 * @return -1 if other is better, 1 if this is better
		 */
		@Override
		public int compareTo(DestWithTrajectory other)
		{
			if (!canReach)
			{
				if (other.canReach)
				{
					// We can't reach, but other can -> other wins
					return -1;
				} else
				{
					// Smaller distance wins
					return -Double.compare(distAtTt, other.distAtTt);
				}
			}

			// we.canReach == true
			if (!canReachCompletely)
			{
				if (other.canReachCompletely)
				{
					// We can't reach completely, but other can -> other wins
					return -1;
				} else if (!other.canReach)
				{
					// We can reach, but other can't -> we win
					return 1;
				} else
				{
					// Smaller velocity wins
					return -Double.compare(velAtTt, other.velAtTt);
				}
			}

			// we.canReach == true
			// we.canReachCompletely == true
			if (isCloseToGoalLine)
			{
				if (!other.canReachCompletely)
				{
					// We can reach completely, but other can't -> we win
					return 1;
				} else if (!other.isCloseToGoalLine)
				{
					// We are close, other not -> other win
					return -1;
				} else
				{
					// Bigger distance to goal line wins
					return Double.compare(distanceToGoalLine, other.distanceToGoalLine);
				}
			}

			// we.canReach == true
			// we.canReachCompletely == true
			// we.isClosToGoalLine == false
			if (other.isCloseToGoalLine)
			{
				// Other is close, we are not -> we win
				return 1;
			}
			// Higher timeLeft wins
			return Double.compare(timeLeftAtTt, other.timeLeftAtTt);
		}
	}
}
