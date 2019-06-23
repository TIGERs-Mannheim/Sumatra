/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import java.io.UnsupportedEncodingException;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Description of a config element (single value).
 * Probably only the name for now.
 * 
 * @author AndreR
 */
public class TigerConfigItemDesc extends ACommand
{
	@SerialData(type = ESerialDataType.UINT16)
	private int					configId;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int					element;
	
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]				name;
	
	/**
	 * Constant for the filename of a config, element number otherwise.
	 */
	public static final int	CONFIG_ITEM_FILE_NAME	= 0xFF;
	
	
	/** Constructor. */
	public TigerConfigItemDesc()
	{
		super(ECommand.CMD_CONFIG_ITEM_DESC, true);
	}
	
	
	/**
	 * @param cfgId
	 * @param element
	 */
	public TigerConfigItemDesc(final int cfgId, final int element)
	{
		super(ECommand.CMD_CONFIG_ITEM_DESC, true);
		
		configId = cfgId;
		this.element = element;
	}
	
	
	/**
	 * @return the configId
	 */
	public int getConfigId()
	{
		return configId;
	}
	
	
	/**
	 * @param configId the configId to set
	 */
	public void setConfigId(final int configId)
	{
		this.configId = configId;
	}
	
	
	/**
	 * @return the element
	 */
	public int getElement()
	{
		return element;
	}
	
	
	/**
	 * @param element the element to set
	 */
	public void setElement(final int element)
	{
		this.element = element;
	}
	
	
	/**
	 * @return
	 */
	@SuppressWarnings("squid:S1166")
	public String getName()
	{
		String text;
		
		try
		{
			text = new String(name, 0, name.length, "US-ASCII");
		} catch (UnsupportedEncodingException err)
		{
			text = "Unsupported Encoding";
		}
		
		return text;
	}
}
