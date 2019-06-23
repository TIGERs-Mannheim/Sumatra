/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent
public class TurnAroundBallObstacle extends CircleObstacle
{
	private final TrackedBall	ball;
	private final IVector2		destination;
	
	
	@SuppressWarnings("unused")
	private TurnAroundBallObstacle()
	{
		super(new Circle(AVector2.ZERO_VECTOR, 0));
		ball = null;
		destination = null;
	}
	
	
	/**
	 * @param destination
	 * @param ball
	 * @param radius
	 */
	public TurnAroundBallObstacle(final IVector2 destination, final TrackedBall ball, final double radius)
	{
		super(new Circle(ball.getPos(), radius));
		this.destination = destination;
		this.ball = ball;
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 destToBall = ball.getPos().subtractNew(destination);
		subPoints.add(ball.getPos().addNew(
				destToBall.getNormalVector().scaleTo(-radius() - (1.5f * Geometry.getBotRadius()))));
		subPoints.add(ball.getPos().addNew(
				destToBall.getNormalVector().scaleTo(radius() + (1.5f * Geometry.getBotRadius()))));
	}
}
