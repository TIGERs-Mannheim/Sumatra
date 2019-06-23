/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.ITrajPathFinder;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Abstract base for trajectory path finders
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ATrajPathFinder implements ITrajPathFinder
{
	@Configurable(defValue = "2.0")
	private static double collisionLookahead = 2;
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", ATrajPathFinder.class);
	}
	
	protected TrajPathGen gen = new TrajPathGen();
	private TrajectoryWithTime<IVector2> lastTraj = null;
	
	
	/**
	 * @param input all necessary input data for planning the path
	 * @return a path if one was found
	 */
	@Override
	public PathCollision calcPath(final TrajPathFinderInput input)
	{
		PathCollision pathCollision = generatePath(input);
		lastTraj = new TrajectoryWithTime<>(pathCollision.getTrajPath(), input.getTimestamp());
		return pathCollision;
	}
	
	
	private double getCollisionLookahead()
	{
		return collisionLookahead;
	}
	
	
	protected IVector2 getDirection(final TrajPathFinderInput input)
	{
		IVector2 toDest = input.getDest().subtractNew(input.getPos());
		IVector2 dir = toDest.scaleToNew(Math.max(100, toDest.getLength()));
		if ((lastTraj != null) && !lastTraj.getAcceleration(input.getTimestamp()).isZeroVector())
		{
			dir = lastTraj.getAcceleration(input.getTimestamp()).scaleToNew(dir.getLength());
		}
		return dir;
	}
	
	
	protected PathCollision getPath(final TrajPathFinderInput input, final IVector2 dest)
	{
		IVector2 pos = input.getPos();
		IVector2 vel = input.getVel();
		TrajPathV2 path = gen.pathTo(input.getMoveConstraints(), pos, vel, dest);
		return getCollision(input, path, null, 0);
	}
	
	
	protected abstract PathCollision generatePath(TrajPathFinderInput input);
	
	
	private double getFirstNonCollisionTime(final TrajPathV2 curPath,
			final List<IObstacle> obstacles)
	{
		double t = 0;
		int i = 0;
		double tUpper = getCollisionLookahead();
		while (t < tUpper)
		{
			if (firstCollidingObstacle(curPath, obstacles, t).isPresent())
			{
				i++;
				t = i * 0.05;
				continue;
			}
			return t;
		}
		return tUpper;
	}
	
	
	private double getLastNonCollisionTime(final TrajPathV2 curPath,
			final List<IObstacle> obstacles)
	{
		int i = 0;
		double tUpper = curPath.getTotalTime();
		
		double t = tUpper;
		while (t >= 0)
		{
			Optional<IObstacle> collider = firstCollidingObstacle(curPath, obstacles, t);
			if (collider.isPresent())
			{
				i++;
				t = tUpper - (0.1 * i);
				continue;
			}
			return t;
		}
		return 0;
	}
	
	
	private TimeCollision getFirstCollisionTime(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final double tStart)
	{
		double t = tStart;
		int i = 0;
		double tUpper = getCollisionLookahead();
		while (t < tUpper)
		{
			Optional<IObstacle> collider = firstCollidingObstacle(curPath, input.getObstacles(), t);
			if (collider.isPresent())
			{
				return new TimeCollision(t, collider.get());
			}
			i++;
			t = tStart + (i * input.getCollisionStepSize());
		}
		return new TimeCollision(Double.POSITIVE_INFINITY, null);
	}
	
	
	private Optional<IObstacle> firstCollidingObstacle(final TrajPathV2 curPath, final List<IObstacle> obstacles,
			final double t)
	{
		IVector2 pos = curPath.getPositionMM(t);
		double vel = curPath.getVelocity(t).getLength();
		double extraMargin = ObstacleGenerator.getExtraMargin(vel);
		for (IObstacle obs : obstacles)
		{
			if (obs.isPointCollidingWithObstacle(pos, t, extraMargin))
			{
				return Optional.of(obs);
			}
		}
		return Optional.empty();
	}
	
	
	protected PathCollision getCollision(final TrajPathFinderInput input, final TrajPathV2 curPath,
			final PathCollision cachedCollision, final double tCachedOffset)
	{
		assert curPath != null;
		PathCollision pCollision = new PathCollision();
		pCollision.setTrajPath(curPath);
		pCollision.setCollisionLookahead(getCollisionLookahead());
		
		if ((cachedCollision != null) && (cachedCollision.getCollisionDurationFront() < tCachedOffset))
		{
			pCollision.setCollisionDurationFront(cachedCollision.getCollisionDurationFront());
		} else
		{
			pCollision.setCollisionDurationFront(getFirstNonCollisionTime(curPath, input.getObstacles()));
		}
		
		double tEnd = curPath.getTotalTime();
		double lastNonCollisionTime = getLastNonCollisionTime(curPath, input.getObstacles());
		pCollision.setCollisionDurationBack(tEnd - lastNonCollisionTime);
		
		if ((cachedCollision != null) && (cachedCollision.getFirstCollisionTime() < tCachedOffset))
		{
			pCollision.setFirstCollisionTime(cachedCollision.getFirstCollisionTime());
		} else
		{
			TimeCollision timeCollision = getFirstCollisionTime(input, curPath,
					Math.max(tCachedOffset, pCollision.getCollisionDurationFront()));
			pCollision.setFirstCollisionTime(timeCollision.t);
			pCollision.setCollider(timeCollision.obstacle);
		}
		if (pCollision.getFirstCollisionTime() >= lastNonCollisionTime)
		{
			pCollision.setFirstCollisionTime(Double.POSITIVE_INFINITY);
		}
		return pCollision;
	}
	
	private static final class TimeCollision
	{
		double t;
		IObstacle obstacle;
		
		
		public TimeCollision(final double t, final IObstacle obstacle)
		{
			this.t = t;
			this.obstacle = obstacle;
		}
	}
}
