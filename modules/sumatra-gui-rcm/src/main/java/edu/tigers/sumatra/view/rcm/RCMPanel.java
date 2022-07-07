/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.rcm;


import edu.tigers.sumatra.rcm.IRCMObserver;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Main layout of RCM is displayed here.
 */
public final class RCMPanel extends JPanel
{
	private final JMenuBar menuBar;
	private final JMenuItem iStartStop;
	private final JTabbedPane controllerTabbedPane;
	private final List<IRCMObserver> observers = new CopyOnWriteArrayList<>();

	// --- activity of startButton (start or stop pressed)
	private boolean startButton = true;


	public RCMPanel()
	{
		setLayout(new BorderLayout());

		menuBar = new JMenuBar();
		JMenu mRcm = new JMenu("RCM");
		JMenu mController = new JMenu("Controllers");
		menuBar.add(mRcm);
		menuBar.add(mController);

		iStartStop = new JMenuItem("Start");
		JMenuItem iRefreshRcm = new JMenuItem("Refresh");
		JMenuItem iRefreshRcmKeep = new JMenuItem("Refresh+Keep connections");
		JMenuItem iReloadControllers = new JMenuItem("Reload");
		JMenuItem iReloadControllersKeep = new JMenuItem("Reload+Keep connections");
		mRcm.add(iStartStop);
		mRcm.add(iRefreshRcm);
		mRcm.add(iRefreshRcmKeep);
		mController.add(iReloadControllers);
		mController.add(iReloadControllersKeep);

		iStartStop.addActionListener(new StartStopAction());
		iRefreshRcm.addActionListener(new RestartAction());
		iRefreshRcmKeep.addActionListener(new RestartKeepAction());
		iReloadControllers.addActionListener(new UpdateControllerAction());
		iReloadControllersKeep.addActionListener(new UpdateControllerKeepAction());

		// --- one Tab for each Controller - set below ---
		controllerTabbedPane = new JTabbedPane();
		controllerTabbedPane.setPreferredSize(new Dimension(1000, 2000));
		add(menuBar, BorderLayout.PAGE_START);
		add(controllerTabbedPane, BorderLayout.CENTER);

		stop();
	}


	public void addObserver(final IRCMObserver observer)
	{
		observers.add(observer);
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
				// nothing to do
			}


			@Override
			public void componentResized(final ComponentEvent e)
			{
				controllerTabbedPane.revalidate();
			}


			@Override
			public void componentMoved(final ComponentEvent e)
			{
				// nothing to do
			}


			@Override
			public void componentHidden(final ComponentEvent e)
			{
				// nothing to do
			}
		});
	}


	public void clearControllerPanels()
	{
		controllerTabbedPane.removeAll();
	}


	public void start()
	{
		for (int i = 0; i < menuBar.getMenuCount(); i++)
		{
			menuBar.getMenu(i).setEnabled(true);
		}
	}


	public void stop()
	{
		for (int i = 0; i < menuBar.getMenuCount(); i++)
		{
			menuBar.getMenu(i).setEnabled(false);
		}
	}


	public void startRcm()
	{
		iStartStop.setText("Stop");
		startButton = false;
	}


	public void stopRcm()
	{
		iStartStop.setText("Start");
		startButton = true;
	}


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

	private class StartStopAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyStartStop(startButton);
		}


		private void notifyStartStop(final boolean activeState)
		{
			for (IRCMObserver observer : observers)
			{
				observer.onStartStopButtonPressed(activeState);
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
