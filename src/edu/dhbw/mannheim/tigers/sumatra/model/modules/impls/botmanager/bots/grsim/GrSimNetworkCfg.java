package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 23, 2013
 * Author(s): Peter Birkenkampf, TilmanS
 * 
 * *********************************************************
 */
import org.apache.commons.configuration.SubnodeConfiguration;


/**
 * Configuration for communicating with grSim
 * 
 * @author Peter Birkenkampf, TilmanS
 * 
 */
public class GrSimNetworkCfg
{
	// --------------------------------------------------------------
	// --- instance-variables ---------------------------------------
	// --------------------------------------------------------------
	
	private String		ip;
	private int			port;
	private boolean	teamYellow;
	private int			key;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	
	/**
	 * @param config
	 */
	public GrSimNetworkCfg(SubnodeConfiguration config)
	{
		ip = config.getString("ip", "127.0.0.1");
		port = config.getInt("port", 20011);
		teamYellow = config.getBoolean("teamYellow", false);
		
		key = config.getInt("[@id]");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the ip
	 */
	public String getIp()
	{
		return ip;
	}
	
	
	/**
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}
	
	
	/**
	 * @return the teamYellow
	 */
	public boolean isTeamYellow()
	{
		return teamYellow;
	}
	
	
	/**
	 * @return the configuration node used to create this object
	 */
	public HierarchicalConfiguration getConfig()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		Node node = new Node("grSimNetwork");
		node.addAttribute(new Node("id", key));
		config.setRoot(node);
		config.addProperty("ip", ip);
		config.addProperty("port", port);
		config.addProperty("teamYellow", teamYellow);
		
		return config;
	}
}
