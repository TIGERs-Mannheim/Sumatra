/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
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
import javax.swing.JCheckBox;
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
public class NetworkSummaryPanel extends JPanel
{
	/**
	 */
	public interface INetworkSummaryPanelObserver
	{
		/**
		 * @param multicast
		 */
		void onEnableMulticastChanged(boolean multicast);
		
		
		/**
		 * @param time
		 */
		void onSleepTimeChanged(long time);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long									serialVersionUID	= 5964199908873519766L;
	
	private final JTextField									packets[];
	private final JTextField									payload[];
	private final JTextField									raw[];
	private final JProgressBar									overhead[];
	private final JProgressBar									load[];
	private final JCheckBox										multicast;
	private final JTextField									updateTime;
	
	private final Chart2D										chart					= new Chart2D();
	private final ITrace2D										rxTrace				= new Trace2DLtd(200);
	private final ITrace2D										txTrace				= new Trace2DLtd(200);
	
	private long													timeOffset			= 0;
	
	private final List<INetworkSummaryPanelObserver>	observers			= new ArrayList<INetworkSummaryPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public NetworkSummaryPanel()
	{
		setLayout(new MigLayout("wrap 2", "[150]10[grow]", "[][]"));
		
		packets = new JTextField[2];
		payload = new JTextField[2];
		raw = new JTextField[2];
		overhead = new JProgressBar[2];
		load = new JProgressBar[2];
		multicast = new JCheckBox();
		updateTime = new JTextField();
		
		final JButton saveSetup = new JButton("Save");
		saveSetup.addActionListener(new Save());
		
		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2", "[100]10[50,fill]"));
		infoPanel.add(new JLabel("Enable Multicast:"));
		infoPanel.add(multicast);
		infoPanel.add(new JLabel("Sleep time [ms]:"));
		infoPanel.add(updateTime);
		infoPanel.add(saveSetup, "span 2, growx, gapy 10");
		infoPanel.setBorder(BorderFactory.createTitledBorder("Setup"));
		
		final JPanel barPanel = new JPanel(new MigLayout("wrap 3", "[100]10[150,fill]10[150,fill]",
				"[20][20][20][20]10[20,fill][20,fill][]"));
		
		for (int i = 0; i < 2; i++)
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
		
		barPanel.add(new JLabel("Packets:"));
		for (int i = 0; i < 2; i++)
		{
			barPanel.add(packets[i]);
		}
		
		barPanel.add(new JLabel("Payload [byte]:"));
		for (int i = 0; i < 2; i++)
		{
			barPanel.add(payload[i]);
		}
		
		barPanel.add(new JLabel("Raw [byte]:"));
		for (int i = 0; i < 2; i++)
		{
			barPanel.add(raw[i]);
		}
		
		barPanel.add(new JLabel("Overhead:"));
		for (int i = 0; i < 2; i++)
		{
			barPanel.add(overhead[i]);
		}
		
		barPanel.add(new JLabel("Load:"));
		for (int i = 0; i < 2; i++)
		{
			barPanel.add(load[i]);
		}
		
		rxTrace.setColor(Color.RED);
		rxTrace.setName("RX raw");
		txTrace.setColor(Color.BLUE);
		txTrace.setName("TX raw");
		
		chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0.0, 28800.0)));
		chart.getAxisX().setRangePolicy(new RangePolicyHighestValues(10));
		chart.getAxisX().setMajorTickSpacing(10);
		chart.getAxisX().setMinorTickSpacing(10);
		chart.setBackground(getBackground());
		chart.setForeground(Color.BLACK);
		chart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
		chart.getAxisY().setAxisTitle(new AxisTitle("bytes"));
		
		chart.addTrace(rxTrace);
		chart.addTrace(txTrace);
		
		add(infoPanel, "spany 2, aligny top");
		add(barPanel, "grow");
		add(chart, "grow, pushy");
		
		timeOffset = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(INetworkSummaryPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(INetworkSummaryPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @param time
	 */
	public void setSleepTime(final long time)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				updateTime.setText(Long.toString(time));
			}
		});
	}
	
	
	/**
	 * @param enable
	 */
	public void setEnableMulticast(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				multicast.setSelected(enable);
			}
		});
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
	
	
	private void notifyEnableMulticastChanged(boolean enable)
	{
		synchronized (observers)
		{
			for (final INetworkSummaryPanelObserver observer : observers)
			{
				observer.onEnableMulticastChanged(enable);
			}
		}
	}
	
	
	private void notifySleepTimeChanged(int time)
	{
		synchronized (observers)
		{
			for (final INetworkSummaryPanelObserver observer : observers)
			{
				observer.onSleepTimeChanged(time);
			}
		}
	}
	
	private class Save implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int time = 0;
			
			notifyEnableMulticastChanged(multicast.isSelected());
			
			try
			{
				time = Integer.parseInt(updateTime.getText());
			} catch (final NumberFormatException ex)
			{
				return;
			}
			
			notifySleepTimeChanged(time);
		}
	}
}
