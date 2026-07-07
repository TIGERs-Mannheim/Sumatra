package edu.tigers.sumatra.log;

import org.apache.logging.log4j.core.LogEvent;


public interface ILogEventConsumer
{
	void onNewLogEvent(final LogEvent logEvent);
}
