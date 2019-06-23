/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Log packet from IR sensors
 * 
 * @author AndreR
 * 
 */
public class TigerKickerIrLog extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final int	LEFT			= 2;
	/** */
	public static final int	CENTER		= 3;
	/** */
	public static final int	RIGHT			= 0;
	/** */
	public static final int	BARRIER		= 1;
	
	private final int			voltage[]	= new int[4];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerIrLog()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		for (int i = 0; i < 4; i++)
		{
			voltage[i] = byteArray2UShort(data, i * 2);
		}
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		for (int i = 0; i < 4; i++)
		{
			short2ByteArray(data, i * 2, voltage[i]);
		}
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_IR_LOG;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 8;
	}
	
	
	/**
	 * @return the voltage
	 */
	public float[] getVoltage()
	{
		final float v[] = new float[4];
		
		for (int i = 0; i < 4; i++)
		{
			v[i] = ((float) voltage[i]) / 10000;
		}
		
		return v;
	}
	
	
	/**
	 * @param v the voltage to set
	 */
	public void setVoltage(float[] v)
	{
		for (int i = 0; i < 4; i++)
		{
			voltage[i] = (int) (v[i] * 10000);
		}
	}
}
