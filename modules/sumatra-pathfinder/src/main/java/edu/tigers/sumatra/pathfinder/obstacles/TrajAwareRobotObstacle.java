/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import static edu.tigers.sumatra.math.SumatraMath.relative;
import static edu.tigers.sumatra.math.SumatraMath.square;


/**
 * An obstacle for a robot that is based on the current trajectory path of the robot
 */
@RequiredArgsConstructor
public class TrajAwareRobotObstacle extends AMovingObstacle
{
	private static final double MAX_SPEED = 3;
	private static final double OBSTACLE_SHAPE_LOOKAHEAD = 0.3;

	private final BotID botID;
	private final ITrajectory<IVector3> trajectory;
	private final double radius;
	private final double tStart;
	private final double extraMarginBase;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + botID.getSaveableString();
	}


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		// always likely, as we know the trajectory
		return true;
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return true;
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		return trajectory.getVelocity(t).getXYVector();
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		var tt = tStart + input.getTimeOffset();
		var pos = trajectory.getPositionMM(tt).getXYVector();
		var vel = trajectory.getVelocity(tt);
		return pos.distanceTo(input.getRobotPos()) - getMargin(vel.getXYVector());
	}


	private double getMargin(IVector2 vel)
	{
		var extraMargin = square(relative(vel.getLength(), 0, MAX_SPEED)) * extraMarginBase;
		return radius + extraMargin;
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableCircle(getObstacleShape(tStart)),
				new DrawableCircle(getObstacleShape(trajectory.getTotalTime())).setColor(Color.gray)
		);
	}


	@Override
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return adaptDestinationForRobotPos(getObstacleShape(tStart, OBSTACLE_SHAPE_LOOKAHEAD), robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		if (hasPriority())
		{
			return adaptDestination(getObstacleShape(tStart, OBSTACLE_SHAPE_LOOKAHEAD), destination);
		}
		return Optional.empty();
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		// Only consider the obstacle if the robot and destination is not inside the obstacle at the start of the trajectory
		// This helps when robots are send to same destination intentionally, like for defenders.
		return getObstacleShape(tStart, OBSTACLE_SHAPE_LOOKAHEAD).isPointInShape(pos)
				|| getObstacleShape(trajectory.getTotalTime()).isPointInShape(pos);
	}


	private ICircle getObstacleShape(double t)
	{
		double margin = getMargin(trajectory.getVelocity(t).getXYVector());
		return Circle.createCircle(trajectory.getPositionMM(t).getXYVector(), margin);
	}


	private I2DShape getObstacleShape(double t, double duration)
	{
		double margin = getMargin(trajectory.getVelocity(t).getXYVector());
		double tEnd = Math.min(t + duration, trajectory.getTotalTime());
		return Tube.create(
				trajectory.getPositionMM(t).getXYVector(),
				trajectory.getPositionMM(tEnd).getXYVector(),
				margin
		);
	}
}
