/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;


/**
 * Data holder for sensor fusion parameters.
 * 
 * @author AndreR
 * 
 */
public class SensorFusionParameters
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	private StateUncertainties		ex	= new StateUncertainties();
	/** */
	private SensorUncertainties	ez	= new SensorUncertainties();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public SensorFusionParameters()
	{
	}
	
	
	/**
	 * Construct from config.
	 * @param config
	 */
	public SensorFusionParameters(SubnodeConfiguration config)
	{
		ex.setConfiguration(config.configurationAt("state"));
		ez.setConfiguration(config.configurationAt("sensor"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Change values according to a TigerCtrlSetFilterParams command.
	 * 
	 * @param params
	 */
	public void updateWithCommand(TigerCtrlSetFilterParams params)
	{
		switch (params.getType())
		{
			case EX_POS:
			{
				ex.setPos(params.getParamsVector3());
			}
				break;
			case EX_VEL:
			{
				ex.setVel(params.getParamsVector3());
			}
				break;
			case EX_ACC:
			{
				ex.setAcc(params.getParamsVector3());
			}
				break;
			case EZ_VISION:
			{
				ez.setVision(params.getParamsVector3());
			}
				break;
			case EZ_ENCODER:
			{
				ez.setEncoder(params.getParamsVector3());
			}
				break;
			case EZ_ACC_GYRO:
			{
				ez.setAccelerometer(params.getParamsVector2());
				ez.setGyroscope(params.getParams()[2]);
			}
				break;
			case EZ_MOTOR:
			{
				ez.setMotor(params.getParamsVector3());
			}
				break;
			default:
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Get configuration.
	 * 
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		
		config.addConfiguration(ex.getConfiguration(), "state", "state");
		config.addConfiguration(ez.getConfiguration(), "sensor", "sensor");
		
		return config;
	}
	
	
	/**
	 * @return the ex
	 */
	public StateUncertainties getEx()
	{
		return ex;
	}
	
	
	/**
	 * @param ex the ex to set
	 */
	public void setEx(StateUncertainties ex)
	{
		this.ex = ex;
	}
	
	
	/**
	 * @return the ez
	 */
	public SensorUncertainties getEz()
	{
		return ez;
	}
	
	
	/**
	 * @param ez the ez to set
	 */
	public void setEz(SensorUncertainties ez)
	{
		this.ez = ez;
	}
}
