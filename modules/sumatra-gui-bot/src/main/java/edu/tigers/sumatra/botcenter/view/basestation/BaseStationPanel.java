/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view.basestation;

import javax.swing.JPanel;

import edu.tigers.sumatra.botcenter.view.bootloader.FirmwareUpdatePanel;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import net.miginfocom.swing.MigLayout;


/**
 * A panel showing details about a {@link TigersBaseStation}
 */
public class BaseStationPanel extends JPanel
{
	private static final long serialVersionUID = -2888008314485655476L;

	private final BaseStationControlPanel controlPanel = new BaseStationControlPanel();
	private final BaseStationWifiStatsPanel wifiStatsPanel = new BaseStationWifiStatsPanel();
	private final BaseStationEthStatsPanel ethStatsPanel = new BaseStationEthStatsPanel();
	private final BaseStationNtpStatsPanel ntpStatsPanel = new BaseStationNtpStatsPanel();
	private final FirmwareUpdatePanel firmwareUpdatePanel = new FirmwareUpdatePanel();


	public BaseStationPanel()
	{
		setName("Base Station");
		setLayout(new MigLayout("wrap 1"));

		add(wifiStatsPanel);
		add(controlPanel);
		add(ethStatsPanel);
		add(ntpStatsPanel);
		add(firmwareUpdatePanel);
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
	 * @return the firmwareUpdatePanel
	 */
	public final FirmwareUpdatePanel getFirmwareUpdatePanel()
	{
		return firmwareUpdatePanel;
	}


	/**
	 * @return the ntpStatsPanel
	 */
	public BaseStationNtpStatsPanel getNtpStatsPanel()
	{
		return ntpStatsPanel;
	}
}
