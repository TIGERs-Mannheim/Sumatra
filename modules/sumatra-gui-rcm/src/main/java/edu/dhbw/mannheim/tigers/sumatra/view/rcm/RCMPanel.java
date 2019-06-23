/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2011
 * Author(s): Sven Frank
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.natives.OsDetector;
import edu.tigers.sumatra.rcm.IRCMObserver;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Main layout of RCM is displayed here.
 * 
 * @author Sven Frank
 */
public final class RCMPanel extends JPanel implements ISumatraView
{
	private static final Logger					log						= Logger.getLogger(RCMPanel.class
																								.getName());
	
	private static final long						serialVersionUID		= 7811945315539136091L;
	
	// --- GUI components ---
	private final JMenuBar							menuBar;
	private final JMenuItem							iStartStop;
	private final JMenuItem							iStartMessaging;
	
	private final JTabbedPane						controllerTabbedPane;
	
	// --- activity of startButton (start or stop pressed)
	private boolean									startButton				= true;
	private boolean									startMQTTButton		= true;
	private boolean									startMessagingButton	= true;
	
	private final List<IMessagingGUIObserver>	observersMessaging	= new CopyOnWriteArrayList<IMessagingGUIObserver>();
	private final List<IRCMObserver>				observers				= new CopyOnWriteArrayList<IRCMObserver>();
	
	
	/**
	  * 
	  */
	public RCMPanel()
	{
		setLayout(new BorderLayout());
		
		menuBar = new JMenuBar();
		JMenu mRcm = new JMenu("RCM");
		JMenu mController = new JMenu("Controllers");
		JMenu mMessaging = new JMenu("Messaging");
		menuBar.add(mRcm);
		menuBar.add(mController);
		menuBar.add(mMessaging);
		
		iStartStop = new JMenuItem("Start");
		JMenuItem iRefreshRcm = new JMenuItem("Refresh");
		JMenuItem iRefreshRcmKeep = new JMenuItem("Refresh+Keep connections");
		JMenuItem iReloadControllers = new JMenuItem("Reload");
		JMenuItem iReloadControllersKeep = new JMenuItem("Reload+Keep connections");
		JMenuItem iStartBroker = new JMenuItem("Start MQTT broker (Windows only)");
		iStartMessaging = new JMenuItem("Start messaging (BT+MQTT)");
		mRcm.add(iStartStop);
		mRcm.add(iRefreshRcm);
		mRcm.add(iRefreshRcmKeep);
		mController.add(iReloadControllers);
		mController.add(iReloadControllersKeep);
		mMessaging.add(iStartMessaging);
		mMessaging.add(iStartBroker);
		
		iStartStop.addActionListener(new StartStopAction());
		iRefreshRcm.addActionListener(new RestartAction());
		iRefreshRcmKeep.addActionListener(new RestartKeepAction());
		iReloadControllers.addActionListener(new UpdateControllerAction());
		iReloadControllersKeep.addActionListener(new UpdateControllerKeepAction());
		iStartMessaging.addActionListener(new StartStopMessagingAction());
		iStartBroker.addActionListener(new StartStopMQTTAction());
		
		// --- one Tab for each Controller - set below ---
		controllerTabbedPane = new JTabbedPane();
		controllerTabbedPane.setPreferredSize(new Dimension(1000, 2000));
		add(menuBar, BorderLayout.PAGE_START);
		add(controllerTabbedPane, BorderLayout.CENTER);
		
		stop();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRCMObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRCMObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStartStop(final boolean activeState)
	{
		for (IRCMObserver observer : observers)
		{
			observer.onStartStopButtonPressed(activeState);
		}
	}
	
	
	/**
	 * @param name
	 * @param controllerPanel
	 */
	public void addControllerPanel(final String name, final ControllerPanel controllerPanel)
	{
		controllerTabbedPane.addTab(name, controllerPanel);
		controllerPanel.addComponentListener(new ComponentListener()
		{
			
			@Override
			public void componentShown(final ComponentEvent e)
			{
			}
			
			
			@Override
			public void componentResized(final ComponentEvent e)
			{
				controllerTabbedPane.revalidate();
			}
			
			
			@Override
			public void componentMoved(final ComponentEvent e)
			{
			}
			
			
			@Override
			public void componentHidden(final ComponentEvent e)
			{
			}
		});
	}
	
	
	/**
	 */
	public void clearControllerPanels()
	{
		controllerTabbedPane.removeAll();
	}
	
	
	/**
	 */
	public final void start()
	{
		for (int i = 0; i < menuBar.getMenuCount(); i++)
		{
			menuBar.getMenu(i).setEnabled(true);
		}
	}
	
	
	/**
	 */
	public final void stop()
	{
		for (int i = 0; i < menuBar.getMenuCount(); i++)
		{
			menuBar.getMenu(i).setEnabled(false);
		}
	}
	
	
	/**
	 * 
	 */
	public void startRcm()
	{
		iStartStop.setText("Stop");
		startButton = false;
	}
	
	
	/**
	 * 
	 */
	public void stopRcm()
	{
		iStartStop.setText("Start");
		startButton = true;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	/**
	 * @param b
	 */
	private void notifyMessaging(final boolean b)
	{
		synchronized (observersMessaging)
		{
			if (b)
			{
				for (IMessagingGUIObserver o : observersMessaging)
				{
					o.onConnect();
				}
			} else
			{
				for (IMessagingGUIObserver o : observersMessaging)
				{
					o.onDisconnect();
				}
			}
		}
	}
	
	
	/**
	 * @param o
	 */
	public void addMessgingGUIObserver(final IMessagingGUIObserver o)
	{
		synchronized (observersMessaging)
		{
			observersMessaging.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeMessgingGUIObserver(final IMessagingGUIObserver o)
	{
		synchronized (observersMessaging)
		{
			observersMessaging.remove(o);
		}
	}
	
	/**
	 * Restart controllers (send stop and start)
	 * 
	 * @author Clemens
	 */
	private class RestartAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMObserver observer : observers)
			{
				observer.onReconnect(false);
			}
			iStartStop.setText("Stop");
			startButton = false;
		}
	}
	
	private class RestartKeepAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMObserver observer : observers)
			{
				observer.onReconnect(true);
			}
			iStartStop.setText("Stop");
			startButton = false;
		}
	}
	
	/**
	 * Action to set Start/Stop Button, run application and disable ControllerPanels
	 * 
	 * @author Clemens
	 */
	private class StartStopAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyStartStop(startButton);
		}
	}
	
	/**
	 * Starts Messaging System
	 * 
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private class StartStopMessagingAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			// --- start button shown ---
			if (startMessagingButton)
			{
				startMessagingButton = false;
				iStartMessaging.setText("Stop messaging (BT+MQTT)");
				notifyMessaging(true);
			} else
			{
				startMessagingButton = true;
				iStartMessaging.setText("Start messaging (BT+MQTT)");
				notifyMessaging(false);
			}
		}
	}
	
	
	/**
	 * Starts MQTT Broker on Windows
	 * 
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private class StartStopMQTTAction implements ActionListener
	{
		private Process	p;
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			// --- start button shown ---
			if (startMQTTButton)
			{
				if (OsDetector.isWindows())
				{
					try
					{
						p = Runtime.getRuntime().exec("../SumatraMessaging/mosquitto/mosquitto.exe");
						log.info("MQTT broker started");
					} catch (Exception err)
					{
						log.warn(
								"Could not start MQTT broker. Note: You need to checkout SumatraMessaging to the same directory as Sumatra",
								err);
					}
				} else
				{
					log.debug("MQTT broker is only on Windows");
					return;
				}
				startMQTTButton = false;
			} else
			{
				if (p != null)
				{
					p.destroy();
					log.info("MQTT broker stopped");
				}
				startMQTTButton = true;
			}
		}
	}
	
	private class UpdateControllerAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMObserver observer : observers)
			{
				observer.setUpController(false);
			}
		}
	}
	
	private class UpdateControllerKeepAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMObserver observer : observers)
			{
				observer.setUpController(true);
			}
		}
	}
}
