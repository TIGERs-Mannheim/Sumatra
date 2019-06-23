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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	private int	innerAx;
	private int	innerAy;
	private int	innerAz;
	private int	outerAx;
	private int	outerAy;
	private int	outerAz;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMovementLis3Log()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		innerAx = byteArray2Int(data, 0);
		innerAy = byteArray2Int(data, 4);
		innerAz = byteArray2Int(data, 8);
		outerAx = byteArray2Int(data, 12);
		outerAy = byteArray2Int(data, 16);
		outerAz = byteArray2Int(data, 20);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte[] data = new byte[getDataLength()];
		
		int2ByteArray(data, 0, innerAx);
		int2ByteArray(data, 4, innerAy);
		int2ByteArray(data, 8, innerAz);
		int2ByteArray(data, 12, outerAx);
		int2ByteArray(data, 16, outerAy);
		int2ByteArray(data, 20, outerAz);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOVEMENT_LIS3_LOG;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 24;
	}
	
	
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
