/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;


/**
 * Holds information about team specific things (see {@link TeamProps}).
 * 
 * @author Gero
 */
public final class TeamConfig extends AConfigClient
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<ITeamConfigObserver>	observers	= new LinkedList<ITeamConfigObserver>();
	
	private TeamProps									team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class TeamConfigHolder
	{
		private static final TeamConfig	CONFIG	= new TeamConfig();
	}
	
	
	private TeamConfig()
	{
		super("team", AAgent.TEAM_CONFIG_PATH, AAgent.KEY_TEAM_CONFIG, AAgent.VALUE_TEAM_CONFIG, true);
	}
	
	
	/**
	 * @return
	 */
	public static TeamConfig getInstance()
	{
		return TeamConfigHolder.CONFIG;
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigClient --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onLoad(final HierarchicalConfiguration xmlConfig)
	{
		team = new TeamProps(xmlConfig);
		
		synchronized (observers)
		{
			for (final ITeamConfigObserver observer : observers)
			{
				observer.onNewTeamConfig(team);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigClient --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newObserver
	 */
	public void addObserver(final ITeamConfigObserver newObserver)
	{
		synchronized (observers)
		{
			observers.add(newObserver);
			
			// Notify current state
			if (team != null)
			{
				newObserver.onNewTeamConfig(team);
			}
		}
	}
	
	
	/**
	 * @param oldObserver
	 * @return
	 */
	public boolean removeObserver(final ITeamConfigObserver oldObserver)
	{
		synchronized (observers)
		{
			return observers.remove(oldObserver);
		}
	}
	
	
	/**
	 * @return the team
	 */
	public TeamProps getTeamProps()
	{
		return team;
	}
}
