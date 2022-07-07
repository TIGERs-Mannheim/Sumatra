/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.replay;

import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.presenter.log.LogView;
import edu.tigers.sumatra.presenter.replay.ReplayControlView;
import edu.tigers.sumatra.util.ShortcutsDialog;
import edu.tigers.sumatra.visualizer.VisualizerView;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;


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
		addView(new VisualizerView());

		updateViewMenu();

		JMenu replayMenu = new JMenu("Replay");
		replayMenu.setMnemonic(KeyEvent.VK_R);

		final JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");
		shortcutMenuItem.addActionListener(actionEvent -> new ShortcutsDialog(ReplayWindow.this));
		replayMenu.add(shortcutMenuItem);

		JMenuItem menuClose = new JMenuItem("Close");
		menuClose.addActionListener(e -> exit());
		replayMenu.add(menuClose);

		getJMenuBar().add(replayMenu);
		addMenuItems();
	}
}
