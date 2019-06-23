/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure.EElementType;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Read a config from the bot.
 * 
 * @author AndreR
 */
public class TigerConfigRead extends ACommand
{
	@SerialData(type = ESerialDataType.UINT16)
	private int				configId;
	
	@SuppressWarnings("squid:S1170")
	@SerialData(type = ESerialDataType.TAIL)
	private final byte[]	data	= null;
	
	
	/** Constructor. */
	public TigerConfigRead()
	{
		super(ECommand.CMD_CONFIG_READ, true);
	}
	
	
	/**
	 * @param cfgId
	 */
	public TigerConfigRead(final int cfgId)
	{
		super(ECommand.CMD_CONFIG_READ, true);
		
		configId = cfgId;
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
	 * Get the values of the config as strings for user presentation.
	 * 
	 * @param structure
	 * @return
	 */
	@SuppressWarnings("squid:S2583")
	public List<String> getData(final TigerConfigFileStructure structure)
	{
		List<String> values = new ArrayList<>();
		List<EElementType> elements = structure.getElements();
		
		if (data == null)
		{
			return values;
		}
		
		int offset = 0;
		
		for (EElementType element : elements)
		{
			if ((offset + element.getSize()) > data.length)
			{
				return values;
			}
			
			String val = null;
			switch (element)
			{
				case UINT8:
					val = Integer.toString(ACommand.byteArray2UByte(data, offset));
					break;
				case INT8:
					val = Integer.toString(data[offset]);
					break;
				case UINT16:
					val = Integer.toString(ACommand.byteArray2UShort(data, offset));
					break;
				case INT16:
					val = Integer.toString(ACommand.byteArray2Short(data, offset));
					break;
				case UINT32:
					val = Long.toString(ACommand.byteArray2UInt(data, offset));
					break;
				case INT32:
					val = Integer.toString(ACommand.byteArray2Int(data, offset));
					break;
				case FLOAT32:
					val = Float.toString(ACommand.byteArray2Float(data, offset));
					break;
				case UNKNOWN:
				default:
					break;
			}
			
			if (val != null)
			{
				values.add(val);
			}
			
			offset += element.getSize();
		}
		
		return values;
	}
}
