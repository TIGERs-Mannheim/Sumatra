/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.ICamFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.OracleExtKalman;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.realworld.RealBotWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;


/**
 * This class is the facade of the WorldPredictorImpls. Every implementation is added in the initModule() method.
 * Implements:
 * ITpsTriggered for stable FPS
 * IWorldPredictorObservable for the created SimpleWorldFrames
 * IMergedCamFrameConsumer for the merged cam-Frame created by the {@link SyncedCamFrameBufferV2} Instantiated by
 * moduli!
 * 
 * @author KaiE
 */
public class WPFacade extends AWorldPredictor implements ITpsTriggered, IWorldPredictorObservable,
		ICamFrameObserver, IMergedCamFrameConsumer, Runnable
{
	private static final Logger																						log							= Logger
																																										.getLogger(WPFacade.class
																																												.getName());
	
	@Configurable(comment = "Allows switching/cumulating the Predictors")
	private static PredictorKey[]																						currentPredictors			= { PredictorKey.Kalman };
	
	@Configurable(comment = "Rate at which the frames are published to the AI")
	private static int																									fps							= 60;
	
	
	private final Map<PredictorKey, AWorldPredictorImplementationBluePrint>								predictors;
	
	private SubnodeConfiguration																						properties;
	
	private WorldFrameWrapper																							latestWorldFrameWrapper	= WorldFrameWrapper
																																										.createDefault(0);
	
	private final ConcurrentHashMap<PredictorKey, LinkedBlockingDeque<MergedCamDetectionFrame>>	syncedCamBufferPerInstance;
	/** set the number of frames per second that are pushed to the observers */
	
	private TpsTrigger																									trigger						= null;
	
	private SyncedCamFrameBufferV2																					detectionFrameBuffer;
	private boolean																										hasConnection				= true;
	private boolean																										geometryReceived			= false;
	
	private FpsCounter																									fpsCounterCam				= new FpsCounter();
	private FpsCounter																									fpsCounterWF				= new FpsCounter();
	private FpsCounter																									fpsCounterCamIn			= new FpsCounter();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor. Requires the {@link SubnodeConfiguration} for Moduli and the Backwards compatibility with the
	 * {@link OracleExtKalman}
	 * 
	 * @param moduliProperties
	 */
	public WPFacade(final SubnodeConfiguration moduliProperties)
	{
		syncedCamBufferPerInstance = new ConcurrentHashMap<>();
		
		predictors = new HashMap<PredictorKey, AWorldPredictorImplementationBluePrint>();
		
		properties = moduliProperties;
		
		
		trigger = new TpsTrigger("WP_Heartbeat");
	}
	
	
	@Override
	public void onPutPredictedWorldFrame(final SimpleWorldFrame predictedFrame, final PredictorKey createdBy)
	{
		if (!Arrays.asList(currentPredictors).contains(createdBy))
		{
			return;
		}
		fpsCounterWF.newFrame();
		predictedFrame.setCamFps(fpsCounterCam.getAvgFps());
		predictedFrame.setWfFps(fpsCounterWF.getAvgFps());
		predictedFrame.setCamInFps(fpsCounterCamIn.getAvgFps());
		WorldFrameWrapper wrapped = new WorldFrameWrapper(predictedFrame);
		for (IWorldFrameConsumer c : consumersHungry)
		{
			c.onNewWorldFrame(wrapped);
		}
		latestWorldFrameWrapper = wrapped;
	}
	
	
	@Override
	public final MergedCamDetectionFrame pollNewCamFrame(final PredictorKey callingWP)
	{
		try
		{
			final LinkedBlockingDeque<MergedCamDetectionFrame> buffer = syncedCamBufferPerInstance.get(callingWP);
			final MergedCamDetectionFrame localReturnFrame;
			localReturnFrame = buffer.pollFirst(2, TimeUnit.SECONDS);
			if ((localReturnFrame == null))
			{
				if (hasConnection)
				{
					log.info("No cam frame found; signal lost");
					hasConnection = false;
				}
			} else
			{
				return localReturnFrame;
			}
		} catch (InterruptedException err)
		{
			log.warn("interrupted while waiting for new CamFrame");
		}
		
		return MergedCamDetectionFrame.emptyFrame();
	}
	
	
	// --------------------------------------------------------------------------
	// --- Override from AModule-------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void initModule() throws InitModuleException
	{
		/**
		 * ==============================================================================================
		 * WHEN ADDING A NEW WORLDPREDICTOR JUST ADD AN OBJECT HERE TO THE PREDICTORS. Also add
		 * an additional PredictorsKey-Entry corresponding to your Predictor
		 * ==============================================================================================
		 */
		if (properties.getBoolean("useRealBotVision", false))
		{
			predictors.put(PredictorKey.BOT, new RealBotWorldPredictor(this));
		} else
		{
			predictors.put(PredictorKey.Kalman, new OracleExtKalman(this));
			// predictors.put(PredictorKey.Neural, new NeuralWP(this));
		}
		/**
		 * ==============================================================================================
		 */
		
		for (AWorldPredictorImplementationBluePrint currAWP : predictors.values())
		{
			currAWP.onFacadeInitModule();
		}
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		ConfigRegistration.applySpezis(EConfigurableCat.WP, properties.getString("lab", "SIM"));
		detectionFrameBuffer = new SyncedCamFrameBufferV2(this);
		for (PredictorKey p : predictors.keySet())
		{
			syncedCamBufferPerInstance.put(p, new LinkedBlockingDeque<>());
		}
		
		for (AWorldPredictorImplementationBluePrint currAWP : predictors.values())
		{
			currAWP.onFacadeStartModule();
		}
		
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.addObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ACam.MODULE_ID + "'!");
		}
		
		trigger.addTpsTriggered(WPFacade.class.getName(), this);
		
		try
		{
			trigger.start(fps);
		} catch (MathException err)
		{
			log.fatal("starting the TpsTrigger created MathException", err);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		clearAllObserversAndConsumers();
		
		trigger.stop();
		
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.removeObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ACam.MODULE_ID + "'!");
		}
		
		detectionFrameBuffer.stopService();
		
		for (Entry<PredictorKey, AWorldPredictorImplementationBluePrint> cnsmr : predictors.entrySet())
		{
			cnsmr.getValue().onFacadeStopModule();
		}
		
		for (IWorldFrameConsumer consumer : consumers)
		{
			consumer.onStop();
		}
	}
	
	
	@Override
	public void deinitModule()
	{
		for (AWorldPredictorImplementationBluePrint currAWP : predictors.values())
		{
			currAWP.onFacadeDeinitModule();
		}
		predictors.clear();
		
		trigger.removeTpsTriggered(WPFacade.class.getName());
	}
	
	
	// --------------------------------------------------------------------------
	// --- Override from Interfaces----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- Override from Runnable------------------------------------------------
	@Override
	public void run()
	{
	}
	
	
	// --- Override from ICamDetnFrameConsumer-----------------------------------
	
	@Override
	public void onNewCameraFrame(final CamDetectionFrame camDetectionFrame)
	{
		if (!hasConnection)
		{
			log.info("new CamDetectionFrame; Vision connected!");
			hasConnection = true;
		}
		fpsCounterCamIn.newFrame();
		detectionFrameBuffer.newCamDetectionFrame(camDetectionFrame);
	}
	
	
	@Override
	public void onNewCameraGeometry(final CamGeometry geometry)
	{
		for (CamCalibration calib : geometry.getCalibrations().values())
		{
			AIConfig.getGeometry().getCameraFocalLength()[calib.getCameraId()] = calib.getFocalLength();
			AIConfig.getGeometry().getCameraPrincipalPointX()[calib.getCameraId()] = calib.getPrincipalPointX();
			AIConfig.getGeometry().getCameraPrincipalPointY()[calib.getCameraId()] = calib.getPrincipalPointY();
		}
		
		if (Geometry.isReceiveGeometry() && !geometryReceived)
		{
			Configuration config = AIConfig.getGeometry().getConfig();
			config.setProperty("field.border.widthL", geometry.getField().getBoundaryWidth());
			config.setProperty("field.border.widthW", geometry.getField().getBoundaryWidth());
			config.setProperty("field.length", geometry.getField().getFieldLength());
			config.setProperty("field.width", geometry.getField().getFieldWidth());
			config.setProperty("field.goal.innerWidth", geometry.getField().getGoalWidth());
			config.setProperty("field.goal.innerDepth", geometry.getField().getGoalDepth());
			AIConfig.setGeometry(new Geometry(config));
			// TODO find penalty lines and arcs
			// geometry.getField().getFieldLines().get(0).
			geometryReceived = true;
			log.info("Received geometry from vision");
		}
	}
	
	
	// --- Override from ITpsTriggered-------------------------------------------
	
	@Override
	public void onElementTriggered()
	{
		if (!hasConnection)
		{
			latestWorldFrameWrapper = WorldFrameWrapper.createDefault(latestWorldFrameWrapper.getSimpleWorldFrame()
					.getId());
		}
		
		for (IWorldFrameConsumer consumer : consumers)
		{
			consumer.onNewWorldFrame(latestWorldFrameWrapper);
		}
		
		notifyNewWorldFrame(latestWorldFrameWrapper);
	}
	
	
	// --- Override from IWorldPredictorObservable-------------------------------
	
	@Override
	public void notifyNewWorldFrame(final WorldFrameWrapper wFrame)
	{
		for (final IWorldPredictorObserver observer : observers)
		{
			observer.onNewWorldFrame(wFrame);
		}
	}
	
	
	// --- Override from IMergedCamFrameConsumer-------------------------------
	
	@Override
	public void notifyNewSyncedCamFrame(final MergedCamDetectionFrame frame)
	{
		fpsCounterCam.newFrame();
		for (PredictorKey pk : predictors.keySet())
		{
			final LinkedBlockingDeque<MergedCamDetectionFrame> buffer = syncedCamBufferPerInstance.get(pk);
			if (buffer.remainingCapacity() == 0)
			{
				buffer.pollFirst();
			}
			if (!buffer.offer(frame))
			{
				log.warn("Could not put CamDetectionFrame. Haven't we made space just before? :o");
			}
		}
		for (IMergedCamFrameObserver obs : mergedFrameObservers)
		{
			obs.onNewCameraFrame(frame);
		}
	}
	
	
	@Override
	public void setLatestBallPosHint(final IVector2 pos)
	{
		detectionFrameBuffer.setLatestBallPosHint(pos);
	}
}
