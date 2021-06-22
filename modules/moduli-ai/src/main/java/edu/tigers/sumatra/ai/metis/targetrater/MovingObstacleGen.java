/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Generate circles from using {@link MovingRobot}s.
 */
public class MovingObstacleGen
{
	@Setter
	private double kickSpeed = RuleConstraints.getMaxBallSpeed();
	@Setter
	private double timeForBotToReact = 0.0;
	@Setter
	private double maxHorizon = Double.MAX_VALUE;


	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start, double timeToKick)
	{
		List<ICircle> circles = new ArrayList<>(bots.size());

		var ballConsultant = Geometry.getBallFactory().createFlatConsultant();
		for (var bot : bots)
		{
			MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius());
			double distBotToStart = movingRobot.getPos().distanceTo(start);
			double timeBallToBot = ballConsultant.getTimeForKick(distBotToStart, kickSpeed) + timeToKick;
			double horizon = Math.max(0, timeBallToBot - timeForBotToReact);
			circles.add(movingRobot.getMovingHorizon(horizon));
		}
		return circles;
	}
}
