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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.IApollonControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.IMetisHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IManualBotObserver;
import edu.moduli.AModule;


/**
 * This is the base class for every agent who wants to control our robots!
 * 
 * @author Gero
 * 
 */
public abstract class AAgent extends AModule implements IWorldFrameConsumer, IRefereeMsgConsumer,
		IAthenaControlHandler, IManualBotObserver, IApollonControlHandler, IMetisHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String			MODULE_TYPE					= "AAgent";
	/** */
	public static final String			MODULE_ID					= "ai";
	
	// --- config ---
	/** */
	public static final String			KEY_AI_CONFIG				= AAgent.class.getName() + ".config";
	/** */
	public static final String			AI_CONFIG_PATH				= "./config/ai/";
	/**  */
	public static final String			VALUE_AI_CONFIG			= "ai_default.xml";
	
	// --- bot config ---
	/** */
	public static final String			KEY_BOT_CONFIG				= AAgent.class.getName() + ".bot";
	/** */
	public static final String			BOT_CONFIG_PATH			= "./config/bot/";
	/**  */
	public static final String			VALUE_BOT_CONFIG			= "bots_default.xml";
	
	// --- geometry ---
	/** */
	public static final String			KEY_GEOMETRY_CONFIG		= AAgent.class.getName() + ".geometry";
	/** */
	public static final String			GEOMETRY_CONFIG_PATH		= "./config/geometry/";
	/**  */
	public static final String			VALUE_GEOMETRY_CONFIG	= "RoboCup_2012.xml";
	
	// --- team ---
	/** */
	public static final String			KEY_TEAM_CONFIG			= AAgent.class.getName() + ".team";
	/** */
	public static final String			TEAM_CONFIG_PATH			= "./config/team/";
	/**  */
	public static final String			VALUE_TEAM_CONFIG			= "yellow_default.xml";
	
	// AI visualization
	/** */
	private final List<IAIObserver>	observers					= new ArrayList<IAIObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param o
	 */
	public void addObserver(IAIObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(IAIObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	/**
	 * @return the observers
	 */
	public final List<IAIObserver> getObservers()
	{
		return observers;
	}
}
