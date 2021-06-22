/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.log;

import edu.tigers.sumatra.log.ILogEventConsumer;
import edu.tigers.sumatra.log.SumatraAppender;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.view.log.IFilterPanelObserver;
import edu.tigers.sumatra.view.log.ISlidePanelObserver;
import edu.tigers.sumatra.view.log.LogPanel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The log presenter handles catching LogEvents from log4j and displays them.
 * Furthermore it can filter the output by custom user strings
 * One more word on the filtering capability:
 * - Enter a user filter -> text is filtered
 * - Reset user filter -> all messages reappear
 * - Select a new log level -> nothing filtered, but only events with a level equal or
 * higher to the log level will appear, all others are lost. They will not even appear
 * if you drop the log level to a lower value.
 * constant.
 */
public class LogPresenter implements ISumatraViewPresenter, IFilterPanelObserver,
		ISlidePanelObserver, ILogEventConsumer
{
	private static final String LOG_VIEW_APPENDER_NAME = "logView";
	private static final Color DEFAULT_COLOR_ALL = new Color(0, 0, 0);
	public static final Color DEFAULT_COLOR_FATAL = new Color(128, 0, 128);
	public static final Color DEFAULT_COLOR_ERROR = new Color(255, 0, 0);
	public static final Color DEFAULT_COLOR_WARN = new Color(0, 0, 255);
	private static final Color DEFAULT_COLOR_INFO = new Color(0, 128, 0);
	private static final Color DEFAULT_COLOR_DEBUG = new Color(96, 96, 96);
	private static final Color DEFAULT_COLOR_TRACE = new Color(0, 0, 0);

	private static final int DISPLAY_CAPACITY = 1000;
	private static final String LOG_LEVEL_KEY = LogPresenter.class.getName() + ".loglevel";
	private final LogEventBuffer liveEventBuffer = new LogEventBuffer();
	private LogEventBuffer eventBuffer = liveEventBuffer;
	private final Map<Level, AttributeSet> attributeSets = new HashMap<>();
	private final SumatraAppender appender;

	private LogPanel logPanel;
	private List<String> allowedStrings = new ArrayList<>();
	private Level logLevel;
	private int numFatals = 0;
	private int numErrors = 0;
	private int numWarnings = 0;

	private final UiThrottler logAppendThrottler = new UiThrottler(100);


	public LogPresenter(final boolean addAppender)
	{
		// set initial log level to info to avoid performance issues
		String strLevel = SumatraModel.getInstance().getUserProperty(LOG_LEVEL_KEY, "INFO");
		logLevel = Level.toLevel(strLevel);

		logPanel = new LogPanel(DISPLAY_CAPACITY, logLevel);

		logPanel.getFilterPanel().addObserver(this);
		logPanel.getSlidePanel().addObserver(this);

		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		appender = lc.getConfiguration().getAppender(LOG_VIEW_APPENDER_NAME);
		if (addAppender)
		{
			appender.addConsumer(this);
		}

		final StyleContext sc = StyleContext.getDefaultStyleContext();
		attributeSets.put(Level.ALL,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_ALL));
		attributeSets.put(Level.FATAL,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_FATAL));
		attributeSets.put(Level.ERROR,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_ERROR));
		attributeSets.put(Level.WARN,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_WARN));
		attributeSets.put(Level.INFO,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_INFO));
		attributeSets.put(Level.DEBUG,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_DEBUG));
		attributeSets.put(Level.TRACE,
				sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, DEFAULT_COLOR_TRACE));

		logAppendThrottler.start();
	}


	/**
	 * Remove all events
	 */
	public void clearEventStorage()
	{
		numFatals = 0;
		numErrors = 0;
		numWarnings = 0;
		eventBuffer.clear();
		SwingUtilities.invokeLater(this::reappendAllEvents);
	}


	@Override
	public void onNewLogEvent(final LogEvent logEvent)
	{
		liveEventBuffer.append(logEvent.toImmutable());

		final Level lvl = logEvent.getLevel();
		if (lvl.equals(Level.FATAL))
		{
			numFatals++;
		} else if (lvl.equals(Level.ERROR))
		{
			numErrors++;
		} else if (lvl.equals(Level.WARN))
		{
			numWarnings++;
		}

		logAppendThrottler.execute(this::appendNewEvents);
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

		SwingUtilities.invokeLater(this::reappendAllEvents);
	}


	@Override
	public void onNewFilter(final List<String> allowed)
	{
		allowedStrings = allowed;

		SwingUtilities.invokeLater(this::reappendAllEvents);
	}


	private void appendNewEvents()
	{
		updateCounters();

		eventBuffer.getNewEvents().stream()
				.filter(this::checkFilters)
				.forEach(this::appendLogEvent);
	}


	private void reappendAllEvents()
	{
		updateCounters();
		eventBuffer.reset();
		logPanel.getTextPane().clear();
		appendNewEvents();
	}


	private void appendLogEvent(final LogEvent event)
	{
		logPanel.getTextPane().append(format(event), attributeSets.get(event.getLevel()));
	}


	private String format(final LogEvent event)
	{
		return new String(appender.getLayout().toByteArray(event));
	}


	private void updateCounters()
	{
		logPanel.getFilterPanel().setNumFatals(numFatals);
		logPanel.getFilterPanel().setNumErrors(numErrors);
		logPanel.getFilterPanel().setNumWarnings(numWarnings);
	}


	private boolean checkForLogLevel(final LogEvent event)
	{
		return event.getLevel().equals(logLevel) || event.getLevel().isMoreSpecificThan(logLevel);
	}


	/**
	 * Checks if the event contains one of the user filter strings.
	 *
	 * @param event Event to check.
	 * @return true if the event contains a user filter string or if there are no filter strings.
	 */
	private boolean checkStringFilter(final LogEvent event)
	{
		if (allowedStrings.isEmpty())
		{
			return true;
		}

		for (final String allowed : allowedStrings)
		{
			if (format(event).contains(allowed))
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
	public boolean checkFilters(final LogEvent event)
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
		if (freeze)
		{
			eventBuffer = liveEventBuffer.copy();
		} else
		{
			eventBuffer = liveEventBuffer;
			logAppendThrottler.execute(this::appendNewEvents);
		}
	}
}
