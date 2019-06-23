/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.log;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import edu.dhbw.mannheim.tigers.sumatra.view.log.LogPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.IFilterPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.ISlidePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.ITreePanelObserver;


/**
 * The log presenter handles catching LoggingEvents from log4j and displays them.
 * Furthermore it can filter the output by custom user strings or via class selection.
 * 
 * One more word on the filtering capability:
 * - Enter a user filter -> text is filtered
 * - Reset user filter -> all messages reappear
 * - Select a class filter -> text is filtered
 * - Select another class filter -> original text is filtered
 * - Select a new log level -> nothing filtered, but only events with a level equal or
 * higher to the log level will appear, all others are lost. They will not even appear
 * if you drop the log level to a lower value.
 * 
 * The number of events that can reappear is limited by the {@link #STORAGE_CAPACITY} ({@value #STORAGE_CAPACITY})
 * constant.
 * 
 * @author AndreR
 * 
 */
public class LogPresenter extends WriterAppender implements IFilterPanelObserver, ISlidePanelObserver,
		ITreePanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// colors
	public static final Color					DEFAULT_COLOR_ALL		= new Color(0, 0, 0);
	public static final Color					DEFAULT_COLOR_FATAL	= new Color(128, 0, 128);
	public static final Color					DEFAULT_COLOR_ERROR	= new Color(255, 0, 0);
	public static final Color					DEFAULT_COLOR_WARN	= new Color(0, 0, 255);
	public static final Color					DEFAULT_COLOR_INFO	= new Color(0, 128, 0);
	public static final Color					DEFAULT_COLOR_DEBUG	= new Color(96, 96, 96);
	public static final Color					DEFAULT_COLOR_TRACE	= new Color(0, 0, 0);
	public static final Color					DEFAULT_COLOR			= new Color(0, 0, 0);
	
	protected final Logger						log						= Logger.getLogger(getClass());
	
	private LogPanel								logPanel					= null;
	
	private static final int					STORAGE_CAPACITY		= 100;
	private static final int					DISPLAY_CAPACITY		= 1000;
	
	private final ArrayList<LoggingEvent>	eventStorage			= new ArrayList<LoggingEvent>(STORAGE_CAPACITY + 1);
	
	ArrayList<String>								allowedClasses			= new ArrayList<String>();
	ArrayList<String>								allowedStrings			= new ArrayList<String>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public LogPresenter()
	{
		logPanel = new LogPanel(DISPLAY_CAPACITY);
		
		logPanel.getFilterPanel().addObserver(this);
		logPanel.getSlidePanel().addObserver(this);
		logPanel.getTreePanel().addObserver(this);
		
		// set internal output layout
		setLayout(new PatternLayout("%d{HH:mm:ss,SSS} %-5p [%t|%C{1}] %m%n"));
		
		Logger.getRootLogger().addAppender(this);
		
		log.debug("Debug color test");
		log.info("Info color test");
		log.warn("Warn color test");
		log.error("Error color test");
		log.fatal("Fatal color test");
	}
	

	public LogPanel getLogPanel()
	{
		return logPanel;
	}
	

	public void append(LoggingEvent logEvent)
	{
		eventStorage.add(logEvent);
		if (eventStorage.size() > STORAGE_CAPACITY)
		{
			eventStorage.remove(0);
		}
		
		appendLoggingEvent(logEvent);
	}
	

	@Override
	public void onNewClassList(ArrayList<String> classes)
	{
		logPanel.getTextPane().clear();
		
		allowedClasses = classes;
		
		for (LoggingEvent event : eventStorage)
		{
			appendLoggingEvent(event);
		}
	}
	

	@Override
	public void onLevelChanged(Level level)
	{
		Logger.getRootLogger().setLevel(level);
	}
	

	@Override
	public void onNewFilter(ArrayList<String> allowed)
	{
		logPanel.getTextPane().clear();
		
		allowedStrings = allowed;
		
		for (LoggingEvent event : eventStorage)
		{
			appendLoggingEvent(event);
		}
	}
	

	private void appendLoggingEvent(LoggingEvent event)
	{
		Color color = DEFAULT_COLOR;
		
		if (!checkStringFilter(event) || !checkClassFilter(event))
		{
			return;
		}
		
		switch (event.getLevel().toInt())
		{
			case Level.ALL_INT:
				color = DEFAULT_COLOR_ALL;
				break;
			case Level.FATAL_INT:
				color = DEFAULT_COLOR_FATAL;
				break;
			case Level.ERROR_INT:
				color = DEFAULT_COLOR_ERROR;
				break;
			case Level.WARN_INT:
				color = DEFAULT_COLOR_WARN;
				break;
			case Level.INFO_INT:
				color = DEFAULT_COLOR_INFO;
				break;
			case Level.DEBUG_INT:
				color = DEFAULT_COLOR_DEBUG;
				break;
			case Level.TRACE_INT:
				color = DEFAULT_COLOR_TRACE;
				break;
		}
		
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		
		logPanel.getTextPane().append(layout.format(event), aset);
	}
	

	/**
	 * 
	 * Checks if the event is from a component which is on the white-list
	 * 
	 * @param event Event to check.
	 * @return true if the event is in the white-list or the white-list is empty.
	 */
	private boolean checkClassFilter(LoggingEvent event)
	{
		if (allowedClasses.isEmpty())
		{
			return true;
		}
		
		// get classname of loggingEvent
		String from = new PatternLayout("%C{1}").format(event);
		
		for (String allowed : allowedClasses)
		{
			if (from.startsWith(allowed))
			{
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * 
	 * Checks if the event contains one of the user filter strings.
	 * 
	 * @param event Event to check.
	 * @return true if the event contains a user filter string or if there are no filter strings.
	 */
	private boolean checkStringFilter(LoggingEvent event)
	{
		if (allowedStrings.isEmpty())
		{
			return true;
		}
		
		for (String allowed : allowedStrings)
		{
			if (layout.format(event).contains(allowed))
			{
				return true;
			}
		}
		
		return false;
	}
}
