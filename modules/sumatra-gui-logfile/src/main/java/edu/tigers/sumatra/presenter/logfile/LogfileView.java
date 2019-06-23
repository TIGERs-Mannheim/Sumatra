/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.logfile;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author AndreR
 */
public class LogfileView extends ASumatraView
{
	/**
	 * Create LogFileView.
	 */
	public LogfileView()
	{
		super(ESumatraViewType.LOGFILE);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new LogfilePresenter();
	}
}
