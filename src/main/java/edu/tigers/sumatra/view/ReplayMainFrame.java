/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view;

import edu.tigers.sumatra.AMainFrame;
import lombok.Getter;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import java.awt.event.KeyEvent;


@Getter
public class ReplayMainFrame extends AMainFrame
{
	private final JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");


	public ReplayMainFrame()
	{
		setTitle("Replay");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JMenu replayMenu = new JMenu("Replay");
		replayMenu.setMnemonic(KeyEvent.VK_R);
		replayMenu.add(shortcutMenuItem);

		getJMenuBar().add(replayMenu);
		addMenuItems();
	}
}
