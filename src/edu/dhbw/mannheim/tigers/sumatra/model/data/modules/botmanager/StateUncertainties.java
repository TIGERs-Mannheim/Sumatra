/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/** */
public class StateUncertainties
{
	/** */
	private Vector3	pos	= new Vector3();
	/** */
	private Vector3	vel	= new Vector3();
	/** */
	private Vector3	acc	= new Vector3();
	
	
	/** */
	public StateUncertainties()
	{
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public StateUncertainties(SubnodeConfiguration config)
	{
		setConfiguration(config);
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public void setConfiguration(SubnodeConfiguration config)
	{
		pos.setConfiguration(config.configurationAt("pos"));
		vel.setConfiguration(config.configurationAt("vel"));
		acc.setConfiguration(config.configurationAt("acc"));
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		
		config.addConfiguration(pos.getConfiguration(), "pos", "pos");
		config.addConfiguration(vel.getConfiguration(), "vel", "vel");
		config.addConfiguration(acc.getConfiguration(), "acc", "acc");
		
		return config;
	}
	
	
	/**
	 * @return the pos
	 */
	public Vector3 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector3 pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @return the vel
	 */
	public Vector3 getVel()
	{
		return vel;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public void setVel(Vector3 vel)
	{
		this.vel = vel;
	}
	
	
	/**
	 * @return the acc
	 */
	public Vector3 getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @param acc the acc to set
	 */
	public void setAcc(Vector3 acc)
	{
		this.acc = acc;
	}
	
}