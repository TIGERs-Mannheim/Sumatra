/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 30, 2012
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

/**
 * Interface for Apollon Control Panel
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public interface IApollonControlPanelObserver
{
	/**
	 * Invoked when the knowledge base name is changed
	 * @param newName
	 */
	void onKnowledgeBaseNameChanged(String newName);
	
	
	/**
	 * Invoked when the acceptable match is changed
	 * @param newAccMatch
	 */
	void onAcceptableMatchChanged(int newAccMatch);
	
	
	/**
	 * Invoked when Database Path is changed
	 * @param newPath
	 */
	void onDatabasePathChanged(String newPath);
	
	
	/**
	 * Invoked when persist strategy is changed. Possibilities are "try to merge" or always override.
	 * @param merge When true it will try to merge.
	 */
	void onPersistStrategyChanged(boolean merge);
	
	
	/**
	 * Invoked when Save Knowledge Base on CLose is changed.
	 * @param saveOnClose
	 */
	void onSaveOnCloseChanged(boolean saveOnClose);
	
	
	/**
	 * Saves Knowledgebase
	 */
	void onSaveKbNow();
	
	
	/**
	 * Clean Knowledgebase
	 */
	void onCleanKbNow();
}
