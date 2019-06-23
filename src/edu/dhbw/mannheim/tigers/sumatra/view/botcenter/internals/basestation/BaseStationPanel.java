/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2013
 * Author(s): AndreR
 * 
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
 * 
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
		 * 
		 * @param numPings
		 * @param payload
		 */
		void onStartPing(int numPings, int payload);
		
		
		/** */
		void onStopPing();
		
		
		/**
		 * 
		 * @param invertPos
		 * @param rate
		 * @param tigersBlue
		 */
		void onVisionCfgChanged(boolean invertPos, int rate, boolean tigersBlue);
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
	private final JCheckBox									tigersBlue			= new JCheckBox("Tigers blue");
	private final JTextField								visionRate			= new JTextField();
	
	private BaseStationNetworkPanel						networkPanel		= null;
	
	private final List<IBaseStationPanelObserver>	observers			= new ArrayList<IBaseStationPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationPanel()
	{
		setLayout(new MigLayout(""));
		
		networkPanel = new BaseStationNetworkPanel();
		
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
		visionPanel.add(new JLabel("Team:"));
		visionPanel.add(tigersBlue);
		visionPanel.add(saveVisionCfgButton, "span 2");
		
		netCfgPanel.setBorder(BorderFactory.createTitledBorder("Network"));
		pingPanel.setBorder(BorderFactory.createTitledBorder("Ping"));
		visionPanel.setBorder(BorderFactory.createTitledBorder("Vision Configuration"));
		
		add(netCfgPanel);
		add(pingPanel, "wrap");
		add(networkPanel, "span 2, wrap");
		add(visionPanel, "span 2");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
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
	 * 
	 * @param invert
	 * @param rate
	 * @param blue
	 */
	public void setVisionConfig(final boolean invert, final int rate, final boolean blue)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				invertVision.setSelected(invert);
				visionRate.setText("" + rate);
				tigersBlue.setSelected(blue);
			}
		});
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IBaseStationPanelObserver observer)
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
	public void removeObserver(IBaseStationPanelObserver observer)
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
	public void setNetCfg(String host, int dstPort, int localPort)
	{
		this.host.setText(host);
		this.dstPort.setText("" + dstPort);
		this.localPort.setText("" + localPort);
	}
	
	
	private void notifyNetCfgChanged(String host, int dstPort, int localPort)
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onNetCfgChanged(host, dstPort, localPort);
			}
		}
	}
	
	
	private void notifyStartPing(int numPings, int payload)
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
	
	
	private void notifyVisionCfgChanged(boolean invert, int rate, boolean blue)
	{
		synchronized (observers)
		{
			for (IBaseStationPanelObserver observer : observers)
			{
				observer.onVisionCfgChanged(invert, rate, blue);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public BaseStationNetworkPanel getNetworkPanel()
	{
		return networkPanel;
	}
	
	/** */
	private class SaveNetCfg implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
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
		public void actionPerformed(ActionEvent arg0)
		{
			int rate;
			
			try
			{
				rate = Integer.parseInt(visionRate.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyVisionCfgChanged(invertVision.isSelected(), rate, tigersBlue.isSelected());
		}
	}
	
	private class StartStopPing implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
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
