/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.log;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Plugin(name = "SumatraAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class SumatraAppender extends AbstractAppender
{
	private static final int BUFFER_SIZE = 1000;
	private final List<ILogEventConsumer> consumers = new CopyOnWriteArrayList<>();
	private final List<LogEvent> logEventBuffer = new LinkedList<>();


	@SuppressWarnings("squid:CallToDeprecatedMethod") // false positive
	private SumatraAppender(String name, Filter filter, final Layout<? extends Serializable> layout)
	{
		super(name, filter, layout, true, new Property[] {});
	}


	@PluginFactory
	@SuppressWarnings("unused") // used by log4j2
	public static SumatraAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Filter") Filter filter,
			@PluginElement("PatternLayout") Layout<? extends Serializable> layout)
	{
		return new SumatraAppender(name, filter, layout);
	}


	public void clear()
	{
		logEventBuffer.clear();
	}


	public synchronized void addConsumer(ILogEventConsumer consumer)
	{
		consumers.add(consumer);
		logEventBuffer.forEach(consumer::onNewLogEvent);
	}


	public void removeConsumer(ILogEventConsumer consumer)
	{
		consumers.remove(consumer);
	}


	@Override
	public synchronized void append(final LogEvent logEvent)
	{
		consumers.forEach(c -> c.onNewLogEvent(logEvent));
		if (logEventBuffer.size() >= BUFFER_SIZE)
		{
			logEventBuffer.remove(0);
		}
		logEventBuffer.add(logEvent.toImmutable());
	}
}
