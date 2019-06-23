/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetStateIndicator;


/**
 * Base station network control and statistics.
 * 
 * @author AndreR
 */
public class BaseStationControlPanel extends JPanel
{
	/** */
	public interface IBaseStationControlPanelObserver
	{
		/**
		 * @param connect
		 */
		void onConnectionChange(boolean connect);
		
		
		/**
		 * @param numPings
		 * @param payload
		 */
		void onStartPing(int numPings, int payload);
		
		
		/** */
		void onStopPing();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long										serialVersionUID	= -3730726072203478050L;
	
	private final JButton											connect				= new JButton("Connect");
	private final NetStateIndicator								netState				= new NetStateIndicator();
	
	private final JTextField										numPings				= new JTextField("1");
	private final JTextField										pingSize				= new JTextField("0");
	private final JTextField										pingDelay			= new JTextField();
	private final JButton											startStopPing		= new JButton("Start");
	private boolean													pingIsActive		= false;
	
	private final List<IBaseStationControlPanelObserver>	observers			= new CopyOnWriteArrayList<IBaseStationControlPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationControlPanel()
	{
		setLayout(new MigLayout("wrap 3", "[100,fill]135[80,fill]10[100,fill]"));
		
		connect.addActionListener(new Connect());
		startStopPing.addActionListener(new StartStopPing());
		
		add(netState);
		add(new JLabel("Ping/s:"));
		add(numPings);
		
		add(connect);
		add(new JLabel("Payload:"));
		add(pingSize);
		
		add(new JLabel("Delay:"), "skip 1");
		add(pingDelay);
		add(startStopPing, "skip 1, span 2");
		
		setBorder(BorderFactory.createTitledBorder("Control"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IBaseStationControlPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBaseStationControlPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyConnectionChange(final boolean connect)
	{
		for (IBaseStationControlPanelObserver observer : observers)
		{
			observer.onConnectionChange(connect);
		}
	}
	
	
	private void notifyStartPing(final int numPings, final int payload)
	{
		synchronized (observers)
		{
			for (IBaseStationControlPanelObserver observer : observers)
			{
				observer.onStartPing(numPings, payload);
			}
		}
	}
	
	
	private void notifyStopPing()
	{
		synchronized (observers)
		{
			for (IBaseStationControlPanelObserver observer : observers)
			{
				observer.onStopPing();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param delay
	 */
	public void setPingDelay(final float delay)
	{
		EventQueue.invokeLater(() -> {
			pingDelay.setText(String.format(Locale.ENGLISH, "%.3fms", delay));
		});
	}
	
	
	/**
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		netState.setConnectionState(state);
		
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
	
	private class Connect implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
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
