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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;


public class ConfigEditorSearchBar extends JComponent
{
	@Getter
	private final JTextField textField = new PlaceholderField(" search for 'text' or '@tags'");


	public ConfigEditorSearchBar()
	{

		setLayout(new BorderLayout());
		var icon = new JLabel();
		icon.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-search-50.png"));


		add(icon, BorderLayout.WEST);
		add(textField, BorderLayout.CENTER);

	}


	public static class PlaceholderField extends JTextField
	{
		private final String placeholder;


		public PlaceholderField(String placeholder)
		{
			super();
			this.placeholder = placeholder;
		}


		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (getText().isEmpty())
			{
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.GRAY);
				Insets ins = getInsets();
				FontMetrics fm = g2.getFontMetrics();
				int x = ins.left;
				int y = getHeight() / 2 + fm.getAscent() / 2 - 1;
				g2.drawString(placeholder, x, y);
				g2.dispose();
			}
		}
	}
}
