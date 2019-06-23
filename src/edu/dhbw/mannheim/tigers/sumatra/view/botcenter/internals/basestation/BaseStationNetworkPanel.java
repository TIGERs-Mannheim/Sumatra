/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetStateIndicator;


/**
 * Base station network control and statistics.
 * 
 * @author AndreR
 * 
 */
public class BaseStationNetworkPanel extends JPanel
{
	/** */
	public interface IBaseStationNetworkPanel
	{
		/**
		 * 
		 * @param connect
		 */
		void onConnectionChange(boolean connect);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID	= -3730726072203478050L;
	
	private JTextField									wifiPackets[]		= new JTextField[2];
	private JTextField									wifiBytes[]			= new JTextField[2];
	private JTextField									wifiRetransmit		= null;
	private JTextField									ethFrames[]			= new JTextField[2];
	private JTextField									ethBytes[]			= new JTextField[2];
	private JButton										connect				= null;
	private NetStateIndicator							netState				= null;
	
	private final List<IBaseStationNetworkPanel>	observers			= new ArrayList<IBaseStationNetworkPanel>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationNetworkPanel()
	{
		connect = new JButton("Connect");
		connect.addActionListener(new Connect());
		
		netState = new NetStateIndicator();
		
		wifiRetransmit = new JTextField();
		
		for (int i = 0; i < 2; i++)
		{
			wifiPackets[i] = new JTextField();
			wifiBytes[i] = new JTextField();
			ethFrames[i] = new JTextField();
			ethBytes[i] = new JTextField();
		}
		
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));
		
		JPanel wifiStatsPanel = new JPanel(new MigLayout("wrap 4", "[80]10[50,fill]10[50,fill]10[50,fill]"));
		wifiStatsPanel.add(new JLabel("Wifi"));
		wifiStatsPanel.add(new JLabel("Packets"));
		wifiStatsPanel.add(new JLabel("Bytes"));
		wifiStatsPanel.add(new JLabel("Retransmit"));
		wifiStatsPanel.add(new JLabel("Rx"));
		wifiStatsPanel.add(wifiPackets[0]);
		wifiStatsPanel.add(wifiBytes[0]);
		wifiStatsPanel.add(new JLabel(""));
		wifiStatsPanel.add(new JLabel("Tx"));
		wifiStatsPanel.add(wifiPackets[1]);
		wifiStatsPanel.add(wifiBytes[1]);
		wifiStatsPanel.add(wifiRetransmit);
		
		JPanel ethStatsPanel = new JPanel(new MigLayout("wrap 3", "[80]10[50,fill]10[50,fill]10[50,fill]"));
		ethStatsPanel.add(new JLabel("Eth"));
		ethStatsPanel.add(new JLabel("Frames"));
		ethStatsPanel.add(new JLabel("Bytes"));
		ethStatsPanel.add(new JLabel("Rx"));
		ethStatsPanel.add(ethFrames[0]);
		ethStatsPanel.add(ethBytes[0]);
		ethStatsPanel.add(new JLabel("Tx"));
		ethStatsPanel.add(ethFrames[1]);
		ethStatsPanel.add(ethBytes[1]);
		
		add(netState);
		add(connect);
		add(wifiStatsPanel);
		add(ethStatsPanel);
		
		setBorder(BorderFactory.createTitledBorder("Network"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IBaseStationNetworkPanel observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(IBaseStationNetworkPanel observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyConnectionChange(boolean connect)
	{
		synchronized (observers)
		{
			for (IBaseStationNetworkPanel observer : observers)
			{
				observer.onConnectionChange(connect);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		netState.setConnectionState(state);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				switch (state)
				{
					case OFFLINE:
					{
						connect.setText("Connect");
					}
						break;
					default:
					{
						connect.setText("Disconnect");
					}
						break;
				}
			}
		});
	}
	
	
	/**
	 * 
	 * @param stats
	 */
	public void setStats(final BaseStationStats stats)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				wifiPackets[0].setText(String.format("%d/%d", stats.getWifiStats().rxPacketsRecv,
						stats.getWifiStats().rxPacketsLost));
				wifiPackets[1].setText(String.format("%d/%d", stats.getWifiStats().txPacketsSent,
						stats.getWifiStats().txPacketsLost));
				
				wifiBytes[0].setText(String.format("%d/%d", stats.getWifiStats().rxBytesRecv,
						stats.getWifiStats().rxBytesLost));
				wifiBytes[1].setText(String.format("%d/%d", stats.getWifiStats().txBytesSent,
						stats.getWifiStats().txBytesLost));
				
				wifiRetransmit.setText("" + stats.getWifiStats().retransmissions);
				
				ethFrames[0].setText(String.format("%d/%d", stats.getEthStats().rxFramesRecv,
						stats.getEthStats().rxFramesLost));
				ethFrames[1].setText("" + stats.getEthStats().txFramesSent);
				
				ethBytes[0].setText(String.format("%d/%d", stats.getEthStats().rxBytesRecv, stats.getEthStats().rxBytesLost));
				ethBytes[1].setText("" + stats.getEthStats().txBytesSent);
			}
		});
	}
	
	private class Connect implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			switch (netState.getState())
			{
				case OFFLINE:
				{
					notifyConnectionChange(true);
				}
					break;
				default:
				{
					notifyConnectionChange(false);
				}
					break;
			}
		}
	}
}
