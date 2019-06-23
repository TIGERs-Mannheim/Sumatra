/*
 * *********************************************************
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.01.2018
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Set data acquisition mode.
 * 
 * @author AndreR
 */
public class TigerDataAcqSetMode extends ACommand
{
	/** */
	@SerialData(type = ESerialDataType.UINT8)
	private int mode = 0;
	
	
	/** Constructor. */
	public TigerDataAcqSetMode()
	{
		super(ECommand.CMD_DATA_ACQ_SET_MODE, true);
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param mode
	 */
	public TigerDataAcqSetMode(final EDataAcquisitionMode mode)
	{
		super(ECommand.CMD_DATA_ACQ_SET_MODE, true);
		
		setDataAcquisitionMode(mode);
	}
	
	
	/**
	 * @return the dataAcqusitionMode
	 */
	public EDataAcquisitionMode getDataAcquisitionMode()
	{
		return EDataAcquisitionMode.getModeConstant(mode);
	}
	
	
	/**
	 * @param dataAcqusitionMode the dataAcqusitionMode to set
	 */
	public void setDataAcquisitionMode(final EDataAcquisitionMode dataAcqusitionMode)
	{
		mode = dataAcqusitionMode.getId();
	}
}
