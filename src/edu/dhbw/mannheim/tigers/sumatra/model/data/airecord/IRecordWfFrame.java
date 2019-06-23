/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import java.util.Date;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;


/**
 * Interface for worldframe
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IRecordWfFrame
{
	
	/**
	 * @return the foeBots
	 */
	BotIDMapConst<TrackedTigerBot> getFoeBots();
	
	
	/**
	 * @return the tigerBotsVisible
	 */
	BotIDMapConst<TrackedTigerBot> getTigerBotsVisible();
	
	
	/**
	 * @return the tigerBotsAvailable
	 */
	IBotIDMap<TrackedTigerBot> getTigerBotsAvailable();
	
	
	/**
	 * @return the blueBots
	 */
	IBotIDMap<TrackedTigerBot> getBots();
	
	
	/**
	 * @param botId
	 * @return the blueBots
	 */
	TrackedTigerBot getBot(BotID botId);
	
	
	/**
	 * @return the ball
	 */
	TrackedBall getBall();
	
	
	/**
	 * @return the SystemTime
	 */
	Date getSystemTime();
	
	
	/**
	 * @return
	 */
	ETeamColor getTeamColor();
	
	
	/**
	 * @return
	 */
	boolean isInverted();
	
	
	/**
	 * @return
	 */
	WorldFramePrediction getWorldFramePrediction();
	
	
	/**
	 * @return
	 */
	long getId();
	
	
	/**
	 * @return
	 */
	List<MergedCamDetectionFrame> getCamFrames();
}