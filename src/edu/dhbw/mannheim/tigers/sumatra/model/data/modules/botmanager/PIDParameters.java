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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Dataholder for PID parameters with config read/write support.
 * 
 * @author AndreR
 * 
 */
public class PIDParameters
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	Kp	= 0.0f;
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	Ki	= 0.0f;
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	Kd	= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public PIDParameters()
	{
	}
	
	
	/**
	 * 
	 * @param kp
	 * @param ki
	 * @param kd
	 */
	public PIDParameters(float kp, float ki, float kd)
	{
		Kp = kp;
		Ki = ki;
		Kd = kd;
	}
	
	
	/**
	 * 
	 * @param config Configuration to read
	 */
	public PIDParameters(SubnodeConfiguration config)
	{
		setConfiguration(config);
	}
	
	
	/**
	 * 
	 * @param params
	 */
	public PIDParameters(PIDParameters params)
	{
		Kp = params.Kp;
		Kd = params.Kd;
		Ki = params.Ki;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param config read config
	 */
	public void setConfiguration(SubnodeConfiguration config)
	{
		Kp = config.getFloat("Kp", 0.0f);
		Ki = config.getFloat("Ki", 0.0f);
		Kd = config.getFloat("Kd", 0.0f);
	}
	
	
	/**
	 * 
	 * @return config
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("Kp", Kp);
		config.addProperty("Ki", Ki);
		config.addProperty("Kd", Kd);
		
		return config;
	}
	
	
	/**
	 * 
	 * @param kp
	 * @param ki
	 * @param kd
	 */
	public void setParameters(float kp, float ki, float kd)
	{
		Kp = kp;
		Ki = ki;
		Kd = kd;
	}
	
	
	/**
	 * 
	 * @param params
	 */
	public void setParameters(float[] params)
	{
		if (params.length < 3)
		{
			return;
		}
		
		Kp = params[0];
		Ki = params[1];
		Kd = params[2];
	}
	
	
	/**
	 * 
	 * @return {Kp, Ki, Kd}
	 */
	public float[] getParameters()
	{
		float[] params = new float[3];
		
		params[0] = Kp;
		params[1] = Ki;
		params[2] = Kd;
		
		return params;
	}
	
	
	/**
	 * @return the kp
	 */
	public float getKp()
	{
		return Kp;
	}
	
	
	/**
	 * @param kp the kp to set
	 */
	public void setKp(float kp)
	{
		Kp = kp;
	}
	
	
	/**
	 * @return the ki
	 */
	public float getKi()
	{
		return Ki;
	}
	
	
	/**
	 * @param ki the ki to set
	 */
	public void setKi(float ki)
	{
		Ki = ki;
	}
	
	
	/**
	 * @return the kd
	 */
	public float getKd()
	{
		return Kd;
	}
	
	
	/**
	 * @param kd the kd to set
	 */
	public void setKd(float kd)
	{
		Kd = kd;
	}
}
