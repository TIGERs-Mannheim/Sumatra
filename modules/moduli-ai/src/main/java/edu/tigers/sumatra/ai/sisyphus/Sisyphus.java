/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): Christian K�nig, Bernhard Perun
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.filter.IPathFilter;
import edu.tigers.sumatra.ai.sisyphus.filter.StubPathFilter;
import edu.tigers.sumatra.ai.sisyphus.finder.IPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.StubPathFinder;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.MovementCon;


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
public final class Sisyphus
{
	private static final Random			RND									= new Random(System.currentTimeMillis());
	private IPathFilter						pathFilter							= new StubPathFilter();
	private IPathFinder						pathFinder							= new StubPathFinder();
																							
	private final List<IPathConsumer>	observers							= new CopyOnWriteArrayList<IPathConsumer>();
																							
	private final BotID						botId;
	private PathFinderInput					pathFinderInput					= null;
	private IPath								currentPath							= null;
																							
	/** unique identity */
	private final int							customId								= RND.nextInt(Integer.MAX_VALUE);
																							
	@Configurable(comment = "Time [ms] - How often should the pathplanning be executed?")
	private static int						defaultPathPlanningInterval	= 20;
																							
	private int									pathPlanningInterval				= defaultPathPlanningInterval;
																							
																							
	static
	{
		ConfigRegistration.registerClass("sisyphus", Sisyphus.class);
	}
	
	
	/**
	 * @param botId
	 * @param moveCon
	 */
	public Sisyphus(final BotID botId, final MovementCon moveCon)
	{
		this.botId = botId;
		pathFinderInput = new PathFinderInput(botId, moveCon);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IPathConsumer observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 * @return
	 */
	public boolean removeObserver(final IPathConsumer observer)
	{
		return observers.remove(observer);
	}
	
	
	private void notifyNewPath(final IPath path)
	{
		for (IPathConsumer pathConsumer : observers)
		{
			pathConsumer.onNewPath(path);
		}
	}
	
	
	private void notifyPotentialNewPath(final IPath path)
	{
		for (IPathConsumer pathConsumer : observers)
		{
			pathConsumer.onPotentialNewPath(path);
		}
	}
	
	
	protected void onNewPath(final IPath path)
	{
		notifyNewPath(path);
	}
	
	
	protected void onPotentialNewPath(final IPath path)
	{
		notifyPotentialNewPath(path);
	}
	
	
	/**
	 * @return the pathFilter
	 */
	public IPathFilter getPathFilter()
	{
		return pathFilter;
	}
	
	
	/**
	 * @param pathFilter the pathFilter to set
	 */
	public void setPathFilter(final IPathFilter pathFilter)
	{
		this.pathFilter = pathFilter;
	}
	
	
	/**
	 * @return the pathFinder
	 */
	public IPathFinder getPathFinder()
	{
		return pathFinder;
	}
	
	
	/**
	 * @param pathFinder the pathFinder to set
	 */
	public void setPathFinder(final IPathFinder pathFinder)
	{
		this.pathFinder = pathFinder;
	}
	
	
	/**
	 * @return the pathFinderInput
	 */
	public final PathFinderInput getPathFinderInput()
	{
		return pathFinderInput;
	}
	
	
	/**
	 * @return the currentPath
	 */
	public final IPath getCurrentPath()
	{
		return currentPath;
	}
	
	
	/**
	 * @param currentPath the currentPath to set
	 */
	public final void setCurrentPath(final IPath currentPath)
	{
		this.currentPath = currentPath;
	}
	
	
	/**
	 * @return the botId
	 */
	public final BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return the customId
	 */
	public final int getCustomId()
	{
		return customId;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + customId;
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Sisyphus other = (Sisyphus) obj;
		if (customId != other.customId)
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * @return the pathPlanningInterval
	 */
	public final int getPathPlanningInterval()
	{
		return pathPlanningInterval;
	}
	
	
	/**
	 * @param pathPlanningInterval the pathPlanningInterval to set
	 */
	public final void setPathPlanningInterval(final int pathPlanningInterval)
	{
		this.pathPlanningInterval = pathPlanningInterval;
	}
}
