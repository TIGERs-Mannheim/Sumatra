/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;


/**
 * Generate covered angles from an arbitrary triangular range
 */
public class AngleRangeGenerator
{
	private IVector2 start;
	private IVector2 endLeft;
	private IVector2 endRight;
	
	private double kickSpeed;
	private IStraightBallConsultant ballConsultant;
	private Map<BotID, MovingRobot> movingRobots = new HashMap<>();
	private Set<BotID> excludedBots = Collections.emptySet();
	
	private double timeToKick = 0;
	private double timeForBotToReact = 0;
	
	
	/**
	 * Generate the covered angles
	 * 
	 * @return a list of all found covered angles within the given range
	 */
	public List<AngleRange> findCoveredAngleRanges()
	{
		List<AngleRange> coveredAngles = new ArrayList<>();
		
		ITriangle triangle = createTriangle();
		IVector2 endCenter = TriangleMath.bisector(start, endLeft, endRight);
		IVector2 startToEndCenter = endCenter.subtractNew(start);
		
		List<MovingRobot> robots = movingRobots.entrySet().stream()
				.filter(entry -> !excludedBots.contains(entry.getKey()))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		for (MovingRobot movingRobot : robots)
		{
			double distBotToStart = movingRobot.getPos().distanceTo(start);
			double timeBallToBot = ballConsultant.getTimeForKick(distBotToStart, kickSpeed) + timeToKick;
			double horizon = Math.max(0, timeBallToBot - timeForBotToReact);
			ICircle obstacleCircle = movingRobot.getMovingHorizon(horizon);
			List<IVector2> intersections = obstacleCircle.tangentialIntersections(start);
			if (triangle.isPointInShape(movingRobot.getPos())
					|| intersections.stream().anyMatch(triangle::isPointInShape))
			{
				addAngleRange(coveredAngles, startToEndCenter, intersections);
			}
		}
		
		return coveredAngles;
	}
	
	
	private void addAngleRange(final List<AngleRange> coveredAngles, final IVector2 startToEndCenter,
			final List<IVector2> intersections)
	{
		Optional<Double> leftAngle = startToEndCenter.angleTo(intersections.get(1).subtractNew(start));
		Optional<Double> rightAngle = startToEndCenter.angleTo(intersections.get(0).subtractNew(start));
		if (leftAngle.isPresent() && rightAngle.isPresent())
		{
			if (rightAngle.get() < leftAngle.get())
			{
				coveredAngles.add(new AngleRange(rightAngle.get(), leftAngle.get()));
			} else
			{
				coveredAngles.add(new AngleRange(leftAngle.get(), rightAngle.get()));
			}
		}
	}
	
	
	/**
	 * Generate all uncovered angle ranges
	 * 
	 * @return all uncovered angle ranges
	 */
	public List<AngleRange> findUncoveredAngleRanges()
	{
		return findUncoveredAngleRanges(findCoveredAngleRanges(), getAngleRange());
	}
	
	
	/**
	 * Generate all uncovered angle ranges
	 * 
	 * @param coveredAngles
	 * @param fullRange
	 * @return
	 */
	public List<AngleRange> findUncoveredAngleRanges(final List<AngleRange> coveredAngles, final AngleRange fullRange)
	{
		coveredAngles.sort(Comparator.comparingDouble(AngleRange::getRightAngle));
		
		List<AngleRange> uncoveredAngles = new ArrayList<>();
		uncoveredAngles.add(fullRange);
		
		for (AngleRange r : coveredAngles)
		{
			List<AngleRange> newUncoveredAngles = new ArrayList<>();
			for (AngleRange c : uncoveredAngles)
			{
				newUncoveredAngles.addAll(c.cutOutRange(r));
			}
			uncoveredAngles = newUncoveredAngles;
		}
		
		return uncoveredAngles;
	}
	
	
	/**
	 * @return the full angle range to be considered
	 */
	public AngleRange getAngleRange()
	{
		IVector2 start2rightEnd = endRight.subtractNew(start);
		IVector2 start2leftEnd = endLeft.subtractNew(start);
		double width = start2leftEnd.angleToAbs(start2rightEnd).orElse(0.0);
		double rightAngle = -width / 2;
		double leftAngle = width / 2;
		return new AngleRange(rightAngle, leftAngle);
	}
	
	
	/**
	 * @return a triangle from start, left and right end + margin for the ball
	 */
	public ITriangle createTriangle()
	{
		return Triangle.fromCorners(start, endLeft, endRight);
	}
	
	
	public IVector2 getStart()
	{
		return start;
	}
	
	
	public void setStart(final IVector2 start)
	{
		this.start = start;
	}
	
	
	public IVector2 getEndLeft()
	{
		return endLeft;
	}
	
	
	public void setEndLeft(final IVector2 endLeft)
	{
		this.endLeft = endLeft;
	}
	
	
	public IVector2 getEndRight()
	{
		return endRight;
	}
	
	
	public void setEndRight(final IVector2 endRight)
	{
		this.endRight = endRight;
	}
	
	
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	public void setTimeToKick(final double timeToKick)
	{
		this.timeToKick = timeToKick;
	}
	
	
	public Map<BotID, MovingRobot> getMovingRobots()
	{
		return movingRobots;
	}
	
	
	public void setMovingRobots(final Map<BotID, MovingRobot> movingRobots)
	{
		this.movingRobots = movingRobots;
	}
	
	
	public void setBallConsultant(final IStraightBallConsultant ballConsultant)
	{
		this.ballConsultant = ballConsultant;
	}
	
	
	public void setTimeForBotToReact(final double timeForBotToReact)
	{
		this.timeForBotToReact = timeForBotToReact;
	}
	
	
	public void setExcludedBots(final Set<BotID> excludedBots)
	{
		this.excludedBots = excludedBots;
	}
}
