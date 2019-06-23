/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 24, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;


/**
 * Interface for different persistence frameworks
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IKnowledgeBasePersistence
{
	/**
	 * Save a knowldegeBase to the DataBase
	 * @param kb
	 * @param merge
	 */
	void saveKnowledgeBase(IKnowledgeBase kb, boolean merge);
	
	
	/**
	 * Loads a knowledge base from DB, if this not exists creates a new GeneralKnowledgeBase
	 * @param name
	 * @return
	 */
	IKnowledgeBase loadKnowledgeBase(String name);
	
	
	/**
	 * called for closing the data connection
	 */
	void close();
	
	
	/**
	 * Opens database connection
	 */
	void open();
	
	
	/**
	 * Is connection open?
	 * @return
	 */
	boolean isOpen();
	
}
