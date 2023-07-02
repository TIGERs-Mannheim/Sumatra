/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class MovingRobotTubeObstacle extends AObstacle
{
	private final BotID botID;
	private final MovingRobot movingRobot;
	private final double minSpeed;


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
		ITube movingHorizon = movingRobot.getMovingHorizonTube(input.getTimeOffset());
		return movingHorizon.distanceTo(input.getRobotPos());
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return input.getRobotVel().getLength2() > minSpeed || input.accelerating();
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableTube(movingRobot.getMovingHorizonTube(0)),
				new DrawableTube(movingRobot.getMovingHorizonTube(movingRobot.getMaxHorizon()))
		);
	}
}
