/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.realworld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.AWorldPredictorImplementationBluePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor.PredictorKey;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This wp will query Tiger Bot 2015 status position for vision signal
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RealBotWorldPredictor extends AWorldPredictorImplementationBluePrint implements Runnable
{
	private static final Logger		log		= Logger.getLogger(RealBotWorldPredictor.class.getName());
	private ScheduledExecutorService	service;
	private int								frameId	= 0;
	private ABotManager					botManager;
	
	
	/**
	 * @param predictor
	 */
	public RealBotWorldPredictor(final AWorldPredictor predictor)
	{
		super(PredictorKey.BOT, predictor);
	}
	
	
	@Override
	public void predict()
	{
	}
	
	
	@Override
	public void onFacadeInitModule()
	{
	}
	
	
	@Override
	public void onFacadeDeinitModule()
	{
	}
	
	
	@Override
	public void onFacadeStartModule()
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
	public void onFacadeStopModule()
	{
		service.shutdown();
	}
	
	
	private CamDetectionFrame createDetectionFrame()
	{
		long time = SumatraClock.nanoTime();
		List<CamBall> balls = new ArrayList<>();
		// add a dummy ball
		balls.add(new CamBall(0, 0, 10000, 10000, 0, 0, 0, 0, 0));
		List<CamRobot> yellowBots = new ArrayList<>();
		List<CamRobot> blueBots = new ArrayList<>();
		return new CamDetectionFrame(time, time, time, 0, frameId++, balls, yellowBots,
				blueBots);
	}
	
	
	@Override
	public void run()
	{
		try
		{
			CamDetectionFrame camFrame = createDetectionFrame();
			
			BotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
			for (ABot bot : botManager.getAllBots().values())
			{
				if (bot.getType() != EBotType.TIGER_V3)
				{
					continue;
				}
				TigerBotV3 botV3 = (TigerBotV3) bot;
				IVector2 pos = botV3.getLatestFeedbackCmd().getPosition().multiplyNew(1000);
				IVector2 vel = botV3.getLatestFeedbackCmd().getVelocity();
				IVector2 acc = botV3.getLatestFeedbackCmd().getAcceleration();
				float angle = botV3.getLatestFeedbackCmd().getOrientation();
				float aVel = botV3.getLatestFeedbackCmd().getAngularVelocity();
				float aAcc = botV3.getLatestFeedbackCmd().getAngularAcceleration();
				TrackedTigerBot tBot = new TrackedTigerBot(bot.getBotID(), pos, vel, acc, 150, angle, aVel, aAcc, 1, bot,
						bot
								.getBotID().getTeamColor());
				bots.put(bot.getBotID(), tBot);
			}
			
			TrackedBall trackedBall = new TrackedBall(camFrame.getBalls().get(0).getPos(), AVector3.ZERO_VECTOR,
					AVector3.ZERO_VECTOR,
					0, true);
			WorldFramePrediction wfp = new FieldPredictor(bots.values(), trackedBall).create();
			
			SimpleWorldFrame wFrame = new SimpleWorldFrame(bots, trackedBall, camFrame.getFrameNumber(),
					wfp);
			// Push!
			setReturnFrame(wFrame);
			pushPredictedFrameToWorldPredictor();
		} catch (Exception err)
		{
			log.error("Exception in main loop", err);
		}
	}
}
