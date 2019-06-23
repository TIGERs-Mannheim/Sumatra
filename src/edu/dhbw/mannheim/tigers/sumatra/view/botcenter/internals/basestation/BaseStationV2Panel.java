/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 3, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseStationV2Panel extends JPanel
{
	/**  */
	private static final long						serialVersionUID	= -2888008314485655476L;
	
	private final BaseStationControlPanel		controlPanel		= new BaseStationControlPanel();
	private final BaseStationWifiStatsPanel	wifiStatsPanel		= new BaseStationWifiStatsPanel();
	private final BaseStationEthStatsPanel		ethStatsPanel		= new BaseStationEthStatsPanel();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationV2Panel()
	{
		setLayout(new MigLayout("wrap 1"));
		
		add(controlPanel);
		add(ethStatsPanel);
		add(wifiStatsPanel);
	}
	
	
	/**
	 * @return the baseStationStatsPanel
	 */
	public BaseStationWifiStatsPanel getWifiStatsPanel()
	{
		return wifiStatsPanel;
	}
	
	
	/**
	 * @return the ethStatsPanel
	 */
	public BaseStationEthStatsPanel getEthStatsPanel()
	{
		return ethStatsPanel;
	}
	
	
	/**
	 * @return
	 */
	public BaseStationControlPanel getControlPanel()
	{
		return controlPanel;
	}
}
