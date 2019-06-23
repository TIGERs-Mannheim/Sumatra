/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren Künemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.Iterator;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.FilterSelector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.BallMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.FoodMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.TigersMotionModel;


/**
 * This class is responsible for the actuality of all objects in the {@link PredictionContext} (
 * {@link PredictionContext#ball}, {@link PredictionContext#food} and {@link PredictionContext#tigers}), and handles
 * new objects on the field ({@link PredictionContext#newBall}, {@link PredictionContext#newFood} and
 * {@link PredictionContext#newTigers})
 */
public class TrackingManager
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// private Logger log = Logger.getLogger(this.getClass().getName());
	
	private PredictionContext	context;
	private double					latestCaptureTimestamp;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public TrackingManager(PredictionContext context)
	{
		this.context = context;
		latestCaptureTimestamp = Double.MIN_VALUE;
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void checkItems()
	{	
		
		latestCaptureTimestamp = context.getLatestCaptureTimestamp();
		
		// --- 1. new objects on the field? ---
		checkNewTigers();
		// if (i == 1)
		checkNewFood();
		// if (i == 2 )
		checkNewBalls();
		
		// --- 2. objects disappeared from field? ---
		// if (i == 3)
		checkTigers();
		// if (i == 4)
		checkFood();
		// if (i == 5)
		checkBall();
		// if (++i == 6)
		// i = 0;
	}
	

	private void checkNewTigers()
	{
		// log.debug("Checking new tigers");
		// synchronized (context.newTigers)
		// {
		// --- check, if there are any new tigerbot detections at all ---
		// --- in general, there should be no change of the tigerbots on the field ---
		if (context.newTigers.isEmpty())
		{
			// --- if there are no new tigerbots, we're done here ---
			return;
		}
		
		// --- there are possible new tigerbots on the field ---
		// --- iterate through all detections of possible new tigerbots ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		Iterator<UnregisteredBot> it = context.newTigers.values().iterator();
		while (it.hasNext())
		{
			UnregisteredBot newTiger = it.next();
			// --- consistent detection of tigerbot? ---
			// --- is the oldest required detection/sighting of the bot newer than the allowed maximum time? ---
			// --- in general, we should have enough detections ---
			if (newTiger.oldTimestamp <= (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT)
					&& newTiger.count >= WPConfig.ADD_MIN_FRAMES_BOTS)
			{
				// --- new tigerbot with sufficient certainty detected ---
				// --- add him to tigers-list of the context and remove it form newTigers-list ---
				// --- after that, continue with next new tigerbot ---
				// NOTE Changed newTiger.get(0) to position

				IFilter tiger = FilterSelector.getTigerFilter();
				IMotionModel motionModel = new TigersMotionModel();
				tiger.init(motionModel, context, newTiger.newTimestamp, newTiger.visionBot);
				synchronized (context.tigers)
				{
					context.tigers.put(newTiger.visionBot.id + WPConfig.TIGER_ID_OFFSET, tiger);
				}

				it.remove();
				// log.debug("Tiger " + new_tiger.get(0).id + " added.");
				continue;
			}
			
			// --- false detection of tigerbot? ---
			// --- is the last detection/sighting of the bot older than the allowed maximum time? ---
			
			if (newTiger.newTimestamp < (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT))
			{
				// --- the detection could not be verified ---
				// --- remove this thing from our newTigers-list ---
				it.remove();
			}
		}// --- end iteration on new tigerbots on the field ---
		// }// --- end synchronize context.newTigers ---
	}
	

	private void checkNewFood()
	{
		// --- check, if there are any new foodbot detections at all ---
		// --- in general, there should be no change of the foodbots on the field ---
		if (context.newFood.isEmpty())
		{
			// --- if there are no new foodbots, we're done here ---
			return;
		}
		
		// --- there are possible new foodbots on the field ---
		// --- iterate through all detections of possible new foodbots ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		Iterator<UnregisteredBot> it = context.newFood.values().iterator();
		while (it.hasNext())
		{
			UnregisteredBot newFood = it.next();
			// --- consistent detection of foodbot? ---
			// --- is the oldest required detection/sighting of the bot newer than the allowed maximum time? ---
			// --- in general, we should have enough detections ---
			if (newFood.oldTimestamp <= (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT)
					&& newFood.count >= WPConfig.ADD_MIN_FRAMES_BOTS)
			{
				// --- new foodbot with sufficient certainty detected ---
				// --- add him to food-list of the context and remove it form newFood-list ---
				// --- after that, continue with next new foodbot ---
				IFilter food = FilterSelector.getFoodFilter();
				IMotionModel motionModel = new FoodMotionModel();
				food.init(motionModel, context, newFood.newTimestamp, newFood.visionBot);
				synchronized (context.food)
				{
					context.food.put(newFood.visionBot.id + WPConfig.FOOD_ID_OFFSET, food);
				}
				
				it.remove();
				continue;
			}
			
			// --- false detection of foodbot? ---
			// --- is the last detection/sighting of the bot older than the allowed maximum time? ---
			if (newFood.newTimestamp < (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BOT))
			{
				// --- the detection could not be verified ---
				// --- remove this thing from our newFood-list ---
				it.remove();
			}
		}// --- end iteration on new foodbots on the field ---
		// }// --- end synchronize context.newFood ---
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
		
		if (newBall.oldTimestamp <= (latestCaptureTimestamp - WPConfig.ADD_MAX_TIME_BALL)
				&& newBall.count >= WPConfig.ADD_MIN_FRAMES_BALL)
		{
			// --- new ball with sufficient certainty detected ---
			// --- add him to balls-list of the context and remove it form newBalls-list ---
			// --- after that, we're done ---
			
//			IBallModule ball = ModuleSelector.getBallModule();
			IFilter ball = FilterSelector.getBallFilter();
			IMotionModel motionModel = new BallMotionModel();
			ball.init(motionModel, context, newBall.newTimestamp, newBall.visionBall);
//			ball.init(context, newBall.newTimestamp, newBall.visionBall);
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
	

	private void checkTigers()
	{
		// log.debug("Checking tigers");
		// synchronized (context.tigers)
		// {
		// --- in general there are tigerbots on the field ---
		// --- so iterate through all tigerbots ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		Iterator<IFilter> it = context.tigers.values().iterator();
		while (it.hasNext())
		{
			IFilter tiger = it.next();
			// --- no consistent detection of tigerbot anymore? ---
			// --- is the last detection of the bot older than the allowed maximum time? ---
			if (tiger.getTimestamp() <= (latestCaptureTimestamp - WPConfig.REM_MAX_TIME_BOT))
			{
				// --- the bot was removed from the field ---
				// --- so remove this bot from our tigers-list ---
				it.remove();
				// log.debug("Tiger " + tiger.id + " removed.");
			}
		}
		// }
	}
	

	private void checkFood()
	{
		// log.debug("Checking food");
		// synchronized (context.food)
		// {
		// --- in general there are foodbots on the field ---
		// --- so iterate through all foodbots ---
		// --- we use Iterator to safely remove data from the Map if neccessary ---
		Iterator<IFilter> it = context.food.values().iterator();
		while (it.hasNext())
		{
			IFilter food = it.next();
			// --- no consistent detection of foodbot anymore? ---
			// --- is the last detection of the bot older than the allowed maximum time? ---
			if (food.getTimestamp() <= (latestCaptureTimestamp - WPConfig.REM_MAX_TIME_BOT))
			{
				// --- the bot was removed from the field ---
				// --- so remove this bot from our food-list ---
				it.remove();
				// log.debug("Food " + food.id + " removed.");
			}
		}
		
		// }
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
		IFilter ball = context.ball;
		
		// --- no consistent detection of ball anymore? ---
		// --- is the last detection of the ball older than the allowed maximum time? ---
		if (ball.getTimestamp() <= (latestCaptureTimestamp - WPConfig.REM_MAX_TIME_BALL))
		{
			// we hold the last known position
			context.ball.keepPositionAliveOnNoObservation();
			
			// --- the ball was removed from the field ---
			// --- so remove this ball from our balls-list ---
			//context.ball = null;
		}
	}
}
