package edu.tigers.sumatra.persistence.log;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.Map;


/**
 * Data object for persisting log events from log4j
 */
public class PersistenceLogEvent
{
	@Getter
	private final long timestamp;
	private final String level;
	private final String thread;
	private final String clazz;
	private final String message;
	private final Map<String, String> contextData;


	public PersistenceLogEvent(final LogEvent event)
	{
		timestamp = event.getTimeMillis();
		level = event.getLevel().toString();
		thread = event.getThreadName();
		clazz = event.getLoggerName();
		message = event.getMessage().getFormattedMessage();
		contextData = event.getContextData().toMap();
	}


	public final LogEvent getLogEvent()
	{
		return Log4jLogEvent.newBuilder()
				.setLoggerName(clazz == null ? "Unknown" : clazz)
				.setLevel(Level.toLevel(level))
				.setMessage(new SimpleMessage(message))
				.setThreadName(thread)
				.setTimeMillis(timestamp)
				.setContextData(new JdkMapAdapterStringMap(contextData, true))
				.build();
	}
}
