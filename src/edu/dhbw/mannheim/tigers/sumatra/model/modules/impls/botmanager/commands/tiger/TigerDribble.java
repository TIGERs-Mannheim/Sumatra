/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Dribble command for the tiger bot.
 * 
 * Velocity is in [rpm] and can be negative to indicate
 * reverse direction.
 * 
 * @author Andre
 * 
 */
public class TigerDribble extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [rpm] */
	private int	rpm;
	
	private final Logger log = Logger.getLogger(getClass());
	
	public static final int MAX_RPM = 25000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set rpm to 0!!!
	 */
	public TigerDribble()
	{
		rpm = 0;
	}
	

	/**
	 * @param speed [rpm]
	 */
	public TigerDribble(int speed)
	{
		rpm = speed;
		
		if(speed > MAX_RPM)
		{
			log.warn("Dribble speed above " + MAX_RPM + ", cut off");
			rpm = MAX_RPM;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		rpm = byteArray2Short(data, 0);
	}
	

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, rpm);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_DRIBBLE;
	}
	
	@Override
	public int getDataLength()
	{
		return 2;
	}
	

	/**
	 * @return [rpm]
	 */
	public int getSpeed()
	{
		return rpm;
	}
	

	public void setSpeed(int rpm)
	{
		this.rpm = rpm;

		if(rpm > MAX_RPM)
		{
			log.warn("Dribble speed above " + MAX_RPM + ", cut off");
			this.rpm = MAX_RPM;
		}
	}
}
