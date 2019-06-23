/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.replay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.presenter.log.LogView;
import edu.tigers.sumatra.presenter.replay.ReplayControlView;


public class ReplayWindow extends AMainFrame
{
	private static final long serialVersionUID = 4040295061416588239L;
	
	
	/**
	 * display replays
	 */
	public ReplayWindow()
	{
		setTitle("Replay");
		
		addView(new LogView(false));
		addView(new ReplayControlView());
		addView(new GameLogView());
		
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
