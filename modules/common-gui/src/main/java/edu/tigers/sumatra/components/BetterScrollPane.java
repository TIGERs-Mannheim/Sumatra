/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.components;

import java.awt.Component;

import javax.swing.JScrollPane;


/**
 * Derived JScrollPane which changes some default values
 *
 * @author David Risch <DavidR@tigers-mannheim.de>
 */

public class BetterScrollPane extends JScrollPane
{
	private static final int SCROLL_INCREMENT = 16;
	
	
	public BetterScrollPane()
	{
		super();
		changeDefaults();
	}
	
	
	public BetterScrollPane(Component component)
	{
		super(component);
		changeDefaults();
	}
	
	
	public BetterScrollPane(int var1, int var2)
	{
		super(var1, var2);
		changeDefaults();
	}
	
	
	public BetterScrollPane(Component component, int var2, int var3)
	{
		super(component, var2, var3);
		changeDefaults();
	}
	
	
	private void changeDefaults()
	{
		this.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT); // change vertical scroll speed
		this.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT); // change horizontal scroll speed
	}
}
