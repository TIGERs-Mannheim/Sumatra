/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.main;

import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoreferee.engine.events.IGameEventDetector.EGameEventDetectorType;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.components.IEnumPanel;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainPanel extends JPanel implements IAutoRefMainPanel, ISumatraView
{
	
	/**  */
	private static final long									serialVersionUID	= 1511856775227796442L;
	
	private StartStopPanel										startStopPanel		= new StartStopPanel();
	private ActiveEnginePanel									activeEnginePanel	= new ActiveEnginePanel();
	private EnumCheckBoxPanel<EGameEventDetectorType>	gameEventPanel;
	
	
	/**
	 * 
	 */
	public AutoRefMainPanel()
	{
		gameEventPanel = new EnumCheckBoxPanel<>(EGameEventDetectorType.class, "Game Events", BoxLayout.PAGE_AXIS);
		
		setupGUI();
	}
	
	
	private void setupGUI()
	{
		setLayout(new MigLayout("center", "[320][]", "[][]"));
		add(startStopPanel, "grow x, top");
		add(gameEventPanel, "span 1 2, wrap");
		add(activeEnginePanel, "grow x, top");
	}
	
	
	@Override
	public IStartStopPanel getStartStopPanel()
	{
		return startStopPanel;
	}
	
	
	@Override
	public IActiveEnginePanel getEnginePanel()
	{
		return activeEnginePanel;
	}
	
	
	@Override
	public IEnumPanel<EGameEventDetectorType> getEventPanel()
	{
		return gameEventPanel;
	}
	
	
	public void setPanelsEnabled(final boolean enabled)
	{
		Arrays.asList(startStopPanel, activeEnginePanel, gameEventPanel).forEach(
				panel -> panel.setPanelEnabled(enabled));
	}
}
