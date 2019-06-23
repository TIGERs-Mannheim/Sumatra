/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;


/**
 * This is a data holder between the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor} and
 * the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent}, which contains all data concerning
 * the current situation on the field.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * @author Gero
 * 
 */
@Entity
public class WorldFrame implements Serializable, IRecordWfFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= 6550048556640958060L;
	
	
	/** our enemies visible */
	public final BotIDMapConst<TrackedBot>			foeBots;
	
	/** tiger bots that were detected by the WorldPredictor */
	public final BotIDMapConst<TrackedTigerBot>	tigerBotsVisible;
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	public final IBotIDMap<TrackedTigerBot>		tigerBotsAvailable;
	
	/**  */
	public final TrackedBall							ball;
	
	/**  */
	public final long										time;
	
	/**  */
	public final Date										sytemTime;
	
	/**  */
	public final TeamProps								teamProps;
	
	/**  */
	public final FrameID									id;
	
	private float											wfFps					= 0;
	private float											camFps				= 0;
	
	private WorldFramePrediction						worldFramePrediction;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param foeBots
	 * @param tigerBotsAvailable
	 * @param tigerBotsVisible
	 * @param ball
	 * @param time
	 * @param frameNumber
	 * @param teamProps
	 * @param cameraId
	 */
	public WorldFrame(IBotIDMap<TrackedBot> foeBots, IBotIDMap<TrackedTigerBot> tigerBotsAvailable,
			IBotIDMap<TrackedTigerBot> tigerBotsVisible, TrackedBall ball, double time, long frameNumber,
			TeamProps teamProps, int cameraId)
	{
		this.ball = ball;
		this.time = ((long) (time / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)) + WPConfig.getFilterTimeOffset();
		sytemTime = new Date();
		this.teamProps = teamProps;
		id = new FrameID(cameraId, frameNumber);
		
		this.foeBots = BotIDMapConst.unmodifiableBotIDMap(foeBots);
		
		this.tigerBotsAvailable = tigerBotsAvailable;
		this.tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(tigerBotsVisible);
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus new collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public WorldFrame(WorldFrame original)
	{
		// Fields
		ball = original.ball;
		time = original.time;
		sytemTime = original.sytemTime;
		teamProps = original.teamProps;
		id = original.id;
		wfFps = original.wfFps;
		worldFramePrediction = original.worldFramePrediction;
		
		
		foeBots = BotIDMapConst.unmodifiableBotIDMap(original.foeBots);
		
		tigerBotsAvailable = original.tigerBotsAvailable;
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(original.tigerBotsVisible);
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder b = new StringBuilder();
		b.append("[WorldFrame, id = ").append(id).append("|\n");
		b.append("Ball: ").append(ball.getPos()).append("|\n");
		b.append("Tigers: ");
		for (final TrackedBot tiger : tigerBotsVisible.values())
		{
			b.append(tiger.getPos()).append(",");
		}
		b.append("|\n");
		b.append("Enemies: ");
		for (final TrackedBot bot : foeBots.values())
		{
			b.append(bot.getPos()).append(",");
		}
		b.append("]\n");
		return b.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Get {@link TrackedTigerBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return tiger {@link TrackedTigerBot}
	 */
	public TrackedTigerBot getTiger(BotID botId)
	{
		return tigerBotsVisible.get(botId);
	}
	
	
	/**
	 * Get foe {@link TrackedBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return foe {@link TrackedBot}
	 */
	public TrackedBot getFoeBot(BotID botId)
	{
		return foeBots.get(botId);
	}
	
	
	/**
	 * @return {@link Iterator} for foe bots map
	 */
	public Iterator<Entry<BotID, TrackedBot>> getFoeBotMapIterator()
	{
		return foeBots.entrySet().iterator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- modifier -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Set fps
	 * 
	 * @param fps
	 */
	public void setWfFps(float fps)
	{
		wfFps = fps;
	}
	
	
	/**
	 * @return the fps
	 */
	public float getWfFps()
	{
		return wfFps;
	}
	
	
	@Override
	public WorldFramePrediction getWorldFramePrediction()
	{
		return worldFramePrediction;
	}
	
	
	/**
	 * @param worldFramePrediction the worldFramePrediction to set
	 */
	public void setWorldFramePrediction(WorldFramePrediction worldFramePrediction)
	{
		this.worldFramePrediction = worldFramePrediction;
	}
	
	
	/**
	 * @return the camFps
	 */
	public final float getCamFps()
	{
		return camFps;
	}
	
	
	/**
	 * @param camFps the camFps to set
	 */
	public final void setCamFps(float camFps)
	{
		this.camFps = camFps;
	}
	
	
	@Override
	public BotIDMapConst<TrackedBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	@Override
	public BotIDMapConst<TrackedTigerBot> getTigerBotsVisible()
	{
		return tigerBotsVisible;
	}
	
	
	@Override
	public IBotIDMap<TrackedTigerBot> getTigerBotsAvailable()
	{
		return tigerBotsAvailable;
	}
	
	
	@Override
	public TrackedBall getBall()
	{
		return ball;
	}
	
	
	@Override
	public long getTime()
	{
		return time;
	}
	
	
	@Override
	public TeamProps getTeamProps()
	{
		return teamProps;
	}
	
	
	@Override
	public Date getSystemTime()
	{
		return sytemTime;
	}
}
