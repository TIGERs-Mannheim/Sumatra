/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.Director;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.benchmarking.Precision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.SyncedCamFrameBuffer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.WriteFlyData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * This class is the core of Sumatras prediction-system. First it dispatches the incoming data, then it initiates their
 * processing. Furthermore, it handles the lifecycle of the whole module
 * 
 * @author Gero
 * 
 */
public class Oracle_extKalman extends AWorldPredictor implements IWorldPredictorObservable
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log		= Logger.getLogger(Oracle_extKalman.class.getName());
	
	
	private SumatraTimer						timer		= null;
	
	// CamDetectionFrame-Consumer
	private ACam								cam		= null;
	private final SyncedCamFrameBuffer	freshCamDetnFrames;
	
	// Processing
	private Director							director;
	
	// Prediction
	private final PredictionContext		context;
	
	private int									useCam	= -1;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param properties
	 */
	public Oracle_extKalman(SubnodeConfiguration properties)
	{
		super();
		
		// TODO WP: Einstellungen abfragen und weitere Einstellung aus wp-config ins moduli config �bertragen
		// ModuleProcessorFactory.getInstance().setProperties(properties.getString("ballPrediction", "LINEAR"),
		// properties.getString("tigerPrediction", "LINEAR"), properties.getString("foePrediction", "LINEAR"));
		
		context = new PredictionContext(properties);
		
		useCam = properties.getInt("useCam", -1);
		
		
		freshCamDetnFrames = new SyncedCamFrameBuffer(context.numberCams,
				(long) (WPConfig.MIN_CAMFRAME_DELAY_TIME / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME));
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule()
	{
		final SumatraModel model = SumatraModel.getInstance();
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			cam.setCamFrameConsumer(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ACam.MODULE_ID + "'!");
		}
		
		try
		{
			timer = (SumatraTimer) model.getModule(ATimer.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		if (WPConfig.DEBUG)
		{
			Precision.getInstance().open(context.stepCount);
		}
		
		if (WPConfig.DEBUG_FLYING)
		{
			WriteFlyData.getInstance().open();
		}
		
		// this is used for observercommunication
		director = new Director(freshCamDetnFrames, context, this);
	}
	
	
	@Override
	public void startModule()
	{
		final SumatraModel model = SumatraModel.getInstance();
		ABotManager botmanager;
		Map<BotID, ABot> botMap = null;
		try
		{
			botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
			botMap = botmanager.getAllBots();
			botmanager.addObserver(this);
			for (final ABot bot : botMap.values())
			{
				onBotAdded(bot);
			}
		} catch (final ModuleNotFoundException e)
		{
			log.error("Botmanager not found!");
		}
		
		director.setConsumers(consumers);
		director.setTimer(timer);
		director.start();
	}
	
	
	@Override
	public void deinitModule()
	{
		if (director != null)
		{
			director.end();
			director.setTimer(null);
			director = null;
			if (WPConfig.DEBUG)
			{
				Precision.getInstance().write();
				Precision.getInstance().clear();
				Precision.getInstance().close();
			}
			
			if (WPConfig.DEBUG_FLYING)
			{
				WriteFlyData.getInstance().write();
				WriteFlyData.getInstance().clear();
				WriteFlyData.getInstance().close();
			}
		}
		
		context.reset();
	}
	
	
	@Override
	public void stopModule()
	{
		for (IWorldFrameConsumer consumer : consumers)
		{
			consumer.onStop();
		}
		timer = null;
		
		if (cam != null)
		{
			cam.setCamFrameConsumer(null);
			cam = null;
		}
		
		synchronized (observers)
		{
			observers.clear();
		}
	}
	
	
	private float noise(float max)
	{
		final float r = (float) ((Math.random() - 0.5) * 2);
		return r * max;
	}
	
	
	private List<CamBall> noisyBalls(List<CamBall> b)
	{
		final List<CamBall> l = new ArrayList<CamBall>();
		for (final CamBall ball : b)
		{
			l.add(new CamBall(ball.confidence, ball.area, ball.pos.x() + noise(WPConfig.NOISE_S), ball.pos.y()
					+ noise(WPConfig.NOISE_S), ball.pos.z(), ball.pixelX, ball.pixelY));
		}
		return l;
	}
	
	
	private List<CamRobot> noisyBots(List<CamRobot> bots)
	{
		final List<CamRobot> l = new ArrayList<CamRobot>();
		for (final CamRobot bot : bots)
		{
			l.add(new CamRobot(bot.confidence, bot.robotID, bot.pos.x() + noise(WPConfig.NOISE_S), bot.pos.y()
					+ noise(WPConfig.NOISE_S), bot.orientation + noise(WPConfig.NOISE_R), bot.pixelX, bot.pixelY, bot.height));
		}
		return l;
	}
	
	
	private CamDetectionFrame addNoise(CamDetectionFrame f)
	{
		final long tCapture = f.tCapture;
		final long tSent = f.tSent;
		final long tReceived = f.tReceived;
		final int cameraId = f.cameraId;
		final long frameNumber = f.frameNumber;
		final double fps = f.fps;
		final List<CamBall> balls = noisyBalls(f.balls);
		final List<CamRobot> robotsYellow = noisyBots(f.robotsYellow);
		final List<CamRobot> robotsBlue = noisyBots(f.robotsBlue);
		final CamDetectionFrame n = new CamDetectionFrame(tCapture, tSent, tReceived, cameraId, frameNumber, fps, balls,
				robotsYellow, robotsBlue, f.teamProps);
		return n;
	}
	
	
	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame camDetectionFrame)
	{
		if (useCam >= 0)
		{
			if (camDetectionFrame.cameraId != useCam)
			{
				return;
			}
		}
		
		synchronized (observers)
		{
			for (IWorldPredictorObserver o : observers)
			{
				o.onNewCamDetectionFrame(camDetectionFrame);
			}
		}
		
		if (WPConfig.ADD_NOISE)
		{
			camDetectionFrame = addNoise(camDetectionFrame);
		}
		freshCamDetnFrames.put(camDetectionFrame);
	}
	
	
	@Override
	public void onBotAdded(ABot bot)
	{
	}
	
	
	@Override
	public void onBotRemoved(ABot bot)
	{
	}
	
	
	@Override
	public void onBotIdChanged(BotID oldId, BotID newId)
	{
	}
	
	
	@Override
	public void notifyNewWorldFrame(SimpleWorldFrame wFrame)
	{
		synchronized (observers)
		{
			for (final IWorldPredictorObserver observer : observers)
			{
				observer.onNewWorldFrame(wFrame);
			}
		}
	}
	
	
	@Override
	public void notifyVisionSignalLost(SimpleWorldFrame emptyWf)
	{
		synchronized (observers)
		{
			for (final IWorldPredictorObserver observer : observers)
			{
				observer.onVisionSignalLost(emptyWf);
			}
		}
	}
	
	
	@Override
	public void onBotConnectionChanged(ABot bot)
	{
	}
	
}
