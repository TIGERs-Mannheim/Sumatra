/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;

import java.awt.Color;
import java.util.List;


public class MovingRobotTubeObstacle extends AMovingRobotObstacle
{
	public MovingRobotTubeObstacle(BotID botID, IMovingRobot movingRobot, double minSpeed, double maxHorizon)
	{
		super(botID, movingRobot, minSpeed, maxHorizon);
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		ITube movingHorizon = movingRobot.getMovingHorizonTube(input.getTimeOffset());
		return movingHorizon.distanceTo(input.getRobotPos());
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableTube(movingRobot.getMovingHorizonTube(0)),
				new DrawableTube(movingRobot.getMovingHorizonTube(maxHorizon)).setColor(Color.gray)
		);
	}
}
