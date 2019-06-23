/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.logfile;

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
