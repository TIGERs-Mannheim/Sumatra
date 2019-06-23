/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.10.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtdReplacing;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats.WifiStats;


/**
 * Show Base Station stats.
 * 
 * @author AndreR
 * 
 */
public class BaseStationStatsPanel extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= -6481472679284543383L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private WifiStatsPanel		wifiPanels[]		= new WifiStatsPanel[BaseStationStats.NUM_BOTS];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationStatsPanel()
	{
		setLayout(new MigLayout("wrap 3"));
		
		for (int i = 0; i < wifiPanels.length; i++)
		{
			wifiPanels[i] = new WifiStatsPanel();
			add(wifiPanels[i]);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param stats
	 */
	public void setStats(BaseStationStats stats)
	{
		for (int i = 0; i < BaseStationStats.NUM_BOTS; i++)
		{
			wifiPanels[i].setStats(stats.getWifiStats()[i]);
		}
	}
	
	private static class WifiStatsPanel extends JPanel
	{
		/**  */
		private static final long	serialVersionUID	= 9205204622066410833L;
		private JLabel					txStat;
		private JLabel					rxStat;
		private JLabel					id;
		
		private final Chart2D		barChart				= new Chart2D();
		private final ITrace2D[]	barTraces			= new Trace2DLtdReplacing[5];
		
		
		public WifiStatsPanel()
		{
			setLayout(new MigLayout("wrap 2", "[30]10[200,fill]"));
			
			txStat = new JLabel("-");
			rxStat = new JLabel("-");
			id = new JLabel("None");
			id.setFont(id.getFont().deriveFont(20.0f));
			
			for (int i = 0; i < 5; i++)
			{
				barTraces[i] = new Trace2DLtdReplacing(1);
			}
			
			barChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 100.0)));
			barChart.getAxisX().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 6.0)));
			barChart.getAxisY().setMajorTickSpacing(10);
			barChart.getAxisY().setMinorTickSpacing(2.5);
			barChart.getAxisY().setPaintGrid(true);
			barChart.getAxisY().setAxisTitle(new AxisTitle("%"));
			barChart.getAxisX().setPaintScale(false);
			barChart.getAxisX().setAxisTitle(new AxisTitle(""));
			barChart.setBackground(getBackground());
			barChart.setGridColor(Color.LIGHT_GRAY);
			
			barTraces[0].setColor(Color.GREEN);
			barTraces[0].setName("link");
			barTraces[0].setTracePainter(new TracePainterVerticalBar(4, barChart));
			barTraces[1].setColor(Color.CYAN);
			barTraces[1].setName("rxLvl");
			barTraces[1].setTracePainter(new TracePainterVerticalBar(4, barChart));
			barTraces[2].setColor(Color.BLUE);
			barTraces[2].setName("txLvl");
			barTraces[2].setTracePainter(new TracePainterVerticalBar(4, barChart));
			barTraces[3].setColor(Color.RED);
			barTraces[3].setName("rxLoss");
			barTraces[3].setTracePainter(new TracePainterVerticalBar(4, barChart));
			barTraces[4].setColor(Color.ORANGE);
			barTraces[4].setName("txLoss");
			barTraces[4].setTracePainter(new TracePainterVerticalBar(4, barChart));
			
			barChart.addTrace(barTraces[0]);
			barChart.addTrace(barTraces[1]);
			barChart.addTrace(barTraces[2]);
			barChart.addTrace(barTraces[3]);
			barChart.addTrace(barTraces[4]);
			
			JLabel idLabel = new JLabel("ID:");
			idLabel.setFont(id.getFont().deriveFont(20.0f));
			add(idLabel);
			add(id);
			
			add(barChart, "spanx 2, grow, h 200");
			
			add(new JLabel("Pkts / Raw / COBS"), "skip 1");
			add(new JLabel("RX:"));
			add(rxStat);
			add(new JLabel("TX:"));
			add(txStat);
			
			barTraces[0].addPoint(1.0, 0.5);
			barTraces[1].addPoint(2.0, 100.0);
			barTraces[2].addPoint(3.0, 55.0);
			barTraces[3].addPoint(4.0, 25.0);
			barTraces[4].addPoint(5.0, 85.0);
			
			setBorder(BorderFactory.createTitledBorder("Bot"));
		}
		
		
		public void setStats(final WifiStats stats)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					rxStat.setText(String.format("%4d / %6d / %6d", stats.getRxPacketsRecv(), stats.getRxBytesRecvRaw(),
							stats.getRxBytesRecvCOBS()));
					txStat.setText(String.format("%4d / %6d / %6d", stats.getTxPacketsSent(), stats.getTxBytesSentRaw(),
							stats.getTxBytesSentCOBS()));
					barTraces[0].addPoint(1.0, stats.getLinkQuality());
					barTraces[1].addPoint(2.0, stats.getRxLevel());
					barTraces[2].addPoint(3.0, stats.getTxLevel());
					barTraces[3].addPoint(4.0, stats.getRxLoss());
					barTraces[4].addPoint(5.0, stats.getTxLoss());
					id.setText(String.format("%2d", stats.getBotId().getNumber()));
					id.setForeground(stats.getBotId().getTeamColor().getColor());
				}
			});
		}
	}
}
