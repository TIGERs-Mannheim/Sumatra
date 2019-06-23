/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;


import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import net.java.games.input.Controller;
import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.RCMPresenter;
import edu.dhbw.mannheim.tigers.sumatra.util.OsDetector;


/**
 * Main layout of RCM is displayed here.
 * 
 * @author Sven Frank
 * 
 */
public final class ShowRCMMainPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log							= Logger.getLogger(ShowRCMMainPanel.class.getName());
	
	private static final long				serialVersionUID			= 7811945315539136091L;
	
	// --- Frame instance ---
	private static ShowRCMMainPanel		instance						= null;
	
	// --- GUI components ---
	private JButton							startStopButton;
	private JButton							refreshConnetionButton;
	private JButton							refreshControllersButton;
	// private JButton exitButton;
	// private JButton clearLoggingButton;
	private JTabbedPane						controllerTabbedPane;
	// private JPanel loggingPanel;
	private JPanel								programRunPanel;
	private JPanel								mainPanel;
	// public JTextArea loggingTextArea;
	// private JScrollPane loggingScrollPane;
	private JToggleButton					startStopMQTTButton;
	private JToggleButton					startStopMessagingButton;
	
	
	// --- list with all ControllerPanels
	private final List<ControllerPanel>	controllerPanelS			= new ArrayList<ControllerPanel>();
	
	// --- status of startButton (start or stop pressed)
	private boolean							startButton					= true;
	private boolean							startMQTTButton			= true;
	private boolean							startMessagingButton		= true;
	
	private List<IMessagingGUIObserver>	observersMessaging		= new LinkedList<IMessagingGUIObserver>();
	
	// --- number of known controllers ---
	private static int						xboxControllerCount		= 0;
	private static int						logitechControllerCount	= 0;
	private static int						keyboardControllerCount	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private ShowRCMMainPanel()
	{
		{
			// --- MainPanel with all Components ---
			mainPanel = new JPanel();
			final MigLayout mainPanelLayout = new MigLayout();
			mainPanel.setLayout(mainPanelLayout);
			{
				// --- Panel with Programm Control Buttons ---
				programRunPanel = new JPanel();
				final MigLayout programRunPanelLayout = new MigLayout();
				programRunPanel.setLayout(programRunPanelLayout);
				mainPanel.add(programRunPanel, "cell 0 0");
				{
					// --- Start-/StopButton ---
					startStopButton = new JButton();
					programRunPanel.add(startStopButton, "cell 0 0");
					startStopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("start.png")));
					startStopButton.addActionListener(new StartStopAction());
					startStopButton.setToolTipText("Connect to Bots");
				}
				{
					// --- Restart Button ---
					refreshConnetionButton = new JButton();
					programRunPanel.add(refreshConnetionButton, "cell 1 0");
					refreshConnetionButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("reload2.png")));
					refreshConnetionButton.addActionListener(new RestartAction());
					refreshConnetionButton.setToolTipText("Reconnect to Bots");
				}
				{
					refreshControllersButton = new JButton();
					programRunPanel.add(refreshControllersButton, "cell 3 0");
					refreshControllersButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("xboxController.png")));
					refreshControllersButton.addActionListener(new UpdateControllerAction());
					refreshControllersButton.setToolTipText("Refresh Controllers");
				}
				{
					// --- Start-/StopButton for MQTT Broker on Windows---
					startStopMQTTButton = new JToggleButton();
					programRunPanel.add(startStopMQTTButton, "cell 4 0");
					startStopMQTTButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("mqtt_logo.png")));
					startStopMQTTButton.addActionListener(new StartStopMQTTAction());
					startStopMQTTButton.setToolTipText("Start MQTT broker for Windows");
				}
				{
					// --- Start-/StopButton for Messaging system---
					startStopMessagingButton = new JToggleButton();
					programRunPanel.add(startStopMessagingButton, "cell 4 0");
					startStopMessagingButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("bluetooth.png")));
					startStopMessagingButton.addActionListener(new StartStopMessagingAction());
					startStopMessagingButton.setToolTipText("Start Messaging system");
				}
			}
			
			
			{
				// --- one Tab for each Controller - set below ---
				controllerTabbedPane = new JTabbedPane();
				mainPanel.add(controllerTabbedPane, "cell 0 1");
				// controllerTabbedPane.setPreferredSize(new java.awt.Dimension(524, 61));
			}
		}
		
		stop();
		
		this.add(mainPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static ShowRCMMainPanel getInstance()
	{
		if (instance == null)
		{
			instance = new ShowRCMMainPanel();
		}
		return instance;
	}
	
	
	/**
	 * Add new ControllerPanel and Tab for each Controller. Save this new ControllerPanel to controllerPanelS-List.
	 * @param newController
	 * @param controllerPresenter
	 * @return newControllerPanel
	 */
	public ControllerPanel addControllerPanel(Controller newController, AControllerPresenter controllerPresenter)
	{
		final ControllerPanel newControllerPanel = new ControllerPanel(newController, controllerPresenter);
		controllerPanelS.add(newControllerPanel);
		controllerTabbedPane.addTab(checkControllerName(newController), newControllerPanel);
		return newControllerPanel;
	}
	
	
	/**
	 * Returns Name for Tabs. If Controller is known, set specified name and count amount of known controllers.
	 * @param knownController
	 * @return ControllerName
	 */
	private String checkControllerName(Controller knownController)
	{
		if (knownController.getName().contains("Xbox 360"))
		{
			xboxControllerCount++;
			return "XBox 360 Controller #" + xboxControllerCount;
		}
		if (knownController.getName().contains("RumblePad"))
		{
			logitechControllerCount++;
			return "Logitech RumblePad #" + logitechControllerCount;
		}
		if (knownController.getName().equals("Standardtastatur (PS/2)"))
		{
			keyboardControllerCount++;
			return knownController.getType() + " #" + keyboardControllerCount;
		}
		return knownController.getType().toString();
	}
	
	
	/**
	 */
	public final void start()
	{
		startStopButton.setEnabled(true);
		startStopMessagingButton.setEnabled(true);
		startStopMQTTButton.setEnabled(true);
		refreshConnetionButton.setEnabled(true);
		refreshControllersButton.setEnabled(true);
	}
	
	
	/**
	 */
	public final void stop()
	{
		startStopButton.setEnabled(false);
		startStopMessagingButton.setEnabled(false);
		startStopMQTTButton.setEnabled(false);
		refreshConnetionButton.setEnabled(false);
		refreshControllersButton.setEnabled(false);
	}
	
	/**
	 * Restart controllers (send stop and start)
	 * @author Clemens
	 * 
	 */
	private class RestartAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Start - Stop
			RCMPresenter.getInstance().onStartStopButtonPressed();
			// --- Start to Stop Button ---
			startStopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("stop.png")));
			startButton = false;
		}
	}
	
	/**
	 * Action to set Start/Stop Button, run application and disable ControllerPanels
	 * @author Clemens
	 * 
	 */
	private class StartStopAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// --- start button shown ---
			if (startButton)
			{
				startRcm();
			}
			// --- stop button shown ----
			else
			{
				stopRcm();
			}
		}
	}
	
	/**
	 * Starts Messaging System
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	private class StartStopMessagingAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// --- start button shown ---
			if (startMessagingButton)
			{
				startMessagingButton = false;
				notifyMessaging(true);
			} else
			{
				startMessagingButton = true;
				notifyMessaging(false);
			}
		}
	}
	
	
	/**
	 * @param b
	 */
	private void notifyMessaging(boolean b)
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
	public void addMessgingGUIObserver(IMessagingGUIObserver o)
	{
		synchronized (observersMessaging)
		{
			observersMessaging.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeMessgingGUIObserver(IMessagingGUIObserver o)
	{
		synchronized (observersMessaging)
		{
			observersMessaging.remove(o);
		}
	}
	
	/**
	 * Starts MQTT Broker on Windows
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 * 
	 */
	private class StartStopMQTTAction implements ActionListener
	{
		private Process	p;
		
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// --- start button shown ---
			if (startMQTTButton)
			{
				if (OsDetector.isWindows())
				{
					try
					{
						p = Runtime.getRuntime().exec("SumatraMessaging/mosquitto/mosquitto.exe");
						log.info("MQTT broker started");
					} catch (Exception err)
					{
						log.warn("Could not start MQTT broker");
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
	
	
	private void startRcm()
	{
		int i = 1;
		boolean atLeastOneControllerIsConnected = false;
		// --- initialize connections and check if they're successfully set up. ---
		for (final boolean isConnected : RCMPresenter.getInstance().onStartStopButtonPressed(startButton))
		{
			if (!isConnected)
			{
				log.warn("Controller of the Tab #" + i + " couldn't connect to Server. Maybe wrong IP!?");
			}
			// --- there should be at least one successfully established connection for polling and sending ---
			else
			{
				atLeastOneControllerIsConnected = true;
			}
			i++;
		}
		// --- start polling - if successfully started, change GUI ---
		if (atLeastOneControllerIsConnected)
		{
			// --- Start to Stop Button ---
			startStopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("stop.png")));
			startButton = false;
		}
	}
	
	
	private void stopRcm()
	{
		// --- Stop to Start Button ---
		RCMPresenter.getInstance().onStartStopButtonPressed(startButton);
		startStopButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("start.png")));
		startButton = true;
		
		// --- enable all Panels with their components
		for (final ControllerPanel cP : controllerPanelS)
		{
			for (final java.awt.Component childComponent : cP.getComponents())
			{
				if (childComponent instanceof Container)
				{
					final Container childContainer = (Container) childComponent;
					for (final java.awt.Component childChildComponent : childContainer.getComponents())
					{
						childChildComponent.setEnabled(true);
					}
				}
			}
		}
	}
	
	private static class UpdateControllerAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			xboxControllerCount = 0;
			RCMPresenter.getInstance().setUpController();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
