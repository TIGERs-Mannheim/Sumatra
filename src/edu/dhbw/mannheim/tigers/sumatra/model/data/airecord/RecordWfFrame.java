/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.airecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
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
 * Wrapper class for worldframe
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 8)
public class RecordWfFrame implements IRecordWfFrame
{
	/** our enemies visible */
	@NotNull
	@AssertValid
	private final BotIDMapConst<TrackedTigerBot>	foeBots;
	
	/** tiger bots that were detected by the WorldPredictor */
	@NotNull
	@AssertValid
	private final BotIDMapConst<TrackedTigerBot>	tigerBotsVisible;
	
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	@NotNull
	private final IBotIDMap<TrackedTigerBot>		tigerBotsAvailable;
	
	/** all bots, foes and tigers */
	@NotNull
	private final IBotIDMap<TrackedTigerBot>		bots;
	@NotNull
	private final TrackedBall							ball;
	@NotNull
	private final Date									systemTime;
	
	@NotNull
	private final ETeamColor							ownTeamColor;
	
	private final boolean								inverted;
	
	private transient WorldFramePrediction			worldFramePrediction	= null;
	
	private final List<MergedCamDetectionFrame>	camFrames;
	
	
	@SuppressWarnings("unused")
	private RecordWfFrame()
	{
		foeBots = null;
		tigerBotsAvailable = null;
		tigerBotsVisible = null;
		ball = null;
		systemTime = null;
		ownTeamColor = null;
		bots = new BotIDMap<TrackedTigerBot>();
		inverted = false;
		camFrames = new ArrayList<>();
	}
	
	
	/**
	 * @param wFrame
	 */
	public RecordWfFrame(final IRecordWfFrame wFrame)
	{
		foeBots = wFrame.getFoeBots();
		tigerBotsVisible = wFrame.getTigerBotsVisible();
		tigerBotsAvailable = wFrame.getTigerBotsAvailable();
		bots = wFrame.getBots();
		ball = wFrame.getBall();
		systemTime = wFrame.getSystemTime();
		ownTeamColor = wFrame.getTeamColor();
		worldFramePrediction = wFrame.getWorldFramePrediction();
		inverted = wFrame.isInverted();
		camFrames = wFrame.getCamFrames();
	}
	
	
	/**
	 * @return the foeBots
	 */
	@Override
	public final BotIDMapConst<TrackedTigerBot> getFoeBots()
	{
		return foeBots;
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
	 * @return the tigerBotsAvailable
	 */
	@Override
	public final IBotIDMap<TrackedTigerBot> getTigerBotsAvailable()
	{
		return tigerBotsAvailable;
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
	 * @return the systemTime
	 */
	@Override
	public final Date getSystemTime()
	{
		return new Date(systemTime.getTime());
	}
	
	
	@Override
	public final ETeamColor getTeamColor()
	{
		return ownTeamColor;
	}
	
	
	@Override
	public final IBotIDMap<TrackedTigerBot> getBots()
	{
		assureBotsFilled();
		return bots;
	}
	
	
	@Override
	public final TrackedTigerBot getBot(final BotID botId)
	{
		assureBotsFilled();
		return bots.getWithNull(botId);
	}
	
	
	/**
	 * As this is a record frame and earlier versions had not field bots,
	 * lets will the list, if it is empty, but there are bots.
	 */
	private void assureBotsFilled()
	{
		if (bots.isEmpty() && !(tigerBotsVisible.isEmpty() && foeBots.isEmpty()))
		{
			bots.putAll(tigerBotsVisible);
			bots.putAll(foeBots);
		}
	}
	
	
	@Override
	public WorldFramePrediction getWorldFramePrediction()
	{
		if (worldFramePrediction == null)
		{
			worldFramePrediction = new FieldPredictor(bots.values(), ball).create();
		}
		return worldFramePrediction;
	}
	
	
	@Override
	public boolean isInverted()
	{
		return inverted;
	}
	
	
	/**
	 * FrameId is not recorded
	 * 
	 * @return the id
	 */
	@Override
	public final long getId()
	{
		return 0;
	}
	
	
	@Override
	public List<MergedCamDetectionFrame> getCamFrames()
	{
		return camFrames;
	}
	
	
	/**
	 * @param worldFramePrediction the worldFramePrediction to set
	 */
	public final void setWorldFramePrediction(final WorldFramePrediction worldFramePrediction)
	{
		this.worldFramePrediction = worldFramePrediction;
	}
}
