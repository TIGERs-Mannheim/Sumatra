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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.SyncedCamFrameBuffer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;


/**
 * This class is the supervisor for the actual processing of the incoming {@link CamDetectionFrame}s
 */
public class Director implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log						= Logger.getLogger(Director.class.getName());
	
	
	private static final long						WAIT_ON_FIRST_FRAME	= 100;
	
	
	private static final int						WAIT_CAM_TIMEOUT		= 5000;
	
	
	private Thread										processingThread;
	
	private final SyncedCamFrameBuffer			camBuffer;
	private final PredictionContext				context;
	private List<IWorldFrameConsumer>			consumers				= new CopyOnWriteArrayList<IWorldFrameConsumer>();
	
	private final TrackingManager					trackingManager;
	private double										trackingFrames;
	
	private final WorldFramePacker				packer;
	// our oracle main class
	private final IWorldPredictorObservable	observable;
	
	private final BallProcessor					ballProcessor;
	private final BotProcessor						botProcessor;
	private final BallCorrector					ballCorrector;
	
	
	private SumatraTimer								timer						= null;
	
	private boolean									correctBallData		= true;
	
	private FpsCounter								fpsCounterCam			= new FpsCounter();
	private FpsCounter								fpsCounterWF			= new FpsCounter();
	
	private boolean									warnNoVision;
	private SimpleWorldFrame						lastwFrame				= null;
	
	private Timer										scheduledNotifier		= null;
	
	private CamDetectionFrame						lastCompletedFrame	= null;
	
	private volatile long							nextFrameNumber		= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param freshCamDetnFrames
	 * @param con
	 * @param obs
	 */
	public Director(SyncedCamFrameBuffer freshCamDetnFrames, PredictionContext con, IWorldPredictorObservable obs)
	{
		camBuffer = freshCamDetnFrames;
		
		context = con;
		packer = new WorldFramePacker(context);
		
		trackingManager = new TrackingManager(context);
		trackingFrames = context.getLatestCaptureTimestamp();
		
		observable = obs;
		
		ballProcessor = new BallProcessor(context);
		botProcessor = new BotProcessor(context);
		
		ballCorrector = new BallCorrector();
		
		warnNoVision = false;
		
		TimerTask action = new TimerTask()
		{
			@Override
			public void run()
			{
				synchronized (context)
				{
					synchronized (packer)
					{
						// Prediction and packaging
						SimpleWorldFrame wFrame = null;
						if (lastCompletedFrame != null)
						{
							FrameID frameID = new FrameID(lastCompletedFrame.cameraId, nextFrameNumber);
							startTime(frameID);
							wFrame = packer.pack(nextFrameNumber, lastCompletedFrame.cameraId);
							wFrame.setCamFps(fpsCounterCam.getAvgFps());
							wFrame.setWfFps(fpsCounterWF.getAvgFps());
							fpsCounterWF.newFrame();
							
							// Push!
							for (IWorldFrameConsumer consumer : consumers)
							{
								consumer.onNewSimpleWorldFrame(wFrame);
								consumer.onNewWorldFrame(createWorldFrame(wFrame, ETeamColor.YELLOW));
								consumer.onNewWorldFrame(createWorldFrame(wFrame, ETeamColor.BLUE));
							}
							
							observable.notifyNewWorldFrame(wFrame);
							stopTime(frameID);
							nextFrameNumber++;
						} else if (lastwFrame != null)
						{
							wFrame = WorldFrameFactory.createEmptyWorldFrame(lastwFrame.getId().getFrameNumber());
							for (IWorldFrameConsumer consumer : consumers)
							{
								consumer.onVisionSignalLost(wFrame);
							}
							observable.notifyVisionSignalLost(wFrame);
						}
						
						if (wFrame != null)
						{
							lastwFrame = wFrame;
						}
					}
				}
			}
		};
		scheduledNotifier = new Timer("WP_Scheduler");
		scheduledNotifier.schedule(action, 0, context.worldframesPublishIntervalMS);
	}
	
	
	/**
	 * Create WF from swf
	 * @param swf
	 * @param teamColor
	 * @return
	 */
	public static WorldFrame createWorldFrame(SimpleWorldFrame swf, ETeamColor teamColor)
	{
		final WorldFrame wf;
		if (TeamConfig.getInstance().getTeamProps().getLeftTeam() != teamColor)
		{
			wf = new WorldFrame(swf.mirrorNew(), teamColor, true);
		} else
		{
			wf = new WorldFrame(swf, teamColor, false);
		}
		return wf;
	}
	
	
	/**
	 * @param consumers
	 */
	public void setConsumers(List<IWorldFrameConsumer> consumers)
	{
		this.consumers = consumers;
	}
	
	
	/**
	 * @param timer
	 */
	public void setTimer(SumatraTimer timer)
	{
		this.timer = timer;
	}
	
	
	/**
	 */
	public void start()
	{
		processingThread = new Thread(this, "WP_Director");
		processingThread.setPriority(Thread.MAX_PRIORITY);
		processingThread.start();
	}
	
	
	@Override
	public void run()
	{
		boolean first = true;
		mainloop: while (!Thread.currentThread().isInterrupted())
		{
			CamDetectionFrame newFrame = null;
			do
			{
				try
				{
					newFrame = camBuffer.take(WAIT_CAM_TIMEOUT, TimeUnit.MILLISECONDS);
					if (newFrame == null)
					{
						if (first)
						{
							Thread.sleep(WAIT_ON_FIRST_FRAME);
							continue mainloop;
						}
						if (warnNoVision)
						{
							camBuffer.reset();
							log.warn("No vision connection!");
							warnNoVision = false;
						}
					} else
					{
						if (!warnNoVision)
						{
							camBuffer.reset();
							log.info("Vision is back");
						}
						warnNoVision = true;
					}
				} catch (final InterruptedException err)
				{
					log.debug("Interrupted while waiting for new CamDetectionFrame.");
					camBuffer.clear();
					// Called from end(), so it is meant to =)
					return;
				}
				if (newFrame == null)
				{
					break;
				}
				if (first)
				{
					WPConfig.setFilterTimeOffset(newFrame.tCapture);
					first = false;
				}
				if (correctBallData)
				{
					try
					{
						newFrame = ballCorrector.correctFrame(newFrame);
					} catch (final Exception err)
					{
						correctBallData = false;
						log.error("Turned off the WP-Ballcorrector, because there was an intern exception!", err);
						break;
					}
					if (newFrame == null)
					{
						log.error("WP got null frame from ballCorrector");
						break;
					}
				}
				
				synchronized (context)
				{
					context.setLatestCaptureTimestamp((newFrame.tCapture - WPConfig.getFilterTimeOffset())
							* WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME);
					context.setLatestTeamProps(newFrame.teamProps);
					
					// Processing // see \/
					botProcessor.process(newFrame.robotsYellow, newFrame.robotsBlue);
					// updates ball filter, uses position information form argument and time-stamp from context
					ballProcessor.process(newFrame.balls);
				}
			} while (camBuffer.merge());
			camBuffer.clear();
			
			synchronized (context)
			{
				botProcessor.normalizePredictionTime();
				ballProcessor.normalizePredictionTime();
				botProcessor.performCollisionAwareLookahead();
				ballProcessor.performCollisionAwareLookahead();
			}
			
			// ###########################
			synchronized (packer)
			{
				lastCompletedFrame = newFrame;
			}
			// ###########################
			
			if ((trackingFrames + WPConfig.TRACKING_CHECK_INTERVAL) <= context.getLatestCaptureTimestamp())
			{
				trackingManager.checkItems();
				trackingFrames = context.getLatestCaptureTimestamp();
			}
			
			fpsCounterCam.newFrame();
		}
	}
	
	
	private void startTime(FrameID frameId)
	{
		if (timer != null)
		{
			timer.start(ETimable.WP, frameId.getFrameNumber());
		}
	}
	
	
	private void stopTime(FrameID frameId)
	{
		if (timer != null)
		{
			timer.stop(ETimable.WP, frameId.getFrameNumber());
		}
	}
	
	
	/**
	 */
	public void end()
	{
		for (IWorldFrameConsumer consumer : consumers)
		{
			consumer.onStop();
		}
		processingThread.interrupt();
		scheduledNotifier.cancel();
		lastwFrame = null;
	}
	
}
