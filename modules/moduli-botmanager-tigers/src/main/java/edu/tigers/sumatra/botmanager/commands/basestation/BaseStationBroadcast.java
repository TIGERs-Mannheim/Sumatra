/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.ids.BotID;
import lombok.Setter;

import java.util.Set;


/**
 * Send broadcast data to all robots.
 *
 * @note Most fields are overwritten by base station.
 */
public class BaseStationBroadcast extends ACommand
{
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int baseStationId = 0xFF;
	@SerialData(type = SerialData.ESerialDataType.UINT32)
	private long allocatedBotIds = 0;
	@Setter
	@SerialData(type = SerialData.ESerialDataType.UINT32)
	private long unixTime = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int flags = 0;
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private final int[] ballPosition = new int[2];
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int ballPosDelay = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int ballCamId = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private final int[] fieldSizeData = new int[3];
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int boundaryWidth = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int goalWidth = 0;
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int goalDepth = 0;


	public BaseStationBroadcast()
	{
		super(ECommand.CMD_BASE_BROADCAST);
	}


	public void setKickerAutocharge(final boolean enable)
	{
		flags &= ~(0x01);

		if (enable)
		{
			flags |= 0x01;
		}
	}


	public void setStrictVelocityLimit(final boolean enable)
	{
		flags &= ~(0x02);

		if (enable)
		{
			flags |= 0x02;
		}
	}


	public void setAllocatedBotIds(final Set<BotID> botIds)
	{
		allocatedBotIds = 0;

		for (var id : botIds)
		{
			allocatedBotIds |= (1L << id.getNumberWithColorOffsetBS());
		}
	}
}
