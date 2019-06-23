/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Tiger bot summary for the overview panel.
 * 
 * @author AndreR
 */
public class TigerBotV2Summary extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 5485796598824650963L;
	private final JTextField	status;
	private final JProgressBar	battery;
	
	private String					name;
	private BotID					id;
	private ENetworkState		netState;
	private final JTextField	cap;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerBotV2Summary()
	{
		setLayout(new MigLayout("fillx", "[100,fill]20[40]10[60,fill]20[30]10[60,fill]", "0[]0"));
		
		cap = new JTextField();
		cap.setHorizontalAlignment(SwingConstants.RIGHT);
		
		name = "Bob";
		id = BotID.get();
		netState = ENetworkState.CONNECTING;
		
		status = new JTextField();
		status.setEditable(false);
		battery = new JProgressBar((int) (ABot.BAT_MIN * 1000), (int) (ABot.BAT_MAX * 1000));
		battery.setStringPainted(true);
		
		add(status);
		add(new JLabel("Battery:"));
		add(battery, "growy");
		add(new JLabel("Kicker:"));
		add(cap);
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Trish (1)"));
		
		setNetworkState(ENetworkState.OFFLINE);
		setBatteryLevel(14.2f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param id
	 */
	public void setId(final BotID id)
	{
		this.id = id;
		updateTitle();
	}
	
	
	/**
	 * @param name
	 */
	public void setBotName(final String name)
	{
		this.name = name;
		updateTitle();
	}
	
	
	/**
	 * @param voltage
	 */
	public void setBatteryLevel(final double voltage)
	{
		battery.setValue((int) (voltage * 1000));
		battery.setString(String.format(Locale.ENGLISH, "%1.2f V", voltage));
	}
	
	
	/**
	 * @param state
	 */
	public void setNetworkState(final ENetworkState state)
	{
		netState = state;
		updateTitle();
	}
	
	
	/**
	 * @param f
	 */
	public void setCap(final double f)
	{
		double green = 1;
		double red = 0;
		
		// increase red level => yellow
		if (f < 125)
		{
			red = f / 125.0;
		} else
		{
			red = 1;
		}
		
		// decrease green level => red
		if (f > 125)
		{
			green = 1 - ((f - 125) / 125.0);
		} else
		{
			green = 1;
		}
		
		if (green < 0)
		{
			green = 0;
		}
		
		if (red < 0)
		{
			red = 0;
		}
		
		final double g = green;
		final double r = red;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				cap.setText(String.format(Locale.ENGLISH, "%3.1fV", f));
				cap.setBackground(new Color((float) r, (float) g, 0));
			}
		});
	}
	
	
	private void updateTitle()
	{
		final String title = String.format("[ %d ] %s", id.getNumber(), name);
		Color borderColor = null;
		String stateText = "";
		
		switch (netState)
		{
			case OFFLINE:
				stateText = "Offline";
				borderColor = Color.RED;
				break;
			case CONNECTING:
				stateText = "Connecting";
				borderColor = Color.BLUE;
				break;
			case ONLINE:
				stateText = "Online";
				borderColor = Color.GREEN;
				break;
		}
		
		final Color col = borderColor;
		final String text = stateText;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(col), title));
				status.setText(text);
			}
		});
	}
	
	
	/**
	 * @return the netState
	 */
	public final ENetworkState getNetState()
	{
		return netState;
	}
}
