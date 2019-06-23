/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import java.util.List;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure.EElementType;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Write a config to the bot.
 * 
 * @author AndreR
 */
public class TigerConfigWrite extends ACommand
{
	@SerialData(type = ESerialDataType.UINT16)
	private int		configId;
	
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	data;
	
	
	/** Constructor. */
	public TigerConfigWrite()
	{
		super(ECommand.CMD_CONFIG_WRITE, true);
	}
	
	
	/**
	 * @param cfgId
	 */
	public TigerConfigWrite(final int cfgId)
	{
		super(ECommand.CMD_CONFIG_WRITE, true);
		
		configId = cfgId;
	}
	
	
	/**
	 * Compile user defined string values into binary config data.
	 * 
	 * @param structure
	 * @param values
	 */
	public void setData(final TigerConfigFileStructure structure, final List<String> values)
	{
		List<EElementType> elements = structure.getElements();
		
		if (elements.size() != values.size())
		{
			return;
		}
		
		int size = 0;
		for (EElementType element : elements)
		{
			size += element.getSize();
		}
		
		data = new byte[size];
		
		int offset = 0;
		
		for (int i = 0; i < values.size(); i++)
		{
			EElementType element = elements.get(i);
			String value = values.get(i);
			
			try
			{
				switch (element)
				{
					case UINT8:
					case INT8:
						ACommand.byte2ByteArray(data, offset, Integer.valueOf(value));
						break;
					case UINT16:
					case INT16:
						ACommand.short2ByteArray(data, offset, Integer.valueOf(value));
						break;
					case UINT32:
					case INT32:
						ACommand.int2ByteArray(data, offset, Integer.valueOf(value));
						break;
					case FLOAT32:
						ACommand.float2ByteArray(data, offset, Float.valueOf(value));
						break;
					case UNKNOWN:
					default:
						break;
				}
			} catch (NumberFormatException ex)
			{
				// ignore
			}
			
			offset += element.getSize();
		}
	}
}
