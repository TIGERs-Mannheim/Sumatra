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
	 * @param ctrl
	 * @param alt
	 * @param shift
	 * @param meta
	 */
	void onFieldClick(IVector2 pos, boolean ctrl, boolean alt, boolean shift, boolean meta);
}
