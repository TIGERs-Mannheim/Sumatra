/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 3, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.basestation;

import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bootloader.FirmwareUpdatePanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseStationPanel extends JPanel
{
	/**  */
	private static final long						serialVersionUID		= -2888008314485655476L;
	
	private final String								name;
	private final BaseStationControlPanel		controlPanel			= new BaseStationControlPanel();
	private final BaseStationWifiStatsPanel	wifiStatsPanel			= new BaseStationWifiStatsPanel();
	private final BaseStationEthStatsPanel		ethStatsPanel			= new BaseStationEthStatsPanel();
	private final BaseStationNtpStatsPanel		ntpStatsPanel			= new BaseStationNtpStatsPanel();
	private final FirmwareUpdatePanel			firmwareUpdatePanel	= new FirmwareUpdatePanel();
	
	
	/**
	 * @param name
	 */
	public BaseStationPanel(final String name)
	{
		this.name = name;
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
	 * @return the name
	 */
	@Override
	public final String getName()
	{
		return name;
	}
	
	
	/**
	 * @return the ntpStatsPanel
	 */
	public BaseStationNtpStatsPanel getNtpStatsPanel()
	{
		return ntpStatsPanel;
	}
}
