/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.EAiTeam;


/**
 * This is the base class for every agent who wants to control our robots!
 * 
 * @author Gero
 */
public abstract class AAgent extends AModule
{
	/** */
	public static final String MODULE_TYPE = "AAgent";
	/** */
	public static final String MODULE_ID = "ai";
	
	
	private final List<IAIObserver> observers = new CopyOnWriteArrayList<>();
	private final List<IVisualizationFrameObserver> visObservers = new CopyOnWriteArrayList<>();
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// nothing to do
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	/**
	 * @param observer
	 */
	public void addVisObserver(final IVisualizationFrameObserver observer)
	{
		visObservers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeVisObserver(final IVisualizationFrameObserver observer)
	{
		visObservers.remove(observer);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final IAIObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IAIObserver o)
	{
		observers.remove(o);
	}
	
	
	protected void notifyAiModeChanged(final EAiTeam aiTeam, final EAIControlState mode)
	{
		synchronized (observers)
		{
			for (IAIObserver observer : observers)
			{
				observer.onAiModeChanged(aiTeam, mode);
			}
		}
	}
	
	
	/**
	 * Reset agent
	 */
	public abstract void reset();
	
	
	/**
	 * @param aiTeam
	 * @param mode
	 */
	public abstract void changeMode(final EAiTeam aiTeam, final EAIControlState mode);
	
	
	/**
	 * @param mode
	 */
	public abstract void changeMode(final EAIControlState mode);
	
	
	/**
	 * Get the ai of the give ai team, if it is running
	 * 
	 * @param selectedTeam
	 * @return
	 */
	public abstract Optional<Ai> getAi(final EAiTeam selectedTeam);
	
	
	/**
	 * If true, process all incoming worldFrames, blocking the WP if necessary
	 * 
	 * @param processAllWorldFrames
	 */
	public abstract void setProcessAllWorldFrames(final boolean processAllWorldFrames);
	
	
	/**
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * 
	 * @param lastAIInfoframe
	 */
	protected void notifyNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		for (final IAIObserver o : observers)
		{
			o.onNewAIInfoFrame(lastAIInfoframe);
		}
	}
	
	
	/**
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * 
	 * @param lastAIInfoframe
	 */
	protected void notifyNewAIInfoFrameVisualize(final VisualizationFrame lastAIInfoframe)
	{
		if (lastAIInfoframe == null)
		{
			return;
		}
		for (final IVisualizationFrameObserver o : visObservers)
		{
			o.onNewVisualizationFrame(lastAIInfoframe);
		}
	}
	
	
	protected void notifyAIStopped(final EAiTeam aiTeam)
	{
		for (final IVisualizationFrameObserver o : visObservers)
		{
			o.onClearVisualizationFrame(aiTeam);
		}
	}
}
