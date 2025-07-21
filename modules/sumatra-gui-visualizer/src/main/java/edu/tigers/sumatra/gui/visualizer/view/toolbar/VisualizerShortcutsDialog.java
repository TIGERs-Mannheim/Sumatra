/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.visualizer.view.toolbar;

import lombok.extern.log4j.Log4j2;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;


/**
 * Show available shortcuts
 */
@Log4j2
public class VisualizerShortcutsDialog extends JDialog
{
	public VisualizerShortcutsDialog()
	{
		setResizable(false);
		setTitle("Visualizer Shortcuts");

		// --- alignment: center on screen ---
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();
		this.setLocation(
				(int) (screenDimension.getWidth() - getWidth()) / 2,
				(int) (screenDimension.getHeight() - getHeight()) / 2
		);

		GridLayout layout = new GridLayout(0, 1);
		layout.setHgap(10);
		layout.setVgap(10);
		var panel = new JPanel(layout);
		String content = getContent();
		panel.add(new JLabel(content.replace("\n", "").replaceAll("<!--(.*?)-->", "").trim()));
		add(panel);

		pack();
		setVisible(true);
	}


	private String getContent()
	{
		try (InputStream is = VisualizerShortcutsDialog.class.getResourceAsStream("/shortcuts.html"))
		{
			if (is == null)
			{
				log.error("Could not find shortcuts.html");
				return "";
			}
			return new String(is.readAllBytes());
		} catch (IOException e)
		{
			log.error("Could not find shortcuts.html file", e);
			return "";
		}
	}
}
