/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


/**
 * Tiger bot summary for the overview panel.
 * 
 * @author AndreR
 * 
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
		setLayout(new MigLayout("fill", "[100,fill]20[40]10[60,fill]20[30]10[60,fill]", "0[]0"));
		
		cap = new JTextField();
		cap.setHorizontalAlignment(JTextField.RIGHT);
		
		name = "Bob";
		id = BotID.createBotId();
		netState = ENetworkState.CONNECTING;
		
		status = new JTextField();
		status.setEditable(false);
		battery = new JProgressBar(12800, 16800);
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
	 * 
	 * @param id
	 */
	public void setId(BotID id)
	{
		this.id = id;
		updateTitle();
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public void setBotName(String name)
	{
		this.name = name;
		updateTitle();
	}
	
	
	/**
	 * 
	 * @param voltage
	 */
	public void setBatteryLevel(final float voltage)
	{
		battery.setValue((int) (voltage * 1000));
		battery.setString(String.format(Locale.ENGLISH, "%1.2f V", voltage));
	}
	
	
	/**
	 * @param state
	 */
	public void setNetworkState(ENetworkState state)
	{
		netState = state;
		updateTitle();
	}
	
	
	/**
	 * @param f
	 */
	public void setCap(final float f)
	{
		float green = 1;
		float red = 0;
		
		// increase red level => yellow
		if (f < 125)
		{
			red = f / 125;
		} else
		{
			red = 1;
		}
		
		// decrease green level => red
		if (f > 125)
		{
			green = 1 - ((f - 125) / 125);
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
		
		final float g = green;
		final float r = red;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				cap.setText(String.format(Locale.ENGLISH, "%3.1fV", f));
				cap.setBackground(new Color(r, g, 0));
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
}
