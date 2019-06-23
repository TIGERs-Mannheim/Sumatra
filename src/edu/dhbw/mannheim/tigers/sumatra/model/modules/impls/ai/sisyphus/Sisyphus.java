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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


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
 * 
 * source:
 * the omniscient and omnipotent god of knowledge aka wikipedia
 */

/**
 * Contains the pathfinding-logic of Sumatra. <br>
 * 
 * @author Christian Koenig, Bernhard Perun, Dirk Klostermann
 */
public class Sisyphus
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	// Logger
	private static final Logger						log								= Logger.getLogger(Sisyphus.class.getName());
	
	// --- errt and dss ---
	private Map<BotID, ScheduledExecutorService>	schedulers						= new ConcurrentHashMap<BotID, ScheduledExecutorService>();
	
	// --- current-paths ---
	private Map<BotID, Path>							currentPaths					= new ConcurrentHashMap<BotID, Path>();
	
	private Map<BotID, PathFinderThread>			pathFinderThreads				= new ConcurrentHashMap<BotID, PathFinderThread>();
	
	// --- just for DEBUG ---
	/** */
	private final List<IAIObserver>					aiObservers;
	
	private Map<BotID, Integer>						destOrientChangedCounter	= new ConcurrentHashMap<BotID, Integer>();
	
	
	// ------------------------------------------------------------------------
	// --- constructor & destructor -------------------------------------------
	// ------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public Sisyphus()
	{
		// --- set aiObservers for DEBUG-data ---
		aiObservers = new ArrayList<IAIObserver>();
	}
	
	
	/**
	 * 
	 * @param observers
	 */
	public Sisyphus(List<IAIObserver> observers)
	{
		// --- set aiObservers for DEBUG-data ---
		aiObservers = observers;
	}
	
	
	// ------------------------------------------------------------------------
	// --- methods ------------------------------------------------------------
	// ------------------------------------------------------------------------
	
	
	/**
	 * Returns a path from bot to target.
	 * 
	 * old param: 'restrictedArea' area the bot is not allowed to enter this area; if there is no such area: use null; if
	 * current botpos
	 * or target are within restrictedArea it is set null automatically
	 * 
	 * @param wFrame current worldframe
	 * @param botId id of bot
	 * @param currentTimeOnSpline
	 * @param moveCon
	 * @return
	 */
	public Path calcPath(WorldFrame wFrame, BotID botId, float currentTimeOnSpline, MovementCon moveCon)
	{
		// --- check botId ---
		if (botId.isUninitializedID())
		{
			return null;
		}
		
		// --- checks in front of path getting ---
		if (wFrame == null)
		{
			log.error("worldframe=null");
			return null;
		}
		
		PathFinderInput pathFinderInput = new PathFinderInput(wFrame, botId, currentPaths, currentTimeOnSpline, moveCon);
		if (schedulers.get(botId) == null)
		{
			initializeSchedulerForBot(botId, wFrame, pathFinderInput);
		} else
		{
			pathFinderThreads.get(botId).setPathFinderInput(pathFinderInput);
			// if there is a new target do something different
			if (currentPaths.get(botId) != null)
			{
				IVector2 oldTarget = currentPaths.get(botId).getTarget();
				IVector2 newTarget = moveCon.getDestCon().getDestination();
				float distanceTargets = oldTarget.subtractNew(newTarget).getLength2();
				float distanceBotNewTarget = wFrame.getTiger(botId).getPos().subtractNew(newTarget).getLength2();
				boolean targetChangedMoreThanTol = !oldTarget.equals(newTarget,
						AIConfig.getBotConfig(wFrame.getTiger(botId).getBotType()).getTolerances().getPositioning());
				boolean targetChanged = (moveCon.getDestCon().isActive() && ((distanceTargets * 10) > distanceBotNewTarget) && (distanceBotNewTarget > 100))
						|| ((distanceBotNewTarget < 100) && targetChangedMoreThanTol);
				boolean orientationChanged = moveCon.getAngleCon().isActive()
						&& !SumatraMath.isEqual(currentPaths.get(botId).getDestOrient(), pathFinderInput.getDstOrient(),
								AIConfig.getBotConfig(wFrame.getTiger(botId).getBotType()).getTolerances().getAiming());
				int counter = 0;
				if (destOrientChangedCounter.containsKey(botId))
				{
					counter = destOrientChangedCounter.get(botId);
				}
				if (targetChanged || orientationChanged || moveCon.isForceNewSpline())
				{
					// if (counter == 0)
					// {
					counter++;
					log.trace("Target changed: " + targetChanged + " / Orientation changed: " + orientationChanged
							+ " (oldOrient: " + currentPaths.get(botId).getDestOrient() + " / newOrient: "
							+ pathFinderInput.getDstOrient() + " (oldDest: " + currentPaths.get(botId).getTarget()
							+ " / newDest: " + pathFinderInput.getTarget() + ")");
					pathFinderThreads.get(botId).getPath().setOld(true);
					pathFinderThreads.get(botId).waitForPath();
					// }
					// TODO DirkK move to config
					// counter = (counter++) % 10;
				} else
				{
					counter = 0;
				}
				destOrientChangedCounter.put(botId, counter);
			}
		}
		
		Path path = currentPaths.get(botId);
		
		// Even the SkillSystem gets its own...
		return path.copyLight();
	}
	
	
	/**
	 * Use this method to create your own (virtual) path
	 * You can set the spline in the path to visualize it
	 * 
	 * @param path
	 */
	public void newExternalPath(Path path)
	{
		notifyObservers(path);
	}
	
	
	private void notifyObservers(Path path)
	{
		for (final IAIObserver o1 : aiObservers)
		{
			// Every observer its own copy!
			o1.onNewPath(path.copyLight());
		}
	}
	
	
	private void initializeSchedulerForBot(BotID botId, WorldFrame wFrame, PathFinderInput pathFinderInput)
	{
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
				"Pathplanner-" + botId.getNumber()));
		schedulers.put(botId, scheduler);
		
		Path path = new Path(botId, pathFinderInput.getTarget(), pathFinderInput.getDstOrient());
		path.setOld(true);
		currentPaths.put(botId, path);
		PathFinderThread pathFinderThread = new PathFinderThread(this, botId, path);
		pathFinderThreads.put(botId, pathFinderThread);
		
		pathFinderThread.setPathFinderInput(pathFinderInput);
		
		TrackedTigerBot bot = wFrame.tigerBotsVisible.getWithNull(botId);
		if (bot == null)
		{
			log.warn("Bot " + botId + " not found.");
			return;
		}
		// calculate a new path every PATH_PLANNING_INTERVAL seconds
		final int pathPlanningInterval = AIConfig.getGeneral(bot.getBotType()).getPathplanningInterval();
		scheduler.scheduleAtFixedRate(pathFinderThread, 0, pathPlanningInterval, TimeUnit.MILLISECONDS);
		
		// to have a path immediately let the thread run once within this thread
		pathFinderThread.waitForPath();
		
	}
	
	
	/**
	 * @param botId
	 * @param path the oldPath to add
	 * @param pfi the PathFinderInput which was used to calculate this path
	 */
	public final void onNewPath(BotID botId, Path path, PathFinderInput pfi)
	{
		currentPaths.put(botId, path);
		pfi.getwFrame().getTiger(botId).setPath(path);
		// --- notify observers ---
		if (path.isChanged())
		{
			notifyObservers(path);
		}
	}
	
	
	/**
	 * stops the thread for the given bot
	 * 
	 * @param botId
	 */
	public final void stopPathPlanning(BotID botId)
	{
		if (schedulers.get(botId) != null)
		{
			clearPath(botId);
			PathFinderInput pfi = pathFinderThreads.get(botId).getPathFinderInput();
			pfi.setActive(false);
			pathFinderThreads.get(botId).setPathFinderInput(pfi);
		}
	}
	
	
	/**
	 * clears the painted path and spline
	 * 
	 * @param botId
	 */
	public final void clearPath(BotID botId)
	{
		final Path path;
		if (currentPaths.get(botId) != null)
		{
			path = new Path(botId, currentPaths.get(botId).getTarget(), currentPaths.get(botId).getDestOrient());
			
		} else
		{
			path = new Path(botId, Vector2.ZERO_VECTOR, 0f);
		}
		notifyObservers(path);
	}
	
	
	/**
	 * stops all threads
	 */
	public final void stopAllPathPlanning()
	{
		for (ScheduledExecutorService scheduler : schedulers.values())
		{
			scheduler.shutdown();
		}
		schedulers.clear();
	}
	
	
	/**
	 * calculates a spline for a bot according a given movement condition
	 * 
	 * NOT TESTED PROPERLY YET
	 * 
	 * @param bot
	 * @param worldFrame
	 * @param moveCon
	 * @return
	 */
	public ISpline calculateSpline(TrackedTigerBot bot, WorldFrame worldFrame, MovementCon moveCon)
	{
		Map<BotID, Path> dummy = new HashMap<BotID, Path>();
		PathFinderInput pfi = new PathFinderInput(worldFrame, bot.getId(), dummy, 0f, moveCon);
		ERRTPlanner_WPC planner = new ERRTPlanner_WPC();
		Path path = planner.calcPath(pfi);
		
		PathFinderThread pft = new PathFinderThread(null, bot.getId(), null);
		pft.addAHermiteSpline(path, pfi);
		
		return path.getSpline();
	}
}
