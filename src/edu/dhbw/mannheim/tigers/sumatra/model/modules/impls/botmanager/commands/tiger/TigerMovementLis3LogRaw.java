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
 * Raw log from MEMS accelerometers
 * 
 * @author AndreR
 * 
 */
@Deprecated
public class TigerMovementLis3LogRaw extends ACommand
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
	public TigerMovementLis3LogRaw()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		innerAx = byteArray2Short(data, 0);
		innerAy = byteArray2Short(data, 2);
		innerAz = byteArray2Short(data, 4);
		outerAx = byteArray2Short(data, 6);
		outerAy = byteArray2Short(data, 8);
		outerAz = byteArray2Short(data, 10);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte[] data = new byte[getDataLength()];
		
		short2ByteArray(data, 0, innerAx);
		short2ByteArray(data, 2, innerAy);
		short2ByteArray(data, 4, innerAz);
		short2ByteArray(data, 6, outerAx);
		short2ByteArray(data, 8, outerAy);
		short2ByteArray(data, 10, outerAz);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOVEMENT_LIS3_LOG_RAW;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 12;
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
