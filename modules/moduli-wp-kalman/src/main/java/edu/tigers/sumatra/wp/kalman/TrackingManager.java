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

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.UnregisteredBot;
import edu.tigers.sumatra.wp.kalman.filter.ExtKalmanFilter;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;
import edu.tigers.sumatra.wp.kalman.motionModels.IMotionModel;
import edu.tigers.sumatra.wp.kalman.motionModels.OmniBot_V3;


/**
 * This class is responsible for the actuality of all objects in the {@link PredictionContext} (
 * {@link PredictionContext#ball}, {@link PredictionContext#blueBots} and {@link PredictionContext#yellowBots}), and
 * handles
 * new objects on the field ({@link PredictionContext#newBlueBots} and {@link PredictionContext#newYellowBots})
 */
public class TrackingManager
{
	@SuppressWarnings("unused")
	private static final Logger		log				= Logger.getLogger(TrackingManager.class.getName());
																	
	private final PredictionContext	context;
	private boolean						firstIteration	= true;
	private boolean						botsSeen			= false;
																	
																	
	/**
	 * @param context
	 */
	public TrackingManager(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param timestamp
	 */
	public void checkItems(final long timestamp)
	{
		// --- 1. new objects on the field? ---
		checkNewBots(context.getNewYellowBots(), context.getYellowBots(), WPConfig.YELLOW_ID_OFFSET, timestamp);
		checkNewBots(context.getNewBlueBots(), context.getBlueBots(), WPConfig.BLUE_ID_OFFSET, timestamp);
		
		// --- 2. objects disappeared from field? ---
		checkBots(timestamp);
		
		firstIteration = !botsSeen;
	}
	
	
	/**
	 * @param contextNewRobot
	 * @param contextRobots
	 * @param offsetId WPConfig.YELLOW_ID_OFFSET
	 * @param timestamp
	 */
	private void checkNewBots(final Map<Integer, UnregisteredBot> contextNewRobot,
			final Map<Integer, IFilter> contextRobots,
			final int offsetId, final long timestamp)
	{
		// --- check, if there are any new bots detections at all ---
		// --- in general, there should be no change of the bots on the field ---
		if (contextNewRobot.isEmpty())
		{
			return;
		}
		
		// --- there are possible new bots on the field ---
		// --- iterate through all detections of possible new bots ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		final Iterator<UnregisteredBot> it = contextNewRobot.values().iterator();
		while (it.hasNext())
		{
			final UnregisteredBot newBot = it.next();
			// --- consistent detection of bots? ---
			// --- is the oldest required detection/sighting of the bot newer than the allowed maximum time? ---
			// --- in general, we should have enough detections ---
			if (firstIteration
					|| ((newBot.getOldTimestamp() <= (timestamp
							- (WPConfig.ADD_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
							&& (newBot.getCount() >= WPConfig.ADD_MIN_FRAMES_BOTS)))
			{
				// --- new bots with sufficient certainty detected ---
				// --- add him to tigers-list of the context and remove it form newTigers-list ---
				// --- after that, continue with next new bots ---
				
				final IFilter bot = new ExtKalmanFilter();
				// final IMotionModel motionModel = new TigersMotionModel();
				final IMotionModel motionModel = new OmniBot_V3();
				bot.init(motionModel, context, newBot.getNewTimestamp(), newBot.getVisionBot());
				contextRobots.put(newBot.getVisionBot().id + offsetId, bot);
				log.debug("New Bot detected: " + newBot.getVisionBot().id);
				it.remove();
				botsSeen = true;
				continue;
			}
			
			// --- false detection of bot? ---
			// --- is the last detection/sighting of the bot older than the allowed maximum time? ---
			
			if (newBot.getNewTimestamp() < (timestamp
					- (WPConfig.ADD_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
			{
				// --- the detection could not be verified ---
				// --- remove this thing from our newTigers-list ---
				it.remove();
			}
		} // --- end iteration on new tigerbots on the field ---
	}
	
	
	private void checkBots(final long timestamp)
	{
		checkBots(context.getYellowBots().values().iterator(), timestamp);
		checkBots(context.getBlueBots().values().iterator(), timestamp);
	}
	
	
	private void checkBots(final Iterator<IFilter> it, final long timestamp)
	{
		while (it.hasNext())
		{
			final IFilter filterBot = it.next();
			// --- no consistent detection of tigerbot anymore? ---
			// --- is the last detection of the bot older than the allowed maximum time? ---
			if (filterBot.getTimestamp() <= (timestamp
					- (WPConfig.REM_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
			{
				// --- the bot was removed from the field ---
				// --- so remove this bot from our tigers-list ---
				it.remove();
				log.debug("Bot " + filterBot.getId() + " vanished.");
			}
		}
	}
	
	
	/**
	 * @param firstIteration the firstIteration to set
	 */
	public final void setFirstIteration(final boolean firstIteration)
	{
		this.firstIteration = firstIteration;
	}
}
