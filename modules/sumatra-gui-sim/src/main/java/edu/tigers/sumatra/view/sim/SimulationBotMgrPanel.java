/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.sim;

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
 * Manage bots of a sumatra base station
 */
public class SimulationBotMgrPanel extends JPanel
{
	private final List<ISimulationBotMgrObserver> observers = new CopyOnWriteArrayList<>();
	
	private final Map<BotID, JCheckBox> checkBoxes = new HashMap<>();
	
	private final JCheckBox autoBotCount = new JCheckBox("Auto", true);
	
	/**
	 * New Panel
	 */
	public SimulationBotMgrPanel()
	{
		setBorder(BorderFactory.createTitledBorder("Bot Management"));
		setLayout(new GridLayout(0, BotID.getAllYellow().size() + 1));
		
		autoBotCount.addActionListener(new AutoBotCountActionListener());
		
		BotID.getAllYellow().forEach(this::addBotCheckbox);
		add(autoBotCount);
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
	public void addObserver(ISimulationBotMgrObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(ISimulationBotMgrObserver o)
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
	
	
	public JCheckBox getAutoBotCount()
	{
		return autoBotCount;
	}
	
	/**
	 * Observer for {@link SimulationBotMgrPanel}
	 */
	public interface ISimulationBotMgrObserver
	{
		void onAddBot(BotID botID);
		
		
		void onRemoveBot(BotID botID);
		
		
		void onSetAutoBotCount(boolean active);
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
				observers.forEach(o -> o.onAddBot(botID));
			} else
			{
				observers.forEach(o -> o.onRemoveBot(botID));
			}
		}
	}
	
	private class AutoBotCountActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JCheckBox checkBox = (JCheckBox) e.getSource();
			if (checkBox.isSelected())
			{
				observers.forEach(o -> o.onSetAutoBotCount(true));
			} else
			{
				observers.forEach(o -> o.onSetAutoBotCount(false));
			}
		}
	}
}
