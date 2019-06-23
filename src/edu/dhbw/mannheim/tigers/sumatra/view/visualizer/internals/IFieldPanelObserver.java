/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.event.MouseEvent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * FieldPanel observer interface.
 * 
 * @author Bernhard
 * 
 */
public interface IFieldPanelObserver
{
	/**
	 * 
	 * @param pos
	 * @param e
	 */
	void onFieldClick(IVector2 pos, MouseEvent e);
}
