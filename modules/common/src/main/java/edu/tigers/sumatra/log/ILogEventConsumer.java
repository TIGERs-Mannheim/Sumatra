/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.log;

import org.apache.logging.log4j.core.LogEvent;


public interface ILogEventConsumer
{
	void onNewLogEvent(final LogEvent logEvent);
}
