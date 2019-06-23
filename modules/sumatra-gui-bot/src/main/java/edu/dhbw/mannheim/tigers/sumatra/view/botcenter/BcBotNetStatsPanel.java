/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotNetStatsPanel extends JPanel
{
	/**  */
	private static final long	serialVersionUID	= 7642097043545288964L;
	
	private final JTextField	rxData				= new JTextField();
	private final JTextField	txData				= new JTextField();
	private final JTextField	rxPackets			= new JTextField();
	private final JTextField	txPackets			= new JTextField();
	
	
	/**
	 * 
	 */
	public BcBotNetStatsPanel()
	{
		setLayout(new MigLayout("wrap 3", "[50][100,fill]10[100,fill]", "[]20[][]"));
		add(new JLabel("Stats"));
		add(new JLabel("Packets"));
		add(new JLabel("Bytes"));
		add(new JLabel("RX"));
		add(rxPackets);
		add(rxData);
		add(new JLabel("TX"));
		add(txPackets);
		add(txData);
	}
	
	
	/**
	 * @param stat
	 */
	public void setTxStat(final Statistics stat)
	{
		txPackets.setText(Integer.toString(stat.packets));
		txData.setText(Integer.toString(stat.payload));
	}
	
	
	/**
	 * @param stat
	 */
	public void setRxStat(final Statistics stat)
	{
		rxPackets.setText(Integer.toString(stat.packets));
		rxData.setText(Integer.toString(stat.payload));
	}
}
