/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;


/**
 * Data holder for on-board controller parameters.
 * 
 * @author AndreR
 * 
 */
public class ControllerParameters
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	private PIDParametersXYW	spline	= new PIDParametersXYW();
	/** */
	private PIDParametersXYW	vel		= new PIDParametersXYW();
	/** */
	private PIDParametersXYW	pos		= new PIDParametersXYW();
	/** */
	private PIDParameters		motor		= new PIDParameters();
	/** */
	private PIDParameters		dribbler	= new PIDParameters();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public ControllerParameters()
	{
	}
	
	
	/**
	 * Construct from config.
	 * @param config
	 */
	public ControllerParameters(SubnodeConfiguration config)
	{
		spline.setConfiguration(config.configurationAt("spline"));
		vel.setConfiguration(config.configurationAt("vel"));
		pos.setConfiguration(config.configurationAt("pos"));
		motor.setConfiguration(config.configurationAt("motor"));
		dribbler.setConfiguration(config.configurationAt("dribbler"));
	}
	
	
	/**
	 * @param params
	 */
	public ControllerParameters(ControllerParameters params)
	{
		spline = new PIDParametersXYW(params.spline);
		vel = new PIDParametersXYW(params.vel);
		pos = new PIDParametersXYW(params.pos);
		motor = new PIDParameters(params.motor);
		dribbler = new PIDParameters(params.dribbler);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Update parameters from a TigerCtrlSetPIDParams command.
	 * 
	 * @param params
	 */
	public void updateWithCommand(TigerCtrlSetPIDParams params)
	{
		switch (params.getParamType())
		{
			case POS_X:
				pos.setX(params.getParams());
				break;
			case POS_Y:
				pos.setY(params.getParams());
				break;
			case POS_W:
				pos.setW(params.getParams());
				break;
			case VEL_X:
				vel.setX(params.getParams());
				break;
			case VEL_Y:
				vel.setY(params.getParams());
				break;
			case VEL_W:
				vel.setW(params.getParams());
				break;
			case SPLINE_X:
				spline.setX(params.getParams());
				break;
			case SPLINE_Y:
				spline.setY(params.getParams());
				break;
			case SPLINE_W:
				spline.setW(params.getParams());
				break;
			case DRIBBLER:
				dribbler = params.getParams();
				break;
			case MOTOR:
				motor = params.getParams();
				break;
			case UNKNOWN:
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
		
		config.addConfiguration(spline.getConfiguration(), "spline", "spline");
		config.addConfiguration(vel.getConfiguration(), "vel", "vel");
		config.addConfiguration(pos.getConfiguration(), "pos", "pos");
		config.addConfiguration(motor.getConfiguration(), "motor", "motor");
		config.addConfiguration(dribbler.getConfiguration(), "dribbler", "dribbler");
		
		return config;
	}
	
	
	/**
	 * @return the pos
	 */
	public PIDParametersXYW getPos()
	{
		return pos;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public void setPos(PIDParametersXYW pos)
	{
		this.pos = pos;
	}
	
	
	/**
	 * @return the vel
	 */
	public PIDParametersXYW getVel()
	{
		return vel;
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public void setVel(PIDParametersXYW vel)
	{
		this.vel = vel;
	}
	
	
	/**
	 * @return the dribbler
	 */
	public PIDParameters getDribbler()
	{
		return dribbler;
	}
	
	
	/**
	 * @param dribbler the dribbler to set
	 */
	public void setDribbler(PIDParameters dribbler)
	{
		this.dribbler = dribbler;
	}
	
	
	/**
	 * @return the spline
	 */
	public final PIDParametersXYW getSpline()
	{
		return spline;
	}
	
	
	/**
	 * @param spline the spline to set
	 */
	public final void setSpline(PIDParametersXYW spline)
	{
		this.spline = spline;
	}
	
	
	/**
	 * @return the motor
	 */
	public final PIDParameters getMotor()
	{
		return motor;
	}
	
	
	/**
	 * @param motor the motor to set
	 */
	public final void setMotor(PIDParameters motor)
	{
		this.motor = motor;
	}
}
