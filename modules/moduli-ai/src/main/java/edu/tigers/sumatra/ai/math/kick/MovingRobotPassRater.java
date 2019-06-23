/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math.kick;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MovingRobotPassRater
{
	@Configurable(defValue = "0.5")
	private static double maxHorizon = 0.5;
	
	@Configurable(defValue = "0.1")
	private static double stepSize = 0.1;
	
	static
	{
		ConfigRegistration.registerClass("metis", MovingRobotPassRater.class);
	}
	
	private Collection<ITrackedBot> bots;
	
	
	/**
	 * A new rater
	 * 
	 * @param bots the bots to consider
	 */
	public MovingRobotPassRater(final Collection<ITrackedBot> bots)
	{
		this.bots = bots;
	}
	
	
	/**
	 * Rate a ball travel line from start to some pass or goal target
	 * 
	 * @param start the start of the kick
	 * @param target the target
	 * @param kickSpeed the kick speed to consider
	 * @return a score between 0 (bad) and 1 (good)
	 */
	public double rateLine(IVector2 start, IVector2 target, double kickSpeed)
	{
		IVector3 kick = Vector3.from2d(target.subtractNew(start).scaleTo(kickSpeed * 1000), 0);
		IBallTrajectory ballTraj = BallFactory.createTrajectoryFromKick(start, kick, false);
		double radius = Geometry.getBotRadius() + Geometry.getBallRadius();
		double tMax = ballTraj.getTimeByPos(target);
		
		List<MovingRobot> movingRobots = bots.stream()
				.map(tBot -> new MovingRobot(tBot, maxHorizon, radius))
				.collect(Collectors.toList());
		
		int numPoints = Math.max(1, (int) (tMax / stepSize));
		int numHits = 0;
		for (double t = 0; t < tMax; t += stepSize)
		{
			ILine ballLine = Line.fromPoints(start, target);
			for (MovingRobot movingRobot : movingRobots)
			{
				ICircle circle = movingRobot.getCircle(t);
				if (!circle.lineSegmentIntersections(ballLine).isEmpty())
				{
					numHits++;
					break;
				}
			}
		}
		return 1 - Math.min(1, (double) numHits / numPoints);
	}
}
