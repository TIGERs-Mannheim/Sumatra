/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation;

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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Base station configuration panel.
 * 
 * @author AndreR
 */
public class BaseStationPanel extends JPanel
{
	/** */
	public interface IBaseStationPanelObserver
	{
		/**
		 * Net config changed.
		 * 
		 * @param host
		 * @param dstPort
		 * @param localPort
		 */
		void onNetCfgChanged(String host, int dstPort, int localPort);
		
		
		/**
		 * @param numPings
		 * @param payload
		 */
		void onStartPing(int numPings, int payload);
		
		
		/** */
		void onStopPing();
		
		
		/**
		 * @param ch
		 * @param rate
		 * @param bots
		 * @param to
		 */
		void onCfgChanged(int ch, int rate, int bots, int to);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long								serialVersionUID	= -2888008314485655476L;
	private final JTextField								localPort			= new JTextField();
	private final JTextField								dstPort				= new JTextField();
	private final JTextField								host					= new JTextField();
	
	private final JTextField								numPings				= new JTextField();
	private final JTextField								pingSize				= new JTextField();
	private final JTextField								pingDelay			= new JTextField();
	private final JButton									startStopPing		= new JButton("Start");
	private boolean											pingIsActive		= false;
	
	private final JCheckBox									invertVision		= new JCheckBox("Invert");
	private final JTextField								visionRate			= new JTextField();
	private final JTextField								maxBots				= new JTextField();
	private final JTextField								channel				= new JTextField();
	private final JTextField								timeout				= new JTextField();
	
	private BaseStationControlPanel						networkPanel		= null;
	
	private final List<IBaseStationPanelObserver>	observers			= new ArrayList<IBaseStationPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationPanel()
	{
		setLayout(new MigLayout(""));
		
		networkPanel = new BaseStationControlPanel();
		
		JButton saveNetCfgButton = new JButton("Save");
		saveNetCfgButton.addActionListener(new SaveNetCfg());
		
		startStopPing.addActionListener(new StartStopPing());
		
		JButton saveVisionCfgButton = new JButton("Save");
		saveVisionCfgButton.addActionListener(new SaveVisionCfg());
		
		final JPanel netCfgPanel = new JPanel(new MigLayout("wrap 2", "[80]10[100,fill]"));
		final JPanel pingPanel = new JPanel(new MigLayout("wrap 2", "[80]10[100,fill]"));
		final JPanel visionPanel = new JPanel(new MigLayout("wrap 2", "[80]10[100,fill]"));
		
		netCfgPanel.add(new JLabel("IP:"));
		netCfgPanel.add(host);
		netCfgPanel.add(new JLabel("Port:"));
		netCfgPanel.add(dstPort);
		netCfgPanel.add(new JLabel("Local port:"));
		netCfgPanel.add(localPort);
		netCfgPanel.add(saveNetCfgButton, "span 2");
		
		pingPanel.add(new JLabel("Ping/s:"));
		pingPanel.add(numPings);
		pingPanel.add(new JLabel("Payload:"));
		pingPanel.add(pingSize);
		pingPanel.add(new JLabel("Delay:"));
		pingPanel.add(pingDelay);
		pingPanel.add(startStopPing, "span 2");
		
		visionPanel.add(new JLabel("Vision"));
		visionPanel.add(invertVision);
		visionPanel.add(new JLabel("Max. Rate:"));
		visionPanel.add(visionRate);
		visionPanel.add(new JLabel("Max. Bots:"));
		visionPanel.add(maxBots);
		visionPanel.add(new JLabel("Channel:"));
		visionPanel.add(channel);
		visionPanel.add(new JLabel("Timeout:"));
		visionPanel.add(timeout);
		visionPanel.add(saveVisionCfgButton, "span 2");
		
		netCfgPanel.setBorder(BorderFactory.createTitledBorder("Network"));
		pingPanel.setBorder(BorderFactory.createTitledBorder("Ping"));
		visionPanel.setBorder(BorderFactory.createTitledBorder("Vision & Bots"));
		
		add(netCfgPanel);
		add(pingPanel, "wrap");
		add(networkPanel, "span 2, wrap");
		add(visionPanel, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param delay
	 */
	public void setPingDelay(final float delay)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				pingDelay.setText(String.format(Locale.ENGLISH, "%.3fms", delay));
			}
		});
	}
	
	
	/**
	 * @param newChannel
	 * @param rate
	 * @param bots
	 * @param to
	 */
	public void setConfig(final int newChannel, final int rate, final int bots, final int to)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				visionRate.setText("" + rate);
				channel.setText("" + newChannel);
				maxBots.setText("" + bots);
				timeout.setText("" + to);
			}
		});
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBaseStationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBaseStationPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * Set network configuration.
	 * 
	 * @param host
	 * @param dstPort
	 * @param localPort
	 */
	public void setNetCfg(final String host, final int dstPort, final int localPort)
	{
		this.host.setText(host);
		this.dstPort.setText("" + dstPort);
		this.localPort.setText("" + localPort);
	}
	
	
	private void notifyNetCfgChanged(final String host, final int dstPort, final int localPort)
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onNetCfgChanged(host, dstPort, localPort);
			}
		}
	}
	
	
	private void notifyStartPing(final int numPings, final int payload)
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onStartPing(numPings, payload);
			}
		}
	}
	
	
	private void notifyStopPing()
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onStopPing();
			}
		}
	}
	
	
	private void notifyCfgChanged(final int ch, final int rate, final int bots, final int to)
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onCfgChanged(ch, rate, bots, to);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public BaseStationControlPanel getNetworkPanel()
	{
		return networkPanel;
	}
	
	/** */
	private class SaveNetCfg implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			int local;
			int dst;
			
			try
			{
				local = Integer.parseInt(localPort.getText());
				dst = Integer.parseInt(dstPort.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyNetCfgChanged(host.getText(), dst, local);
		}
	}
	
	private class SaveVisionCfg implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			int rate;
			int bots;
			int ch;
			int to;
			
			try
			{
				rate = Integer.parseInt(visionRate.getText());
				bots = Integer.parseInt(maxBots.getText());
				ch = Integer.parseInt(channel.getText());
				to = Integer.parseInt(timeout.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyCfgChanged(ch, rate, bots, to);
		}
	}
	
	private class StartStopPing implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			if (pingIsActive)
			{
				pingIsActive = false;
				notifyStopPing();
				
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						startStopPing.setText("Start");
					}
				});
			} else
			{
				int np = 0;
				int payload = 0;
				
				try
				{
					np = Integer.parseInt(numPings.getText());
					payload = Integer.parseInt(pingSize.getText());
				} catch (NumberFormatException e)
				{
					return;
				}
				
				notifyStartPing(np, payload);
				
				pingIsActive = true;
				
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						startStopPing.setText("Stop");
					}
				});
			}
		}
	}
}
