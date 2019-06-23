/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.bot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * This wp will query Tiger Bot 2015 status position for vision signal
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RealBotWorldPredictor extends AWorldPredictor implements Runnable
{
	private static final Logger		log		= Logger.getLogger(RealBotWorldPredictor.class.getName());
	private int								frameId	= 0;
	private ABotManager					botManager;
	private ScheduledExecutorService	service;
	
	
	/**
	 * @param config
	 */
	public RealBotWorldPredictor(final SubnodeConfiguration config)
	{
		super(config);
	}
	
	
	@Override
	public void run()
	{
		try
		{
			long timestamp = System.nanoTime();
			
			BotIDMap<ITrackedBot> bots = new BotIDMap<>();
			for (IBot bot : botManager.getAllBots().values())
			{
				if (bot.getType() != EBotType.TIGER_V3)
				{
					continue;
				}
				TigerBotV3 botV3 = (TigerBotV3) bot;
				TrackedBot tBot = new TrackedBot(timestamp, bot.getBotId());
				tBot.setPos(botV3.getLatestFeedbackCmd().getPosition().multiplyNew(1000));
				tBot.setVel(botV3.getLatestFeedbackCmd().getVelocity());
				tBot.setAngle(botV3.getLatestFeedbackCmd().getOrientation());
				tBot.setaVel(botV3.getLatestFeedbackCmd().getAngularVelocity());
				bots.put(bot.getBotId(), tBot);
			}
			
			TrackedBall trackedBall = new TrackedBall(new Vector3(10000, 10000, 0), AVector3.ZERO_VECTOR,
					AVector3.ZERO_VECTOR);
			
			SimpleWorldFrame wFrame = new SimpleWorldFrame(bots, trackedBall, frameId,
					timestamp);
			
			pushFrame(wFrame);
			frameId++;
		} catch (Exception err)
		{
			log.error("Exception in main loop", err);
		}
	}
	
	
	@Override
	protected void processCameraDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
	}
	
	
	@Override
	public void start()
	{
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find botManager", err);
		}
		
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("WP_RealBot"));
		service.scheduleAtFixedRate(this, 0, 16, TimeUnit.MILLISECONDS);
	}
	
	
	@Override
	public void stop()
	{
		service.shutdown();
	}
}
