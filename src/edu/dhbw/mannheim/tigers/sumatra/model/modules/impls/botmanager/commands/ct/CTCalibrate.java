/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

public class CTCalibrate extends ACommand
{
	private int time;
	
	public CTCalibrate()
	{
		this.time = 2000;
	}
	
	public CTCalibrate(int time)
	{
		this.time = time;
	}
	
	public void setData(byte[] data)
	{
		time = byteArray2UShort(data, 0);
	}
	
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, time);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CT_CALIBRATE;
	}
	
	@Override
	public int getDataLength()
	{
		return 2;
	}


	public void setTime(int time)
	{
		this.time = time;
	}
	
	public int getTime()
	{
		return this.time;
	}
}
