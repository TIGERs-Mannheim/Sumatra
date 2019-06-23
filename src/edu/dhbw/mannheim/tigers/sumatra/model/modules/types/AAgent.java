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

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;


/**
 * This is the base class for every agent who wants to control our robots!
 * 
 * @author Gero
 * 
 */
public abstract class AAgent extends AModule implements IWorldFrameConsumer, IRefereeMsgConsumer
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
	
	/** */
	public static final String			KEY_AI_CONFIG				= AAgent.class.getName() + ".config";
	/**  */
	public static final String			VALUE_AI_CONFIG			= "ai_default.xml";
	
	/** */
	public static final String			KEY_SKILL_CONFIG			= AAgent.class.getName() + ".skills";
	/** */
	public static final String			VALUE_SKILL_CONFIG		= "skills.xml";
	
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
	public static final String			VALUE_GEOMETRY_CONFIG	= "RoboCup_2014.xml";
	
	// --- team ---
	/** */
	public static final String			KEY_TEAM_CONFIG			= AAgent.class.getName() + ".team.v2";
	/** */
	public static final String			TEAM_CONFIG_PATH			= "./config/team/";
	/**  */
	public static final String			VALUE_TEAM_CONFIG			= "team_default.xml";
	
	// AI visualization
	/** */
	private final List<IAIObserver>	observers					= new ArrayList<IAIObserver>();
	
	private ETeamColor					teamColor					= ETeamColor.UNINITIALIZED;
	
	
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
	
	
	/**
	 * @return
	 */
	@Override
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	/**
	 * 
	 * @param color
	 */
	protected final void setTeamColor(ETeamColor color)
	{
		teamColor = color;
	}
}
