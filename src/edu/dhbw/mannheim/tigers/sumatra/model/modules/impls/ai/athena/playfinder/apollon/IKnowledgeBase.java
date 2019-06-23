/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * A KnowledgeBase stores the result of plays together with the field situation, when it was chosen.
 * It can recommend a play according to the current field situation.
 * 
 * @author dirk
 * 
 */
public interface IKnowledgeBase
{
	
	/**
	 * This methods returns a ComparisonResult for each play given by {@code plays}.<br>
	 * It will compare {@code numFieldsToCompare} fields for each play with the {@code currentField}.
	 * 
	 * @param plays plays to look up in KnowledgeBase
	 * @param currentField the KnowledgeField that should be compared with those in the KnowledgeBase
	 * @param numFieldsToCompare How many fields should be compared for each play
	 * @return a list of ComparisonResults, for each play one ComparisonResult
	 * @see IKnowledgeBase#findNextFailedPlaysSorted(List, AKnowledgeField, int)
	 */
	List<IComparisonResult> findNextSuccessfulPlaysSorted(List<EPlay> plays, AKnowledgeField currentField,
			int numFieldsToCompare);
	
	
	/**
	 * This methods returns a ComparisonResult for each play given by {@code plays}.<br>
	 * It will compare {@code numFieldsToCompare} fields for each play with the {@code currentField}.
	 * 
	 * @param plays plays to look up in KnowledgeBase
	 * @param currentField the KnowledgeField that should be compared with those in the KnowledgeBase
	 * @param numFieldsToCompare How many fields should be compared for each play
	 * @return a list of ComparisonResults, for each play one ComparisonResult
	 * @see IKnowledgeBase#findNextSuccessfulPlaysSorted(List, AKnowledgeField, int)
	 */
	List<IComparisonResult> findNextFailedPlaysSorted(List<EPlay> plays, AKnowledgeField currentField,
			int numFieldsToCompare);
	
	
	/**
	 * Reset all counters within the knowledgeFields
	 * You should do this after you finished your query to the knowledgeBase,
	 * so that the knowledgeBase can start from a new random position and will
	 * check all fields again.
	 */
	void resetCounters();
	
	
	/**
	 * Add a new entry into knowledgeBase
	 * 
	 * @param knowledgePlay
	 */
	void addKnowledgePlay(KnowledgePlay knowledgePlay);
	
	
	/**
	 * Getter for the name of the knowledge Base for DB.
	 * 
	 * @return
	 */
	String getName();
	
	
	/**
	 * 
	 * Setter for the name of the knowledge Base for DB.
	 * 
	 * @param name
	 */
	void setName(String name);
	
	
	/**
	 * Setter for the version of a knowledgeBase.
	 * Version is used for concurrency checking in DB.
	 * 
	 * @param ver
	 */
	void setVersion(long ver);
	
	
	/**
	 * Getter for the version of a knowledgeBase.
	 * Version is used for concurrency checking in DB.
	 * @return
	 * 
	 */
	long getVersion();
	
	
	/**
	 * @return
	 */
	List<KnowledgePlay> getKnowledgePlays();
	
	
	/**
	 * Do what ever has to be initialized after loading from database
	 */
	void initialize();
	
	
	/**
	 * @return statistics about current knowledgebase
	 */
	KbStatistics getKbStatistics();
}
