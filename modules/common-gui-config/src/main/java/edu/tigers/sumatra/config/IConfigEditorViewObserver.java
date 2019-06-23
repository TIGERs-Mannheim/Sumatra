/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.config;

/**
 * This is a view observer-interface which is used to connect user-input with the underlying presenter
 * 
 * @author Gero
 */
public interface IConfigEditorViewObserver
{
	/**
	 * @param configKey The key the config is identified by
	 * @return Whether all save-actions were successful
	 */
	boolean onSavePressed(String configKey);
	
	
	/**
	 * The user wants to apply his config-changes made in the editor to the model
	 * 
	 * @param configKey
	 */
	void onApplyPressed(String configKey);
	
	
	/**
	 * The user wants to apply his config-changes made in the editor to the model
	 * 
	 * @param configKey
	 */
	void onReloadPressed(String configKey);
}
