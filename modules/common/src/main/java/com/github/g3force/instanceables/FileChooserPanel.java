/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

// This is not yet at the right place, but I don't know where it belongs :|

package com.github.g3force.instanceables;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;
import java.nio.file.Path;


public class FileChooserPanel extends JPanel
{
	private transient Path selectedPath;


	public FileChooserPanel(Path defaultPath)
	{
		this.selectedPath = defaultPath;

		JTextField pathTextField = new JTextField();
		pathTextField.setEnabled(false);
		pathTextField.setText(defaultPath.getFileName().toString());

		JButton chooseButton = new JButton("Choose File");
		chooseButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser(selectedPath.toString());
			int result = fileChooser.showOpenDialog(FileChooserPanel.this);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				selectedPath = file.toPath();
				pathTextField.setText(selectedPath.getFileName().toString());
			}
		});

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(chooseButton);
		add(pathTextField);
	}


	public Path getSelectedPath()
	{
		return selectedPath;
	}
}
