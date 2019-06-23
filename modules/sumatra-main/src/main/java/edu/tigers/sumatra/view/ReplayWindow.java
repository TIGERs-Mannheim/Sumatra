/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogView;
import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.aicenter.AICenterView;
import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.ids.ETeamColor;
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
	private static final long serialVersionUID = 4040295061416588239L;
	
	
	/**
	 */
	public ReplayWindow()
	{
		setTitle("Replay");
		
		addView(new AICenterView(ETeamColor.YELLOW));
		addView(new AICenterView(ETeamColor.BLUE));
		addView(new LogView(false));
		addView(new VisualizerAiView());
		addView(new BotOverviewView());
		addView(new StatisticsView(ETeamColor.YELLOW));
		addView(new StatisticsView(ETeamColor.BLUE));
		addView(new OffensiveStrategyView());
		addView(new ReplayControlView());
		
		updateViewMenu();
		
		JMenu replayMenu = new JMenu("Replay");
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
