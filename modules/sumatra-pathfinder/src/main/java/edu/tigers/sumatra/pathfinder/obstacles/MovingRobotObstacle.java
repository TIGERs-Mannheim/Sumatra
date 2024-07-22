/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;

import java.awt.Color;
import java.util.List;


public class MovingRobotObstacle extends AMovingRobotObstacle
{
	public MovingRobotObstacle(BotID botID, IMovingRobot movingRobot, double minSpeed, double maxHorizon)
	{
		super(botID, movingRobot, minSpeed, maxHorizon);
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		ICircle movingHorizon = movingRobot.getMovingHorizon(input.getTimeOffset());
		if (movingHorizon.isPointInShape(input.getRobotPos()))
		{
			return 0;
		}
		return movingHorizon.distanceTo(input.getRobotPos());
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableCircle(movingRobot.getMovingHorizon(0)),
				new DrawableCircle(movingRobot.getMovingHorizon(maxHorizon)).setColor(Color.gray)
		);
	}
}
