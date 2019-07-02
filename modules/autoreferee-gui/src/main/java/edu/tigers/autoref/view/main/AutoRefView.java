/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.view.main;

import edu.tigers.autoref.presenter.AutoRefPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


public class AutoRefView extends ASumatraView
{
	public AutoRefView()
	{
		super(ESumatraViewType.AUTOREFEREE);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new AutoRefPresenter();
	}
}
