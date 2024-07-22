/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.config;

import edu.tigers.sumatra.util.ImageScaler;
import lombok.Getter;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BorderLayout;


public class ConfigEditorSearchBar extends JComponent
{
	@Getter
	private final JTextField textField = new JTextField();


	public ConfigEditorSearchBar()
	{

		setLayout(new BorderLayout());
		var icon = new JLabel();
		icon.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-search-50.png"));


		add(icon, BorderLayout.WEST);
		add(textField, BorderLayout.CENTER);

	}
}
