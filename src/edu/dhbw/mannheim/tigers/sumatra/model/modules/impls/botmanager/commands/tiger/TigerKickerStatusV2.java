/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
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
 * Kicker status information.
 * - Charge current [mA]
 * - Capacitor level [V]
 * - temps
 * 
 * - Mode (Manual, Automatic charging)
 * - Arm state
 * - Arm device
 * 
 * @author AndreR
 * 
 */
public class TigerKickerStatusV2 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mA], uint16_t */
	@SerialData(type = ESerialDataType.UINT16)
	private int			chargeCurrent;
	/** [V*e-1], uint16_t */
	@SerialData(type = ESerialDataType.UINT16)
	private int			capLevel;
	// °Ce-1
	@SerialData(type = ESerialDataType.UINT16)
	private final int	TDiode[]	= new int[2];
	// °Ce-1
	@SerialData(type = ESerialDataType.UINT16)
	private final int	TIGBT[]	= new int[2];
	
	
	// LENGTH = 12 bytes
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerStatusV2()
	{
		super(ECommand.CMD_KICKER_STATUSV2);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
		this.chargeCurrent = (int) (chargeCurrent * 1000);
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
	public void setCapLevel(int capLevel)
	{
		this.capLevel = capLevel * 10;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float[] getTDiode()
	{
		final float temp[] = new float[2];
		temp[0] = TDiode[0] / 10.0f;
		temp[1] = TDiode[1] / 10.0f;
		
		return temp;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float[] getTIGBT()
	{
		final float temp[] = new float[2];
		temp[0] = TIGBT[0] / 10.0f;
		temp[1] = TIGBT[1] / 10.0f;
		
		return temp;
	}
}
