/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.10.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Tiger power supply information.
 * 
 * @author AndreR
 */
public class TigerSystemPowerLog extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mA] */
	@SerialData(type = ESerialDataType.UINT16)
	private int			iVCC;
	/** [mA] */
	@SerialData(type = ESerialDataType.UINT16)
	private int			i5V;
	/** [mA] */
	@SerialData(type = ESerialDataType.UINT16)
	private int			i3V3;
	/** [mV] */
	@SerialData(type = ESerialDataType.UINT16)
	private final int	u[]	= new int[4];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerSystemPowerLog()
	{
		super(ECommand.CMD_SYSTEM_POWER_LOG);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the iVCC
	 */
	public float getiVCC()
	{
		return iVCC / 1000.0f;
	}
	
	
	/**
	 * @return the i5V
	 */
	public float getI5V()
	{
		return i5V / 1000.0f;
	}
	
	
	/**
	 * @return the i3V3
	 */
	public float getI3V3()
	{
		return i3V3 / 1000.0f;
	}
	
	
	/**
	 * @param index
	 * @return the u
	 */
	public float getU(final int index)
	{
		if ((index >= 4) || (index < 0))
		{
			return 0;
		}
		
		return u[index] / 1000.0f;
	}
	
	
	/**
	 * @return
	 */
	public float getBatLevel()
	{
		float bat = 0;
		
		if (getU(1) <= 1e-6)
		{
			return getU(0);
		}
		for (int i = 0; i < 4; i++)
		{
			bat += getU(i);
		}
		
		return bat;
	}
}
