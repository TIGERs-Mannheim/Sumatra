/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.sim;

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
