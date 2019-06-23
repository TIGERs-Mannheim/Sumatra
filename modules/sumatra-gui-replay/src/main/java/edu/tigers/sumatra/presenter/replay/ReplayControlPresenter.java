/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.replay;

import java.awt.Component;

import edu.tigers.sumatra.view.replay.ReplayControlPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Replay control view presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPresenter extends ASumatraViewPresenter
{
	private final ReplayControlPanel panel = new ReplayControlPanel();
	
	
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
	public ReplayControlPanel getReplayPanel()
	{
		return panel;
	}
}
