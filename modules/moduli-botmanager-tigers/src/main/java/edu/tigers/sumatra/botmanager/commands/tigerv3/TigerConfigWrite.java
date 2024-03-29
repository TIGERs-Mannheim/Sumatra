/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure.EElementType;
import edu.tigers.sumatra.botmanager.serial.SerialByteConverter;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import lombok.Getter;

import java.util.List;


/**
 * Write a config to the bot.
 * 
 * @author AndreR
 */
public class TigerConfigWrite extends ACommand
{
	@Getter
	@SerialData(type = ESerialDataType.UINT16)
	private int configId;
	
	@SerialData(type = ESerialDataType.TAIL)
	private byte[] data = new byte[0];
	
	
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
				writeElement(offset, element, value);
			} catch (NumberFormatException ex)
			{
				// ignore
			}
			
			offset += element.getSize();
		}
	}
	
	
	private void writeElement(final int offset, final EElementType element, final String value)
	{
		switch (element)
		{
			case UINT8:
			case INT8:
				SerialByteConverter.byte2ByteArray(data, offset, Integer.valueOf(value));
				break;
			case UINT16:
			case INT16:
				SerialByteConverter.short2ByteArray(data, offset, Integer.valueOf(value));
				break;
			case UINT32:
			case INT32:
				SerialByteConverter.int2ByteArray(data, offset, Integer.valueOf(value));
				break;
			case FLOAT32:
				SerialByteConverter.float2ByteArray(data, offset, Float.valueOf(value));
				break;
			case UNKNOWN:
			default:
				break;
		}
	}
}
