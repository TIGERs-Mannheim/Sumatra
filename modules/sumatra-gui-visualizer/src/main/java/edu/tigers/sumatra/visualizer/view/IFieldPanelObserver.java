/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

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
