/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.PingStats;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcBotPingPanel extends JPanel
{
	/**  */
	private static final long							serialVersionUID	= 4531411392390831333L;
	
	private final JTextField							minDelay				= new JTextField();
	private final JTextField							avgDelay				= new JTextField();
	private final JTextField							maxDelay				= new JTextField();
	private final JTextField							lostPings			= new JTextField();
	private final JTextField							numPings				= new JTextField("10");
	private final JTextField							pingPayload			= new JTextField("0");
	private final JButton								startStopPing		= new JButton("Start");
	
	private final List<IBcBotPingPanelObserver>	observers			= new CopyOnWriteArrayList<IBcBotPingPanelObserver>();
	
	
	/**
	 * 
	 */
	public BcBotPingPanel()
	{
		setLayout(new MigLayout("wrap 2", "[50][100,fill]"));
		
		startStopPing.addActionListener(new StartStopPing());
		
		add(new JLabel("Num Pings:"));
		add(numPings);
		add(new JLabel("Payload:"));
		add(pingPayload);
		add(startStopPing, "span 2");
		add(new JLabel("Min Delay:"));
		add(minDelay);
		add(new JLabel("Avg Delay:"));
		add(avgDelay);
		add(new JLabel("Max Delay:"));
		add(maxDelay);
		add(new JLabel("Lost Pings:"));
		add(lostPings);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBcBotPingPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBcBotPingPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * @param stats
	 */
	public void setPingStats(final PingStats stats)
	{
		minDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.minDelay));
		avgDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.avgDelay));
		maxDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.maxDelay));
		lostPings.setText(Integer.toString(stats.lostPings));
	}
	
	
	private void notifyStartPing(final int numPings, final int payloadSize)
	{
		for (IBcBotPingPanelObserver observer : observers)
		{
			observer.onStartPing(numPings, payloadSize);
		}
	}
	
	
	private void notifyStopPing()
	{
		for (IBcBotPingPanelObserver observer : observers)
		{
			observer.onStopPing();
		}
	}
	
	private class StartStopPing implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			if (startStopPing.getText().equals("Start"))
			{
				int num = 0;
				int payload = 0;
				
				try
				{
					num = Integer.valueOf(numPings.getText());
					payload = Integer.valueOf(pingPayload.getText());
				} catch (final NumberFormatException err)
				{
					return;
				}
				
				notifyStartPing(num, payload);
				
				startStopPing.setText("Stop");
			} else
			{
				notifyStopPing();
				
				startStopPing.setText("Start");
			}
		}
	}
	
	
	/**
	 */
	public static interface IBcBotPingPanelObserver
	{
		/**
		 * @param numPings
		 * @param payloadSize
		 */
		void onStartPing(final int numPings, final int payloadSize);
		
		
		/**
		 */
		void onStopPing();
	}
}
