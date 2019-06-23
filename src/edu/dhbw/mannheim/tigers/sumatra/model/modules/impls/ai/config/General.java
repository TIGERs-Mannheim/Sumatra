/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;


/**
 * Configuration object for general parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class General
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String	nodePath	= "general.";
	
	private final int		keeperId;
	
	/** [mm/s] */
	private final float	maxVelocity;
	/** [mm/s^2] */
	private final float	maxAcceleration;
	/** [mm/s^2] */
	private final float	maxDeacceleration;
	/** [mm] */
	private final float	maxBreakingDist;
	
	/** [mm/s] */
	private final float	maxShootVelocity;
	/** [mm/s] */
	private final float	maxPassVeloctiy;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public General(XMLConfiguration configFile)
	{
		keeperId = configFile.getInt(nodePath + "keeperId");
		
		maxVelocity = configFile.getInt(nodePath + "maxVelocity");
		maxAcceleration = configFile.getFloat(nodePath + "maxAcceleration");
		maxDeacceleration = configFile.getFloat(nodePath + "maxDeacceleration");
		maxBreakingDist = configFile.getFloat(nodePath + "maxBreakingDist");
		
		maxShootVelocity = configFile.getFloat(nodePath + "maxShootVelocity");
		maxPassVeloctiy = configFile.getFloat(nodePath + "maxPassVeloctiy");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the keeperId
	 */
	public int getKeeperId()
	{
		return keeperId;
	}
	

	/**
	 * @return the maxBreakingDist [mm]
	 */
	public float getMaxBreakingDist()
	{
		return maxBreakingDist;
	}
	

	/**
	 * @return the maxVelocity [mm/s]
	 */
	public float getMaxVelocity()
	{
		return maxVelocity;
	}
	

	/**
	 * @return the maxAcceleration [mm/s^2]
	 */
	public float getMaxAcceleration()
	{
		return maxAcceleration;
	}
	
	
	/**
	 * @return the maxDeacceleration [mm/s^2]
	 */
	public float getMaxDeacceleration()
	{
		return maxDeacceleration;
	}
	

	/**
	 * @return the maxShootVelocity [mm/s]
	 */
	public float getMaxShootVelocity()
	{
		return maxShootVelocity;
	}
	

	/**
	 * @return the maxPassVeloctiy [mm/s]
	 */
	public float getMaxPassVeloctiy()
	{
		return maxPassVeloctiy;
	}
	
}
