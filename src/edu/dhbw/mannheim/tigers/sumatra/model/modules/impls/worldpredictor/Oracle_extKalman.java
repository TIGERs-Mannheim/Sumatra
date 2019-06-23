/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Maren Künemund <Orphen@fantasymail.de>,
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

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.Timer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.BotControlObserver;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.moduli.exceptions.ModuleNotFoundException;


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
	private final Logger									log						= Logger.getLogger(getClass());
	

	private final SumatraModel							model						= SumatraModel.getInstance();
	
	protected ITimer										timer						= null;
	
	// CamDetectionFrame-Consumer
	private ACam											cam						= null;
	private SyncedCamFrameBuffer                 freshCamDetnFrames;
	
	// Handling of robot control commands
	private ArrayList<BotControlObserver>			botControlObserver	= new ArrayList<BotControlObserver>(5);
	
	// Processing
	private Director										director;
	
	// Prediction
	private final PredictionContext					context;
	
	private int useCam = -1;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public Oracle_extKalman(SubnodeConfiguration properties)
	{
		super();
		
		// TODO WP: Einstellungen abfragen und weitere Einstellung aus wp-config ins moduli config übertragen
		// ModuleProcessorFactory.getInstance().setProperties(properties.getString("ballPrediction", "LINEAR"),
		// properties.getString("tigerPrediction", "LINEAR"), properties.getString("foePrediction", "LINEAR"));
		
		context = new PredictionContext(properties);
		
		useCam = properties.getInt("useCam",-1);
		
		
		freshCamDetnFrames = new SyncedCamFrameBuffer(context.numberCams, (long) (WPConfig.MIN_CAMFRAME_DELAY_TIME / WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME));
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule()
	{		
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			cam.setCamFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ACam.MODULE_ID + "'!");
		}
		
		try
		{
			timer = (ATimer) model.getModule(Timer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		if (WPConfig.DEBUG)
			Precision.getInstance().open(context.stepCount);
		
		if (WPConfig.DEBUG_FLYING)
			WriteFlyData.getInstance().open();
		
		log.info("Initialized.");
		
		director = new Director(freshCamDetnFrames, context, this); // this is used for observercommunication
	}
	

	@Override
	public void startModule()
	{
		SumatraModel model = SumatraModel.getInstance();
		ABotManager botmanager;
		Map<Integer, ABot> botMap = null;		
		try
		{
			botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
			botMap = botmanager.getAllBots();
			botmanager.addObserver(this);
			for (ABot bot : botMap.values())
			{
				//botControlObserver.add(new BotControlObserver(bot, context.tigers));
				onBotAdded(bot);
			}
		} catch (ModuleNotFoundException e)
		{
			log.error("Botmanager not found!");
		}
		
		if (consumer == null)
		{
			log.warn("No consumer setted!! WorldPredictor will go to bed.");
			return;
		}
		
		director.setConsumer(consumer);
		director.setTimer(timer);
		director.start();
		
		log.info("Started.");
	}
	

	@Override
	public void deinitModule()
	{
		if (director != null)
		{
			director.end();
			director.setConsumer(null);
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
		
		log.info("Deinitialized.");
	}
	

	@Override
	public void stopModule()
	{
		consumer = null;
		timer = null;
		
		if (cam != null)
		{
			cam.setCamFrameConsumer(null);
			cam = null;
		}
		
		synchronized (botControlObserver)
		{
			botControlObserver.clear();
		}
		
		synchronized (observers)
		{
			observers.clear();
		}
		
		synchronized (functionalObservers)
		{
			functionalObservers.clear();
		}
		
		log.info("Stopped.");
	}
	
	private float noise(float max)
	{
		float r = (float) ((Math.random()-0.5)*2);
		return r*max;
	}
	
	private List<CamBall> noisyBalls(List<CamBall> b)
	{
		List<CamBall> l = new ArrayList<CamBall>();
		for (CamBall ball: b)
		{
			l.add(new CamBall(ball.confidence, ball.area, ball.pos.x + noise(WPConfig.NOISE_S), ball.pos.y + noise(WPConfig.NOISE_S), ball.pos.z, ball.pixelX, ball.pixelY));
		}
		return l;
	}
	
	private List<CamRobot> noisyBots(List<CamRobot> bots)
	{
		List<CamRobot> l = new ArrayList<CamRobot>();
		for (CamRobot bot: bots)
		{
			l.add(new CamRobot(bot.confidence, bot.robotID, bot.pos.x + noise(WPConfig.NOISE_S), bot.pos.y + noise(WPConfig.NOISE_S), bot.orientation+noise(WPConfig.NOISE_R), bot.pixelX, bot.pixelY, bot.height));
		}
		return l;		
	}
	
	private CamDetectionFrame addNoise(CamDetectionFrame f)
	{
		long tCapture = f.tCapture;
		long tSent = f.tSent;
		long tReceived = f.tReceived;
		int cameraId = f.cameraId;
		long frameNumber = f.frameNumber;
		double fps = f.fps;
		List<CamBall> balls = noisyBalls(f.balls);
		List<CamRobot> tigers = noisyBots(f.robotsTigers);
		List<CamRobot> enemies = noisyBots(f.robotsEnemies);
		CamDetectionFrame n = new CamDetectionFrame(tCapture, tSent, tReceived, cameraId, frameNumber, fps , balls , tigers , enemies);
		return n;
	}

	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame camDetectionFrame)
	{
		if (useCam >= 0)
		{
			if (camDetectionFrame.cameraId != useCam)
				return;
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
		try 
		{ 
			ABotManager botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID); 
			botControlObserver.add(new BotControlObserver(bot, context.tigers, botmanager)); 
		} 
		catch (ModuleNotFoundException e) 
		{ 
			log.error("Botmanager not found!"); 
		} 
	}

	@Override
	public void onBotRemoved(ABot bot)
	{
		int id = bot.getBotId();
		for (BotControlObserver bco : botControlObserver)
		{
			if (bco.getId() == id)
			{
				bco.stopObserving();
				botControlObserver.remove(bco);
				return;
			}
		}
	}

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
		for (BotControlObserver bco : botControlObserver)
		{
			if (bco.getId() == oldId)
			{
				bco.setId(newId);
				return;
			}
		}		
	}
	
	
	@Override
	public void notifyFunctionalNewWorldFrame(WorldFrame wFrame)
	{
		synchronized (functionalObservers)
		{
			for (IWorldPredictorObserver observer : functionalObservers)
			{
				observer.onNewWorldFrame(new WorldFrame(wFrame));
			}
		}
	}
	

	@Override
	public void notifyNewWorldFrame(WorldFrame wFrame)
	{
		synchronized (observers)
		{
			for (IWorldPredictorObserver observer : observers)
			{
				observer.onNewWorldFrame(new WorldFrame(wFrame));
			}
		}
	}
}
