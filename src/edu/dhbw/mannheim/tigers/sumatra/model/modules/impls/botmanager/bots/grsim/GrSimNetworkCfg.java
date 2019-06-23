package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 23, 2013
 * Author(s): Peter Birkenkampf, TilmanS
 * *********************************************************
 */
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;


/**
 * Configuration for communicating with grSim
 * 
 * @author Peter Birkenkampf, TilmanS
 */
public class GrSimNetworkCfg
{
	// --------------------------------------------------------------
	// --- instance-variables ---------------------------------------
	// --------------------------------------------------------------
	
	private String		ip;
	private int			port, backPort;
	private int			portCfg;
	private boolean	teamYellow;
	private int			key;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	
	/**
	 * @param config
	 */
	public GrSimNetworkCfg(final SubnodeConfiguration config)
	{
		ip = config.getString("ip", "127.0.0.1");
		portCfg = config.getInt("port", 20011);
		port = UserConfig.getGrSimCommandPort();
		backPort = UserConfig.getGrSimCommandBackPort();
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
	 * @return
	 */
	public int getBackPort()
	{
		return backPort;
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
		config.addProperty("port", portCfg);
		config.addProperty("teamYellow", teamYellow);
		
		return config;
	}
	
	
}
