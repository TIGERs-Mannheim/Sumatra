/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;


/**
 * Bot structure parameters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class Structure
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float	frontAngle;
	private final float	backAngle;
	private final float	botRadius;
	private final float	wheelRadius;
	private final float	mass;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Default params
	 */
	public Structure()
	{
		frontAngle = 45;
		backAngle = 60;
		botRadius = 0.049f;
		wheelRadius = 0.035f;
		mass = 4;
	}
	
	
	/**
	 * @param frontAngle
	 * @param backAngle
	 * @param botRadius
	 * @param wheelRadius
	 * @param mass
	 */
	public Structure(float frontAngle, float backAngle, float botRadius, float wheelRadius, float mass)
	{
		super();
		this.frontAngle = frontAngle;
		this.backAngle = backAngle;
		this.botRadius = botRadius;
		this.wheelRadius = wheelRadius;
		this.mass = mass;
	}
	
	
	/**
	 * @param configurationAt
	 */
	public Structure(SubnodeConfiguration configurationAt)
	{
		frontAngle = configurationAt.getFloat("frontAngle");
		backAngle = configurationAt.getFloat("backAngle");
		botRadius = configurationAt.getFloat("botRadius");
		wheelRadius = configurationAt.getFloat("wheelRadius");
		mass = configurationAt.getFloat("mass");
	}
	
	
	/**
	 * @param orig
	 */
	public Structure(Structure orig)
	{
		frontAngle = orig.frontAngle;
		backAngle = orig.backAngle;
		botRadius = orig.botRadius;
		wheelRadius = orig.wheelRadius;
		mass = orig.mass;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * 
	 * @return config
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("frontAngle", frontAngle);
		config.addProperty("backAngle", backAngle);
		config.addProperty("botRadius", botRadius);
		config.addProperty("wheelRadius", wheelRadius);
		config.addProperty("mass", mass);
		
		return config;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the frontAngle
	 */
	public final float getFrontAngle()
	{
		return frontAngle;
	}
	
	
	/**
	 * @return the backAngle
	 */
	public final float getBackAngle()
	{
		return backAngle;
	}
	
	
	/**
	 * @return the botRadius
	 */
	public final float getBotRadius()
	{
		return botRadius;
	}
	
	
	/**
	 * @return the wheelRadius
	 */
	public final float getWheelRadius()
	{
		return wheelRadius;
	}
	
	
	/**
	 * @return the mass
	 */
	public final float getMass()
	{
		return mass;
	}
}
