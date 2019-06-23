/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.10.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * Tiger power supply information.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemPowerLog extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int iVCC;	// [mA]
	private int i5V;	// [mA]
	private int i3V3;	// [mA]
	private int u[] = new int[4];	// [mV]	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerSystemPowerLog()
	{
	}	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------

	@Override
	public void setData(byte[] data)
	{
		iVCC = byteArray2UShort(data, 0);
		i5V  = byteArray2UShort(data, 2);
		i3V3 = byteArray2UShort(data, 4);
		u[0] = byteArray2UShort(data, 6);
		u[1] = byteArray2UShort(data, 8);
		u[2] = byteArray2UShort(data, 10);
		u[3] = byteArray2UShort(data, 12);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, iVCC);
		short2ByteArray(data, 2, i5V);
		short2ByteArray(data, 4, i3V3);
		short2ByteArray(data, 6, u[0]);
		short2ByteArray(data, 8, u[1]);
		short2ByteArray(data, 10, u[2]);
		short2ByteArray(data, 12, u[3]);

		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_POWER_LOG;
	}
	
	@Override
	public int getDataLength()
	{
		return 14;
	}

	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the iVCC
	 */
	public float getiVCC()
	{
		return iVCC/1000.0f;
	}

	/**
	 * @return the i5V
	 */
	public float getI5V()
	{
		return i5V/1000.0f;
	}

	/**
	 * @return the i3V3
	 */
	public float getI3V3()
	{
		return i3V3/1000.0f;
	}

	/**
	 * @return the u
	 */
	public float getU(int index)
	{
		if(index >= 4 || index < 0)
		{
			return 0;
		}
		
		return u[index]/1000.0f;
	}
	
	public float getBatLevel()
	{
		float bat = 0;
		
		for(int i = 0; i < 4; i++)
		{
			bat += getU(i);
		}
		
		return bat;
	}
}
