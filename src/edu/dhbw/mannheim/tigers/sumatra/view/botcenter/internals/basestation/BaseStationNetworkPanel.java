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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtdReplacing;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;

import java.awt.Color;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats.EthStats;
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
	
	private JButton										connect				= null;
	private NetStateIndicator							netState				= null;
	private JLabel											txStat;
	private JLabel											rxStat;
	private JTextField									updateRate;
	
	private final Chart2D								barChart				= new Chart2D();
	private final ITrace2D[]							barTraces			= new Trace2DLtdReplacing[3];
	
	private final List<IBaseStationNetworkPanel>	observers			= new ArrayList<IBaseStationNetworkPanel>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationNetworkPanel()
	{
		setLayout(new MigLayout("wrap 2", "[fill]10[200, fill]"));
		
		connect = new JButton("Connect");
		connect.addActionListener(new Connect());
		
		netState = new NetStateIndicator();
		
		txStat = new JLabel("-");
		rxStat = new JLabel("-");
		updateRate = new JTextField("Rate: 0Hz");
		
		for (int i = 0; i < 3; i++)
		{
			barTraces[i] = new Trace2DLtdReplacing(1);
		}
		
		barChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 100.0)));
		barChart.getAxisX().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 4.0)));
		barChart.getAxisY().setMajorTickSpacing(10);
		barChart.getAxisY().setMinorTickSpacing(2.5);
		barChart.getAxisY().setPaintGrid(true);
		barChart.getAxisY().setAxisTitle(new AxisTitle("%"));
		barChart.getAxisX().setPaintScale(false);
		barChart.getAxisX().setAxisTitle(new AxisTitle(""));
		barChart.setBackground(getBackground());
		barChart.setGridColor(Color.LIGHT_GRAY);
		
		barTraces[0].setColor(Color.RED);
		barTraces[0].setName("rxLvl");
		barTraces[0].setTracePainter(new TracePainterVerticalBar(4, barChart));
		barTraces[1].setColor(Color.GREEN);
		barTraces[1].setName("txLvl");
		barTraces[1].setTracePainter(new TracePainterVerticalBar(4, barChart));
		barTraces[2].setColor(Color.BLUE);
		barTraces[2].setName("rxLoss");
		barTraces[2].setTracePainter(new TracePainterVerticalBar(4, barChart));
		
		barChart.addTrace(barTraces[0]);
		barChart.addTrace(barTraces[1]);
		barChart.addTrace(barTraces[2]);
		
		JPanel ethStatePanel = new JPanel(new MigLayout("wrap 2", "[30]10[80,fill]"));
		ethStatePanel.add(new JLabel("Frames / Bytes"), "skip 1");
		ethStatePanel.add(new JLabel("RX:"));
		ethStatePanel.add(rxStat);
		ethStatePanel.add(new JLabel("TX:"));
		ethStatePanel.add(txStat);
		
		add(netState);
		add(barChart, "spany 4, grow, h 200");
		add(connect);
		add(updateRate);
		add(ethStatePanel, "aligny bottom");
		
		barTraces[0].addPoint(1.0, 0.5);
		barTraces[1].addPoint(2.0, 100.0);
		barTraces[2].addPoint(3.0, 55.0);
		
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
	public void setStats(final EthStats stats)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				updateRate.setText(String.format("Rate: %dHz", stats.getUpdateRate()));
				rxStat.setText(String.format("%4d / %6d", stats.getRxFramesRecv(), stats.getRxBytesRecv()));
				txStat.setText(String.format("%4d / %6d", stats.getTxFramesSent(), stats.getTxBytesSent()));
				barTraces[0].addPoint(1.0, stats.getRxLevel());
				barTraces[1].addPoint(2.0, stats.getTxLevel());
				barTraces[2].addPoint(3.0, stats.getRxLoss());
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
