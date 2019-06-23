/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 25, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.path.TrajPathGen;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.path.TrajPathV2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderV4 implements ITrajPathFinder
{
	@SuppressWarnings("unused")
	private static final Logger				log						= Logger
			.getLogger(TrajPathFinderV4.class.getName());
	
	private TrajectoryWithTime<IVector2>	lastTraj					= null;
	private IVector2								lastSubDest				= null;
	private TrajPathGen							gen						= new TrajPathGen();
	
	
	private double									maxProcessingTime		= 0;
	private double									sumProcessingTime		= 0;
	private double									countProcessingTime	= 0;
	
	
	/**
	 */
	public TrajPathFinderV4()
	{
	}
	
	
	/**
	 * @param input
	 * @return
	 */
	@Override
	public Optional<TrajectoryWithTime<IVector2>> calcPath(final TrajPathFinderInput input)
	{
		input.getDebugShapes().clear();
		List<IObstacle> obstacles = new ArrayList<>(input.getObstacles());
		
		if (input.isFastStop())
		{
			return Optional.empty();
		}
		
		long t0 = System.nanoTime();
		TrajPathV2 newPath = generatePath(input, obstacles);
		
		long t1 = System.nanoTime();
		double t = (t1 - t0) / 1e6;
		if (t > maxProcessingTime)
		{
			maxProcessingTime = t;
		}
		
		if (newPath == null)
		{
			return Optional.empty();
		}
		
		if (newPath.getTotalTime() > 0.1)
		{
			sumProcessingTime += t;
			countProcessingTime++;
			double avg = sumProcessingTime / countProcessingTime;
			if ((avg > 5) && (countProcessingTime > 10))
			{
				// log.warn(String.format("%.1f | %.1f | %.1f", t, avg, maxProcessingTime));
			}
		}
		
		lastTraj = new TrajectoryWithTime<>(newPath, input.getTimestamp());
		return Optional.of(lastTraj);
	}
	
	
	private TrajPathV2 generatePath(
			final TrajPathFinderInput input,
			final List<IObstacle> obstacles)
	{
		IVector2 toDest = input.getDest().subtractNew(input.getPos());
		IVector2 dir = toDest.scaleToNew(Math.max(100, toDest.getLength()));
		if ((lastTraj != null) && !lastTraj.getAcceleration(input.getTimestamp()).isZeroVector())
		{
			dir = lastTraj.getAcceleration(input.getTimestamp()).scaleToNew(dir.getLength());
		}
		
		PathCollision bestPathCollision = getPath(input, input.getDest(), obstacles);
		if (bestPathCollision.isOptimal())
		{
			return bestPathCollision.trajPath;
		}
		
		if (lastSubDest != null)
		{
			bestPathCollision = getDestPath(input, obstacles, lastSubDest, bestPathCollision);
		}
		
		double angleStep = 0.4;
		double angleMax = AngleMath.PI;
		double angle = 0;
		int timeoutSteps = 10;
		int timeoutCtr = -1;
		do
		{
			angle *= -1;
			if (angle >= 0)
			{
				angle += angleStep;
			}
			
			for (double len = 100; len <= 3100; len += 1000)
			{
				IVector2 subDest = input.getPos().addNew(dir.turnNew(angle).scaleTo(len));
				
				bestPathCollision = getDestPath(input, obstacles, subDest, bestPathCollision);
				if ((timeoutCtr < 0) && bestPathCollision.isValid())
				{
					timeoutCtr = timeoutSteps;
				}
			}
			timeoutCtr--;
		} while ((Math.abs(angle) < angleMax) && (timeoutCtr != 0));
		
		if (bestPathCollision.isValid())
		{
			// full path found!
			return bestPathCollision.trajPath;
		}
		
		input.getMoveCon().getMoveConstraints().setVelMax(1.0);
		
		return bestPathCollision.trajPath;
	}
	
	
	private PathCollision getDestPath(final TrajPathFinderInput input,
			final List<IObstacle> obstacles, final IVector2 subDest, PathCollision bestPathCollision)
	{
		PathCollision subPathCollision = getPath(input, subDest, obstacles);
		PathCollision pc = subPathCollision;
		for (double t = 0.2; t <= subPathCollision.getLastValidTime(); t += 0.2)
		{
			TrajPathV2 path = gen.append(subPathCollision.trajPath, input.getMoveCon().getMoveConstraints(), t,
					input.getDest());
			pc = getCollision(input, path, obstacles, pc, t);
			if ((bestPathCollision == null) || pc.isBetterThan(bestPathCollision))
			{
				lastSubDest = subDest;
				bestPathCollision = pc;
			}
		}
		return bestPathCollision;
	}
	
	
	private PathCollision getPath(final TrajPathFinderInput input, final IVector2 dest, final List<IObstacle> obstacles)
	{
		IVector2 pos = input.getPos();
		IVector2 vel = input.getVel();
		TrajPathV2 path = gen.pathTo(input.getMoveCon().getMoveConstraints(), pos, vel, dest);
		return getCollision(input, path, obstacles, null, 0);
	}
	
	
	private double getFirstNonCollisionTime(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final List<IObstacle> obstacles)
	{
		double t = 0;
		int i = 0;
		while (t < curPath.getTotalTime())
		{
			if (isColliding(curPath, obstacles, t))
			{
				i++;
				t = (i * 0.05);
				continue;
			}
			return t;
		}
		return curPath.getTotalTime();
	}
	
	
	private double getLastNonCollisionTime(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final List<IObstacle> obstacles, final double tEnd)
	{
		int i = 0;
		double t = tEnd;
		while (t > 0)
		{
			if (isColliding(curPath, obstacles, t))
			{
				i++;
				t = tEnd - (0.05 * i);
				continue;
			}
			return t;
		}
		return 0;
	}
	
	
	private double getFirstCollisionTime(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final List<IObstacle> obstacles, final double tStart, final double tEnd)
	{
		double t = tStart;
		int i = 0;
		while (t < tEnd)
		{
			if (isColliding(curPath, obstacles, t))
			{
				return t;
			}
			i++;
			t = tStart + (i * input.getCollisionStepSize());
		}
		return Double.POSITIVE_INFINITY;
	}
	
	
	private boolean isColliding(final TrajPathV2 curPath, final List<IObstacle> obstacles, final double t)
	{
		IVector2 pos = curPath.getPositionMM(t);
		double vel = curPath.getVelocity(t).getLength();
		double extraMargin = ObstacleGenerator.getExtraMargin(vel);
		for (IObstacle obs : obstacles)
		{
			if (obs.isPointCollidingWithObstacle(pos, t, extraMargin))
			{
				return true;
			}
		}
		return false;
	}
	
	
	private PathCollision getCollision(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final List<IObstacle> obstacles, final PathCollision cachedCollision, final double tCachedOffset)
	{
		PathCollision pCollision = new PathCollision();
		pCollision.trajPath = curPath;
		
		if ((cachedCollision != null) && (cachedCollision.collisionDurationFront < tCachedOffset))
		{
			pCollision.collisionDurationFront = cachedCollision.collisionDurationFront;
		} else
		{
			pCollision.collisionDurationFront = getFirstNonCollisionTime(input, curPath, obstacles);
		}
		
		double tEnd = input.getMinTrajTime() + curPath.getTotalTime();
		double lastNonCollisionTime = getLastNonCollisionTime(input, curPath, obstacles, tEnd);
		pCollision.collisionDurationBack = tEnd - lastNonCollisionTime;
		
		if ((cachedCollision != null) && (cachedCollision.firstCollisionTime < tCachedOffset))
		{
			pCollision.firstCollisionTime = cachedCollision.firstCollisionTime;
		} else
		{
			pCollision.firstCollisionTime = getFirstCollisionTime(input, curPath, obstacles,
					Math.max(tCachedOffset, pCollision.collisionDurationFront),
					lastNonCollisionTime);
		}
		return pCollision;
	}
	
	
	private static class PathCollision
	{
		TrajPathV2	trajPath;
		double		collisionDurationFront;
		double		collisionDurationBack;
		double		firstCollisionTime;
		
		
		double getLastValidTime()
		{
			return Math.min(firstCollisionTime, trajPath.getTotalTime());
		}
		
		
		boolean isBetterThan(final PathCollision pCollision)
		{
			double firstCollisionTimeDiff = firstCollisionTime - pCollision.firstCollisionTime;
			if (Math.abs(firstCollisionTimeDiff) > 0.1)
			{
				if (firstCollisionTimeDiff > 0)
				{
					return true;
				}
				return false;
			}
			double firstNonCollisionTimeDiff = collisionDurationFront - pCollision.collisionDurationFront;
			if (Math.abs(firstNonCollisionTimeDiff) > 0.1)
			{
				if (firstNonCollisionTimeDiff < 0)
				{
					return true;
				}
				return false;
			}
			double collisionDurationBackDiff = collisionDurationBack - pCollision.collisionDurationBack;
			if (Math.abs(collisionDurationBackDiff) > 0.1)
			{
				if (collisionDurationBackDiff < 0)
				{
					return true;
				}
				return false;
			}
			double totalTimeDiff = (trajPath.getTotalTime()) - (pCollision.trajPath.getTotalTime());
			if (Math.abs(totalTimeDiff) > 0.1)
			{
				if (totalTimeDiff < 0)
				{
					return true;
				}
				return false;
			}
			return false;
		}
		
		
		boolean isOptimal()
		{
			return isValid() &&
					(collisionDurationFront <= 0) &&
					(collisionDurationBack <= 0);
		}
		
		
		boolean isValid()
		{
			return !Double.isFinite(firstCollisionTime);
		}
	}
}
