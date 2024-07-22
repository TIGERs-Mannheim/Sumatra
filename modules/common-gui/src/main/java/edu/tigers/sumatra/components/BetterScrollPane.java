/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.components;

import javax.swing.JScrollPane;
import java.awt.Component;


/**
 * Derived JScrollPane which changes some default values
 *
 * @author David Risch <DavidR@tigers-mannheim.de>
 */

public class BetterScrollPane extends JScrollPane
{
	private static final int SCROLL_INCREMENT = 16;


	public BetterScrollPane(Component component)
	{
		super(component);
		changeDefaults();
	}


	private void changeDefaults()
	{
		this.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT); // change vertical scroll speed
		this.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT); // change horizontal scroll speed
	}
}
