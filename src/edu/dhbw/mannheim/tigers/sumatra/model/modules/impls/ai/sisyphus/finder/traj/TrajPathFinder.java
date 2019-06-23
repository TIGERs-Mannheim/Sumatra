/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinder implements ITrajPathFinder
{
	@SuppressWarnings("unused")
	private static final Logger		log						= Logger.getLogger(TrajPathFinder.class.getName());
	
	private final Random					rnd						= new Random();
	private final ABot					bot;
	
	@Configurable
	private static float					onPathTolerance		= 200f;
	
	@Configurable
	private static float					newPathTimeTolerance	= 0.3f;
	
	
	private volatile TrajPath			curPath					= null;
	private List<IObstacle>				allCurrentObstacles	= new ArrayList<>();
	private long							tLastPath				= System.nanoTime();
	private final boolean				passive;
	
	private transient IVector2			lastDest					= null;
	private transient float				lastTargetAngle		= 0;
	private transient int				lastId					= -2;
	private transient TrajPathNode	lastNode					= null;
	
	
	/**
	 * @param bot
	 */
	public TrajPathFinder(final ABot bot)
	{
		this.bot = bot;
		passive = true;
	}
	
	
	/**
	 * @param bot
	 * @param passive
	 */
	public TrajPathFinder(final ABot bot, final boolean passive)
	{
		this.bot = bot;
		this.passive = passive;
	}
	
	
	/**
	 * @param input
	 * @return the best possible path, or null iff !forcePath and no path found without collision
	 */
	@Override
	public TrajPath calcPath(final TrajPathFinderInput input)
	{
		IVector2 pos = input.getPos();
		input.getDebugShapes().clear();
		
		allCurrentObstacles = input.getObstacles();
		List<IObstacle> obstacles = new ArrayList<>(input.getObstacles());
		
		// remove obstacles that lie within start pos
		obstacles.removeIf(obs -> obs.isPointCollidingWithObstacle(pos, 0));
		
		TrajPath path = getUpdatedPath(input, curPath);
		boolean collision = path.hasCollision(obstacles);
		boolean onPath = passive || isOnPath(path, input.getPos());
		TrajPath curBestPath = null;
		if (!onPath)
		{
			if (!passive)
			{
				log.debug("not on path");
			}
			path = generateDirectPath(input, 0, System.nanoTime());
		} else if (collision)
		{
			if (!passive)
			{
				log.trace("Collision");
			}
		} else
		{
			curBestPath = path;
		}
		
		TrajPath pathStub = getMinimumPathStub(path, input);
		List<IVector2> subPoints = generateRandomPointsForObstacles(input.getPos(), obstacles);
		
		TrajPath newPath = null;
		long tStart = System.nanoTime();
		int numSubPoints = 1;
		do
		{
			Collections.shuffle(subPoints, rnd);
			List<IVector2> nextSubPoints = subPoints.subList(0,
					Math.min(subPoints.size(), input.getNumNodes2TryPerIteration()));
			// for (IVector2 nsp : nextSubPoints)
			// {
			// input.getDebugShapes().add(new DrawablePoint(nsp, Color.magenta));
			// }
			newPath = recursivePathGeneration(input, obstacles, curBestPath, pathStub, nextSubPoints, tStart,
					numSubPoints);
			numSubPoints++;
			if (numSubPoints > input.getMaxSubPoints())
			{
				break;
			}
		} while ((newPath == null) && (((System.nanoTime() - tStart) / 1e9f) < (input.getMaxProcessingTime())));
		
		if (newPath != null)
		{
			if ((curPath == null)
					|| !curPath.getFinalDestination().equals(newPath.getFinalDestination(), 1) || (curPath
							.getRemainingTime() > (newPath.getRemainingTime() + newPathTimeTolerance)))
			{
				curPath = newPath;
			}
			tLastPath = System.nanoTime();
		} else if (((System.nanoTime() - tLastPath) / 1e9) > input.getForcePathAfter())
		{
			curPath = path;
		} else
		{
			log.trace("No path found.\n" + path + "\n" + curBestPath + "\n" + collision);
			curPath = null;
		}
		
		if ((curPath != null) && !onPath)
		{
			curPath.getNodeAt(curPath.getCurrentTime()).setReset(true);
		}
		
		return curPath;
	}
	
	
	private boolean isOnPath(final TrajPath path, final IVector2 pos)
	{
		IVector2 p1 = path.getPosition(path.getCurrentTime() - 0.1f);
		IVector2 p2 = path.getPosition(path.getCurrentTime() + 0.1f);
		float dist = GeoMath.distancePL(pos, p1, p2);
		if (dist > onPathTolerance)
		{
			log.debug("Bot " + bot.getBotID() + " not on path! dist = " + dist + " > " + onPathTolerance);
			return false;
		}
		return true;
	}
	
	
	protected TrajPath generateDirectPath(final TrajPathFinderInput input, final float curTime, final long tStart)
	{
		BangBangTrajectory2D traj = TrajectoryGenerator.generatePositionTrajectory(bot, input.getPos(),
				input.getVel(), input.getDest());
		ITrajectory1D trajW = TrajectoryGenerator.generateRotationTrajectory(bot, input.getOrientation(),
				input.getaVel(), input.getTargetAngle(), traj);
		List<TrajPathNode> nodes = new ArrayList<>(1);
		float endTime = Math.max(traj.getTotalTime(), trajW.getTotalTime());
		TrajPathNode node = new TrajPathNode(traj, trajW, endTime, 0);
		node.setReset(true);
		nodes.add(node);
		return new TrajPath(nodes, curTime, tStart);
	}
	
	
	/**
	 * @param input
	 * @param curPath
	 * @return
	 */
	public TrajPath getUpdatedPath(final TrajPathFinderInput input, final TrajPath curPath)
	{
		final TrajPath path;
		
		if (curPath == null)
		{
			path = generateDirectPath(input, 0, System.nanoTime());
		} else
		{
			// update current path
			TrajPath currentPath = curPath;
			currentPath.updateCurrentTime();
			path = updateDestination(currentPath, input);
			assert currentPath.getPosition(currentPath.getCurrentTime())
					.equals(path.getPosition(path.getCurrentTime()), 1) : "Jump!";
			assert AngleMath.isEqual(currentPath.getOrientation(currentPath.getCurrentTime()),
					path.getOrientation(path.getCurrentTime()), 0.15f);
			assert AngleMath.isEqual(path.getFinalOrientation(), input.getTargetAngle());
		}
		return path;
	}
	
	
	private TrajPath finalPathGeneration(final TrajPathFinderInput input, final List<IObstacle> obstacles,
			final TrajPath curBestPath,
			final TrajPath curPathStub)
	{
		float futureTime = curPathStub.getTotalTime() - curPathStub.getCurrentTime();
		IVector2 dest = shiftDestination(input.getDest(), futureTime);
		
		BangBangTrajectory2D finalTraj;
		ITrajectory1D finalTrajW;
		
		if (dest.equals(curPathStub.getFinalDestination(), 1))
		{
			finalTraj = curPathStub.getNodes().get(curPathStub.getNodes().size() - 1).getTraj();
			finalTrajW = curPathStub.getNodes().get(curPathStub.getNodes().size() - 1).getTrajW();
		} else
		{
			finalTraj = TrajectoryGenerator.generatePositionTrajectory(bot,
					curPathStub.getPosition(curPathStub.getTotalTime()),
					curPathStub.getVelocity(curPathStub.getTotalTime()),
					dest);
			finalTrajW = TrajectoryGenerator.generateRotationTrajectory(bot,
					curPathStub.getOrientation(curPathStub.getTotalTime()),
					curPathStub.getaVel(curPathStub.getTotalTime()),
					input.getTargetAngle(),
					finalTraj);
		}
		
		int nextId = curPathStub.getNodes().get(curPathStub.getNodes().size() - 1).getId() + 1;
		float endTime = Math.max(finalTraj.getTotalTime(), finalTrajW.getTotalTime());
		TrajPathNode newNode = new TrajPathNode(finalTraj, finalTrajW, endTime, nextId);
		
		float baseTime = curPathStub.getBaseTimeAt(curPathStub.getTotalTime());
		assert baseTime <= (curPathStub.getTotalTime() + 1e-5f) : baseTime + "<=" + curPathStub.getTotalTime();
		float tCollision = newNode.getEarliestCollision(obstacles, baseTime);
		assert tCollision >= 0;
		
		if (tCollision >= newNode.getEndTime())
		{
			// no collision ahead, this is a valid path!
			List<TrajPathNode> nodes = new ArrayList<>(curPathStub.getNodes().size() + 1);
			nodes.addAll(curPathStub.getNodes());
			nodes.add(newNode);
			TrajPath newPath = new TrajPath(nodes, curPathStub.getCurrentTime(), curPathStub.getStartTime());
			// input.getDebugShapes().add(newPath);
			if ((curBestPath == null))
			{
				return newPath;
			}
			if ((curBestPath.getRemainingTime() > (newPath.getRemainingTime())))
			{
				return newPath;
			}
			return curBestPath;
		}
		// input.getDebugShapes().add(new DrawablePoint(newNode.getTraj().getPosition(tCollision)));
		return null;
	}
	
	
	private TrajPath recursivePathGeneration(final TrajPathFinderInput input, final List<IObstacle> obstacles,
			final TrajPath curBestPath,
			final TrajPath curPathStub,
			final List<IVector2> subPoints,
			final long startTime,
			final int numRecursions)
	{
		TrajPath newBestPath = finalPathGeneration(input, obstacles, curBestPath, curPathStub);
		
		// collision on final trajectory.
		
		float processingTime = (System.nanoTime() - startTime) / 1e9f;
		boolean noSubPointsLeft = subPoints.isEmpty();
		boolean timeRunOut = processingTime >= (input.getMaxProcessingTime());
		if (noSubPointsLeft)
		{
			return curBestPath;
		}
		if (timeRunOut)
		{
			if (!passive)
			{
				log.debug("Time run out.");
			}
			return curBestPath;
		}
		if (numRecursions <= 0)
		{
			return curBestPath;
		}
		
		TrajPathNode lastStubNode = curPathStub.getNodes().get(curPathStub.getNodes().size() - 1);
		BangBangTrajectory2D firstTraj = lastStubNode.getTraj();
		ITrajectory1D firstTrajW = lastStubNode.getTrajW();
		TrajPathNode firstNode = new TrajPathNode(firstTraj, firstTrajW, firstTraj.getTotalTime(), lastStubNode.getId());
		float baseTime = curPathStub.getBaseTimeAt(curPathStub.getTotalTime());
		float tCollisionFirstTraj = firstNode.getEarliestCollision(obstacles, baseTime);
		float minTime = lastStubNode.getEndTime();
		float maxTime = Math.min(firstTraj.getTotalTime() - input.getTrajOffset(), tCollisionFirstTraj);
		List<Float> timesOnFirstTraj = getTimesOnTrajectory(firstTraj, minTime, maxTime, input.getNumPoints2TryOnTraj());
		
		for (IVector2 subPoint : subPoints)
		{
			for (float t : timesOnFirstTraj)
			{
				firstNode = new TrajPathNode(firstTraj, firstTrajW, t, lastStubNode.getId());
				
				BangBangTrajectory2D supportTraj = TrajectoryGenerator.generatePositionTrajectory(bot,
						firstTraj.getPosition(t),
						firstTraj.getVelocity(t),
						subPoint);
				ITrajectory1D supportTrajW = TrajectoryGenerator.generateRotationTrajectory(bot,
						firstTrajW.getPosition(t),
						firstTrajW.getVelocity(t),
						input.getTargetAngle(),
						supportTraj);
				
				TrajPathNode supportNode = new TrajPathNode(supportTraj, supportTrajW, supportTraj.getTotalTime(),
						lastStubNode.getNextId());
				float tCollisionSupportTraj = supportNode.getEarliestCollision(obstacles, baseTime + t);
				List<Float> timesOnSupportTraj = getTimesOnTrajectory(firstTraj, input.getTrajOffset(),
						Math.min(tCollisionSupportTraj, supportTraj.getTotalTime()),
						input.getNumPoints2TryOnTraj());
				
				for (int t2i = 0; t2i < timesOnSupportTraj.size(); t2i++)
				{
					float t2 = timesOnSupportTraj.get(t2i);
					supportNode = new TrajPathNode(supportTraj, supportTrajW, t2, lastStubNode.getNextId());
					List<TrajPathNode> supportPathNodes = new ArrayList<>(curPathStub.getNodes().size() + 1);
					supportPathNodes.addAll(curPathStub.getNodes().subList(0, curPathStub.getNodes().size() - 1));
					supportPathNodes.add(firstNode);
					supportPathNodes.add(supportNode);
					TrajPath supportPathStub = new TrajPath(supportPathNodes, curPathStub.getCurrentTime(),
							curPathStub.getStartTime());
					List<IVector2> nextSubPoints = new ArrayList<>(subPoints);
					nextSubPoints.remove(subPoint);
					newBestPath = recursivePathGeneration(input, obstacles, newBestPath, supportPathStub,
							nextSubPoints, startTime, numRecursions - 1);
					if (newBestPath != null)
					{
						return newBestPath;
					}
					// TrajPath finalPath2 = finalPathGeneration(input, obstacles, newBestPath, supportPathStub, subPoints,
					// startTime);
					// if (finalPath2 != null)
					// {
					// return finalPath2;
					// }
				}
			}
		}
		
		return newBestPath;
		// return recursivePathGeneration(input, obstacles, newBestPath, curPathStub, subPoints, startTime,
		// numRecursions - 1);
	}
	
	
	private TrajPath updateDestination(final TrajPath path, final TrajPathFinderInput input)
	{
		IVector2 shiftedFinalDest = shiftDestination(input.getDest(), path.getTotalTime());
		IVector2 curDest = path.getFinalDestination();
		float curOrientation = path.getFinalOrientation();
		boolean destChanged = !curDest.equals(shiftedFinalDest, 1);
		boolean angleChanged = !AngleMath.isEqual(curOrientation, input.getTargetAngle());
		if (destChanged || angleChanged)
		{
			// log.info(String.format("Updated %.5f %.5f, %.5f %.5f, %.5f %.5f", shiftedFinalDest.x(), curDest.x(),
			// shiftedFinalDest.y(), curDest.y(), path.getFinalOrientation(), input.getTargetAngle()));
			float earliestTime = getEarliestPossibleConnectionTime(path, input);
			
			if (path.getNodeIdxAt(earliestTime) < (path.getNodes().size() - 1))
			{
				// replace last element with new trajectory to dest
				List<TrajPathNode> nodes = new ArrayList<>(path.getNodes());
				TrajPathNode lastNode = nodes.remove(nodes.size() - 1);
				
				BangBangTrajectory2D traj = TrajectoryGenerator.generatePositionTrajectory(bot,
						lastNode.getTraj().getPosition(0),
						lastNode.getTraj().getVelocity(0),
						shiftedFinalDest);
				ITrajectory1D trajW = TrajectoryGenerator.generateRotationTrajectory(bot,
						lastNode.getTrajW().getPosition(0),
						lastNode.getTrajW().getVelocity(0),
						input.getTargetAngle(),
						traj);
				float totalTime = Math.max(traj.getTotalTime(), trajW.getTotalTime());
				TrajPathNode newLastNode = new TrajPathNode(traj, trajW, totalTime, lastNode.getId());
				nodes.add(newLastNode);
				
				float curTime = path.getCurrentTime();
				long tStart = path.getStartTime();
				TrajPath newPath = new TrajPath(nodes, curTime, tStart);
				
				assert newPath.getPosition(newPath.getCurrentTime())
						.equals(path.getPosition(path.getCurrentTime()), 1) : "Jump!";
				assert AngleMath.isEqual(newPath.getOrientation(newPath.getCurrentTime()),
						path.getOrientation(path.getCurrentTime()), 0.15f);
				assert AngleMath.isEqual(newPath.getFinalOrientation(), input.getTargetAngle());
				return newPath;
			}
			
			TrajPath stubPath = getMinimumPathStub(path, input);
			List<TrajPathNode> nodes = new ArrayList<>(stubPath.getNodes());
			BangBangTrajectory2D traj = TrajectoryGenerator.generatePositionTrajectory(bot,
					stubPath.getPosition(stubPath.getTotalTime()),
					stubPath.getVelocity(stubPath.getTotalTime()),
					shiftedFinalDest);
			ITrajectory1D trajW = TrajectoryGenerator.generateRotationTrajectory(bot,
					stubPath.getOrientation(stubPath.getTotalTime()),
					stubPath.getaVel(stubPath.getTotalTime()),
					input.getTargetAngle(),
					traj);
			
			int nextId = nodes.get(nodes.size() - 1).getId() + 1;
			float endTime = Math.max(traj.getTotalTime(), trajW.getTotalTime());
			TrajPathNode newNode = new TrajPathNode(traj, trajW, endTime, nextId);
			nodes.add(newNode);
			assert newNode.getTraj().getPosition(newNode.getEndTime()).equals(shiftedFinalDest, 1);
			assert AngleMath.isEqual(newNode.getTrajW().getPosition(newNode.getEndTime()), input.getTargetAngle());
			
			TrajPath newPath = new TrajPath(nodes, stubPath.getCurrentTime(), stubPath.getStartTime());
			assert newPath.getPosition(newPath.getCurrentTime())
					.equals(path.getPosition(path.getCurrentTime()), 1) : "Jump!";
			assert AngleMath.isEqual(newPath.getOrientation(newPath.getCurrentTime()),
					path.getOrientation(path.getCurrentTime()), 0.15f);
			assert AngleMath.isEqual(newPath.getFinalOrientation(), input.getTargetAngle());
			return newPath;
		}
		return path;
	}
	
	
	private float getEarliestPossibleConnectionTime(final TrajPath path, final TrajPathFinderInput input)
	{
		float curTime = path.getCurrentTime();
		int curIdx = path.getNodeIdxAt(curTime);
		TrajPathNode curNode = path.getNodes().get(curIdx);
		float curBaseTime = path.getBaseTimeAt(curTime);
		float curTrajTime = curTime - curBaseTime;
		float curTimeLeft = curNode.getEndTime() - curTrajTime;
		if (curTimeLeft > input.getTrajOffset())
		{
			return curTime + input.getTrajOffset();
		}
		if ((curIdx + 1) < path.getNodes().size())
		{
			float nextBaseTime = curBaseTime + curNode.getEndTime();
			return nextBaseTime + input.getTrajOffset();
		}
		return curTime + input.getTrajOffset();
	}
	
	
	private TrajPath getMinimumPathStub(final TrajPath curPath, final TrajPathFinderInput input)
	{
		float earliestTime = getEarliestPossibleConnectionTime(curPath, input);
		assert earliestTime >= (curPath.getCurrentTime() - 1e-5f);
		int firstNodeIdx = curPath.getNodeIdxAt(earliestTime);
		int curNodeIdx = curPath.getNodeIdxAt(curPath.getCurrentTime());
		TrajPathNode curNode = curPath.getNodes().get(firstNodeIdx);
		
		List<TrajPathNode> nodes = new ArrayList<>(firstNodeIdx + 1);
		nodes.addAll(curPath.getNodes().subList(0, firstNodeIdx));
		float baseTime = 0;
		float obsoleteTime = 0;
		for (int i = 0; i < (curNodeIdx - 1); i++)
		{
			TrajPathNode node = curPath.getNodes().get(i);
			if ((node.getEndTime() + baseTime) < curPath.getCurrentTime())
			{
				obsoleteTime += node.getEndTime();
				nodes.remove(node);
			}
			baseTime += node.getEndTime();
		}
		// assert nodes.isEmpty() || ((nodes.get(nodes.size() - 1).getId() + 1) == curNode.getId());
		
		long tStart = curPath.getStartTime() + (long) (obsoleteTime * 1e9);
		float curTime = curPath.getCurrentTime() - obsoleteTime;
		float curBaseTime = curPath.getBaseTimeAt(earliestTime);
		float trajEndTime = Math.max(earliestTime - curBaseTime, input.getTrajOffset());
		int nodeId = curNode.getId();
		TrajPathNode newNode = new TrajPathNode(curNode.getTraj(), curNode.getTrajW(), trajEndTime, nodeId);
		nodes.add(newNode);
		TrajPath stubPath = new TrajPath(nodes, curTime, tStart);
		assert stubPath.getPosition(stubPath.getCurrentTime())
				.equals(curPath.getPosition(curPath.getCurrentTime()), 1) : "Jump!";
		assert AngleMath.isEqual(stubPath.getOrientation(stubPath.getCurrentTime()),
				curPath.getOrientation(curPath.getCurrentTime()), 0.15f);
		
		return stubPath;
	}
	
	
	private IVector2 shiftDestination(final IVector2 dest, final float t)
	{
		IVector2 newDest = dest;
		for (IObstacle obs : allCurrentObstacles)
		{
			if (obs.isPointCollidingWithObstacle(newDest, t))
			{
				newDest = obs.nearestPointOutsideObstacle(newDest, t);
			}
		}
		return newDest;
	}
	
	
	private List<Float> getTimesOnTrajectory(final BangBangTrajectory2D traj, final float tStart, final float tEnd,
			final int maxN)
	{
		if (tStart >= tEnd)
		{
			return Collections.emptyList();
		}
		
		float len = Math.max(tEnd - tStart, 0);
		int num = Math.min(maxN, (int) (len / 0.1f) + 1);
		
		List<Float> times = new ArrayList<>(num);
		
		times.add(tStart);
		
		for (int i = 0; i < 4; i++)
		{
			float tx = traj.getX().getPart(i).tEnd;
			if ((tx >= tStart) && (tx <= tEnd) && !times.contains(tx))
			{
				times.add(tx);
			}
			float ty = traj.getY().getPart(i).tEnd;
			if ((ty >= tStart) && (ty <= tEnd) && !times.contains(ty))
			{
				times.add(ty);
			}
		}
		for (int i = 0; i < (maxN * 2); i++)
		{
			if (times.size() == num)
			{
				break;
			}
			float t = Math.min(tEnd, ((float) Math.abs(rnd.nextGaussian()) * (tEnd - tStart) * 2) + tStart);
			if (!times.contains(t))
			{
				times.add(t);
			}
		}
		
		return times;
	}
	
	
	private List<IVector2> generateRandomPointsForObstacles(final IVector2 curBotPos, final List<IObstacle> obstacles)
	{
		List<IVector2> rndPoints = new ArrayList<>(obstacles.size());
		for (IObstacle obs : obstacles)
		{
			obs.generateObstacleAvoidancePoints(curBotPos, rnd, rndPoints);
		}
		return rndPoints;
	}
	
	
	/**
	 * @param path
	 * @param mirror
	 * @return
	 */
	public final ACommand getPositioningCommand(final TrajPath path, final boolean mirror)
	{
		float t = path.getVeryCurrentTime();
		int curIdx = path.getNodeIdxAt(t);
		int preIdx = curIdx - 1;
		int nextIdx = Math.min(curIdx + 1, path.getNodes().size() - 1);
		float curBaseTime = path.getBaseTimeAt(t);
		float trajTime = t - curBaseTime;
		
		TrajPathNode curNode = path.getNodes().get(curIdx);
		
		IVector2 dest;
		float orient;
		float transTime;
		final int id;
		if (trajTime < (curNode.getEndTime() / 2f))
		{
			// first half
			dest = curNode.getDestination();
			orient = curNode.getTargetAngle();
			id = curNode.getId();
			if (preIdx < 0)
			{
				if (lastNode != null)
				{
					transTime = lastNode.getEndTime();
				} else
				{
					transTime = 0;
				}
			} else
			{
				transTime = path.getNodes().get(preIdx).getEndTime();
			}
		} else
		{
			// second half
			dest = path.getNodes().get(nextIdx).getDestination();
			orient = path.getNodes().get(nextIdx).getTargetAngle();
			transTime = path.getNodes().get(curIdx).getEndTime();
			id = path.getNodes().get(nextIdx).getId();
		}
		if (mirror)
		{
			dest = dest.multiplyNew(-1);
			orient = AngleMath.normalizeAngle(orient + AngleMath.PI);
		}
		
		if (curNode.isReset())
		{
			transTime = -1 - path.getRndId();
		}
		
		if ((id != lastId) || curNode.isReset())
		{
			lastDest = dest;
			lastTargetAngle = orient;
		}
		lastId = id;
		lastNode = curNode;
		
		return new TigerSystemBotSkill(new BotSkillGlobalPosition(lastDest, lastTargetAngle, transTime));
	}
	
	
	/**
	 * @return the bot
	 */
	public final ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @return the curPath
	 */
	public final TrajPath getCurPath()
	{
		return curPath;
	}
	
	
	/**
	 * 
	 */
	public void reset()
	{
		curPath = null;
	}
}
