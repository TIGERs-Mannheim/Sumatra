/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra;

import java.util.List;


/**
 * Interface for the main frame. Necessary for the nogui feature.
 * 
 * @author AndreR
 */
public interface IMainFrame
{
	/**
	 * @param o
	 */
	void addObserver(IMainFrameObserver o);
	
	
	/**
	 * @param o
	 */
	void removeObserver(IMainFrameObserver o);
	
	
	/**
	 * @param filename
	 */
	void loadLayout(final String filename);
	
	
	/**
	 * @param filename
	 */
	void saveLayout(String filename);
	
	
	/**
	 * @param names
	 */
	void setMenuLayoutItems(List<String> names);
	
	
	/**
	 * @param name
	 */
	void selectLayoutItem(String name);
	
}
