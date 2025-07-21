/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

/**
 * @author AndreR <andre@ryll.cc>
 */
public enum EMessageType
{
	/** ignore message */
	BLANK(0),
	/** try to guess message type by parsing the data */
	UNKNOWN(1),
	SSL_VISION_2010(2),
	SSL_REFBOX_2013(3),
	SSL_VISION_2014(4),
	SSL_VISION_TRACKER_2020(5),
	SSL_INDEX_2021(6),

	/** BaseStationACommand sent by Sumatra */
	TIGERS_BASE_STATION_CMD_SENT(1002),
	/** BaseStationACommand received by Sumatra */
	TIGERS_BASE_STATION_CMD_RECEIVED(1003),
	;

	private final int id;
	
	
	EMessageType(final int id)
	{
		this.id = id;
	}
	
	
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * Convert an id to an enum.
	 * 
	 * @param id
	 * @return enum
	 */
	public static EMessageType getMessageTypeConstant(final int id)
	{
		for (EMessageType s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}
		
		return UNKNOWN;
	}
}
