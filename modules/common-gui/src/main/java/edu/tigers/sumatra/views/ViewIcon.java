/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Okt 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ViewIcon implements Icon
{
	
	private static final int ICON_SIZE = 8;
	
	
	@Override
	public int getIconHeight()
	{
		return ICON_SIZE;
	}
	
	
	@Override
	public int getIconWidth()
	{
		return ICON_SIZE;
	}
	
	
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
