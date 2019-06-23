/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;


/**
 * This is the base class for every agent who wants to control our robots!
 * 
 * @author Gero
 */
public abstract class AAgent extends AModule implements IWorldFrameConsumer, IRefereeMsgConsumer,
		IMultiTeamMessageConsumer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String			MODULE_TYPE					= "AAgent";
	/** */
	public static final String			MODULE_ID_YELLOW			= "ai_yellow";
	/** */
	public static final String			MODULE_ID_BLUE				= "ai_blue";
	
	// --- config ---
	/** */
	public static final String			AI_CONFIG_PATH				= "./config/ai/";
	
	
	// --- geometry ---
	/** */
	public static final String			KEY_GEOMETRY_CONFIG		= AAgent.class.getName() + ".geometry";
	/** */
	public static final String			GEOMETRY_CONFIG_PATH		= "./config/geometry/";
	/**  */
	public static final String			VALUE_GEOMETRY_CONFIG	= "grSim.xml";
	
	
	/** */
	private final List<IAIObserver>	observers					= new ArrayList<IAIObserver>();
	
	private ETeamColor					teamColor					= ETeamColor.UNINITIALIZED;
	
	private final List<IAIObserver>	visObservers				= new CopyOnWriteArrayList<IAIObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addVisObserver(final IAIObserver observer)
	{
		visObservers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeVisObserver(final IAIObserver observer)
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
	
	
	/**
	 * @return the observers
	 */
	public final List<IAIObserver> getObservers()
	{
		return observers;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	/**
	 * @param color
	 */
	protected final void setTeamColor(final ETeamColor color)
	{
		teamColor = color;
	}
	
	
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
	protected void notifyNewAIInfoFrameVisualize(final AIInfoFrame lastAIInfoframe)
	{
		if (lastAIInfoframe == null)
		{
			return;
		}
		for (final IAIObserver o : visObservers)
		{
			o.onNewAIInfoFrame(lastAIInfoframe);
		}
	}
	
	
	protected void notifyAIStopped(final ETeamColor teamColor)
	{
		for (final IAIObserver o : visObservers)
		{
			o.onAIStopped(teamColor);
		}
	}
	
	
	/**
	 * @param ex
	 * @param frame
	 * @param prevFrame
	 */
	protected void notifyNewAIExceptionVisualize(final Throwable ex, final AIInfoFrame frame, final AIInfoFrame prevFrame)
	{
		for (final IAIObserver o : visObservers)
		{
			o.onAIException(ex, frame, prevFrame);
		}
	}
}
