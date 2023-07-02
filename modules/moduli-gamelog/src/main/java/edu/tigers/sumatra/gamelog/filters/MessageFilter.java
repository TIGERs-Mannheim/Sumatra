/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog.filters;

import edu.tigers.sumatra.gamelog.EMessageType;


@FunctionalInterface
public interface MessageFilter
{
	/**
	 *
	 * @param timestampNs
	 * @param messageType
	 * @return True to keep message, false to filter it out.
	 */
	boolean filter(long timestampNs, EMessageType messageType);
}
