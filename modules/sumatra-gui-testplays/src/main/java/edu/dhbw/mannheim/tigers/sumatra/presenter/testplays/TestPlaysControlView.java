/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.presenter.testplays;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class TestPlaysControlView extends ASumatraView
{
	
	/**
	 * Creates a new control view
	 */
	public TestPlaysControlView()
	{
		super(ESumatraViewType.TEST_PLAYS);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new TestPlaysControlPresenter();
	}
}
