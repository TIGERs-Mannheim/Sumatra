/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.aicenter.AICenterView;
import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.offensive.OffensiveStatisticsView;
import edu.tigers.sumatra.offensive.OffensiveStrategyView;
import edu.tigers.sumatra.statistics.StatisticsView;
import edu.tigers.sumatra.visualizer.VisualizerAiView;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayWindow extends AMainFrame
{
	private static final long	serialVersionUID	= 4040295061416588239L;
	
	
	/**
	 * display replays
	 */
	public ReplayWindow()
	{
		setTitle("Replay");
		
		addView(new AICenterView());
		addView(new LogView(false));
		addView(new VisualizerAiView());
		addView(new BotOverviewView());
		addView(new StatisticsView());
		addView(new OffensiveStrategyView());
		addView(new ReplayControlView());
		addView(new GameLogView());
		addView(new OffensiveStatisticsView());
		
		updateViewMenu();
		
		JMenu replayMenu = new JMenu("Replay");
		replayMenu.setMnemonic(KeyEvent.VK_R);
		JMenuItem menuClose = new JMenuItem("Close");
		menuClose.addActionListener(new CloseListener());
		replayMenu.add(menuClose);
		getJMenuBar().add(replayMenu);
		addMenuItems();
	}
	
	private class CloseListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			exit();
		}
		
	}
}
