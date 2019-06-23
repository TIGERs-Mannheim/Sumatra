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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.SyncedCamFrameBuffer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * This class is the supervisor for the actual processing of the incoming {@link CamDetectionFrame}s
 */
public class Director implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger								log		= Logger.getLogger(getClass());
	

	private Thread										processingThread;
	
	private final SyncedCamFrameBuffer			camBuffer;
	private final PredictionContext				context;
	private IWorldFrameConsumer					consumer	= null;
	
	private final TrackingManager					trackingManager;
	private double										trackingFrames;
	
	private final WorldFramePacker				packer;
	private final IWorldPredictorObservable	observable;										// our oracle main class
																													
	private final BallProcessor					ballProcessor;
	private final BotProcessor						botProcessor;
	private final BallCorrector					ballCorrector;
	
	private CamDetectionFrame						newFrame	= null;
	
	private ITimer										timer		= null;
	private boolean									first;
	
	
	// --------------------------------------------------------------------------
	// --- constructors----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Director(SyncedCamFrameBuffer freshCamDetnFrames, PredictionContext context,
			IWorldPredictorObservable observable)
	{
		this.camBuffer = freshCamDetnFrames;
		
		this.context = context;
		this.packer = new WorldFramePacker(context);
		
		this.trackingManager = new TrackingManager(this.context);
		this.trackingFrames = context.getLatestCaptureTimestamp();
		
		this.observable = observable;
		
		this.ballProcessor = new BallProcessor(this.context);
		this.botProcessor = new BotProcessor(this.context);
		
		this.ballCorrector = new BallCorrector();
	}
	

	public void setConsumer(IWorldFrameConsumer consumer)
	{
		this.consumer = consumer;
	}
	

	public void setTimer(ITimer timer)
	{
		this.timer = timer;
	}
	

	public void start()
	{
		processingThread = new Thread(this, "WP_Director");
		processingThread.setPriority(Thread.MAX_PRIORITY);
		processingThread.start();
	}
	

	@Override
	public void run()
	{
		first = true;
		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				do
				{
					newFrame = camBuffer.take();
					if(first) {
						WPConfig.FILTER_TIME_OFFSET = newFrame.tCapture;
						first = false;
					}
					if(WPConfig.CORRECT_BALL_DATA)
					{
						try
						{
							newFrame = ballCorrector.correctFrame(newFrame);
						} catch (Exception err)
						{
							WPConfig.CORRECT_BALL_DATA= false;
							log.error("Turned off the WP-Ballcorrector, because there was an intern exception!");
						}
					}
					startTime(newFrame);					
					
					context.setLatestCaptureTimestamp((newFrame.tCapture-WPConfig.FILTER_TIME_OFFSET)*WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME);
					
					// Processing
					botProcessor.process(newFrame.robotsTigers, newFrame.robotsEnemies); // see \/
					ballProcessor.process(newFrame.balls); // updates ball filter, uses position information form argument
																		// and time-stamp from context
				} while (camBuffer.merge());
				botProcessor.normalizePredictionTime();
				ballProcessor.normalizePredictionTime();
				botProcessor.performCollisionAwareLookahead();
				ballProcessor.performCollisionAwareLookahead();
			} catch (InterruptedException err)
			{
				log.debug("Interrupted while waiting for new CamDetectionFrame.");
				camBuffer.clear();
				break; // Called from end(), so it is meant to =)
			}
			camBuffer.clear();
			
			// Prediction and packaging
			WorldFrame wFrame = packer.pack(newFrame.frameNumber, newFrame.cameraId);
			
			// Push!
			consumer.onNewWorldFrame(new WorldFrame(wFrame));
			
			observable.notifyFunctionalNewWorldFrame(wFrame);
			observable.notifyNewWorldFrame(wFrame);
			
			if (trackingFrames + WPConfig.TRACKING_CHECK_INTERVAL <= context.getLatestCaptureTimestamp())
			{
				trackingManager.checkItems();
				trackingFrames = context.getLatestCaptureTimestamp();
			}
			stopTime(wFrame);
		}
	}

	private void startTime(CamDetectionFrame detnFrame)
	{
		if (timer != null)
		{
			timer.startWP(detnFrame);
		}
	}
	

	private void stopTime(WorldFrame wFrame)
	{
		if (timer != null)
		{
			timer.stopWP(wFrame);
		}
	}
	

	public void end()
	{
		processingThread.interrupt();
	}
	
}
