/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static edu.tigers.sumatra.math.SumatraMath.relative;
import static edu.tigers.sumatra.math.SumatraMath.square;


/**
 * An obstacle for a robot that is based on the current trajectory path of the robot
 */
@RequiredArgsConstructor
public class TrajAwareRobotObstacle extends AObstacle
{
	private static final double MAX_SPEED = 3;

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
	protected void configure()
	{
		setMotionLess(false);
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		var tt = tStart + input.getTimeOffset();
		var pos = trajectory.getPositionMM(tt).getXYVector();
		var vel = trajectory.getVelocity(tt);
		return pos.distanceTo(input.getRobotPos()) - getMargin(vel);
	}


	private double getMargin(IVector3 vel)
	{
		var extraMargin = square(relative(vel.getLength(), 0, MAX_SPEED)) * extraMarginBase;
		return radius + extraMargin;
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		var botPos = trajectory.getPositionMM(tStart).getXYVector();
		var botVel = trajectory.getVelocity(tStart);
		return List.of(
				new DrawableCircle(Circle.createCircle(botPos, getMargin(botVel)))
		);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		double margin = getMargin(trajectory.getVelocity(0));
		ICircle circle = Circle.createCircle(trajectory.getPositionMM(0).getXYVector(), margin);
		return adaptDestination(circle, robotPos, destination);
	}
}
