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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats.BotStats;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Show Base Station Wifi stats
 * 
 * @author AndreR
 */
public class BaseStationWifiStatsPanel extends JPanel
{
	
	/**  */
	private static final long						serialVersionUID	= -9048719448114258641L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Map<BotID, WifiStatsPanel>	wifiPanels			= new TreeMap<>(BotID.getComparator());
	private final WifiStatsHeaderPanel			headerPanel			= new WifiStatsHeaderPanel();
	private final JTextField						updateRate			= new JTextField();
	private final JLabel								updateRateText		= new JLabel("Update Rate:");
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** Default constructor. */
	public BaseStationWifiStatsPanel()
	{
		setLayout(new MigLayout("wrap 1"));
		
		setBorder(BorderFactory.createTitledBorder("Wireless"));
		
		updatePanels();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void updatePanels()
	{
		final JPanel thisPanel = this;
		
		SwingUtilities.invokeLater(() -> {
			removeAll();
			
			add(updateRateText, "split 2");
			add(updateRate, "width 100");
			
			if (!wifiPanels.isEmpty())
			{
				add(headerPanel);
				
				for (final JPanel panel : wifiPanels.values())
				{
					add(panel, "wrap, gapbottom 0");
				}
			} else
			{
				add(new JLabel("No bots connected."), "wrap");
			}
			
			add(Box.createGlue(), "push");
			
			SwingUtilities.updateComponentTreeUI(thisPanel);
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param stats
	 */
	public void setStats(final BaseStationWifiStats stats)
	{
		boolean panelsModified = false;
		
		EventQueue.invokeLater(() -> updateRate.setText(Integer.toString(stats.getUpdateRate()) + "Hz"));
		
		for (BotID id : new ArrayList<>(wifiPanels.keySet()))
		{
			if (!stats.isBotConnected(id))
			{
				wifiPanels.remove(id);
				panelsModified = true;
			}
		}
		
		for (int i = 0; i < BaseStationWifiStats.NUM_BOTS; i++)
		{
			BotStats botStats = stats.getBotStats()[i];
			if (botStats.getBotId().isUninitializedID())
			{
				continue;
			}
			
			if (!wifiPanels.containsKey(botStats.getBotId()))
			{
				wifiPanels.put(botStats.getBotId(), new WifiStatsPanel());
				panelsModified = true;
			}
			
			wifiPanels.get(botStats.getBotId()).setStats(botStats, stats.getUpdateRate());
		}
		
		if (panelsModified)
		{
			updatePanels();
		}
	}
	
	private static class WifiStatsHeaderPanel extends JPanel
	{
		/**  */
		private static final long serialVersionUID = -3542692054124461579L;
		
		
		public WifiStatsHeaderPanel()
		{
			setLayout(new MigLayout("wrap 6", "[20,fill]5[20]20[80,fill]5[80,fill]20[80,fill]5[80,fill]"));
			
			add(new JLabel("--- To Bot ---", SwingConstants.CENTER), "skip 2, span 2");
			add(new JLabel("--- From Bot ---", SwingConstants.CENTER), "span 2");
			add(new JLabel("Pkt. / Bytes", SwingConstants.CENTER), "skip 2");
			add(new JLabel("Loss", SwingConstants.CENTER));
			add(new JLabel("Pkt. / Bytes", SwingConstants.CENTER));
			add(new JLabel("Loss", SwingConstants.CENTER));
		}
	}
	
	private static class WifiStatsPanel extends JPanel
	{
		/**  */
		private static final long		serialVersionUID	= 9205204622066410833L;
		
		private final JProgressBar[]	nrfStats				= new JProgressBar[4];
		private final JProgressBar[]	queueStats			= new JProgressBar[2];
		private final JLabel[]			queueTraffic		= new JLabel[2];
		private final JProgressBar		linkQuality;
		
		private final JLabel				id;
		
		
		public WifiStatsPanel()
		{
			setLayout(new MigLayout("wrap 6", "[20,fill]5[20]20[80,fill]5[80,fill]20[80,fill]5[80,fill]"));
			
			linkQuality = new JProgressBar(SwingConstants.VERTICAL, 0, 1000);
			linkQuality.setStringPainted(true);
			linkQuality.setSize(20, 30);
			
			id = new JLabel("None", SwingConstants.CENTER);
			id.setFont(id.getFont().deriveFont(20.0f));
			
			add(id, "span 1 2");
			add(linkQuality, "span 1 2, h 40px");
			
			for (int i = 0; i < 4; i++)
			{
				nrfStats[i] = new JProgressBar(0, 1000);
				nrfStats[i].setStringPainted(true);
				add(nrfStats[i]);
			}
			
			for (int i = 0; i < 2; i++)
			{
				queueTraffic[i] = new JLabel("", SwingConstants.CENTER);
			}
			
			for (int i = 0; i < 2; i++)
			{
				queueStats[i] = new JProgressBar(0, 1000);
				queueStats[i].setStringPainted(true);
			}
			
			add(queueTraffic[0]);
			add(queueStats[0]);
			add(queueTraffic[1]);
			add(queueStats[1]);
		}
		
		
		/**
		 * Set bot statistics.
		 * 
		 * @param stats
		 * @param rate
		 */
		public void setStats(final BotStats stats, final int rate)
		{
			EventQueue.invokeLater(() -> doSetStats(stats, rate));
		}
		
		
		@SuppressWarnings("squid:S1192")
		private void doSetStats(final BotStats stats, final int rate)
		{
			nrfStats[0].setValue((int) (stats.nrf.getTxSaturation(rate) * 1000));
			nrfStats[1].setValue((int) (stats.nrf.getTxLoss() * 1000));
			nrfStats[2].setValue((int) (stats.nrf.getRxSaturation(rate) * 1000));
			nrfStats[3].setValue((int) (stats.nrf.getRxLoss() * 1000));
			
			nrfStats[0].setString(String.format("%5d / %5d", stats.nrf.txPackets, stats.nrf.txBytes));
			nrfStats[1].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.nrf.getTxLoss() * 100));
			nrfStats[2].setString(String.format("%5d / %5d", stats.nrf.rxPackets, stats.nrf.rxBytes));
			nrfStats[3].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.nrf.getRxLoss() * 100));
			
			queueStats[0].setValue((int) (stats.queue.getTxLoss() * 1000));
			queueStats[1].setValue((int) (stats.queue.getRxLoss() * 1000));
			
			queueStats[0].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.queue.getTxLoss() * 100));
			queueStats[1].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.queue.getRxLoss() * 100));
			
			queueTraffic[0].setText(String.format("%5d / %5d", stats.queue.txPackets, stats.queue.txBytes));
			queueTraffic[1].setText(String.format("%5d / %5d", stats.queue.rxPackets, stats.queue.rxBytes));
			
			linkQuality.setValue((int) (stats.nrf.getLinkQuality() * 1000));
			linkQuality.setString(String.format(Locale.ENGLISH, "%3.0f%%", stats.nrf.getLinkQuality() * 100));
			
			if (stats.getBotId().isBot())
			{
				id.setForeground(stats.getBotId().getTeamColor().getColor());
				id.setText(Integer.toString(stats.getBotId().getNumber()));
			} else
			{
				setVisible(false);
			}
		}
	}
}
