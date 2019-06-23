/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.views.impl;

import edu.dhbw.mannheim.tigers.sumatra.presenter.sim.SimulationPresenter;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ESumatraViewType;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


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
