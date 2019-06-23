/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent
public class TurnAroundBallObstacle extends Circle
{
	private final TrackedBall	ball;
	private final IVector2		destination;
	
	
	@SuppressWarnings("unused")
	private TurnAroundBallObstacle()
	{
		super();
		ball = null;
		destination = null;
		radius = 0;
	}
	
	
	/**
	 * @param destination
	 * @param ball
	 * @param radius
	 */
	public TurnAroundBallObstacle(final IVector2 destination, final TrackedBall ball, final float radius)
	{
		this.destination = destination;
		this.ball = ball;
		this.radius = radius;
		center = ball.getPos();
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 destToBall = ball.getPos().subtractNew(destination);
		subPoints.add(ball.getPos().addNew(
				destToBall.getNormalVector().multiplyNew(-radius - (1.5f * AIConfig.getGeometry().getBotRadius()))));
		subPoints.add(ball.getPos().addNew(
				destToBall.getNormalVector().multiplyNew(radius + (1.5f * AIConfig.getGeometry().getBotRadius()))));
	}
	
}
