/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.basestation;

import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import net.miginfocom.swing.MigLayout;


/**
 * Show Base Station NTP stats
 * 
 * @author AndreR
 */
public class BaseStationNtpStatsPanel extends JPanel
{
	/**  */
	private static final long	serialVersionUID	= -5383224794122650976L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final JCheckBox		primarySync;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationNtpStatsPanel()
	{
		setLayout(new MigLayout("wrap 2", "[100,fill]10[100,fill]"));
		
		primarySync = new JCheckBox();
		
		add(new JLabel("Synchronized", SwingConstants.LEFT));
		add(primarySync);
		
		setBorder(BorderFactory.createTitledBorder("NTP Status"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param stats
	 */
	public void setStats(final BaseStationEthStats stats)
	{
		EventQueue.invokeLater(() -> {
			primarySync.setSelected(stats.isNtpSync());
		});
	}
}
