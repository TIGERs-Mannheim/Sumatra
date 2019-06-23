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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	/**
	 *
	 */
	public interface ITigerBotV2SummaryObserver
	{
		/** */
		void onConnectionChange();
		
		
		/**
		 * @param oofCheck
		 */
		void onOOFCheckChange(boolean oofCheck);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= 5485796598824650963L;
	private final JTextField								status;
	private final JCheckBox									oofCheck;
	private final JProgressBar								battery;
	private final JButton									connect;
	
	private String												name;
	private BotID												id;
	private ENetworkState									netState;
	
	private final List<ITigerBotV2SummaryObserver>	observers			= new ArrayList<ITigerBotV2SummaryObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerBotV2Summary()
	{
		setLayout(new MigLayout("fill", "[100,fill]10[100,fill]30[60]10[60]10[60]30[40]5[60]", "0[]0"));
		
		name = "Bob";
		id = new BotID();
		netState = ENetworkState.CONNECTING;
		
		status = new JTextField();
		status.setEditable(false);
		oofCheck = new JCheckBox("OOF Check");
		battery = new JProgressBar(12800, 16800);
		battery.setStringPainted(true);
		
		connect = new JButton("Disconnect");
		
		connect.addActionListener(new Connect());
		oofCheck.addActionListener(new OOFChange());
		
		add(status);
		add(connect);
		add(oofCheck);
		add(new JLabel("Battery:"));
		add(battery, "growy");
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Trish (1)"));
		
		setNetworkState(ENetworkState.OFFLINE);
		setBatteryLevel(14.2f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(ITigerBotV2SummaryObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(ITigerBotV2SummaryObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
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
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				battery.setValue((int) (voltage * 1000));
				battery.setString(String.format(Locale.ENGLISH, "%1.2f V", voltage));
			}
		});
	}
	
	
	/**
	 * @param enable
	 */
	public void setOofCheck(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				oofCheck.setSelected(enable);
			}
		});
	}
	
	
	/**
	 * @param state
	 */
	public void setNetworkState(ENetworkState state)
	{
		netState = state;
		updateTitle();
	}
	
	
	private void notifyConnectionChange()
	{
		synchronized (observers)
		{
			for (final ITigerBotV2SummaryObserver observer : observers)
			{
				observer.onConnectionChange();
			}
		}
	}
	
	
	private void notifyOOFChange(boolean oofCheck)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2SummaryObserver observer : observers)
			{
				observer.onOOFCheckChange(oofCheck);
			}
		}
	}
	
	
	private void updateTitle()
	{
		final String title = String.format("[ %d ] %s", id.getNumber(), name);
		Color borderColor = null;
		String stateText = "";
		String buttonText = "Disconnect";
		
		switch (netState)
		{
			case OFFLINE:
				stateText = "Offline";
				borderColor = Color.RED;
				buttonText = "Connect";
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
		final String bText = buttonText;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(col), title));
				status.setText(text);
				connect.setText(bText);
			}
		});
	}
	
	private class Connect implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyConnectionChange();
		}
	}
	
	private class OOFChange implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyOOFChange(oofCheck.isSelected());
		}
	}
}
