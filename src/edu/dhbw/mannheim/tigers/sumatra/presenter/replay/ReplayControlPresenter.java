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

import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Replay control view presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayControlPresenter implements ISumatraViewPresenter
{
	private final ReplayPanel	panel	= new ReplayPanel();
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
	}
	
	
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
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	/**
	 * @return
	 */
	public ReplayPanel getReplayPanel()
	{
		return panel;
	}
	
}
