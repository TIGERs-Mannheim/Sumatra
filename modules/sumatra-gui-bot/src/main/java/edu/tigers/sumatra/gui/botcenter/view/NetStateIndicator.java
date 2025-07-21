/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view;

import edu.tigers.sumatra.botmanager.communication.ENetworkState;

import javax.swing.JTextField;
import java.awt.Color;


/**
 * Coloured JTextField that uses an ENetworkState.
 */
public class NetStateIndicator extends JTextField
{
	private static final long serialVersionUID = 4127310126348792093L;

	private ENetworkState state = ENetworkState.OFFLINE;


	public NetStateIndicator()
	{
		setEditable(false);

		setConnectionState(state);
	}



	/**
	 * Set connection state.
	 *
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		this.state = state;

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
			default:
				break;
		}
	}


	/**
	 * @return
	 */
	public ENetworkState getState()
	{
		return state;
	}
}
