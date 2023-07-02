/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

import lombok.Getter;


public enum GameLogType
{
	LOG_FILE("SSL_LOG_FILE"),
	LABELER_FILE("SSL_LABELER_DATA"),
	UNKNOWN(""),
	;

	@Getter
	private final String header;


	GameLogType(String header)
	{
		this.header = header;
	}

}
