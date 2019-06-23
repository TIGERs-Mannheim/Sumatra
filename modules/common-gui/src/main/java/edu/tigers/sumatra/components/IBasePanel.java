/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.components;

/**
 * @author "Lukas Magel"
 * @param <T>
 */
public interface IBasePanel<T>
{
	/**
	 * @param observer
	 */
	public void addObserver(T observer);
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(T observer);
	
	
	/**
	 * @param enabled
	 */
	public void setPanelEnabled(boolean enabled);
}
