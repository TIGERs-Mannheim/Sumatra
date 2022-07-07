/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import lombok.Getter;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


/**
 * Icon of a sumatra view
 */
@Getter
public class ViewIcon implements Icon
{
	private static final int ICON_SIZE = 8;
	private int iconHeight = ICON_SIZE;
	private int iconWidth = ICON_SIZE;


	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y)
	{
		Color oldColor = g.getColor();

		g.setColor(new Color(70, 70, 70));
		g.fillRect(x, y, ICON_SIZE, ICON_SIZE);

		g.setColor(new Color(100, 230, 100));
		g.fillRect(x + 1, y + 1, ICON_SIZE - 2, ICON_SIZE - 2);

		g.setColor(oldColor);
	}
}
