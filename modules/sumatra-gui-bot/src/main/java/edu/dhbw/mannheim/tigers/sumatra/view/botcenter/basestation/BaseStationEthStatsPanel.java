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
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import net.miginfocom.swing.MigLayout;


/**
 * Show Base Station ETH stats
 * 
 * @author AndreR
 */
public class BaseStationEthStatsPanel extends JPanel
{
	/**  */
	private static final long	serialVersionUID	= 2509698325399806711L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final JTextField	rxTraffic;
	private final JTextField	txTraffic;
	private final JProgressBar	rxLoss;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationEthStatsPanel()
	{
		setLayout(new MigLayout("wrap 3", "[100,fill]10[100,fill]10[100,fill]115"));
		
		add(new JLabel("Outgoing", SwingConstants.CENTER));
		add(new JLabel("Incomming", SwingConstants.CENTER));
		add(new JLabel("Inc. Loss", SwingConstants.CENTER));
		
		txTraffic = new JTextField("-");
		txTraffic.setHorizontalAlignment(SwingConstants.CENTER);
		rxTraffic = new JTextField("-");
		rxTraffic.setHorizontalAlignment(SwingConstants.CENTER);
		rxLoss = new JProgressBar(0, 1000);
		rxLoss.setStringPainted(true);
		
		add(txTraffic);
		add(rxTraffic);
		add(rxLoss);
		
		setBorder(BorderFactory.createTitledBorder("Network Interface (on BS)"));
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
			rxTraffic.setText(String.format("%5d / %5d", stats.getRxFrames(), stats.getRxBytes()));
			txTraffic.setText(String.format("%5d / %5d", stats.getTxFrames(), stats.getTxBytes()));
			
			rxLoss.setValue((int) (stats.getRxLoss() * 1000));
			rxLoss.setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.getRxLoss() * 100));
		});
	}
}
