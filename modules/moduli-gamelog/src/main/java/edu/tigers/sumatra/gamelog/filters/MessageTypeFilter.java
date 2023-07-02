/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog.filters;

import edu.tigers.sumatra.gamelog.EMessageType;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;

@RequiredArgsConstructor
public class MessageTypeFilter implements MessageFilter
{
	private final EnumSet<EMessageType> acceptedTypes;

	@Override
	public boolean filter(long timestampNs, EMessageType messageType)
	{
		return acceptedTypes.contains(messageType);
	}
}
