/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): rYan
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * Interface for the main frame. Necessary for the nogui feature.
 * 
 * @author AndreR
 * 
 */
public interface IMainFrame
{
	public void addObserver(IMainFrameObserver o);
	public void removeObserver(IMainFrameObserver o);
	public void setStartStopButtonState(boolean enable);
	public void setStartStopButtonState(boolean enable, ImageIcon icon);
	public void loadLayout(final String filename);
	public void saveLayout(String filename);
	public void setMenuLayoutItems(ArrayList<String> names);
	public void setMenuModuliItems(ArrayList<String> names);
	public void addView(ISumatraView view);
	public void selectModuliItem(String name);
	public void selectLayoutItem(String name);
	public void setLookAndFeel(String lafName);
}
