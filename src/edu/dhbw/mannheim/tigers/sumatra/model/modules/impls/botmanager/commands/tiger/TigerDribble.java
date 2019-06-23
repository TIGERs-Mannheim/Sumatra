/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Dribble command for the tiger bot.
 * Velocity is in [rpm] and can be negative to indicate
 * reverse direction.
 * 
 * @author Andre
 */
public class TigerDribble extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log		= Logger.getLogger(TigerDribble.class.getName());
	
	/** [rpm] */
	@SerialData(type = ESerialDataType.UINT16)
	private int							rpm;
	/** */
	public static final int			MAX_RPM	= 40000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set rpm to 0!!!
	 */
	public TigerDribble()
	{
		super(ECommand.CMD_MOTOR_DRIBBLE);
		
		rpm = 0;
	}
	
	
	/**
	 * @param speed [rpm]
	 */
	public TigerDribble(final int speed)
	{
		super(ECommand.CMD_MOTOR_DRIBBLE);
		
		rpm = speed;
		
		if (speed > MAX_RPM)
		{
			log.warn("Dribble speed above " + MAX_RPM + ", cut off");
			rpm = MAX_RPM;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return [rpm]
	 */
	public int getSpeed()
	{
		return rpm;
	}
	
	
	/**
	 * @param rpm
	 */
	public void setSpeed(final int rpm)
	{
		this.rpm = rpm;
		
		if (rpm > MAX_RPM)
		{
			log.warn("Dribble speed above " + MAX_RPM + ", cut off");
			this.rpm = MAX_RPM;
		}
	}
}
