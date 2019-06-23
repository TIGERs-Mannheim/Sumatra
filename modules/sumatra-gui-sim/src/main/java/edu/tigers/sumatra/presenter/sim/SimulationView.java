/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.sim;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationView extends ASumatraView
{
	/**
	 */
	public SimulationView()
	{
		super(ESumatraViewType.SIMULATION);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new SimulationPresenter();
	}
}
