/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.01.2011
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
 * Log from MEMS accelerometers
 * 
 * @author AndreR
 * 
 */
public class TigerMovementLis3Log extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.INT32)
	private int	innerAx;
	@SerialData(type = ESerialDataType.INT32)
	private int	innerAy;
	@SerialData(type = ESerialDataType.INT32)
	private int	innerAz;
	@SerialData(type = ESerialDataType.INT32)
	private int	outerAx;
	@SerialData(type = ESerialDataType.INT32)
	private int	outerAy;
	@SerialData(type = ESerialDataType.INT32)
	private int	outerAz;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMovementLis3Log()
	{
		super(ECommand.CMD_MOVEMENT_LIS3_LOG);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public int[] getInner()
	{
		final int[] inner = new int[3];
		
		inner[0] = innerAx;
		inner[1] = innerAy;
		inner[2] = innerAz;
		
		return inner;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int[] getOuter()
	{
		final int[] outer = new int[3];
		
		outer[0] = outerAx;
		outer[1] = outerAy;
		outer[2] = outerAz;
		
		return outer;
	}
}
