/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.views;

import java.util.List;

import javax.swing.JMenu;


/**
 * Every component that wants to be displayed as a view
 * in the docking framework must implement this interface.
 * 
 * @author AndreR
 * 
 */
public interface ISumatraView
{
	/**
	 * You may return a list of JMenus here. They are dynamically added
	 * to the main MenuBar if the view is shown.
	 * 
	 * @return Optional JMenus or null if not used.
	 */
	List<JMenu> getCustomMenus();
	
	
	/**
	 * Called if the view is shown.
	 * 
	 * You may use this function together with onHidden for
	 * e.g. controlling a rendering loop.
	 */
	void onShown();
	
	
	/**
	 * Called if the view is hidden.
	 */
	void onHidden();
	
	
	/**
	 * Called if the view got focused.
	 */
	void onFocused();
	
	
	/**
	 * Called if the view lost the focus.
	 */
	void onFocusLost();
}
