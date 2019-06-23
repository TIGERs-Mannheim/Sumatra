/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import java.awt.event.MouseEvent;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * FieldPanel observer interface.
 * 
 * @author Bernhard
 */
public interface IFieldPanelObserver
{
	/**
	 * @param pos
	 * @param e
	 */
	void onFieldClick(IVector2 pos, MouseEvent e);
	
	
	/**
	 * @param pos
	 * @param e
	 */
	default void onMouseMoved(final IVector2 pos, final MouseEvent e)
	{
	}
}
