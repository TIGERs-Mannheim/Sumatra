/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 30, 2012
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;


/**
 * Encapsulates the capability of handling {@link ApollonControl}-objects.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IApollonControlHandler
{
	/**
	 * Passes the control-object with new instructions
	 * 
	 * @param newControl
	 */
	void onNewApollonControl(ApollonControl newControl);
	
	
	/**
	 * Save KnowledgeBase
	 */
	void onSaveKnowledgeBase();
}