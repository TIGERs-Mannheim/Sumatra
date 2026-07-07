package edu.tigers.sumatra.config;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

public class ConfigEditorSearchBar extends JComponent
{
	@Getter
	private final JTextField textField =
			new PlaceholderField(" search for 'text' or '@tags'");

	private final JLabel icon = new JLabel();


	public ConfigEditorSearchBar()
	{
		setLayout(new BorderLayout());

		add(icon, BorderLayout.WEST);
		add(textField, BorderLayout.CENTER);

		updateIcons();
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateIcons();
	}

	private void updateIcons()
	{
		Color color = UIManager.getColor("Label.foreground");
		icon.setIcon(
				IconFontSwing.buildIcon(
						FontAwesome.SEARCH,
						16,
						color));
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

				Color color = UIManager.getColor("Label.foreground");
				g2.setColor(color);

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