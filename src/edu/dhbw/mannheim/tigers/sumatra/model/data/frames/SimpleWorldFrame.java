/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
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
	private final IBotIDMap<TrackedTigerBot>	bots;
	private final TrackedBall						ball;
	private final Date								systemTime;
	private final long								id;
	private final WorldFramePrediction			worldFramePrediction;
	
	private float										wfFps			= 0;
	private float										camFps		= 0;
	private float										camInFps		= 0;
	
	private List<MergedCamDetectionFrame>		camFrames	= new ArrayList<>();
	
	
	/**
	 * @param bots
	 * @param ball
	 * @param frameNumber
	 * @param wfp
	 */
	public SimpleWorldFrame(final IBotIDMap<TrackedTigerBot> bots, final TrackedBall ball, final long frameNumber,
			final WorldFramePrediction wfp)
	{
		this.ball = ball;
		systemTime = new Date();
		id = frameNumber;
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
		systemTime = swf.systemTime;
		id = swf.id;
		bots = swf.bots;
		worldFramePrediction = swf.worldFramePrediction;
		wfFps = swf.wfFps;
		camFps = swf.camFps;
		camFrames = swf.camFrames;
	}
	
	
	/**
	 * Copy all data from IRecordWfFrame to a new SimpleWorldFrame.
	 * Some data in SimpleWorldFrame will be uninitialized!
	 * 
	 * @param rwf
	 */
	public SimpleWorldFrame(final IRecordWfFrame rwf)
	{
		ball = rwf.getBall();
		systemTime = rwf.getSystemTime();
		id = rwf.getId();
		bots = rwf.getBots();
		worldFramePrediction = rwf.getWorldFramePrediction();
		wfFps = 0;
		camFps = 0;
		camFrames = rwf.getCamFrames();
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
		TrackedBall mBall = new TrackedBall(getBall()).mirror();
		WorldFramePrediction wfp = worldFramePrediction.mirrorNew();
		SimpleWorldFrame frame = new SimpleWorldFrame(bots, mBall, id, wfp);
		frame.setCamFrames(getCamFrames());
		return frame;
	}
	
	
	/**
	 * Creates a new WorldFrame without bots
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	public static SimpleWorldFrame createEmptyWorldFrame(final long frameNumber)
	{
		final IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				true);
		
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), ball).create();
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, frameNumber, wfp);
		swf.setCamFps(0);
		swf.setWfFps(0);
		return swf;
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
	 * @return the bot or null
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
	 * @return the sytemTime
	 */
	public final Date getSystemTime()
	{
		return new Date(systemTime.getTime());
	}
	
	
	/**
	 * @return the id
	 */
	public final long getId()
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
	
	
	/**
	 * @param fps
	 */
	public void setCamInFps(final float fps)
	{
		camInFps = fps;
	}
	
	
	/**
	 * @return the camInFps
	 */
	public float getCamInFps()
	{
		return camInFps;
	}
	
	
	/**
	 * @return the camFrame
	 */
	public final List<MergedCamDetectionFrame> getCamFrames()
	{
		return camFrames;
	}
	
	
	/**
	 * @param camFrames the camFrame to set
	 */
	public final void setCamFrames(final List<MergedCamDetectionFrame> camFrames)
	{
		this.camFrames = camFrames;
	}
}
