/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 19, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;


/**
 * Right click menu on number on bot.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotPopUpMenu extends JPopupMenu
{
	/**  */
	private static final long						serialVersionUID	= 5547621546041047224L;
	
	private final BotID								botId;
	private final JCheckBoxMenuItem				hideFromAi			= new JCheckBoxMenuItem("Hide from AI");
	private final JCheckBoxMenuItem				hideFromRcm			= new JCheckBoxMenuItem("Hide from RCM");
	private final JCheckBoxMenuItem				disableBot			= new JCheckBoxMenuItem("Disable bot");
	private final JMenuItem							charge				= new JMenuItem("Charge");
	private final JMenuItem							discharge			= new JMenuItem("Discharge");
	
	private final List<IBotPopUpMenuObserver>	observers			= new CopyOnWriteArrayList<IBotPopUpMenuObserver>();
	
	
	/**
	 * @param tBot
	 */
	public BotPopUpMenu(final TrackedTigerBot tBot)
	{
		botId = tBot.getId();
		add(charge);
		add(discharge);
		add(hideFromAi);
		add(hideFromRcm);
		add(disableBot);
		
		hideFromAi.setSelected((tBot.getBot() != null) && tBot.getBot().isHideFromAi());
		hideFromAi.addActionListener(new HideFromAiActionListener());
		hideFromRcm.setSelected((tBot.getBot() != null) && tBot.getBot().isHideFromRcm());
		hideFromRcm.addActionListener(new HideFromRcmActionListener());
		disableBot.setSelected(
				(tBot.getBot() != null)
						&& tBot.getBot().getControlledBy().equals(ABotManager.BLOCKED_BY_SUMATRA));
		disableBot.addActionListener(new DisableBotActionListener());
		charge.addActionListener(new ChargeActionListener());
		discharge.addActionListener(new DischargeActionListener());
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBotPopUpMenuObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBotPopUpMenuObserver observer)
	{
		observers.remove(observer);
	}
	
	private class HideFromAiActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onHideBotFromAiClicked(botId, checked);
			}
		}
	}
	
	private class HideFromRcmActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onHideBotFromRcmClicked(botId, checked);
			}
		}
	}
	
	private class DisableBotActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onDisableBotClicked(botId, checked);
			}
		}
	}
	
	private class ChargeActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onCharge(botId);
			}
		}
	}
	
	private class DischargeActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onDischarge(botId);
			}
		}
	}
	
	/**
	 */
	public interface IBotPopUpMenuObserver
	{
		/**
		 * @param botId
		 * @param hide
		 */
		default void onHideBotFromAiClicked(final BotID botId, final boolean hide)
		{
		}
		
		
		/**
		 * @param botId
		 * @param hide
		 */
		default void onHideBotFromRcmClicked(final BotID botId, final boolean hide)
		{
		}
		
		
		/**
		 * @param botId
		 * @param disable
		 */
		default void onDisableBotClicked(final BotID botId, final boolean disable)
		{
		}
		
		
		/**
		 * @param botId
		 */
		default void onCharge(final BotID botId)
		{
		}
		
		
		/**
		 * @param botId
		 */
		default void onDischarge(final BotID botId)
		{
		}
	}
}
