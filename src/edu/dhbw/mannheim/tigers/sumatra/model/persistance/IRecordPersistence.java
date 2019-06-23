/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;


/**
 * Interface for saving and loading record data
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IRecordPersistence
{
	/**
	 * @return
	 */
	List<IRecordFrame> load();
	
	
	/**
	 * @return
	 */
	List<BerkeleyLogEvent> loadLogEvents();
	
	
	/**
	 * Load a set of record frames
	 * 
	 * @param startIndex
	 * @param length
	 * @return
	 */
	List<IRecordFrame> load(int startIndex, int length);
	
	
	/**
	 * Number of frames in the datastore
	 * 
	 * @return
	 */
	int size();
	
	
	/**
	 * @param recordFrames
	 */
	void saveFrames(List<IRecordFrame> recordFrames);
	
	
	/**
	 * Close the underlying datastore
	 */
	void close();
	
	
	/**
	 * @param logEvents
	 */
	void saveLogEvent(List<BerkeleyLogEvent> logEvents);
}
