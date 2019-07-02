/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import java.awt.Component;
import java.awt.Container;


public class GUIUtilities
{
	
	private GUIUtilities()
	{
	}
	
	
	public static void setEnabledRecursive(Component c, boolean enabled)
	{
		if (c instanceof Container)
		{
			for (Component child : ((Container) c).getComponents())
			{
				setEnabledRecursive(child, enabled);
			}
		}
		c.setEnabled(enabled);
	}
}
