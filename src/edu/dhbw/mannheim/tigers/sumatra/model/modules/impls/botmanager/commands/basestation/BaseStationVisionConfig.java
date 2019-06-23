/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Configure vision rate and field coordinate inversion.
 * 
 * @author AndreR
 * 
 */
public class BaseStationVisionConfig extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private boolean	positionInverted	= false;
	private boolean	tigersBlue			= false;
	private int			visionRate			= 30;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationVisionConfig()
	{
	}
	
	
	/**
	 * 
	 * @param invertPos
	 * @param visionRate
	 * @param tigersBlue
	 */
	public BaseStationVisionConfig(boolean invertPos, int visionRate, boolean tigersBlue)
	{
		positionInverted = invertPos;
		this.visionRate = visionRate;
		this.tigersBlue = tigersBlue;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		positionInverted = byteArray2UByte(data, 0) != 0;
		tigersBlue = byteArray2UByte(data, 1) != 0;
		visionRate = byteArray2UShort(data, 2);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, positionInverted == true ? 1 : 0);
		byte2ByteArray(data, 1, tigersBlue == true ? 1 : 0);
		short2ByteArray(data, 2, visionRate);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BASE_VISION_CONFIG;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 4;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the positionInverted
	 */
	public boolean isPositionInverted()
	{
		return positionInverted;
	}
	
	
	/**
	 * @param positionInverted the positionInverted to set
	 */
	public void setPositionInverted(boolean positionInverted)
	{
		this.positionInverted = positionInverted;
	}
	
	
	/**
	 * @return the visionRate
	 */
	public int getVisionRate()
	{
		return visionRate;
	}
	
	
	/**
	 * @param visionRate the visionRate to set
	 */
	public void setVisionRate(int visionRate)
	{
		this.visionRate = visionRate;
	}
	
	
	/**
	 * @return the tigersBlue
	 */
	public boolean isTigersBlue()
	{
		return tigersBlue;
	}
	
	
	/**
	 * @param tigersBlue the tigersBlue to set
	 */
	public void setTigersBlue(boolean tigersBlue)
	{
		this.tigersBlue = tigersBlue;
	}
}
