/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view;

import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.io.Serial;


public class BaseStationPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 5473638829022902726L;

	private final JLabel state;
	private final JLabel rate;
	private final JLabel channel;


	public BaseStationPanel()
	{
		final TitledBorder border = BorderFactory.createTitledBorder("Base Station");
		setBorder(border);
		setLayout(new MigLayout("fill, inset 0", "[]5[]5[]5[]5[]"));

		state = new JLabel("Unused");
		rate = new JLabel("Rate: -");
		channel = new JLabel("CH: -");

		add(state);
		state.setMinimumSize(new Dimension(70, 0));
		add(rate);
		rate.setMinimumSize(new Dimension(60, 0));
		add(channel);
		channel.setMinimumSize(new Dimension(40, 0));
	}


	public void setOnline(boolean isOnline)
	{
		rate.setVisible(isOnline);
		channel.setVisible(isOnline);
	}


	public void setState(final String stateName)
	{
		if (!state.getText().equals(stateName))
		{
			state.setText(stateName);
		}
	}


	public void setUpdateRate(final int updateRate)
	{
		rate.setText(String.format("Rate: %3dHz", updateRate));
	}


	public void setChannel(final int channel)
	{
		this.channel.setText(String.format("CH: %3d", channel));
	}
}
