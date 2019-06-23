/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2013
 * Author(s): AndreR
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
 */
public class PIDParameters
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	kp	= 0.0f;
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	ki	= 0.0f;
	@SerialData(type = ESerialDataType.FLOAT32)
	private float	kd	= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public PIDParameters()
	{
	}
	
	
	/**
	 * @param kp
	 * @param ki
	 * @param kd
	 */
	public PIDParameters(final float kp, final float ki, final float kd)
	{
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
	}
	
	
	/**
	 * @param config Configuration to read
	 */
	public PIDParameters(final SubnodeConfiguration config)
	{
		setConfiguration(config);
	}
	
	
	/**
	 * @param params
	 */
	public PIDParameters(final PIDParameters params)
	{
		kp = params.kp;
		kd = params.kd;
		ki = params.ki;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param config read config
	 */
	public void setConfiguration(final SubnodeConfiguration config)
	{
		kp = config.getFloat("Kp", 0.0f);
		ki = config.getFloat("Ki", 0.0f);
		kd = config.getFloat("Kd", 0.0f);
	}
	
	
	/**
	 * @return config
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("Kp", kp);
		config.addProperty("Ki", ki);
		config.addProperty("Kd", kd);
		
		return config;
	}
	
	
	/**
	 * @param kp
	 * @param ki
	 * @param kd
	 */
	public void setParameters(final float kp, final float ki, final float kd)
	{
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
	}
	
	
	/**
	 * @param params
	 */
	public void setParameters(final float[] params)
	{
		if (params.length < 3)
		{
			return;
		}
		
		kp = params[0];
		ki = params[1];
		kd = params[2];
	}
	
	
	/**
	 * @return {Kp, Ki, Kd}
	 */
	public float[] getParameters()
	{
		float[] params = new float[3];
		
		params[0] = kp;
		params[1] = ki;
		params[2] = kd;
		
		return params;
	}
	
	
	/**
	 * @return the kp
	 */
	public float getKp()
	{
		return kp;
	}
	
	
	/**
	 * @param kp the kp to set
	 */
	public void setKp(final float kp)
	{
		this.kp = kp;
	}
	
	
	/**
	 * @return the ki
	 */
	public float getKi()
	{
		return ki;
	}
	
	
	/**
	 * @param ki the ki to set
	 */
	public void setKi(final float ki)
	{
		this.ki = ki;
	}
	
	
	/**
	 * @return the kd
	 */
	public float getKd()
	{
		return kd;
	}
	
	
	/**
	 * @param kd the kd to set
	 */
	public void setKd(final float kd)
	{
		this.kd = kd;
	}
	
	
	@Override
	public String toString()
	{
		return String.format("[kp=%s, ki=%s, kd=%s]", kp, ki, kd);
	}
}
