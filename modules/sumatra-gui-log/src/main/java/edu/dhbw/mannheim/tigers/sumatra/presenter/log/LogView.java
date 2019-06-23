/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.log;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LogView extends ASumatraView
{
	private final boolean	addAppender;
	
	
	/**
	 * @param addAppender
	 */
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
