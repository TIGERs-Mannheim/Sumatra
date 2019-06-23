/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.util.Date;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;


/**
 * This is a data holder between the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor} and
 * the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent}, which contains all data concerning
 * the current situation on the field.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
public class SimpleWorldFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IBotIDMap<TrackedTigerBot>	bots;
	
	/**  */
	public final TrackedBall						ball;
	
	/**  */
	private final long								time;
	
	/**  */
	private final Date								systemTime;
	
	/**  */
	private final FrameID							id;
	
	private final WorldFramePrediction			worldFramePrediction;
	
	private float										wfFps		= 0;
	private float										camFps	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bots
	 * @param ball
	 * @param time
	 * @param frameNumber
	 * @param cameraId
	 * @param wfp
	 */
	public SimpleWorldFrame(final IBotIDMap<TrackedTigerBot> bots, final TrackedBall ball, final double time,
			final long frameNumber,
			final int cameraId, final WorldFramePrediction wfp)
	{
		this.ball = ball;
		this.time = ((long) (time / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)) + WPConfig.getFilterTimeOffset();
		systemTime = new Date();
		id = new FrameID(cameraId, frameNumber);
		this.bots = BotIDMapConst.unmodifiableBotIDMap(bots);
		worldFramePrediction = wfp;
	}
	
	
	/**
	 * Soft copy
	 * 
	 * @param swf
	 */
	public SimpleWorldFrame(final SimpleWorldFrame swf)
	{
		ball = swf.getBall();
		time = swf.time;
		systemTime = swf.systemTime;
		id = swf.id;
		bots = swf.bots;
		worldFramePrediction = swf.worldFramePrediction;
		wfFps = swf.wfFps;
		camFps = swf.camFps;
	}
	
	
	/**
	 * Create a new instance of this SimpleWorldFrame and mirror bots and ball
	 * 
	 * @return
	 */
	public SimpleWorldFrame mirrorNew()
	{
		IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		for (TrackedTigerBot bot : this.bots.values())
		{
			TrackedTigerBot mBot = new TrackedTigerBot(bot);
			mBot.mirrorBot();
			bots.put(bot.getId(), mBot);
		}
		TrackedBall mBall = new TrackedBall(ball);
		mBall.mirror();
		WorldFramePrediction wfp = worldFramePrediction.mirrorNew();
		SimpleWorldFrame frame = new SimpleWorldFrame(bots, mBall, time, id.getFrameNumber(), id.getCam(), wfp);
		return frame;
	}
	
	
	/**
	 * @return all bots
	 */
	public final IBotIDMap<TrackedTigerBot> getBots()
	{
		return bots;
	}
	
	
	/**
	 * @param botId
	 * @return the bot
	 */
	public final TrackedTigerBot getBot(final BotID botId)
	{
		return bots.getWithNull(botId);
	}
	
	
	/**
	 * @return the ball
	 */
	public final TrackedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @return the time
	 */
	public final long getTime()
	{
		return time;
	}
	
	
	/**
	 * @return the sytemTime
	 */
	public final Date getSystemTime()
	{
		return systemTime;
	}
	
	
	/**
	 * @return the id
	 */
	public final FrameID getId()
	{
		return id;
	}
	
	
	/**
	 * @return the wfFps
	 */
	public final float getWfFps()
	{
		return wfFps;
	}
	
	
	/**
	 * @param wfFps the wfFps to set
	 */
	public final void setWfFps(final float wfFps)
	{
		this.wfFps = wfFps;
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
	public final void setCamFps(final float camFps)
	{
		this.camFps = camFps;
	}
	
	
	/**
	 * @return the worldFramePrediction
	 */
	public final WorldFramePrediction getWorldFramePrediction()
	{
		return worldFramePrediction;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
