/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;


/**
 * Dataholder for three PIDParameters named x, y and w.
 * 
 * @author AndreR
 * 
 */
public class PIDParametersXYW
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private PIDParameters	x	= new PIDParameters();
	private PIDParameters	y	= new PIDParameters();
	private PIDParameters	w	= new PIDParameters();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public PIDParametersXYW()
	{
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public PIDParametersXYW(SubnodeConfiguration config)
	{
		setConfiguration(config);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param config
	 */
	public void setConfiguration(SubnodeConfiguration config)
	{
		x.setConfiguration(config.configurationAt("x"));
		y.setConfiguration(config.configurationAt("y"));
		w.setConfiguration(config.configurationAt("w"));
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		
		config.addConfiguration(x.getConfiguration(), "x", "x");
		config.addConfiguration(y.getConfiguration(), "y", "y");
		config.addConfiguration(w.getConfiguration(), "w", "w");
		
		return config;
	}
	
	
	/**
	 * @return the x
	 */
	public PIDParameters getX()
	{
		return x;
	}
	
	
	/**
	 * @param x the x to set
	 */
	public void setX(PIDParameters x)
	{
		this.x = x;
	}
	
	
	/**
	 * @return the y
	 */
	public PIDParameters getY()
	{
		return y;
	}
	
	
	/**
	 * @param y the y to set
	 */
	public void setY(PIDParameters y)
	{
		this.y = y;
	}
	
	
	/**
	 * @return the w
	 */
	public PIDParameters getW()
	{
		return w;
	}
	
	
	/**
	 * @param w the w to set
	 */
	public void setW(PIDParameters w)
	{
		this.w = w;
	}
}
