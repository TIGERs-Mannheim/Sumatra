/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import java.util.Date;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.WorldFramePrediction;


/**
 * TODO Nicolai Ommer <nicolai.ommer@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IRecordWfFrame
{
	
	/**
	 * @return the foeBots
	 */
	public abstract BotIDMapConst<TrackedBot> getFoeBots();
	
	
	/**
	 * @return the tigerBotsVisible
	 */
	public abstract BotIDMapConst<TrackedTigerBot> getTigerBotsVisible();
	
	
	/**
	 * @return the tigerBotsAvailable
	 */
	public abstract IBotIDMap<TrackedTigerBot> getTigerBotsAvailable();
	
	
	/**
	 * @return the ball
	 */
	public abstract TrackedBall getBall();
	
	
	/**
	 * @return the time
	 */
	public abstract long getTime();
	
	
	/**
	 * @return the SystemTime
	 */
	public abstract Date getSystemTime();
	
	
	/**
	 * @return the teamProps
	 */
	public abstract TeamProps getTeamProps();
	
	
	/**
	 * @return the worldFramePrediction
	 */
	public WorldFramePrediction getWorldFramePrediction();
	
}