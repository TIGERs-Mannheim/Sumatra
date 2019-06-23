/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.basestation;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.tigers.sumatra.ids.BotID;


/**
 * Manage bots of a basestation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BaseStationBotMgrPanel extends JPanel
{
	private final List<IBaseStationBotMgrObserver> observers = new CopyOnWriteArrayList<>();
	
	private final Map<BotID, JCheckBox> checkBoxes = new HashMap<>();
	
	
	/**
	 * New Panel
	 */
	public BaseStationBotMgrPanel()
	{
		setBorder(BorderFactory.createTitledBorder("Bot Management"));
		setLayout(new GridLayout(2, BotID.getAllYellow().size()));
		
		BotID.getAllYellow().forEach(this::addBotCheckbox);
		BotID.getAllBlue().forEach(this::addBotCheckbox);
	}
	
	
	private void addBotCheckbox(final BotID botID)
	{
		JCheckBox btn = new JCheckBox(botID.getNumber() + botID.getTeamColor().name().substring(0, 1));
		add(btn);
		btn.addActionListener(new BotActionListener(botID));
		checkBoxes.put(botID, btn);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(IBaseStationBotMgrObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IBaseStationBotMgrObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * Set checkbox state of given bot
	 * 
	 * @param botID
	 * @param botAvailable
	 */
	public void setBotAvailable(final BotID botID, final boolean botAvailable)
	{
		checkBoxes.get(botID).setSelected(botAvailable);
	}
	
	private class BotActionListener implements ActionListener
	{
		private final BotID botID;
		
		
		BotActionListener(BotID botID)
		{
			this.botID = botID;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBox checkBox = (JCheckBox) e.getSource();
			if (checkBox.isSelected())
			{
				for (IBaseStationBotMgrObserver o : observers)
				{
					o.onAddBot(botID);
				}
			} else
			{
				for (IBaseStationBotMgrObserver o : observers)
				{
					o.onRemoveBot(botID);
				}
			}
		}
	}
	
	/**
	 * Observer for {@link BaseStationBotMgrPanel}
	 */
	public interface IBaseStationBotMgrObserver
	{
		/**
		 * @param botID
		 */
		void onAddBot(BotID botID);
		
		
		/**
		 * @param botID
		 */
		void onRemoveBot(BotID botID);
	}
}
