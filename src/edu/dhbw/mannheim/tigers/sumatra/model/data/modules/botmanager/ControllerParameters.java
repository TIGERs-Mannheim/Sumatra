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
	private PIDParametersXYW	pos		= new PIDParametersXYW();
	/** */
	private PIDParametersXYW	vel		= new PIDParametersXYW();
	/** */
	private PIDParametersXYW	acc		= new PIDParametersXYW();
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
		pos.setConfiguration(config.configurationAt("pos"));
		vel.setConfiguration(config.configurationAt("vel"));
		acc.setConfiguration(config.configurationAt("acc"));
		dribbler.setConfiguration(config.configurationAt("dribbler"));
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
		switch (params.getType())
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
			case ACC_X:
				acc.setX(params.getParams());
				break;
			case ACC_Y:
				acc.setY(params.getParams());
				break;
			case ACC_W:
				acc.setW(params.getParams());
				break;
			case DRIBBLER:
				dribbler = params.getParams();
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
		
		config.addConfiguration(pos.getConfiguration(), "pos", "pos");
		config.addConfiguration(vel.getConfiguration(), "vel", "vel");
		config.addConfiguration(acc.getConfiguration(), "acc", "acc");
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
	 * @return the acc
	 */
	public PIDParametersXYW getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @param acc the acc to set
	 */
	public void setAcc(PIDParametersXYW acc)
	{
		this.acc = acc;
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
}
