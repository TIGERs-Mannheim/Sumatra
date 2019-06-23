package edu.tigers.sumatra.log;


import java.io.IOException;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;


/**
 * <p>
 * JUL bridge/router for log4j. Since jMonkeyEngine uses JUL (Java Unified Logging), and JGF uses log4j, this bridge is
 * installed so logging statements sent to JUL are handled by JGF logging system.
 * </p>
 * 
 * @author Christian Stein (JGF), jjmontes (JGF), Gero
 */
public class JULLoggingBridge extends Handler
{
	
	
	private final boolean	classname;
	
	private final boolean	format;
	
	
	/**
	 * Resets the entire JUL logging system and adds a single JulLoggingBridge handler.
	 * instance to the root logger.
	 */
	public static void install()
	{
		install(new JULLoggingBridge(true, true));
	}
	
	
	/**
	 * Resets the entire JUL logging system and adds the JULLoggingBridge instance
	 * to the root logger.
	 * 
	 * @param handler
	 */
	public static void install(final JULLoggingBridge handler)
	{
		LogManager.getLogManager().reset();
		LogManager.getLogManager().getLogger("").addHandler(handler);
	}
	
	
	/**
	 * Rereads the JUL configuration.
	 * 
	 * @throws IOException
	 * @throws SecurityException
	 */
	public static void uninstall() throws IOException
	{
		LogManager.getLogManager().readConfiguration();
	}
	
	
	/**
	 * Initialize this handler.
	 * 
	 * @param classname
	 *           Use the source class name provided by the LogRecord to get
	 *           the log4j Logger name. If <code>false</code>, the raw name
	 *           of the JUL logger is used.
	 * @param format
	 *           If <code>true</code>, use the attached formatter if
	 *           available. If <code>false</code> the formatter is ignored.
	 */
	public JULLoggingBridge(final boolean classname, final boolean format)
	{
		this.classname = classname;
		this.format = format;
	}
	
	
	/**
	 * No-op implementation.
	 */
	@Override
	public void close()
	{
		// Empty
	}
	
	
	/**
	 * No-op implementation.
	 */
	@Override
	public void flush()
	{
		// Empty
	}
	
	
	/**
	 * Return the Logger instance that will be used for logging.
	 */
	protected Logger getPublisher(final LogRecord record)
	{
		String name = null;
		if (classname)
		{
			name = record.getSourceClassName();
		}
		if (name == null)
		{
			name = record.getLoggerName();
		}
		if (name == null)
		{
			name = JULLoggingBridge.class.getName();
		}
		return Logger.getLogger(name);
	}
	
	
	/**
	 * Returns {@code Level.ALL} as this cares about discarding log
	 * statements.
	 */
	@Override
	public final synchronized Level getLevel()
	{
		return Level.ALL;
	}
	
	
	/**
	 * <p>
	 * Publish a LogRecord.
	 * </p>
	 * <p>
	 * The logging request was made initially to a Logger object, which initialized the LogRecord and forwarded it here.
	 * </p>
	 * <p>
	 * This handler ignores the Level attached to the LogRecord, as this cares about discarding log statements.
	 * </p>
	 * 
	 * @param record
	 *           Description of the log event. A null record is silently
	 *           ignored and is not published.
	 */
	@Override
	public void publish(final LogRecord record)
	{
		/*
		 * Silently ignore null records.
		 */
		if (record == null)
		{
			return;
		}
		/*
		 * Get our logger for publishing the record.
		 */
		final Logger publisher = getPublisher(record);
		// can be null!
		final Throwable thrown = record.getThrown();
		// can be null!
		String message = record.getMessage();
		if (format && (getFormatter() != null))
		{
			try
			{
				message = getFormatter().format(record);
			} catch (final Exception ex)
			{
				reportError(null, ex, ErrorManager.FORMAT_FAILURE);
				return;
			}
		}
		if (message == null)
		{
			return;
		}
		/*
		 * TRACE
		 */
		if (record.getLevel().intValue() <= Level.FINEST.intValue())
		{
			publisher.trace(message, thrown);
			return;
		}
		/*
		 * DEBUG
		 */
		if (record.getLevel() == Level.FINER)
		{
			publisher.debug(message, thrown);
			return;
		}
		if (record.getLevel() == Level.FINE)
		{
			publisher.debug(message, thrown);
			return;
		}
		/*
		 * INFO
		 */
		if (record.getLevel() == Level.CONFIG)
		{
			publisher.info(message, thrown);
			return;
		}
		if (record.getLevel() == Level.INFO)
		{
			publisher.info(message, thrown);
			return;
		}
		/*
		 * WARN
		 */
		if (record.getLevel() == Level.WARNING)
		{
			publisher.warn(message);
			return;
		}
		/*
		 * ERROR
		 */
		if (record.getLevel().intValue() >= Level.SEVERE.intValue())
		{
			publisher.error(message, thrown);
			return;
		}
		
		
		/*
		 * Still here? Fallback and out.
		 */
		publishFallback(record, publisher);
	}
	
	
	/**
	 * <p>
	 * Called by publish if no level value matched.
	 * </p>
	 * <p>
	 * This implementation uses log4j DEBUG level.
	 * </p>
	 * 
	 * @param record
	 *           to publish
	 * @param publisher
	 *           who logs out
	 */
	protected void publishFallback(final LogRecord record, final Logger publisher)
	{
		publisher.debug(record.getMessage(), record.getThrown());
	}
	
}
