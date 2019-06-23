/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.IPlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;


/**
 * Interface for a record frame
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IRecordFrame
{
	
	/**
	 * @return the worldFrame
	 */
	IRecordWfFrame getWorldFrame();
	
	
	/**
	 * @return the refereeMsg
	 */
	RefereeMsg getLatestRefereeMsg();
	
	
	/**
	 * @return the tacticalInfo
	 */
	ITacticalField getTacticalField();
	
	
	/**
	 * @return the playStrategy
	 */
	IPlayStrategy getPlayStrategy();
	
	
	/**
	 * @return
	 */
	AresData getAresData();
	
	
	/**
	 * Can this implementation be stored in a file?
	 * 
	 * @return
	 */
	boolean isPersistable();
	
	
	/**
	 * Color of the Tigers bots
	 * 
	 * @return
	 */
	ETeamColor getTeamColor();
	
	
	/**
	 * 
	 */
	void cleanUp();
}