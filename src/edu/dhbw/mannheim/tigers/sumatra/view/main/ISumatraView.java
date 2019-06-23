/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;

/**
 * Every component that wants to be displayed as a view
 * in the docking framework must implement this interface.
 * 
 * Note that the ID and the title must be unique among all views.
 * 
 * @author AndreR
 * 
 */
public interface ISumatraView
{
	/**
	 * Get the unique ID of the view.
	 * 
	 * @return View ID.
	 */
	public int getID();
	
	/**
	 * Get the unique view title. 
	 * 
	 * @return View title.
	 */
	public String getTitle();
	
	/**
	 * The component which is to be displayed in the view panel.
	 * This can be anything that extends from Component (e.g. JPanel).
	 * 
	 * @return View component.
	 */
	public Component getViewComponent();
	
	/**
	 * You may return a list of JMenus here. They are dynamically added
	 * to the main MenuBar if the view is shown. 
	 * 
	 * @return Optional JMenus or null if not used.
	 */
	public List<JMenu> getCustomMenus();
	
	/**
	 * Called if the view is shown.
	 * 
	 * You may use this function together with onHidden for
	 * e.g. controlling a rendering loop.
	 */
	public void onShown();
	
	/**
	 * Called if the view is hidden.
	 */
	public void onHidden();
	
	/**
	 * Called if the view got focused.
	 */
	public void onFocused();
	
	/**
	 * Called if the view lost the focus.
	 */
	public void onFocusLost();
}
