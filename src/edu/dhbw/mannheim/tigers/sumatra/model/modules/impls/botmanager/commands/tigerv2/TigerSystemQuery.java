/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Query configs from bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerSystemQuery extends ACommand
{
	@SerialData(type = ESerialDataType.UINT16)
	private int	queryType	= 0;
	
	/**
	 */
	public enum EQueryType
	{
		/**  */
		CTRL_PID(0),
		/**  */
		CTRL_STRUCTURE(1),
		/**  */
		CTRL_SENSOR(2),
		/**  */
		CTRL_STATE(3),
		/**  */
		CTRL_TYPE(4),
		/** */
		KICKER_CONFIG(5);
		
		private final int	id;
		
		
		private EQueryType(final int id)
		{
			this.id = id;
		}
	}
	
	
	/**
	 */
	public TigerSystemQuery()
	{
		super(ECommand.CMD_SYSTEM_QUERY, true);
	}
	
	
	/**
	 * @param queryType
	 */
	public TigerSystemQuery(final EQueryType queryType)
	{
		super(ECommand.CMD_SYSTEM_QUERY, true);
		this.queryType = queryType.id;
	}
	
	
	/**
	 * @param queryType the queryType to set
	 */
	public void setQueryType(final EQueryType queryType)
	{
		this.queryType = queryType.id;
	}
}
