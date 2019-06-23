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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.UnregisteredBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.FilterSelector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.IMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.TigersMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This class is responsible for the actuality of all objects in the {@link PredictionContext} (
 * {@link PredictionContext#ball}, {@link PredictionContext#blueBots} and {@link PredictionContext#yellowBots}), and
 * handles
 * new objects on the field ({@link PredictionContext#newBlueBots} and {@link PredictionContext#newYellowBots})
 */
public class TrackingManager
{
	@SuppressWarnings("unused")
	private static final Logger		log	= Logger.getLogger(TrackingManager.class.getName());
	
	private final PredictionContext	context;
	
	
	/**
	 * @param context
	 */
	public TrackingManager(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 *
	 */
	public void checkItems()
	{
		// --- 1. new objects on the field? ---
		checkNewBots(context.getNewYellowBots(), context.getYellowBots(), WPConfig.YELLOW_ID_OFFSET);
		checkNewBots(context.getNewBlueBots(), context.getBlueBots(), WPConfig.BLUE_ID_OFFSET);
		
		// --- 2. objects disappeared from field? ---
		checkBots();
	}
	
	
	/**
	 * @param contextNewRobot
	 * @param contextRobots
	 * @param offsetId WPConfig.YELLOW_ID_OFFSET
	 */
	private void checkNewBots(final Map<Integer, UnregisteredBot> contextNewRobot,
			final Map<Integer, IFilter> contextRobots,
			final int offsetId)
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
			if ((newBot.getOldTimestamp() <= (SumatraClock.nanoTime() - (WPConfig.ADD_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
					&& (newBot.getCount() >= WPConfig.ADD_MIN_FRAMES_BOTS))
			{
				// --- new bots with sufficient certainty detected ---
				// --- add him to tigers-list of the context and remove it form newTigers-list ---
				// --- after that, continue with next new bots ---
				
				final IFilter bot = FilterSelector.getTigerFilter();
				final IMotionModel motionModel = new TigersMotionModel();
				bot.init(motionModel, context, newBot.getNewTimestamp(), newBot.getVisionBot());
				contextRobots.put(newBot.getVisionBot().id + offsetId, bot);
				// System.out.println(newBot.getOldTimestamp());
				// System.out.println(System.nanoTime());
				// System.out.println();
				log.debug("New Bot detected: " + newBot.getVisionBot().id);
				it.remove();
				continue;
			}
			
			// --- false detection of bot? ---
			// --- is the last detection/sighting of the bot older than the allowed maximum time? ---
			
			if (newBot.getNewTimestamp() < (SumatraClock.nanoTime() - (WPConfig.ADD_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
			{
				// --- the detection could not be verified ---
				// --- remove this thing from our newTigers-list ---
				it.remove();
			}
		}// --- end iteration on new tigerbots on the field ---
	}
	
	
	private void checkBots()
	{
		checkBots(context.getYellowBots().values().iterator());
		checkBots(context.getBlueBots().values().iterator());
	}
	
	
	private void checkBots(final Iterator<IFilter> it)
	{
		while (it.hasNext())
		{
			final IFilter filterBot = it.next();
			// --- no consistent detection of tigerbot anymore? ---
			// --- is the last detection of the bot older than the allowed maximum time? ---
			if (filterBot.getTimestamp() <= (SumatraClock.nanoTime() - (WPConfig.REM_MAX_TIME_BOT / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME)))
			{
				// --- the bot was removed from the field ---
				// --- so remove this bot from our tigers-list ---
				it.remove();
				log.debug("Bot " + filterBot.getId() + " vanished.");
			}
		}
	}
}
