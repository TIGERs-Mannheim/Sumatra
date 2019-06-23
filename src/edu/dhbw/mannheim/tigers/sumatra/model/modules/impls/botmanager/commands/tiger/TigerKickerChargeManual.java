/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Manual kicker charge control.
 * Only intended for maintenance. Do not use this from skills or the AI.
 * 
 * @author AndreR
 * 
 */
public class TigerKickerChargeManual extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** PWM cycles (@6Mhz) */
	@SerialData(type = ESerialDataType.UINT16)
	private int	onTicks;
	/** PWM cycles (@6Mhz) */
	@SerialData(type = ESerialDataType.UINT16)
	private int	offTicks;
	/** ms (limited to 6s) */
	@SerialData(type = ESerialDataType.UINT16)
	private int	duration;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerChargeManual()
	{
		super(ECommand.CMD_KICKER_CHARGE_MANUAL);
		
		onTicks = 0;
		offTicks = 100;
		duration = 0;
	}
	
	
	/**
	 * 
	 * @param on
	 * @param off
	 * @param duration
	 */
	public TigerKickerChargeManual(int on, int off, int duration)
	{
		super(ECommand.CMD_KICKER_CHARGE_MANUAL);
		
		onTicks = on;
		offTicks = off;
		this.duration = duration * 10;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
