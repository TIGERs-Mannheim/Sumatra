/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.log;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.view.log.IFilterPanelObserver;
import edu.tigers.sumatra.view.log.ISlidePanelObserver;
import edu.tigers.sumatra.view.log.LogPanel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * The log presenter handles catching LoggingEvents from log4j and displays them.
 * Furthermore it can filter the output by custom user strings
 * One more word on the filtering capability:
 * - Enter a user filter -> text is filtered
 * - Reset user filter -> all messages reappear
 * - Select a new log level -> nothing filtered, but only events with a level equal or
 * higher to the log level will appear, all others are lost. They will not even appear
 * if you drop the log level to a lower value.
 * constant.
 */
public class LogPresenter extends WriterAppender implements ISumatraViewPresenter, IFilterPanelObserver,
		ISlidePanelObserver
{
	private static final Color DEFAULT_COLOR_ALL = new Color(0, 0, 0);
	public static final Color DEFAULT_COLOR_FATAL = new Color(128, 0, 128);
	public static final Color DEFAULT_COLOR_ERROR = new Color(255, 0, 0);
	public static final Color DEFAULT_COLOR_WARN = new Color(0, 0, 255);
	private static final Color DEFAULT_COLOR_INFO = new Color(0, 128, 0);
	private static final Color DEFAULT_COLOR_DEBUG = new Color(96, 96, 96);
	private static final Color DEFAULT_COLOR_TRACE = new Color(0, 0, 0);

	private static final int DISPLAY_CAPACITY = 1000;
	private static final String LOG_LEVEL_KEY = LogPresenter.class.getName() + ".loglevel";
	private final LogEventBuffer eventBuffer = new LogEventBuffer();
	private final Map<Integer, AttributeSet> attributeSets = new HashMap<>();

	private final LogEventSync eventSync = new LogEventSync()
	{
	};

	private LogPanel logPanel;
	private List<String> allowedStrings = new ArrayList<>();
	private Level logLevel;
	private int numFatals = 0;
	private int numErrors = 0;
	private int numWarnings = 0;
	private boolean freeze = false;

	private final UiThrottler logAppendThrottler = new UiThrottler(100);


	public LogPresenter(final boolean addAppender)
	{
		// set initial log level to info to avoid performance issues
		String strLevel = SumatraModel.getInstance().getUserProperty(LOG_LEVEL_KEY, "INFO");
		logLevel = Level.toLevel(strLevel);

		logPanel = new LogPanel(DISPLAY_CAPACITY, logLevel);

		logPanel.getFilterPanel().addObserver(this);
		logPanel.getSlidePanel().addObserver(this);

		// set internal output layout -> see log4j.properties
		setLayout(new PatternLayout("%d{ABSOLUTE} %-5p [%t|%c{1}] %m%n"));
		if (addAppender)
		{
			Logger.getRootLogger().addAppender(this);
		}

		final StyleContext sc = StyleContext.getDefaultStyleContext();
		attributeSets.put(Priority.ALL_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_ALL));
		attributeSets.put(Priority.FATAL_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_FATAL));
		attributeSets.put(Priority.ERROR_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_ERROR));
		attributeSets.put(Priority.WARN_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_WARN));
		attributeSets.put(Priority.INFO_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_INFO));
		attributeSets.put(Priority.DEBUG_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_DEBUG));
		attributeSets.put(Level.TRACE_INT,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_TRACE));

		logAppendThrottler.start();
	}


	/**
	 * Remove all events
	 */
	public void clearEventStorage()
	{
		logPanel.getTextPane().clear();
		numFatals = 0;
		numErrors = 0;
		numWarnings = 0;
		synchronized (eventSync)
		{
			eventBuffer.clear();
		}
		onLevelChanged(logLevel);
	}


	@Override
	public void append(final LoggingEvent logEvent)
	{
		synchronized (eventSync)
		{
			eventBuffer.append(logEvent);
		}

		switch (logEvent.getLevel().toInt())
		{
			case Priority.FATAL_INT:
				numFatals++;
				break;
			case Priority.ERROR_INT:
				numErrors++;
				break;
			case Priority.WARN_INT:
				numWarnings++;
				break;
			default:
		}

		if (!freeze)
		{
			logAppendThrottler.execute(this::reappendAllEvents);
		}
	}


	@Override
	public void onLevelChanged(final Level level)
	{
		if (level.equals(logLevel))
		{
			return;
		}

		logLevel = level;
		SumatraModel.getInstance().setUserProperty(LOG_LEVEL_KEY, level.toString());
		logPanel.getTextPane().clear();

		SwingUtilities.invokeLater(this::reappendAllEvents);
	}


	@Override
	public void onNewFilter(final List<String> allowed)
	{
		allowedStrings = allowed;

		SwingUtilities.invokeLater(this::reappendAllEvents);
	}


	private void reappendAllEvents()
	{
		logPanel.getTextPane().clear();
		updateCounters();

		synchronized (eventSync)
		{
			final List<LoggingEvent> events = StreamSupport.stream(eventBuffer.spliterator(), false)
					.filter(this::checkFilters)
					.collect(Collectors.toList());
			events.forEach(this::appendLoggingEvent);
		}
	}


	private void appendLoggingEvent(final LoggingEvent event)
	{
		logPanel.getTextPane().append(layout.format(event), attributeSets.get(event.getLevel().toInt()));
	}


	private void updateCounters()
	{
		logPanel.getFilterPanel().setNumFatals(numFatals);
		logPanel.getFilterPanel().setNumErrors(numErrors);
		logPanel.getFilterPanel().setNumWarnings(numWarnings);
	}


	private boolean checkForLogLevel(final LoggingEvent event)
	{
		return event.getLevel().isGreaterOrEqual(logLevel);
	}


	/**
	 * Checks if the event contains one of the user filter strings.
	 *
	 * @param event Event to check.
	 * @return true if the event contains a user filter string or if there are no filter strings.
	 */
	private boolean checkStringFilter(final LoggingEvent event)
	{
		if (allowedStrings.isEmpty())
		{
			return true;
		}

		for (final String allowed : allowedStrings)
		{
			if (layout.format(event).contains(allowed))
			{
				return true;
			}
		}

		return false;
	}


	/**
	 * @param event
	 * @return
	 */
	public boolean checkFilters(final LoggingEvent event)
	{
		return !(!checkStringFilter(event) || !checkForLogLevel(event));
	}


	@Override
	public Component getComponent()
	{
		return logPanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return logPanel;
	}


	@Override
	public void onFreeze(final boolean freeze)
	{
		this.freeze = freeze;
		eventBuffer.setFreeze(freeze);
		if (!freeze)
		{
			SwingUtilities.invokeLater(this::reappendAllEvents);
		}
	}

	private interface LogEventSync
	{
	}
}
