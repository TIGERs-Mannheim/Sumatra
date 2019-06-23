/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.replay;

import java.awt.Component;

import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Replay control view presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPresenter extends ASumatraViewPresenter
{
	private final ReplayPanel	panel	= new ReplayPanel();
	
	
	@Override
	public Component getComponent()
	{
		return panel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	
	
	/**
	 * @return
	 */
	public ReplayPanel getReplayPanel()
	{
		return panel;
	}
	
}
