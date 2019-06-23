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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.Iterator;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.FilterSelector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.TigersMotionModel;


/**
 * This class is responsible for the actuality of all objects in the {@link PredictionContext} (
 * {@link PredictionContext#ball}, {@link PredictionContext#blueBots} and {@link PredictionContext#yellowBots}), and
 * handles
 * new objects on the field ({@link PredictionContext#newBall}, {@link PredictionContext#newBlueBots} and
 * {@link PredictionContext#newYellowBots})
 */
public class TrackingManager
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// private Logger log = Logger.getLogger(this.getClass().getName());
	
	private final PredictionContext	context;
	private double							latestCaptureTimestamp;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param context
	 */
	public TrackingManager(PredictionContext context)
	{
		this.context = context;
		latestCaptureTimestamp = Double.MIN_VALUE;
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 *
	 */
	public void checkItems()
	{
		
		latestCaptureTimestamp = context.getLatestCaptureTimestamp();
		
		// --- 1. new objects on the field? ---
		checkNewBots(context.newYellowBots, context.yellowBots, WPConfig.YELLOW_ID_OFFSET);
		checkNewBots(context.newBlueBots, context.blueBots, WPConfig.BLUE_ID_OFFSET);
		checkNewBalls();
		
		// --- 2. objects disappeared from field? ---
		checkBots();
		checkBall();
	}
	
	
	/**
	 * @param contextNewRobot
	 * @param contextRobots
	 * @param offsetId WPConfig.YELLOW_ID_OFFSET
	 */
	private void checkNewBots(Map<Integer, UnregisteredBot> contextNewRobot, Map<Integer, IFilter> contextRobots,
			int offsetId)
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
			if ((newBot.oldTimestamp <= (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT))
					&& (newBot.count >= WPConfig.ADD_MIN_FRAMES_BOTS))
			{
				// --- new bots with sufficient certainty detected ---
				// --- add him to tigers-list of the context and remove it form newTigers-list ---
				// --- after that, continue with next new bots ---
				
				final IFilter bot = FilterSelector.getTigerFilter();
				final IMotionModel motionModel = new TigersMotionModel();
				bot.init(motionModel, context, newBot.newTimestamp, newBot.visionBot);
				contextRobots.put(newBot.visionBot.id + offsetId, bot);
				
				it.remove();
				continue;
			}
			
			// --- false detection of bot? ---
			// --- is the last detection/sighting of the bot older than the allowed maximum time? ---
			
			if (newBot.newTimestamp < (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT))
			{
				// --- the detection could not be verified ---
				// --- remove this thing from our newTigers-list ---
				it.remove();
			}
		}// --- end iteration on new tigerbots on the field ---
	}
	
	
	private void checkNewBalls()
	{
		// log.debug("Checking new balls");
		// synchronized (context.newBalls)
		// {
		// --- ATTENTION: we handle only one ball in the following! tracking of more than one ball is not supported
		// yet. ---
		// --- this method works only correct, if there is only a ball with id 0 in newBalls list ---
		// --- balls with other ids are not supported yet ---
		
		// ISortedBuffer<CamInfoBall> newBall;
		UnregisteredBall newBall;
		
		// --- get new ball with id 0 ---
		// --- in general, there should be none available, so we jump to the catch block and are done ---
		newBall = context.newBall;
		if (newBall == null)
		{
			return;
		}
		
		
		// --- there is a new ball with id 0 on the field ---
		
		// --- consistent detection of ball? ---
		// --- is the oldest required detection/sighting of the ball newer than the allowed maximum time? ---
		// --- in general, we should have enough detections ---
		// CamInfoBall oldestSighting = newBall.peekLast(); // As FIFO may get get smaller we have to check for null!
		
		if ((newBall.oldTimestamp <= (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BALL))
				&& (newBall.count >= WPConfig.ADD_MIN_FRAMES_BALL))
		{
			// --- new ball with sufficient certainty detected ---
			// --- add him to balls-list of the context and remove it form newBalls-list ---
			// --- after that, we're done ---
			
			// IBallModule ball = ModuleSelector.getBallModule();
			final IFilter ball = FilterSelector.getBallFilter();
			final IMotionModel motionModel = new BallMotionModel();
			ball.init(motionModel, context, newBall.newTimestamp, newBall.visionBall);
			// ball.init(context, newBall.newTimestamp, newBall.visionBall);
			context.ball = ball;
			context.newBall = null;
			// log.debug("Ball 0 added.");
			return;
		}
		
		// --- false detection of ball? ---
		// --- is the last detection/sighting of the ball older than the allowed maximum time? ---
		if (newBall.newTimestamp < (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT))
		{
			// --- the detection could not be verified ---
			// --- remove this thing from our newBalls-list ---
			context.newBall = null;
		}
		// }// --- end synchronize context.newBalls ---
	}
	
	
	private void checkBots()
	{
		checkBots(context.yellowBots.values().iterator());
		checkBots(context.blueBots.values().iterator());
	}
	
	
	private void checkBots(Iterator<IFilter> it)
	{
		while (it.hasNext())
		{
			final IFilter filterBot = it.next();
			// --- no consistent detection of tigerbot anymore? ---
			// --- is the last detection of the bot older than the allowed maximum time? ---
			if (filterBot.getTimestamp() <= (latestCaptureTimestamp - WPConfig.REM_MAX_TIME_BOT))
			{
				// --- the bot was removed from the field ---
				// --- so remove this bot from our tigers-list ---
				it.remove();
			}
		}
	}
	
	
	private void checkBall()
	{
		// --- if ball is empty because we just started to track ball
		if (context.ball == null)
		{
			return;
		}
		
		// --- in general there are balls on the field ---
		// --- so iterate through all balls ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		final IFilter ball = context.ball;
		
		// --- no consistent detection of ball anymore? ---
		// --- is the last detection of the ball older than the allowed maximum time? ---
		if (ball.getTimestamp() <= (latestCaptureTimestamp - WPConfig.REM_MAX_TIME_BALL))
		{
			// we hold the last known position
			context.ball.keepPositionAliveOnNoObservation();
			
			// --- the ball was removed from the field ---
			// --- so remove this ball from our balls-list ---
			// context.ball = null;
		}
	}
}
