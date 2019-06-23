/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AICom;
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
	 * @return
	 */
	RefereeMsg getNewRefereeMsg();
	
	
	/**
	 * @return the tacticalInfo
	 */
	default ITacticalField getTacticalField()
	{
		throw new IllegalStateException("No tactical field available.");
	}
	
	
	/**
	 * @return the playStrategy
	 */
	default IPlayStrategy getPlayStrategy()
	{
		throw new IllegalStateException("No play strategy available.");
	}
	
	
	/**
	 * @return
	 */
	default AresData getAresData()
	{
		throw new IllegalStateException("No ares data available.");
	}
	
	
	/**
	 * Can this implementation be stored in a file?
	 * 
	 * @return
	 */
	default boolean isPersistable()
	{
		return false;
	}
	
	
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
	
	
	/**
	 * @return
	 */
	float getFps();
	
	
	/**
	 * @param id
	 */
	void setId(int id);
	
	
	/**
	 * @return
	 */
	AICom getAICom();
}
