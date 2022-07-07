/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Optional;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AroundObstacleCalc
{
	private IVector2 obstacle;
	private IVector2 ballPos;
	private ITrackedBot bot;
	
	private double obstacleRadius = Geometry.getBotRadius();
	
	
	/**
	 * @param obstacle the obstacle position
	 * @param ballPos the current ball pos
	 * @param bot the bot to handle
	 */
	public AroundObstacleCalc(final IVector2 obstacle, final IVector2 ballPos, final ITrackedBot bot)
	{
		this.obstacle = obstacle;
		this.ballPos = ballPos;
		this.bot = bot;
	}
	
	
	/**
	 * Adapt the target orientation for around ball destination
	 * 
	 * @param targetOrientation the desired target orientation
	 * @return an adapted targetOrientation
	 */
	public double adaptTargetOrientation(final double targetOrientation)
	{
		IVector2 obs2Ball = getBallPos().subtractNew(obstacle);
		IVector2 obs2Pos = getPos().subtractNew(obstacle);
		if (obs2Ball.angleTo(obs2Pos).orElse(0.0) > 0)
		{
			return targetOrientation + 0.3;
		}
		
		return targetOrientation - 0.3;
	}
	
	
	/**
	 * Ensure the destination is not crossing the obstacle.<br>
	 * This is required to make sure that the robot does not try to drive "through" the obstacle
	 * 
	 * @param desiredDest the desired destination that will be checked
	 * @return the given desiredDest if it does not conflict; a non colliding dest else
	 */
	public IVector2 avoidObstacle(final IVector2 desiredDest)
	{
		ILine line = Line.fromPoints(getPos(), desiredDest);
		IVector2 lp = line.leadPointOf(obstacle);
		if (line.isPointOnLineSegment(lp) && (line.distanceTo(obstacle) < (obstacleRadius + Geometry.getBotRadius())))
		{
			IVector2 normal = lp.subtractNew(obstacle);
			return obstacle.addNew(normal.scaleToNew(obstacleRadius + Geometry.getBotRadius() + 15));
		}
		return desiredDest;
	}
	
	
	/**
	 * Check if an alternative destination is required to get the ball near the obstacle
	 * 
	 * @param desiredDestination the desired destination
	 * @return true, if alternative dest is needed
	 */
	public boolean isAroundObstacleNeeded(final IVector2 desiredDestination)
	{
		var botToBall = getBallPos().subtractNew(getPos());
		var ballToOpponent = obstacle.subtractNew(getBallPos());
		var angle = botToBall.angleToAbs(ballToOpponent).orElse(0.0);
		double obstacleToBall = obstacle.distanceTo(ballPos);
		double botToBallDist = desiredDestination.distanceTo(ballPos);
		return obstacleToBall - botToBallDist < obstacleRadius && angle > 30;
	}
	
	
	/**
	 * If the ball is near the obstacle, a destination as near as possible to the obstacle is returned.
	 * 
	 * @return
	 */
	public Optional<IVector2> getAroundObstacleDest()
	{
		double a = getDistance(0);
		double b = Math.max(Geometry.getBotRadius(), getBallPos().distanceTo(obstacle));
		double c = obstacleRadius + Geometry.getBotRadius();
		
		double gamma = SumatraMath.acos(((c * c) - (a * a) - (b * b)) / (-2.0 * a * b));
		if (!Double.isFinite(gamma))
		{
			return Optional.empty();
		}
		IVector2 turnBase = obstacle.subtractNew(getBallPos()).scaleTo(a);
		
		IVector2 obs2Ball = getBallPos().subtractNew(obstacle);
		IVector2 obs2Pos = getPos().subtractNew(obstacle);
		if (obs2Ball.angleTo(obs2Pos).orElse(0.0) > 0)
		{
			gamma *= -1;
		}
		
		return Optional.of(getBallPos().addNew(turnBase.turnNew(gamma)));
	}
	
	
	private double getDistance(final double margin)
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
	}
	
	
	private IVector2 getBallPos()
	{
		return ballPos;
	}
	
	
	private IVector2 getPos()
	{
		return bot.getPos();
	}
	
	
	private ITrackedBot getTBot()
	{
		return bot;
	}
}
