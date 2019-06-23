/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Kicker status information.
 * - Charge current [mA]
 * - Capacitor level [V]
 * - IR barrier level [V]
 * 
 * - Mode (Manual, Automatic charging)
 * - Arm state
 * - Arm device
 * 
 * @author AndreR
 * 
 */
public class TigerKickerStatusV3 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mA], uint16_t */
	private int	chargeCurrent;
	/** [V*e-1], uint16_t */
	private int	capLevel;
	/** [V*e-1], uint16_t */
	private int	irLevel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerStatusV3()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		chargeCurrent = byteArray2UShort(data, 0);
		capLevel = byteArray2UShort(data, 2);
		irLevel = byteArray2UShort(data, 4);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, chargeCurrent);
		short2ByteArray(data, 2, capLevel);
		short2ByteArray(data, 4, irLevel);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_STATUSV3;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 6;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getChargeCurrent()
	{
		return chargeCurrent / 1000f;
	}
	
	
	/**
	 * 
	 * @param chargeCurrent
	 */
	public void setChargeCurrent(float chargeCurrent)
	{
		this.chargeCurrent = (int) (chargeCurrent * 1000.0f);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getCapLevel()
	{
		return capLevel / 10.0f;
	}
	
	
	/**
	 * 
	 * @param capLevel
	 */
	public void setCapLevel(float capLevel)
	{
		this.capLevel = (int) (capLevel * 10.0f);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getIrLevel()
	{
		return irLevel / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param irLevel
	 */
	public void setIrLevel(float irLevel)
	{
		this.irLevel = (int) (irLevel * 10000.0f);
	}
}
