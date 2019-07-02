/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.IPathFinder;
import edu.tigers.sumatra.pathfinder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Abstract base for trajectory path finders. It mainly implements the logic to create trajectory paths and collisions,
 * but not the actual algorithm to find the best path.
 */
public abstract class ATrajPathFinder implements IPathFinder
{
	@Configurable(defValue = "2.0", comment = "Lookahead [s] for collision detection. Collisions after this time are not considered. Shorter paths are still checked for collisions until this lookahead.")
	private static double collisionLookahead = 2.0;

	@Configurable(defValue = "5.0", comment = "Fixed penalty to add to a path with a collision")
	private static double collisionPenalty = 5.0;

	static
	{
		ConfigRegistration.registerClass("sisyphus", ATrajPathFinder.class);
	}

	private TrajectoryWithTime<IVector2> lastTraj = null;


	/**
	 * @param input all necessary input data for planning the path
	 * @return a path if one was found
	 */
	@Override
	public TrajPathCollision calcPath(final PathFinderInput input)
	{
		TrajPathCollision pathCollision = generatePath(input);
		lastTraj = new TrajectoryWithTime<>(pathCollision.getTrajPath(), input.getTimestamp());
		return pathCollision;
	}


	private double getCollisionLookahead()
	{
		return collisionLookahead;
	}


	protected IVector2 getDirection(final PathFinderInput input)
	{
		IVector2 toDest = input.getDest().subtractNew(input.getPos());
		IVector2 dir = toDest;
		if ((lastTraj != null) && !lastTraj.getAcceleration(input.getTimestamp()).isZeroVector())
		{
			dir = lastTraj.getAcceleration(input.getTimestamp());
		}
		return dir;
	}


	protected TrajPathCollision getPath(final PathFinderInput input, final IVector2 dest)
	{
		IVector2 pos = input.getPos();
		IVector2 vel = input.getVel();
		TrajPath path = TrajPath.with(input.getMoveConstraints(), pos, vel, dest);
		return getCollision(input, path, null, 0);
	}


	protected abstract TrajPathCollision generatePath(PathFinderInput input);


	private double getFirstNonCollisionTime(final TrajPath curPath,
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


	private double getLastNonCollisionTime(final TrajPath curPath,
			final List<IObstacle> obstacles)
	{
		int i = 0;
		double tUpper = Math.max(getCollisionLookahead(), curPath.getTotalTime());

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


	private TimeCollision getFirstCollisionTime(final PathFinderInput input, final TrajPath curPath,
			final double tStart, final double tEnd)
	{
		double t = tStart;
		int i = 0;
		double tUpper = Math.min(tEnd, getCollisionLookahead());
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


	private Optional<IObstacle> firstCollidingObstacle(final TrajPath curPath, final List<IObstacle> obstacles,
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


	protected TrajPathCollision getCollision(final PathFinderInput input, final TrajPath curPath,
			final TrajPathCollision cachedCollision, final double tCachedOffset)
	{
		assert curPath != null;
		TrajPathCollision pCollision = new TrajPathCollision();
		pCollision.setTrajPath(curPath);
		pCollision.setCollisionLookahead(getCollisionLookahead());

		double firstNonCollisionTime;
		if ((cachedCollision != null) && (cachedCollision.getCollisionDurationFront() < tCachedOffset))
		{
			firstNonCollisionTime = cachedCollision.getCollisionDurationFront();
		} else
		{
			firstNonCollisionTime = getFirstNonCollisionTime(curPath, input.getObstacles());
		}
		pCollision.setCollisionDurationFront(firstNonCollisionTime);

		double tEnd = curPath.getTotalTime();
		double lastNonCollisionTime = getLastNonCollisionTime(curPath, input.getObstacles());
		pCollision.setCollisionDurationBack(Math.max(0, tEnd - lastNonCollisionTime));

		if ((cachedCollision != null) && (cachedCollision.getFirstCollisionTime() < tCachedOffset))
		{
			pCollision.setFirstCollisionTime(cachedCollision.getFirstCollisionTime());
			pCollision.setCollider(cachedCollision.getCollider().orElse(null));
		} else
		{
			TimeCollision timeCollision = getFirstCollisionTime(
					input,
					curPath,
					Math.max(tCachedOffset, pCollision.getCollisionDurationFront()),
					lastNonCollisionTime);
			pCollision.setFirstCollisionTime(timeCollision.t);
			pCollision.setCollider(timeCollision.obstacle);
		}
		pCollision.setPenaltyScore(ratePathCollision(pCollision));
		return pCollision;
	}


	private double ratePathCollision(TrajPathCollision p)
	{
		// the longer the path, the more penalty is added
		double penalty = p.getTrajPath().getTotalTime();

		if (p.hasCollision())
		{
			penalty += collisionPenalty;
		} else if (p.getTrajPath().getTotalTime() >= collisionLookahead)
		{
			final IVector2 lookaheadPos = p.getTrajPath().getPosition(collisionLookahead);
			final IVector2 finalPos = p.getTrajPath().getPosition(p.getTrajPath().getTotalTime());
			penalty += lookaheadPos.distanceTo(finalPos);
		}

		if (p.hasIntermediateCollision())
		{
			// more penalty, the sooner the collision will happen
			penalty += Math.max(0, collisionLookahead - p.getFirstCollisionTime());
		}

		if (!p.isAlwaysColliding())
		{
			// adding a penalty to front collision produces paths that first try to leave the obstacle
			// this is useful for bot obstacles: instead of trying to drive through the obstacle, drive a bit backward and
			// around
			penalty += 3 * p.getCollisionDurationFront();
		}

		return penalty;
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
