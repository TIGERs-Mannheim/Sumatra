/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.presenter.log.LogPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayLogController implements IReplayController
{
	private static final long LOG_BUFFER_BEFORE = 500;
	private static final long LOG_BUFFER_AFTER = 500;
	private List<BerkeleyLogEvent> logEventBuffer = null;
	private List<LogEvent> lastLogEventsPast = new LinkedList<>();
	private List<LogEvent> lastLogEventsFuture = new LinkedList<>();

	private LogPresenter logPresenter;


	public ReplayLogController(List<ASumatraView> sumatraViews)
	{
		for (ASumatraView view : sumatraViews)
		{
			if (view.getType() == ESumatraViewType.LOG)
			{
				logPresenter = (LogPresenter) view.getPresenter();
			}
		}
	}


	@Override
	public void update(final BerkeleyDb db, final WorldFrameWrapper wfw)
	{
		initLogEventBuffer(db);

		if (logEventBuffer.isEmpty())
		{
			return;
		}

		long unixTimestamp = wfw.getUnixTimestamp();
		List<LogEvent> logEventsPast = new LinkedList<>();
		List<LogEvent> logEventsFuture = new LinkedList<>();
		updateLogEvents(unixTimestamp, logEventsPast, logEventsFuture);

		boolean reprint = isReprint(lastLogEventsPast, logEventsPast) || isReprint(lastLogEventsFuture, logEventsFuture);
		if (reprint)
		{
			logPresenter.clearEventStorage();
			for (LogEvent event : logEventsPast)
			{
				logPresenter.onNewLogEvent(event);
			}

			final Log4jLogEvent barrierEvent = Log4jLogEvent.newBuilder()
					.setLoggerName(this.getClass().getCanonicalName())
					.setLevel(Level.FATAL)
					.setMessage(new SimpleMessage(
							"------------------------------------------------ Past to Future barrier ------------------------------------------------"))
					.build();
			logPresenter.onNewLogEvent(barrierEvent);

			for (LogEvent event : logEventsFuture)
			{
				logPresenter.onNewLogEvent(event);
			}
		}
		lastLogEventsPast = logEventsPast;
		lastLogEventsFuture = logEventsFuture;
	}


	private void updateLogEvents(final long curTime, final List<LogEvent> logEventsPast,
			final List<LogEvent> logEventsFuture)
	{
		long timeStamp = curTime;
		for (BerkeleyLogEvent event : logEventBuffer)
		{
			if ((event.getTimestamp() >= (timeStamp - LOG_BUFFER_BEFORE))
					&& (event.getTimestamp() <= (timeStamp + LOG_BUFFER_AFTER))
					&& logPresenter.checkFilters(event.getLogEvent()))
			{
				if (event.getTimestamp() >= timeStamp)
				{
					logEventsFuture.add(event.getLogEvent());
				} else
				{
					logEventsPast.add(event.getLogEvent());
				}
			}
		}
	}


	private void initLogEventBuffer(final BerkeleyDb db)
	{
		if (logEventBuffer == null)
		{
			if (db != null)
			{
				logEventBuffer = db.getAll(BerkeleyLogEvent.class);
			} else
			{
				logEventBuffer = new ArrayList<>(0);
			}
		}
	}


	private boolean isReprint(final List<LogEvent> lastLogEvents, final List<LogEvent> logEvents)
	{
		if (lastLogEvents.size() != logEvents.size())
		{
			return true;
		}
		for (int i = 0; i < lastLogEvents.size(); i++)
		{
			String str1 = lastLogEvents.get(i).getMessage().getFormattedMessage();
			String str2 = logEvents.get(i).getMessage().getFormattedMessage();
			if (!str1.equals(str2))
			{
				return true;
			}
		}
		return false;
	}
}
