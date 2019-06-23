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
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IActiveAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;


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
 * @author Christian Koenig, Bernhard Perun
 */
public class Sisyphus implements IActiveAIProcessor
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	
	// --- log ---
	private final Logger					log		= Logger.getLogger(getClass());
	
	// --- errt and dss ---
	// private final ERRTPlanner errtPlanner = new ERRTPlanner();
	// private final DynamicSafetySearch dynamicSafetySearch = new DynamicSafetySearch();
	
	// --- current-paths ---
	private final ArrayList<Path>		oldPaths	= new ArrayList<Path>();
	
	// --- just for debug ---
	public final List<IAIObserver>	aiObservers;
	
	private static final boolean		IS_SECOND_TRY = false;
	
	
	// ------------------------------------------------------------------------
	// --- constructor & destructor -------------------------------------------
	// ------------------------------------------------------------------------
	
	public Sisyphus(List<IAIObserver> observers)
	{
		// --- set aiObservers for debug-data ---
		aiObservers = observers;
		
		// --- init oldPaths list - add 0-12 robots to oldPaths-list ---
		for (int i = 0; i < 13; i++)
		{
			oldPaths.add(null);
		}
		
	}
	

	@Override
	public void start()
	{
		
	}
	

	@Override
	public void stop()
	{
		
	}
	

	// ------------------------------------------------------------------------
	// --- methods ------------------------------------------------------------
	// ------------------------------------------------------------------------
	
	/**
	 * Returns a path from bot to target.
	 * @param wFrame current worldframe
	 * @param botId id of bot
	 * @param target description of the target point (format: Vector2)
	 */
	public Path calcPath(WorldFrame wFrame, int botId, IVector2 target)
	{
		return calcPath(wFrame, botId, target, true, false, EGameSituation.GAME);// considerBall --> true; kickOff --> false
	}
	

	/**
	 * Returns a path from bot to target.
	 * @param wFrame current worldframe
	 * @param botId id of bot
	 * @param target description of the target point (format: Vector2)
	 */
	public Path calcPath(WorldFrame wFrame, int botId, IVector2 target, boolean considerBall)
	{
		return calcPath(wFrame, botId, target, considerBall, false, EGameSituation.GAME);// kickOff --> false
	}

	/**
	 * Returns a path from bot to target.
	 * @param wFrame current worldframe
	 * @param botId id of bot
	 * @param target description of the target point (format: PathPoint with end-velocity and end-angle)
	 * @param restrictedArea area the bot is not allowed to enter this area; if there is no such area: use null; if
	 *           current botpos
	 *           or target are within restrictedArea it is set null automatically
	 */
	public Path calcPath(WorldFrame wFrame, int botId, IVector2 target, boolean considerBall, boolean isGoalie, EGameSituation gameSit)
	{
		// log.debug("get path");
		
		// --- checks in front of path getting ---
		if (wFrame == null)
		{
			log.error("worldframe=null");
			return null;
		}
		if (target == null)
		{
			log.error("target=null");
			return null;
		}
		
		// --- ERRT - algorithm ---
		ERRTPlanner_WPC errtPlanner = new ERRTPlanner_WPC();
		Path errtPath = errtPlanner.doCalculation(wFrame, botId, oldPaths.get(botId), target, considerBall, isGoalie, gameSit, IS_SECOND_TRY);
		// System.out.println("got path. elements: "+errtPath.path.size()+"; target: "+target+"; current: "+wFrame.tigerBots.get(botId).pos);
		// Path errtPath = new Path(botId);
		// errtPath.path.add(target);
		

		// --- dynamic safety search - algorithm ---
		// dynamicSafetySearch.doCalculation(errtPath, wFrame, botId);
		
		// spline smoothing only first time and when path has changed
		if (errtPath.changed || errtPath.getSpline() == null)
		{
			// if (errtPath.changed)
			// System.out.println("path changed");
			// if (errtPath.getSpline() == null)
			// System.out.println("new path");
			
			XYSpline spline = new XYSpline(errtPath.path, wFrame.tigerBots.get(botId).pos);
			errtPath.setSpline(spline);
			errtPath.changed = true;
		}
		// --- set new path as old path ---
		oldPaths.set(botId, errtPath);
		

		// --- notify observers ---
		for (IAIObserver o1 : aiObservers)
		{
			o1.onNewPath(oldPaths.get(botId).copyLight()); // Every observer its own copy!
		}
		
		return errtPath.copyLight(); // Even the SkillSystem gets its own...
	}
}