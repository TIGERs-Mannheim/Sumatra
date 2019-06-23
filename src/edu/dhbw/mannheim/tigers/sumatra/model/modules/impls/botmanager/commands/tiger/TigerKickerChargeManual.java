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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

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
	private int onTicks;		// PWM cycles (@6Mhz)
	private int offTicks;	// PWM cycles (@6Mhz)
	private int duration;	// ms (limited to 6s)
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerKickerChargeManual()
	{
		onTicks = 0;
		offTicks = 100;
		duration = 0;
	}

	public TigerKickerChargeManual(int on, int off, int duration)
	{
		this.onTicks = on;
		this.offTicks = off;
		this.duration = duration*10;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		onTicks = byteArray2UShort(data, 0);
		offTicks = byteArray2UShort(data, 2);
		duration = byteArray2UShort(data, 4);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, onTicks);
		short2ByteArray(data, 2, offTicks);
		short2ByteArray(data, 4, duration);

		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_CHARGE_MANUAL;
	}
	
	@Override
	public int getDataLength()
	{
		return 6;
	}	
}
