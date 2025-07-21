/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view.basestation;

import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;


/**
 * A panel showing details about a {@link TigersBaseStation}
 */
public class BaseStationPanel extends JPanel
{
	private final BaseStationControlPanel controlPanel = new BaseStationControlPanel();
	private final BaseStationWifiStatsPanel wifiStatsPanel = new BaseStationWifiStatsPanel();
	private final BaseStationEthStatsPanel ethStatsPanel = new BaseStationEthStatsPanel();
	private final BaseStationNtpStatsPanel ntpStatsPanel = new BaseStationNtpStatsPanel();


	public BaseStationPanel()
	{
		setName("Base Station");
		setLayout(new MigLayout("wrap 1"));

		add(wifiStatsPanel);
		add(controlPanel);
		add(ethStatsPanel);
		add(ntpStatsPanel);
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


	/**
	 * @return the ntpStatsPanel
	 */
	public BaseStationNtpStatsPanel getNtpStatsPanel()
	{
		return ntpStatsPanel;
	}
}
