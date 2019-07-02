/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.presenter.log.LogPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayLogController implements IReplayController
{
	private static final Logger log = Logger.getLogger(ReplayLogController.class.getName());
	private static final long LOG_BUFFER_BEFORE = 500;
	private static final long LOG_BUFFER_AFTER = 500;
	private List<BerkeleyLogEvent> logEventBuffer = null;
	private List<LoggingEvent> lastLogEventsPast = new LinkedList<>();
	private List<LoggingEvent> lastLogEventsFuture = new LinkedList<>();

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
		List<LoggingEvent> logEventsPast = new LinkedList<>();
		List<LoggingEvent> logEventsFuture = new LinkedList<>();
		updateLogEvents(unixTimestamp, logEventsPast, logEventsFuture);

		boolean reprint = isReprint(lastLogEventsPast, logEventsPast) || isReprint(lastLogEventsFuture, logEventsFuture);
		if (reprint)
		{
			logPresenter.clearEventStorage();
			for (LoggingEvent event : logEventsPast)
			{
				logPresenter.append(event);
			}
			logPresenter.append(new LoggingEvent(this.getClass().getCanonicalName(), log, Level.FATAL,
					"------------------------------------------------ Past to Future barrier ------------------------------------------------",
					null));
			for (LoggingEvent event : logEventsFuture)
			{
				logPresenter.append(event);
			}
		}
		lastLogEventsPast = logEventsPast;
		lastLogEventsFuture = logEventsFuture;
	}


	private void updateLogEvents(final long curTime, final List<LoggingEvent> logEventsPast,
			final List<LoggingEvent> logEventsFuture)
	{
		long timeStamp = curTime;
		for (BerkeleyLogEvent event : logEventBuffer)
		{
			if ((event.getTimestamp() >= (timeStamp - LOG_BUFFER_BEFORE))
					&& (event.getTimestamp() <= (timeStamp + LOG_BUFFER_AFTER))
					&& logPresenter.checkFilters(event.getLoggingEvent()))
			{
				if (event.getTimestamp() >= timeStamp)
				{
					logEventsFuture.add(event.getLoggingEvent());
				} else
				{
					logEventsPast.add(event.getLoggingEvent());
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


	private boolean isReprint(final List<LoggingEvent> lastLogEvents, final List<LoggingEvent> logEvents)
	{
		if (lastLogEvents.size() != logEvents.size())
		{
			return true;
		}
		for (int i = 0; i < lastLogEvents.size(); i++)
		{
			String str1 = lastLogEvents.get(i).getRenderedMessage();
			String str2 = logEvents.get(i).getRenderedMessage();
			if (!str1.equals(str2))
			{
				return true;
			}
		}
		return false;
	}
}
