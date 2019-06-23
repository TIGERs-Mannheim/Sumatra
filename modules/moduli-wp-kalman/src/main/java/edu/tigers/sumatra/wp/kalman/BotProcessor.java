/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.kalman.data.ABotMotionResult;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.UnregisteredBot;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 * Prepares all new data from the incoming
 * {@link edu.tigers.sumatra.cam.data.CamDetectionFrame} concerning the bots (enemies and
 * tigers), and add
 * it to the {@link PredictionContext} if necessary
 */
public class BotProcessor
{
	
	private final PredictionContext		context;
	
	private final Map<BotID, CamRobot>	lastDetections	= new HashMap<>();
	
	
	/**
	 * @param context
	 */
	public BotProcessor(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param camYellowBots
	 * @param camBlueBots
	 */
	public void process(final List<CamRobot> camYellowBots, final List<CamRobot> camBlueBots)
	{
		// ---Check oldTigers list and update each founded element
		// ---if incoming CamRobos not known, check list of newTigers
		// ---if found in newTigers refresh element
		// ---if not found add to list
		
		for (final CamRobot visionBotCam : camYellowBots)
		{
			final int botID = visionBotCam.getRobotID() + WPConfig.YELLOW_ID_OFFSET;
			
			CamRobot last = lastDetections.get(visionBotCam.getBotId());
			if ((last != null) && (last.getCameraId() != visionBotCam.getCameraId()))
			{
				if ((visionBotCam.getTimestamp()) < (last.getTimestamp() + 1e8))
				{
					continue;
				}
			}
			final IFilter existingBot = context.getYellowBots().get(botID);
			processBot(botID, existingBot, visionBotCam, last, context.getNewYellowBots(), ETeamColor.YELLOW);
			lastDetections.put(visionBotCam.getBotId(), visionBotCam);
		}
		
		// --- same for blue ~~~~
		for (final CamRobot visionBotCam : camBlueBots)
		{
			final int botID = visionBotCam.getRobotID() + WPConfig.BLUE_ID_OFFSET;
			
			CamRobot last = lastDetections.get(visionBotCam.getBotId());
			if ((last != null) && (last.getCameraId() != visionBotCam.getCameraId()))
			{
				if ((visionBotCam.getTimestamp()) < (last.getTimestamp() + 1e8))
				{
					continue;
				}
			}
			final IFilter existingBot = context.getBlueBots().get(botID);
			processBot(botID, existingBot, visionBotCam, last, context.getNewBlueBots(), ETeamColor.BLUE);
			lastDetections.put(visionBotCam.getBotId(), visionBotCam);
		}
	}
	
	
	private void processBot(final int botID, final IFilter existingBot, final CamRobot visionBotCam,
			final CamRobot lastVisionBotCam,
			final Map<Integer, UnregisteredBot> contextNewBots, final ETeamColor color)
	{
		final WPCamBot visionBot = new WPCamBot(visionBotCam);
		if (existingBot != null)
		{
			// drop doubled observation if bot is in overlap area of cameras
			final double dt = (visionBotCam.getTimestamp() - existingBot.getTimestamp())
					* WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;
			if (dt <= WPConfig.MIN_CAMFRAME_DELAY_TIME)
			{
				return;
			}
			
			final ABotMotionResult oldState = (ABotMotionResult) existingBot
					.getPrediction(existingBot.getTimestamp());
			
			existingBot.observation(visionBotCam.getTimestamp(), visionBot);
			existingBot.getMotion().estimateControl(existingBot, oldState, visionBotCam, lastVisionBotCam, dt);
			return;
		}
		
		UnregisteredBot newBot = contextNewBots.get(botID);
		if (newBot != null)
		{
			newBot.addBot(visionBotCam.getTimestamp(), visionBot);
		} else
		{
			newBot = new UnregisteredBot(visionBotCam.getTimestamp(), visionBot);
			contextNewBots.put(botID, newBot);
		}
	}
}
