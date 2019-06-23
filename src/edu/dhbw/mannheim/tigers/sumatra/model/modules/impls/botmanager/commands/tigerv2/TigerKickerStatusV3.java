/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Kicker status information.
 * - Charge current [mA]
 * - Capacitor level [V]
 * - IR barrier level [V]
 * - Mode (Manual, Automatic charging)
 * - Arm state
 * - Arm device
 * 
 * @author AndreR
 */
public class TigerKickerStatusV3 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mA], uint16_t */
	@SerialData(type = ESerialDataType.UINT16)
	private int	chargeCurrent;
	/** [V*e-1], uint16_t */
	@SerialData(type = ESerialDataType.UINT16)
	private int	capLevel;
	/** [V*e-1], uint16_t */
	@SerialData(type = ESerialDataType.UINT16)
	private int	irLevel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerStatusV3()
	{
		super(ECommand.CMD_KICKER_STATUSV3);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public float getChargeCurrent()
	{
		return chargeCurrent / 1000f;
	}
	
	
	/**
	 * @param chargeCurrent
	 */
	public void setChargeCurrent(final float chargeCurrent)
	{
		this.chargeCurrent = (int) (chargeCurrent * 1000.0f);
	}
	
	
	/**
	 * @return
	 */
	public float getCapLevel()
	{
		return capLevel / 10.0f;
	}
	
	
	/**
	 * @param capLevel
	 */
	public void setCapLevel(final float capLevel)
	{
		this.capLevel = (int) (capLevel * 10.0f);
	}
	
	
	/**
	 * @return
	 */
	public float getIrLevel()
	{
		return irLevel / 10000.0f;
	}
	
	
	/**
	 * @param irLevel
	 */
	public void setIrLevel(final float irLevel)
	{
		this.irLevel = (int) (irLevel * 10000.0f);
	}
}
