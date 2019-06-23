/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 19, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.visualizer.BotStatus;


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
																					
	private final List<IBotPopUpMenuObserver>	observers			= new CopyOnWriteArrayList<IBotPopUpMenuObserver>();
																					
																					
	/**
	 * @param botId
	 * @param status
	 */
	public BotPopUpMenu(final BotID botId, final BotStatus status)
	{
		this.botId = botId;
		add(hideFromAi);
		add(hideFromRcm);
		
		hideFromAi.setSelected(status.isHideAi());
		hideFromAi.addActionListener(new HideFromAiActionListener());
		hideFromRcm.setSelected(status.isHideRcm());
		hideFromRcm.addActionListener(new HideFromRcmActionListener());
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
	}
}
