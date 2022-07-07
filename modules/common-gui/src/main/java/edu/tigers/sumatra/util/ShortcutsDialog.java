/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.util;

import edu.tigers.sumatra.components.BetterScrollPane;
import net.miginfocom.swing.MigLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;


/**
 * Show available shortcuts
 */
public class ShortcutsDialog extends JDialog
{
	private static final long serialVersionUID = 3461893941869192656L;


	public ShortcutsDialog(Container parent)
	{
		// --- window configuration ---
		this.setSize(400, 400);
		setResizable(false);
		setTitle("Shortcuts");

		// --- alignment: center on screen ---
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();
		this.setLocation((int) (screenDimension.getWidth() - getWidth()) / 2,
				(int) (screenDimension.getHeight() - getHeight()) / 2);

		JPanel scPanel = new JPanel(new MigLayout("fill", ""));
		for (UiShortcut shortcut : GlobalShortcuts.getShortcuts(parent))
		{
			JLabel lblId = new JLabel(shortcut.getName());
			JLabel lblDesc = new JLabel(shortcut.getKeys());
			scPanel.add(lblId);
			scPanel.add(lblDesc, "wrap");
		}
		BetterScrollPane scrollPane = new BetterScrollPane(scPanel);
		add(scrollPane);
		setVisible(true);
	}
}
