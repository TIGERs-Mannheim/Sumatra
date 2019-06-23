/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.util.Collection;
import java.util.List;


/**
 * Interface for saving and loading record data
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IRecordPersistence extends IFrameByTimestampPersistence, IPersistence
{
	/**
	 * @return
	 */
	List<BerkeleyLogEvent> loadLogEvents();
	
	
	/**
	 * @param logEvents
	 */
	void saveLogEvent(List<BerkeleyLogEvent> logEvents);
	
	
	/**
	 * Get a single frame by time
	 * 
	 * @param tCur
	 * @return
	 */
	RecordFrame getRecordFrame(final long tCur);
	
	
	/**
	 * @param tCur
	 * @return
	 */
	RecordCamFrame getCamFrame(final long tCur);
	
	
	/**
	 * @param recordFrames
	 */
	void saveRecordFrames(Collection<RecordFrame> recordFrames);
	
	
	/**
	 * @param camFrames
	 */
	void saveCamFrames(Collection<RecordCamFrame> camFrames);
}
