/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyAutomaticBestFit;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;


/**
 * Network statistics panel.
 * 
 * @author AndreR
 * 
 */
public class NetworkPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID	= 5964199908873519766L;
	
	private final JTextField						packets[];
	private final JTextField						payload[];
	private final JTextField						raw[];
	private final JProgressBar						overhead[];
	private final JProgressBar						load[];
	private final JTextField						numPings				= new JTextField();
	private final JTextField						delay					= new JTextField();
	
	private final Chart2D							chart					= new Chart2D();
	private final ITrace2D							rxTrace				= new Trace2DLtd(200);
	private final ITrace2D							txTrace				= new Trace2DLtd(200);
	private final ITrace2D							delayTrace			= new Trace2DLtd(2000);
	
	private long										timeOffset			= 0;
	
	private final List<INetworkPanelObserver>	observers			= new ArrayList<INetworkPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public NetworkPanel()
	{
		setLayout(new MigLayout("", "[]10[fill]"));
		
		packets = new JTextField[4];
		payload = new JTextField[4];
		raw = new JTextField[4];
		overhead = new JProgressBar[4];
		load = new JProgressBar[4];
		
		final JPanel barPanel = new JPanel(new MigLayout("wrap 5",
				"[100]10[150,fill]10[150,fill]10[150,fill]10[150,fill]", "[20][20][20][20]10[20,fill][20,fill][]"));
		
		final JPanel pingPanel = new JPanel(new MigLayout("wrap 2", "[50]10[50,fill]"));
		pingPanel.setBorder(BorderFactory.createTitledBorder("Ping"));
		
		for (int i = 0; i < 4; i++)
		{
			packets[i] = new JTextField();
			payload[i] = new JTextField();
			raw[i] = new JTextField();
			overhead[i] = new JProgressBar(0, 1000);
			overhead[i].setStringPainted(true);
			load[i] = new JProgressBar(0, 1000);
			load[i].setStringPainted(true);
		}
		
		barPanel.add(new JLabel("TX per second"), "skip");
		barPanel.add(new JLabel("RX per second"));
		barPanel.add(new JLabel("TX sum"));
		barPanel.add(new JLabel("RX sum"));
		
		barPanel.add(new JLabel("Packets:"));
		for (int i = 0; i < 4; i++)
		{
			barPanel.add(packets[i]);
		}
		
		barPanel.add(new JLabel("Payload [byte]:"));
		for (int i = 0; i < 4; i++)
		{
			barPanel.add(payload[i]);
		}
		
		barPanel.add(new JLabel("Raw [byte]:"));
		for (int i = 0; i < 4; i++)
		{
			barPanel.add(raw[i]);
		}
		
		barPanel.add(new JLabel("Overhead:"));
		for (int i = 0; i < 4; i++)
		{
			barPanel.add(overhead[i]);
		}
		
		barPanel.add(new JLabel("Load:"));
		for (int i = 0; i < 4; i++)
		{
			barPanel.add(load[i]);
		}
		
		rxTrace.setColor(Color.RED);
		rxTrace.setName("RX raw");
		txTrace.setColor(Color.BLUE);
		txTrace.setName("TX raw");
		delayTrace.setColor(Color.GREEN);
		delayTrace.setName("delay");
		
		final AAxis<AxisScalePolicyAutomaticBestFit> delayAxis = new AxisLinear<AxisScalePolicyAutomaticBestFit>();
		delayAxis.setRange(new Range(0, 100.0));
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 28800.0)));
		chart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		chart.getAxisX().setMajorTickSpacing(10);
		chart.getAxisX().setMinorTickSpacing(10);
		chart.addAxisYRight(delayAxis);
		delayAxis.setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 100.0)));
		chart.setBackground(getBackground());
		chart.setForeground(Color.BLACK);
		chart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		chart.getAxisY().setAxisTitle(new AxisTitle("bytes"));
		
		chart.addTrace(rxTrace);
		chart.addTrace(txTrace);
		chart.addTrace(delayTrace, chart.getAxisX(), delayAxis);
		
		final JButton start = new JButton("Start");
		final JButton stop = new JButton("Stop");
		
		start.addActionListener(new StartPing());
		stop.addActionListener(new StopPing());
		
		pingPanel.add(new JLabel("Pings/s:"));
		pingPanel.add(numPings);
		pingPanel.add(new JLabel("Delay:"));
		pingPanel.add(delay);
		pingPanel.add(start, "span, split 2");
		pingPanel.add(stop);
		
		add(barPanel);
		add(pingPanel, "wrap");
		add(chart, "span, grow, pushy");
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(INetworkPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(INetworkPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @param stat
	 */
	public void setTxStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				packets[0].setText(Integer.toString(stat.packets));
				payload[0].setText(Integer.toString(stat.payload));
				raw[0].setText(Integer.toString(stat.raw));
				overhead[0].setValue((int) (stat.getOverheadPercentage() * 1000));
				load[0].setValue((int) (stat.getLoadPercentage(1.0f) * 1000));
				
				overhead[0].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getOverheadPercentage() * 100));
				load[0].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getLoadPercentage(1.0f) * 100));
			}
		});
		
		txTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, stat.raw);
	}
	
	
	/**
	 * @param stat
	 */
	public void setRxStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				packets[1].setText(Integer.toString(stat.packets));
				payload[1].setText(Integer.toString(stat.payload));
				raw[1].setText(Integer.toString(stat.raw));
				overhead[1].setValue((int) (stat.getOverheadPercentage() * 1000));
				load[1].setValue((int) (stat.getLoadPercentage(1.0f) * 1000));
				
				overhead[1].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getOverheadPercentage() * 100));
				load[1].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getLoadPercentage(1.0f) * 100));
			}
		});
		
		rxTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, stat.raw);
	}
	
	
	/**
	 * @param stat
	 */
	public void setTxAllStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				packets[2].setText(Integer.toString(stat.packets));
				payload[2].setText(Integer.toString(stat.payload));
				raw[2].setText(Integer.toString(stat.raw));
				overhead[2].setValue((int) (stat.getOverheadPercentage() * 1000));
				load[2].setValue((int) (stat.getLoadPercentageWithLastReset() * 1000));
				
				overhead[2].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getOverheadPercentage() * 100));
				load[2].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getLoadPercentageWithLastReset() * 100));
			}
		});
	}
	
	
	/**
	 * @param stat
	 */
	public void setRxAllStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				packets[3].setText(Integer.toString(stat.packets));
				payload[3].setText(Integer.toString(stat.payload));
				raw[3].setText(Integer.toString(stat.raw));
				overhead[3].setValue((int) (stat.getOverheadPercentage() * 1000));
				load[3].setValue((int) (stat.getLoadPercentageWithLastReset() * 1000));
				
				overhead[3].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getOverheadPercentage() * 100));
				load[3].setString(String.format(Locale.ENGLISH, "%1.2f%%", stat.getLoadPercentageWithLastReset() * 100));
			}
		});
	}
	
	
	/**
	 * @param d
	 */
	public void setDelay(final float d)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				delayTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, d);
				
				delay.setText(String.format(Locale.ENGLISH, "%1.2f ms", d));
			}
		});
	}
	
	
	private void notifyStartPing(int numPings)
	{
		synchronized (observers)
		{
			for (final INetworkPanelObserver observer : observers)
			{
				observer.onStartPing(numPings);
			}
		}
	}
	
	
	private void notifyStopPing()
	{
		synchronized (observers)
		{
			for (final INetworkPanelObserver observer : observers)
			{
				observer.onStopPing();
			}
		}
	}
	
	private class StartPing implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int num = 0;
			
			try
			{
				num = Integer.valueOf(numPings.getText());
			} catch (final NumberFormatException err)
			{
				return;
			}
			
			notifyStartPing(num);
		}
	}
	
	private class StopPing implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyStopPing();
		}
	}
}
