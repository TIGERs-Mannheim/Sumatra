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
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Contains the file structure of a config file.
 * 
 * @author AndreR
 */
public class TigerConfigFileStructure extends ACommand
{
	/**
	 * Element type ID.
	 */
	public enum EElementType
	{
		/** */
		UINT8(0, 1, 0, 255),
		/** */
		INT8(1, 1, -128, 127),
		/** */
		UINT16(2, 2, 0, 65535),
		/** */
		INT16(3, 2, -32768, 32767),
		/** */
		UINT32(4, 4, 0, 4294967295L),
		/** */
		INT32(5, 4, -2147483648L, 2147483647L),
		/** */
		FLOAT32(6, 4, 0, 0),
		/** */
		UNKNOWN(7, 0, 0, 0);
		
		private final int		typeId;
		private final int		size;
		private final long	min;
		private final long	max;
		
		
		private EElementType(final int typeId, final int size, final long min, final long max)
		{
			this.typeId = typeId;
			this.size = size;
			this.min = min;
			this.max = max;
		}
		
		
		/**
		 * @return
		 */
		public int getTypeId()
		{
			return typeId;
		}
		
		
		/**
		 * @return
		 */
		public int getSize()
		{
			return size;
		}
		
		
		/**
		 * @return
		 */
		public long getMin()
		{
			return min;
		}
		
		
		/**
		 * @return
		 */
		public long getMax()
		{
			return max;
		}
		
		
		/**
		 * Get element type enum from id.
		 * 
		 * @param id
		 * @return
		 */
		public static EElementType getElementType(final int id)
		{
			for (EElementType s : values())
			{
				if (s.getTypeId() == id)
				{
					return s;
				}
			}
			
			return UNKNOWN;
		}
	}
	
	@SerialData(type = ESerialDataType.UINT16)
	private int		configId;
	
	@SerialData(type = ESerialDataType.UINT16)
	private int		version;
	
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	structure;
	
	
	/** Constructor. */
	public TigerConfigFileStructure()
	{
		super(ECommand.CMD_CONFIG_FILE_STRUCTURE, true);
	}
	
	
	/**
	 * @return the configId
	 */
	public int getConfigId()
	{
		return configId;
	}
	
	
	/**
	 * @return the version
	 */
	public int getVersion()
	{
		return version;
	}
	
	
	/**
	 * Get a list of describing elements.
	 * 
	 * @return
	 */
	public List<EElementType> getElements()
	{
		List<EElementType> elements = new ArrayList<>();
		
		for (byte item : structure)
		{
			EElementType eType = EElementType.getElementType(item);
			elements.add(eType);
		}
		
		return elements;
	}
}
