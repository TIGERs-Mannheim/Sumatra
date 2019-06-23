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

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
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
@Entity
public class RecordWfFrame implements IRecordWfFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** our enemies visible */
	private BotIDMapConst<TrackedBot>		foeBots;
	/** tiger bots that were detected by the WorldPredictor */
	private BotIDMapConst<TrackedTigerBot>	tigerBotsVisible;
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	private IBotIDMap<TrackedTigerBot>		tigerBotsAvailable;
	/**  */
	private TrackedBall							ball;
	/**  */
	private long									time;
	/**  */
	private Date									systemTime;
	/**  */
	private TeamProps								teamProps;
	
	private WorldFramePrediction				worldPrediction;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param wFrame
	 */
	public RecordWfFrame(WorldFrame wFrame)
	{
		foeBots = wFrame.foeBots;
		tigerBotsVisible = wFrame.tigerBotsVisible;
		tigerBotsAvailable = wFrame.tigerBotsAvailable;
		ball = wFrame.ball;
		time = wFrame.time;
		setSystemTime(new Date());
		teamProps = wFrame.teamProps;
		worldPrediction = wFrame.getWorldFramePrediction();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the foeBots
	 */
	@Override
	public final BotIDMapConst<TrackedBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	/**
	 * @param foeBots the foeBots to set
	 */
	public final void setFoeBots(BotIDMapConst<TrackedBot> foeBots)
	{
		this.foeBots = foeBots;
	}
	
	
	/**
	 * @return the tigerBotsVisible
	 */
	@Override
	public final BotIDMapConst<TrackedTigerBot> getTigerBotsVisible()
	{
		return tigerBotsVisible;
	}
	
	
	/**
	 * @param tigerBotsVisible the tigerBotsVisible to set
	 */
	public final void setTigerBotsVisible(BotIDMapConst<TrackedTigerBot> tigerBotsVisible)
	{
		this.tigerBotsVisible = tigerBotsVisible;
	}
	
	
	/**
	 * @return the tigerBotsAvailable
	 */
	@Override
	public final IBotIDMap<TrackedTigerBot> getTigerBotsAvailable()
	{
		return tigerBotsAvailable;
	}
	
	
	/**
	 * @param tigerBotsAvailable the tigerBotsAvailable to set
	 */
	public final void setTigerBotsAvailable(IBotIDMap<TrackedTigerBot> tigerBotsAvailable)
	{
		this.tigerBotsAvailable = tigerBotsAvailable;
	}
	
	
	/**
	 * @return the ball
	 */
	@Override
	public final TrackedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @param ball the ball to set
	 */
	public final void setBall(TrackedBall ball)
	{
		this.ball = ball;
	}
	
	
	/**
	 * @return the time
	 */
	@Override
	public final long getTime()
	{
		return time;
	}
	
	
	/**
	 * @param time the time to set
	 */
	public final void setTime(long time)
	{
		this.time = time;
	}
	
	
	/**
	 * @return the teamProps
	 */
	@Override
	public final TeamProps getTeamProps()
	{
		return teamProps;
	}
	
	
	/**
	 * @param teamProps the teamProps to set
	 */
	public final void setTeamProps(TeamProps teamProps)
	{
		this.teamProps = teamProps;
	}
	
	
	/**
	 * @return the systemTime
	 */
	@Override
	public final Date getSystemTime()
	{
		return systemTime;
	}
	
	
	/**
	 * @param systemTime the systemTime to set
	 */
	public final void setSystemTime(Date systemTime)
	{
		this.systemTime = systemTime;
	}
	
	
	@Override
	public WorldFramePrediction getWorldFramePrediction()
	{
		return worldPrediction;
	}
}
