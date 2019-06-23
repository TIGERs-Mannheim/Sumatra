/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 29, 2010
 * Author(s): bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;


/**
 * Meta class for icons.
 * 
 * @author bernhard
 * 
 */
public final class Icons
{
	private static final int	ICON_SIZE	= 8;
	
	
	private Icons()
	{
		
	}
	
	/**
	 * Custom view icon.
	 */
	public static final Icon	VIEW_ICON	= new Icon()
														{
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
															public void paintIcon(Component c, Graphics g, int x, int y)
															{
																Color oldColor = g.getColor();
																
																g.setColor(new Color(70, 70, 70));
																g.fillRect(x, y, ICON_SIZE, ICON_SIZE);
																
																g.setColor(new Color(100, 230, 100));
																g.fillRect(x + 1, y + 1, ICON_SIZE - 2, ICON_SIZE - 2);
																
																g.setColor(oldColor);
															}
														};
}
