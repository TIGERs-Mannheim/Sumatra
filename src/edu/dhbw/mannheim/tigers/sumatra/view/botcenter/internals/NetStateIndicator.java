/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.06.2013
 * Author(s): rYan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


/**
 * Coloured JTextField that uses an ENetworkState.
 * 
 * @author AndreR
 * 
 */
public class NetStateIndicator extends JTextField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= 4127310126348792093L;
	
	private ENetworkState		state					= ENetworkState.OFFLINE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public NetStateIndicator()
	{
		setEditable(false);
		
		setConnectionState(state);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set connection state.
	 * 
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		this.state = state;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				switch (state)
				{
					case OFFLINE:
						setText("Offline");
						setBackground(new Color(255, 128, 128));
						break;
					case CONNECTING:
						setText("Connecting");
						setBackground(Color.CYAN);
						break;
					case ONLINE:
						setText("Online");
						setBackground(Color.GREEN);
						break;
				}
			}
		});
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ENetworkState getState()
	{
		return state;
	}
}
