/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.log;

import edu.tigers.sumatra.gui.log.presenter.LogPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * A Sumatra view for log messages.
 */
public class LogView extends ASumatraView
{
	private final boolean addAppender;


	public LogView(final boolean addAppender)
	{
		super(ESumatraViewType.LOG);
		this.addAppender = addAppender;
	}


	@Override
	public ISumatraViewPresenter createPresenter()
	{
		return new LogPresenter(addAppender);
	}
}
