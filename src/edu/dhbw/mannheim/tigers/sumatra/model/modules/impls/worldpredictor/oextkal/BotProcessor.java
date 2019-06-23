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
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.benchmarking.Precision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.OmnibotControl_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;


/**
 * Prepares all new data from the incoming
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame} concerning the bots (enemies and
 * tigers), and add
 * it to the {@link PredictionContext} if necessary
 */
public class BotProcessor
{
	
	private final PredictionContext	context;
	private int								count;
	
	
	/**
	 * @param context
	 */
	public BotProcessor(PredictionContext context)
	{
		this.context = context;
		count = 0;
	}
	
	
	/**
	 * @param camYellowBots
	 * @param camBlueBots
	 */
	public void process(List<CamRobot> camYellowBots, List<CamRobot> camBlueBots)
	{
		// ---Check oldTigers list and update each founded element
		// ---if incoming CamRobos not known, check list of newTigers
		// ---if found in newTigers refresh element
		// ---if not found add to list
		final double timestamp = context.getLatestCaptureTimestamp();
		
		for (final CamRobot visionBotCam : camYellowBots)
		{
			final WPCamBot visionBot = new WPCamBot(visionBotCam);
			final int botID = visionBot.id + WPConfig.YELLOW_ID_OFFSET;
			
			final IFilter existingBot = context.yellowBots.get(botID);
			processBot(visionBotCam, botID, existingBot, timestamp, context.newYellowBots);
		}
		
		// --- same for blue ~~~~
		for (final CamRobot visionBotCam : camBlueBots)
		{
			final WPCamBot visionBot = new WPCamBot(visionBotCam);
			final int botID = visionBot.id + WPConfig.BLUE_ID_OFFSET;
			
			final IFilter existingBot = context.blueBots.get(botID);
			processBot(visionBotCam, botID, existingBot, timestamp, context.newBlueBots);
		}
	}
	
	
	private void processBot(CamRobot visionBotCam, int botID, IFilter existingBot, double timestamp,
			Map<Integer, UnregisteredBot> contextNewBots)
	{
		final WPCamBot visionBot = new WPCamBot(visionBotCam);
		if (existingBot != null)
		{
			// ---For benchmarking purpose
			if (count < 10)
			{
				count++;
			} else
			{
				for (int i = 0; i <= context.stepCount; i++)
				{
					Precision.getInstance().addBot(existingBot.getLookaheadTimestamp(i),
							(RobotMotionResult_V2) existingBot.getLookahead(i), botID);
				}
				Precision.getInstance().addCamBot(timestamp, visionBot, botID);
			}
			
			// drop doubled observation if bot is in overlap area of cameras
			final double dt = timestamp - existingBot.getTimestamp();
			if (dt <= WPConfig.MIN_CAMFRAME_DELAY_TIME)
			{
				return;
			}
			
			final RobotMotionResult_V2 oldState = (RobotMotionResult_V2) existingBot.getLookahead(0);
			
			existingBot.observation(timestamp, visionBot);
			
			estimateControl(oldState, existingBot, dt);
			return;
		}
		
		UnregisteredBot newBot = contextNewBots.get(botID);
		if (newBot != null)
		{
			newBot.addBot(timestamp, visionBot);
		} else
		{
			newBot = new UnregisteredBot(timestamp, visionBot);
			contextNewBots.put(botID, newBot);
		}
	}
	
	
	/**
	 */
	public void normalizePredictionTime()
	{
		final double nowTime = context.getLatestCaptureTimestamp();
		
		final Iterator<IFilter> foodIt = context.blueBots.values().iterator();
		while (foodIt.hasNext())
		{
			final IFilter food = foodIt.next();
			food.updateOffset(nowTime);
		}
		
		final Iterator<IFilter> tigerIt = context.yellowBots.values().iterator();
		while (tigerIt.hasNext())
		{
			final IFilter tiger = tigerIt.next();
			tiger.updateOffset(nowTime);
		}
	}
	
	
	/**
	 */
	public void performCollisionAwareLookahead()
	{
		for (int i = 1; i <= context.stepCount; i++)
		{
			final Iterator<IFilter> foodIt = context.blueBots.values().iterator();
			while (foodIt.hasNext())
			{
				final IFilter food = foodIt.next();
				food.performLookahead(i);
			}
			
			final Iterator<IFilter> tigerIt = context.yellowBots.values().iterator();
			while (tigerIt.hasNext())
			{
				final IFilter tiger = tigerIt.next();
				tiger.performLookahead(i);
			}
			
			// TODO WP: Implement new collision control
			/*
			 * foodIt = context.food.values().iterator();
			 * while (foodIt.hasNext())
			 * {
			 * IFilter food = foodIt.next();
			 * food.collisionControl(i);
			 * }
			 * 
			 * tigerIt = context.tigers.values().iterator();
			 * while (tigerIt.hasNext())
			 * {
			 * ITigerModule tiger = tigerIt.next();
			 * tiger.collisionControl(i);
			 * }
			 */
		}
	}
	
	
	private void estimateControl(RobotMotionResult_V2 oldState, IFilter bot, double dt)
	{
		final double oldX = oldState.x;
		final double oldY = oldState.y;
		final double oldTheta = oldState.orientation;
		
		final RobotMotionResult_V2 newState = (RobotMotionResult_V2) bot.getLookahead(0);
		final double newX = newState.x;
		final double newY = newState.y;
		final double newTheta = newState.orientation;
		
		final double sinOri = Math.sin(oldTheta);
		final double cosOri = Math.cos(oldTheta);
		
		// Determine new v_x and v_y
		final double dX = (newX - oldX);
		final double dY = (newY - oldY);
		
		final double vT = ((cosOri * dX) + (sinOri * dY)) / dt;
		final double vO = ((-sinOri * dX) + (cosOri * dY)) / dt;
		
		// Determine new omega
		double dOmega = newTheta - oldTheta;
		if (Math.abs(dOmega) > Math.PI)
		{
			dOmega = ((2 * Math.PI) - Math.abs(dOmega)) * (-1 * Math.signum(dOmega));
		}
		final double omega = dOmega / dt;
		
		// Determine new eta
		final double eta = 0.0 / dt;
		
		// Set determined values (control)
		bot.setControl(new OmnibotControl_V2(vT, vO, omega, eta));
	}
}
