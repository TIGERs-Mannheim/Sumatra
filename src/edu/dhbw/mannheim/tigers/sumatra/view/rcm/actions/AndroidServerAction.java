/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network.AndroidRCMServer;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ShowRCMMainPanel;


/**
 * Connect/disconnect to Android Server Bridge to Sumatra on Button click.
 * 
 * @author Sven Frank
 * 
 */
public class AndroidServerAction implements ActionListener
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private AndroidRCMServer	androidRCMServer;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AndroidServerAction()
	{
		androidRCMServer = new AndroidRCMServer();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent e)
	{
		final JButton androidServerButton = ShowRCMMainPanel.getInstance().getAndroidServerButton();
		
		// --- connection active button shown ---
		if (androidRCMServer.isRunning())
		{
			androidRCMServer.stopServer();
			
			androidServerButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("android.png")));
		}
		// --- connection closed button shown ---
		else
		{
			androidRCMServer.startServer();
			androidServerButton.setIcon(new ImageIcon(ClassLoader.getSystemResource("androidclose.png")));
		}
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
