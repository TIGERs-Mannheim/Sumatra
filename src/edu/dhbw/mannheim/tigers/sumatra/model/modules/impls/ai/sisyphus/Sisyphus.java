/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): Christian K�nig, Bernhard Perun
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/*
 * As a punishment from the gods for his trickery, Sisyphus was made to roll a huge
 * rock up a steep hill, but before he could reach the top of the hill, the rock would
 * always roll back down again, forcing him to begin again. The maddening nature
 * of the punishment was reserved for Sisyphus due to his hubristic belief that his
 * cleverness surpassed that of Zeus. Sisyphus took the bold step of reporting one of
 * Zeus' sexual conquests, telling the river god Asopus of the whereabouts of his
 * daughter Aegina. Zeus had taken her away, but regardless of the impropriety of Zeus'
 * frequent conquests, Sisyphus overstepped his bounds by considering himself a peer
 * of the gods who could rightfully report their indiscretions. As a result, Zeus
 * displayed his own cleverness by binding Sisyphus to an eternity of frustration.
 * Accordingly, pointless or interminable activities are often described as Sisyphean.
 * Sisyphus was a common subject for ancient writers and was depicted by the painter
 * Polygnotus on the walls of the Lesche at Delphi.
 * source:
 * the omniscient and omnipotent god of knowledge aka wikipedia
 */

/**
 * Contains the pathfinding-logic of Sumatra. <br>
 * 
 * @author Christian Koenig, Bernhard Perun, Dirk Klostermann
 */
public class Sisyphus implements IPathConsumer
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	
	// --- errt and dss ---
	private Map<BotID, ScheduledExecutorService>	schedulers					= new ConcurrentHashMap<BotID, ScheduledExecutorService>();
	
	// --- current-paths ---
	private Map<BotID, Path>							currentPaths				= new ConcurrentHashMap<BotID, Path>();
	private Map<BotID, Path>							latestPaths					= new ConcurrentHashMap<BotID, Path>();
	
	private Map<BotID, PathFinderThread>			pathFinderThreads			= new ConcurrentHashMap<BotID, PathFinderThread>();
	
	private final SplineTrajectoryGenerator		trajGeneratorMove			= new SplineTrajectoryGenerator();
	
	/** number of new paths between start/stop (usually one Skill lifetime) */
	private Map<BotID, Integer>						newPathCounter				= new ConcurrentHashMap<BotID, Integer>();
	
	@Configurable(comment = "Time [ms] - How often should the pathplanning be executed?")
	private static int									pathPlanningInterval		= 20;
	
	/** [rad/s] */
	@Configurable(comment = "Vel [rad/s] - Max rotation velocity to use for spline generation", speziType = EBotType.class, spezis = { "GRSIM" })
	public static float									maxRotateVelocity			= 10;
	/** [rad/s^2] */
	@Configurable(comment = "Vel [rad/s^2] - Max rotation acceleration to use for spline generation", speziType = EBotType.class, spezis = { "GRSIM" })
	public static float									maxRotateAcceleration	= 15;
	/**  */
	@Configurable(comment = "Func [poly] - This function maps the normal angle at path points to speed at this point")
	public static IFunction1D							normalAngleToSpeed		= Function1dPoly.linear(0, 2);
	
	/** [m/s] */
	@Configurable(comment = "Vel [m/s] - Max linear velocity to use for spline generation", speziType = EBotType.class, spezis = { "GRSIM" })
	public static float									maxLinearVelocity			= 2.5f;
	/** [m/s^2] */
	@Configurable(comment = "Acc [m/s^2] - Max linear acceleration to use for spline generation", speziType = EBotType.class, spezis = { "GRSIM" })
	public static float									maxLinearAcceleration	= 2.5f;
	
	@Configurable(comment = "Points on a path with a combined angle*distance score below this value will be removed")
	private static float									pathReductionScore		= 0.0f;
	
	/** factor to increase curve speed */
	@Configurable(comment = "factor to increase curve speed (CAREFUL: only 1.0f is really secure)")
	public static float									curveSpeed					= 1.5f;
	
	
	// ------------------------------------------------------------------------
	// --- constructor & destructor -------------------------------------------
	// ------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public Sisyphus()
	{
	}
	
	
	// ------------------------------------------------------------------------
	// --- observers ----------------------------------------------------------
	// ------------------------------------------------------------------------
	
	private final Map<BotID, IPathConsumer>	observers	= new ConcurrentSkipListMap<BotID, IPathConsumer>();
	
	
	/**
	 * @param botId
	 * @param observer
	 */
	public void addObserver(final BotID botId, final IPathConsumer observer)
	{
		synchronized (observers)
		{
			observers.put(botId, observer);
		}
	}
	
	
	/**
	 * @param botId
	 */
	public void removeObserver(final BotID botId)
	{
		synchronized (observers)
		{
			observers.remove(botId);
		}
	}
	
	
	private void notifyNewPath(final Path path)
	{
		synchronized (observers)
		{
			IPathConsumer pathConsumer = observers.get(path.getBotID());
			if (pathConsumer != null)
			{
				pathConsumer.onNewPath(path);
			}
		}
	}
	
	
	private void notifyPotentialNewPath(final Path path)
	{
		synchronized (observers)
		{
			IPathConsumer pathConsumer = observers.get(path.getBotID());
			if (pathConsumer != null)
			{
				pathConsumer.onPotentialNewPath(path);
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// --- methods ------------------------------------------------------------
	// ------------------------------------------------------------------------
	
	/**
	 * Start the PP thread with given input
	 * 
	 * @param botId
	 * @param moveCon
	 * @return
	 */
	public Path startPathPlanning(final BotID botId, final MovementCon moveCon)
	{
		assureBotInit(botId);
		
		newPathCounter.put(botId, 0);
		
		PathFinderInput pathFinderInput = new PathFinderInput(botId, currentPaths, 0, moveCon);
		PathFinderThread thread = pathFinderThreads.get(botId);
		thread.start(pathFinderInput);
		return currentPaths.get(botId);
	}
	
	
	/**
	 * @param botId
	 */
	public void stopPathPlanning(final BotID botId)
	{
		PathFinderThread thread = pathFinderThreads.get(botId);
		if (thread != null)
		{
			thread.stop();
		}
		clearPath(botId);
	}
	
	
	/**
	 * @param botId
	 */
	public void clearPath(final BotID botId)
	{
		currentPaths.remove(botId);
		latestPaths.remove(botId);
	}
	
	
	@Override
	public void onNewPath(final Path path)
	{
		currentPaths.put(path.getBotID(), path);
		Integer counter = newPathCounter.get(path.getBotID());
		if (counter == null)
		{
			counter = 0;
		}
		newPathCounter.put(path.getBotID(), counter + 1);
		notifyNewPath(path);
	}
	
	
	@Override
	public void onPotentialNewPath(final Path path)
	{
		latestPaths.put(path.getBotID(), path);
		notifyPotentialNewPath(path);
	}
	
	
	private void assureBotInit(final BotID botId)
	{
		if (schedulers.containsKey(botId))
		{
			return;
		}
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
				"Pathplanner-" + botId.getNumber() + "-" + botId.getTeamColor().name()));
		schedulers.put(botId, scheduler);
		
		PathFinderThread pathFinderThread = new PathFinderThread(this, botId);
		pathFinderThreads.put(botId, pathFinderThread);
		
		// calculate a new path every PATH_PLANNING_INTERVAL seconds
		scheduler.scheduleAtFixedRate(pathFinderThread, 0, pathPlanningInterval, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * stops all threads
	 */
	public final void stopAllPathPlanning()
	{
		for (PathFinderThread thread : pathFinderThreads.values())
		{
			thread.stop();
			thread.onTermination();
		}
		for (ScheduledExecutorService scheduler : schedulers.values())
		{
			scheduler.shutdown();
		}
		schedulers.clear();
		pathFinderThreads.clear();
	}
	
	
	/**
	 * calculates a spline for a bot according a given movement condition
	 * 
	 * @param botId
	 * @param worldFrame
	 * @param moveCon
	 * @param pathPlanningParams
	 * @return
	 */
	public ISpline calculateSpline(final BotID botId, final SimpleWorldFrame worldFrame, final MovementCon moveCon,
			final TuneableParameter pathPlanningParams)
	{
		Map<BotID, Path> paths = new HashMap<BotID, Path>();
		PathFinderInput pfi = new PathFinderInput(botId, paths, 0f, moveCon);
		pfi.update(worldFrame);
		
		IPathFinder pathFinder = new ERRTPlanner_WPC();
		if (pathPlanningParams != null)
		{
			((ERRTPlanner_WPC) pathFinder).setAdjustableParams(pathPlanningParams);
		}
		Path path = pathFinder.calcPath(pfi);
		addAHermiteSpline(worldFrame, path, pfi);
		return path.getSpline();
	}
	
	
	/**
	 * calculates a spline for a bot according a given movement condition
	 * 
	 * @param botId
	 * @param worldFrame
	 * @param moveCon
	 * @return
	 */
	public ISpline calculateSpline(final BotID botId, final SimpleWorldFrame worldFrame, final MovementCon moveCon)
	{
		return calculateSpline(botId, worldFrame, moveCon, null);
	}
	
	
	/**
	 * Add a spline to the path
	 * 
	 * @param wFrame
	 * @param path
	 * @param pfi
	 */
	public void addAHermiteSpline(final SimpleWorldFrame wFrame, final Path path, final PathFinderInput pfi)
	{
		TrackedTigerBot bot = wFrame.getBot(pfi.getBotId());
		float dstOrient = pfi.getDstOrient();
		List<IVector2> mPath = new ArrayList<IVector2>();
		mPath.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		
		for (IVector2 p : path.getPath())
		{
			mPath.add(DistanceUnit.MILLIMETERS.toMeters(p));
		}
		
		if (pfi.getMoveCon().getSpeed() > 0)
		{
			trajGeneratorMove.setPositionTrajParams(pfi.getMoveCon().getSpeed(), Sisyphus.maxLinearAcceleration);
		} else
		{
			trajGeneratorMove.setPositionTrajParams(Sisyphus.maxLinearVelocity, Sisyphus.maxLinearAcceleration);
		}
		trajGeneratorMove.setRotationTrajParams(maxRotateVelocity, Sisyphus.maxRotateAcceleration);
		trajGeneratorMove.setSplineParams(normalAngleToSpeed);
		trajGeneratorMove.setReducePathScore(pathReductionScore);
		
		SplinePair3D trajectories = trajGeneratorMove.create(mPath, bot.getVel(), pfi.getMoveCon().getVelAtDestination(),
				bot.getAngle(), dstOrient, bot.getaVel(), 0);
		path.setHermiteSpline(trajectories);
		trajectories.setStartTime(System.nanoTime());
	}
	
	
	/**
	 * Currently executed path
	 * 
	 * @return the currentPaths
	 */
	public Map<BotID, Path> getCurrentPaths()
	{
		return currentPaths;
	}
	
	
	/**
	 * Potential new path that were calculated last
	 * 
	 * @return the currentPaths
	 */
	public Map<BotID, Path> getLatestPaths()
	{
		return latestPaths;
	}
	
	
	/**
	 * Number of new paths that were used in current Pathplanning episode (Skill)
	 * 
	 * @return
	 */
	public Map<BotID, Integer> getNumberOfPaths()
	{
		return newPathCounter;
	}
	
}
