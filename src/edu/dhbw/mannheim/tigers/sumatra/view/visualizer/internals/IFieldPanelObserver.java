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

/**
 * FieldPanel observer interface.
 * 
 * @author Bernhard
 * 
 */
public interface IFieldPanelObserver
{
	public void onFieldClick(int x, int y, boolean ctrl, boolean alt);
}
