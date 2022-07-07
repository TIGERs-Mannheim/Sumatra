/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.toolbar;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;


/**
 * Show available shortcuts
 */
@Log4j2
public class CaptureSettingsDialog extends JDialog
{
	@Getter
	private final JTextField txtRecordingWidth = new JTextField();
	@Getter
	private final JTextField txtRecordingHeight = new JTextField();


	public CaptureSettingsDialog()
	{
		setSize(400, 200);
		setResizable(false);
		setTitle("Capture settings");

		// --- alignment: center on screen ---
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();
		this.setLocation((int) (screenDimension.getWidth() - getWidth()) / 2,
				(int) (screenDimension.getHeight() - getHeight()) / 2);

		var panel = new JPanel(new GridLayout(0, 1));
		int borderSize = 20;
		panel.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
		panel.add(new JLabel("width:"));
		panel.add(txtRecordingWidth);
		panel.add(new JLabel("height:"));
		panel.add(txtRecordingHeight);

		add(panel);
	}
}
