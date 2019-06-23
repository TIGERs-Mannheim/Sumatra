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
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.benchmarking.Precision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.OmnibotControl_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.UnregisteredBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.WPCamBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;



/**
 * Prepares all new data from the incoming {@link CamDetectionFrame} concerning the bots (enemies and tigers), and add
 * it to the {@link PredictionContext} if necessary
 */
public class BotProcessor
{
	
	private PredictionContext	context;
	private int						count;
	
	
	public BotProcessor(PredictionContext context)
	{
		this.context = context;
		count = 0;
	}
	

	public void process(List<CamRobot> camTigers, List<CamRobot> camEnemies)
	{
		// ---Check oldTigers list and update each founded element
		// ---if incoming CamRobos not known, check list of newTigers
		// ---if found in newTigers refresh element
		// ---if not found add to list
		double timestamp = context.getLatestCaptureTimestamp();
		for (CamRobot visionBot_Cam : camTigers)
		{
			WPCamBot visionBot = new WPCamBot(visionBot_Cam);
			int botID = visionBot.id + WPConfig.TIGER_ID_OFFSET;
			
			IFilter existingBot = context.tigers.get(botID);
			if (existingBot != null)
			{
				// ---For benchmarking purpose
				if (count < 10)
					count++;
				else
				{
					for (int i = 0; i <= context.stepCount; i++) {
						Precision.getInstance().addBot(existingBot.getLookaheadTimestamp(i),
								(RobotMotionResult_V2) existingBot.getLookahead(i), botID);
					}
					Precision.getInstance().addCamBot(timestamp, visionBot, botID);
				}
				existingBot.observation(timestamp, visionBot);
				continue;
			}
			
			UnregisteredBot newBot = context.newTigers.get(botID);
			if (newBot != null)
			{
				newBot.addBot(timestamp, visionBot);
			} else
			{
				newBot = new UnregisteredBot(timestamp, visionBot);
				context.newTigers.put(botID, newBot);
			}
		}
		
		// --- same for food ~~~~
		for (CamRobot visionBot_Cam : camEnemies)
		{
			WPCamBot visionBot = new WPCamBot(visionBot_Cam);
			int botID = visionBot.id + WPConfig.FOOD_ID_OFFSET;
			
			IFilter existingBot = context.food.get(botID);
			
			if (existingBot != null)
			{
				
				// ---For benchmarking purpose
				if (count < 10)
					count++;
				else
				{
					for (int i = 0; i <= context.stepCount; i++) {
						Precision.getInstance().addBot(existingBot.getLookaheadTimestamp(i),
								(RobotMotionResult_V2) existingBot.getLookahead(i), botID);
					}
					Precision.getInstance().addCamBot(timestamp, visionBot, botID);
				}
				
				// drop doubled observation if bot is in overlap area of cameras
				double dt = timestamp - existingBot.getTimestamp();
				if (dt <= WPConfig.MIN_CAMFRAME_DELAY_TIME)
				{
					continue;
				}
				
				RobotMotionResult_V2 oldState = (RobotMotionResult_V2) existingBot.getLookahead(0);
				
				existingBot.observation(timestamp, visionBot);
				
				estimateControl(oldState, existingBot, dt);
				continue;
			}
			
			UnregisteredBot newBot = context.newFood.get(botID);
			if (newBot != null)
			{
				newBot.addBot(timestamp, visionBot);
			} else
			{
				newBot = new UnregisteredBot(timestamp, visionBot);
				context.newFood.put(botID, newBot);
			}
		}
	}
	
	public void normalizePredictionTime()
	{
		double nowTime = context.getLatestCaptureTimestamp();
		
		Iterator<IFilter> foodIt = context.food.values().iterator();
		while (foodIt.hasNext())
		{
			IFilter food = foodIt.next();
			food.updateOffset(nowTime);
		}
		
		Iterator<IFilter> tigerIt = context.tigers.values().iterator();
		while (tigerIt.hasNext())
		{
			IFilter tiger = tigerIt.next();
			tiger.updateOffset(nowTime);
		}
	}
	
	public void performCollisionAwareLookahead()
	{
		for (int i = 1; i <= context.stepCount; i++)
		{
			Iterator<IFilter> foodIt = context.food.values().iterator();
			while (foodIt.hasNext())
			{
				IFilter food = foodIt.next();
				food.performLookahead(i);
			}
			
			Iterator<IFilter> tigerIt = context.tigers.values().iterator();
			while (tigerIt.hasNext())
			{
				IFilter tiger = tigerIt.next();
				tiger.performLookahead(i);
			}
			
			//TODO WP: Implement new collision control
/*			foodIt = context.food.values().iterator();
			while (foodIt.hasNext())
			{
				IFilter food = foodIt.next();
				food.collisionControl(i);
			}
			
			tigerIt = context.tigers.values().iterator();
			while (tigerIt.hasNext())
			{
				ITigerModule tiger = tigerIt.next();
				tiger.collisionControl(i);
			}*/
		}
	}
	
	private void estimateControl(RobotMotionResult_V2 oldState, IFilter bot, double dt)
	{	
		double oldX		= oldState.x;
		double oldY		= oldState.y;
		double oldTheta= oldState.orientation;
		
		RobotMotionResult_V2 newState = (RobotMotionResult_V2) bot.getLookahead(0);
		double newX		= newState.x;
		double newY		= newState.y;
		double newTheta= newState.orientation;
		
		double sinOri  = Math.sin(oldTheta);
		double cosOri  = Math.cos(oldTheta);

		//Determine new v_x and v_y
		double d_x = (newX - oldX);
		double d_y = (newY - oldY);
		
		double v_t = (  cosOri*d_x + sinOri*d_y)/dt;
		double v_o = (- sinOri*d_x + cosOri*d_y)/dt;
				
		//Determine new omega
		double dOmega = newTheta - oldTheta;
		if (Math.abs(dOmega) > Math.PI)
		{
			dOmega = (2*Math.PI - Math.abs(dOmega)) * (-1*Math.signum(dOmega));
		}
		double omega = dOmega/dt;
			
		//Determine new eta
		double eta = 0.0/dt;
		
		//Set determined values (control)		
		bot.setControl(new OmnibotControl_V2(v_t, v_o, omega, eta));
	}
}
