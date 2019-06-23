/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.TimeSync;
import edu.tigers.sumatra.cam.data.ACamObject;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.ETimable;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.flyingBalls.Altigraph;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 */
public abstract class AWorldPredictor extends AModule implements ICamFrameObserver, IConfigObserver
{
	@SuppressWarnings("unused")
	private static final Logger				log						= Logger.getLogger(AWorldPredictor.class.getName());
	
	/** */
	public static final String					MODULE_TYPE				= "AWorldPredictor";
	/** */
	public static final String					MODULE_ID				= "worldpredictor";
	
	private SumatraTimer							timer						= null;
	private List<IWorldFrameObserver>		observers				= new CopyOnWriteArrayList<>();
	private List<IWorldFrameObserver>		prioObservers			= new CopyOnWriteArrayList<>();
	private boolean								geometryReceived		= false;
	private Map<Integer, Double>				lastTCaptures			= new HashMap<>();
	
	private WorldInfoProcessor					infoProcessor			= new WorldInfoProcessor();
	private final List<IWfPostProcessor>	postProcessors			= new ArrayList<>();
	
	private CamBall								lastSeenBall			= new CamBall();
	private long									latestTimestamp		= 0;
	
	@Configurable(comment = "P1 of rectangle defining a range where objects are ignored")
	private static IVector2						exclusionRectP1		= Vector2.ZERO_VECTOR;
	@Configurable(comment = "P2 of rectangle defining a range where objects are ignored")
	private static IVector2						exclusionRectP2		= Vector2.ZERO_VECTOR;
	@Configurable
	private static boolean						ownThread				= false;
	
	
	private ExecutorService						execService;
	private final Object							execServiceSync		= new Object();
	private final Object							lastSeenBallSync		= new Object();
	
	private long									frameId					= 0;
	
	
	@Configurable(comment = "correct flying balls")
	private static boolean						correctFlyingBalls	= true;
	private static final double				MAX_DIST_BALL			= 50;
	private static final double				MAX_ORIENTATION_DIFF	= 0.1;
	
	private final Altigraph						altigraph				= new Altigraph();
	
	static
	{
		ConfigRegistration.registerClass("wp", AWorldPredictor.class);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param config
	 */
	public AWorldPredictor(final SubnodeConfiguration config)
	{
		ConfigRegistration.registerConfigurableCallback("wp", this);
	}
	
	
	private void startTime(final long frameId)
	{
		if (timer != null)
		{
			timer.start(ETimable.WP, frameId);
		}
	}
	
	
	private void stopTime(final long frameId)
	{
		if (timer != null)
		{
			timer.stop(ETimable.WP, frameId);
		}
	}
	
	
	private boolean isWithinExclusionRectangle(final IVector2 p)
	{
		Rectangle rect = new Rectangle(exclusionRectP1, exclusionRectP2);
		return rect.isPointInShape(p, -0.00001f);
	}
	
	
	private <T extends ACamObject> List<T> filterCamObjects(final List<T> incoming)
	{
		List<T> newObjects = new ArrayList<>();
		for (T newBall : incoming)
		{
			if (isWithinExclusionRectangle(newBall.getPos().getXYVector()))
			{
				continue;
			}
			newObjects.add(newBall);
		}
		return newObjects;
	}
	
	
	private void processCameraDetectionFrameInternal(final SSL_DetectionFrame detectionFrame, final TimeSync timeSync)
	{
		Double tLast = lastTCaptures.get(detectionFrame.getCameraId());
		if ((tLast != null) && (tLast > detectionFrame.getTCapture()))
		{
			return;
		}
		lastTCaptures.put(detectionFrame.getCameraId(), detectionFrame.getTCapture());
		
		startTime(frameId);
		
		long localCaptureNs = timeSync.sync(detectionFrame.getTCapture());
		long localSentNs = timeSync.sync(detectionFrame.getTSent());
		
		final List<CamBall> balls = new ArrayList<CamBall>();
		final List<CamRobot> blues = new ArrayList<CamRobot>();
		final List<CamRobot> yellows = new ArrayList<CamRobot>();
		
		// --- if we play from left to right, turn ball and robots, so that we're always playing from right to left ---
		// --- process team Blue ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, ETeamColor.BLUE, frameId, detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process team Yellow ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(bot, ETeamColor.YELLOW, frameId,
					detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process ball ---
		for (final SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, localCaptureNs, localSentNs, detectionFrame.getCameraId(),
					frameId));
		}
		
		List<CamRobot> newBotY = filterCamObjects(yellows);
		List<CamRobot> newBotB = filterCamObjects(blues);
		List<CamBall> newBalls = filterCamObjects(balls);
		
		
		CamDetectionFrame cFrame = new CamDetectionFrame(localCaptureNs, localSentNs, detectionFrame.getCameraId(),
				frameId, newBalls, newBotY, newBotB);
		processCamDetectionFrame(cFrame);
		
		ExtendedCamDetectionFrame eFrame = processCamDetectionFrame(cFrame);
		
		for (IWorldFrameObserver obs : observers)
		{
			obs.onNewCamDetectionFrame(eFrame);
		}
		for (IWorldFrameObserver obs : prioObservers)
		{
			obs.onNewCamDetectionFrame(eFrame);
		}
		processCameraDetectionFrame(eFrame);
		stopTime(frameId);
		frameId++;
	}
	
	
	/**
	 * @param cFrame
	 * @return
	 */
	public ExtendedCamDetectionFrame processCamDetectionFrame(final CamDetectionFrame cFrame)
	{
		CamBall ball = findCurrentBall(cFrame.getBalls());
		ball = correctBall(ball, cFrame.getRobotsYellow(), cFrame.getRobotsBlue());
		return new ExtendedCamDetectionFrame(cFrame, ball);
	}
	
	
	private static CamRobot convertRobot(
			final SSL_DetectionRobot bot,
			final ETeamColor color,
			final long frameId,
			final int camId,
			final long tCapture,
			final long tSent)
	{
		double orientation = bot.getOrientation();
		double x = bot.getX();
		double y = bot.getY();
		BotID botId = BotID.createBotId(bot.getRobotId(), color);
		return new CamRobot(bot.getConfidence(), bot.getPixelX(), bot.getPixelY(), tCapture, tSent, camId, frameId, x, y,
				orientation, bot.getHeight(), botId);
	}
	
	
	private static CamBall convertBall(final SSL_DetectionBall ball, final long tCapture, final long tSent,
			final int camId,
			final long frameId)
	{
		double x;
		double y;
		x = ball.getX();
		y = ball.getY();
		
		return new CamBall(ball.getConfidence(),
				ball.getArea(),
				x, y,
				ball.getZ(),
				ball.getPixelX(), ball.getPixelY(),
				tCapture,
				tSent,
				camId,
				frameId);
	}
	
	
	private CamBall findCurrentBall(final List<CamBall> balls)
	{
		synchronized (lastSeenBallSync)
		{
			double shortestDifference = Double.MAX_VALUE;
			CamBall selectedBall = null;
			for (CamBall ball : balls)
			{
				if (isWithinExclusionRectangle(ball.getPos().getXYVector()))
				{
					continue;
				}
				
				double diff = ball.getPos().subtractNew(lastSeenBall.getPos()).getLength2();
				if (diff < shortestDifference)
				{
					selectedBall = ball;
					shortestDifference = diff;
				}
			}
			if (selectedBall == null)
			{
				return lastSeenBall;
			}
			
			double dt = (selectedBall.getTimestamp() - lastSeenBall.getTimestamp()) / 1e9;
			double dist = selectedBall.getPos().subtractNew(lastSeenBall.getPos()).getLength2() / 1000.0;
			double vel = dist / dt;
			if (vel > 15)
			{
				// high velocity, probably noise
				log.debug("high vel: " + vel);
				return lastSeenBall;
			}
			
			double waitForNextBallTime = 0;
			if (!Geometry.getFieldWBorders().isPointInShape(selectedBall.getPos().getXYVector()) &&
					Geometry.getFieldWBorders().isPointInShape(lastSeenBall.getPos().getXYVector()))
			{
				waitForNextBallTime += 1;
			}
			
			if (selectedBall.getCameraId() != lastSeenBall.getCameraId())
			{
				waitForNextBallTime += 0.05;
			}
			
			if (dt < waitForNextBallTime)
			{
				return lastSeenBall;
			}
			
			lastSeenBall = selectedBall;
			shortestDifference = Double.MAX_VALUE;
			return new CamBall(lastSeenBall);
		}
	}
	
	
	protected abstract void processCameraDetectionFrame(final ExtendedCamDetectionFrame frame);
	
	
	protected void processCameraGeometry(final CamGeometry geometry)
	{
	}
	
	
	protected void start()
	{
	}
	
	
	protected void stop()
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param swf
	 */
	public final void pushFrame(final SimpleWorldFrame swf)
	{
		try
		{
			if (swf.getTimestamp() < latestTimestamp)
			{
				return;
			}
			latestTimestamp = swf.getTimestamp();
			
			SimpleWorldFrame ppSwf = swf;
			for (IWfPostProcessor pp : postProcessors)
			{
				ppSwf = pp.process(ppSwf);
			}
			WorldFrameWrapper wrapped = infoProcessor.createWorldFrameWrapper(ppSwf);
			for (IWorldFrameObserver c : prioObservers)
			{
				c.onNewWorldFrame(wrapped);
			}
			infoProcessor.processSimpleWorldFrame(wrapped);
			for (IWorldFrameObserver c : observers)
			{
				c.onNewWorldFrame(wrapped);
			}
		} catch (Throwable err)
		{
			log.error("Error processing worldframe", err);
		}
	}
	
	
	protected void processMotionContext(final MotionContext context, final long timestamp)
	{
		for (IWfPostProcessor pp : postProcessors)
		{
			pp.processMotionContext(context, timestamp);
		}
	}
	
	
	/**
	 * Set a new last ball pos to force the BallProcessor to use another visible ball
	 * 
	 * @param pos
	 */
	public void setLatestBallPosHint(final IVector2 pos)
	{
		synchronized (lastSeenBallSync)
		{
			long timestamp = lastSeenBall.getTimestamp() + (long) (1e8);
			lastSeenBall = new CamBall(1, 0, pos.x(), pos.y(), 0, 0, 0, timestamp, timestamp, lastSeenBall.getCameraId(),
					lastSeenBall.getFrameId());
			resetBall(timestamp, pos);
		}
	}
	
	
	/**
	 * correct the ball frame, if ball is flying
	 * 
	 * @param ballToUse
	 * @param yellowBots
	 * @param blueBots
	 * @return
	 */
	public CamBall correctBall(final CamBall ballToUse, final List<CamRobot> yellowBots, final List<CamRobot> blueBots)
	{
		if (correctFlyingBalls)
		{
			// get Data of Ball and Bots
			final List<CamRobot> bots = new ArrayList<>(blueBots.size() + yellowBots.size());
			bots.addAll(blueBots);
			bots.addAll(yellowBots);
			
			// if kick possible, add new fly
			CamRobot possShooter = findPossibleShooter(ballToUse, bots);
			if (possShooter != null)
			{
				// append new fly
				// log.trace("kicker zone identified: " + possShooter.getPos() + " " + possShooter.getOrientation());
				altigraph.addKickerZoneIdentified(possShooter.getPos(), possShooter.getOrientation());
			}
			
			// append the ball to old and new flys
			altigraph.addCamFrame(ballToUse.getPos().getXYVector(), ballToUse.getCameraId());
			
			// if ball is flying
			if (altigraph.isBallFlying())
			{
				// log.trace("Ball is flying: " + altigraph.getCorrectedFrame().z());
				CamBall newBall = new CamBall(ballToUse.getConfidence(), ballToUse.getArea(), altigraph
						.getCorrectedFrame().x(),
						altigraph.getCorrectedFrame().y(), altigraph.getCorrectedFrame().z(),
						ballToUse.getPixelX(),
						ballToUse.getPixelY(), ballToUse.gettCapture(), ballToUse.gettSent(), ballToUse.getCameraId(),
						ballToUse.getFrameId());
				return newBall;
			}
		}
		return ballToUse;
	}
	
	
	/*
	 * find out the potential kicker-bot
	 */
	private CamRobot findPossibleShooter(final CamBall ball, final List<CamRobot> bots)
	{
		for (final CamRobot bot : bots)
		{
			IVector2 kickerPos = GeoMath
					.getBotKickerPos(bot.getPos(), bot.getOrientation(), Geometry.getCenter2DribblerDistDefault());
			if (GeoMath.distancePP(kickerPos, ball.getPos().getXYVector()) > (MAX_DIST_BALL + Geometry
					.getBotRadius()))
			{
				continue;
			}
			IVector2 bot2Ball = ball.getPos().getXYVector().subtractNew(bot.getPos());
			if (Math.abs(AngleMath.difference(bot2Ball.getAngle(), bot.getOrientation())) > MAX_ORIENTATION_DIFF)
			{
				continue;
			}
			
			return bot;
		}
		return null;
	}
	
	
	/**
	 * @param timestamp
	 * @param pos
	 */
	public void resetBall(final long timestamp, final IVector2 pos)
	{
	}
	
	
	/**
	 * @param consumer
	 */
	public final void addWorldFrameConsumer(final IWorldFrameObserver consumer)
	{
		observers.add(consumer);
	}
	
	
	/**
	 * @param consumer
	 */
	public final void removeWorldFrameConsumer(final IWorldFrameObserver consumer)
	{
		observers.remove(consumer);
	}
	
	
	/**
	 * @param consumer
	 */
	public final void addWorldFramePrioConsumer(final IWorldFrameObserver consumer)
	{
		prioObservers.add(consumer);
	}
	
	
	/**
	 * @param consumer
	 */
	public final void removeWorldFramePrioConsumer(final IWorldFrameObserver consumer)
	{
		prioObservers.remove(consumer);
	}
	
	
	/**
	 * @param pp
	 */
	public final void addPostProcessor(final IWfPostProcessor pp)
	{
		postProcessors.add(pp);
	}
	
	
	/**
	 * @param pp
	 * @return
	 */
	public final boolean removePostProcessor(final IWfPostProcessor pp)
	{
		return postProcessors.remove(pp);
	}
	
	
	@Override
	public final void onNewCameraFrame(final SSL_DetectionFrame frame, final TimeSync timeSync)
	{
		synchronized (execServiceSync)
		{
			if (ownThread && (execService != null))
			{
				execService.execute(() -> processCameraDetectionFrameInternal(frame, timeSync));
			} else
			{
				processCameraDetectionFrameInternal(frame, timeSync);
			}
		}
	}
	
	
	@Override
	public final void onNewCameraGeometry(final CamGeometry geometry)
	{
		if (Geometry.isReceiveGeometry())
		{
			Geometry.setCamDetection(geometry);
			if (!geometryReceived)
			{
				geometryReceived = true;
				log.info("Received geometry from vision");
			}
		}
		
		processCameraGeometry(geometry);
	}
	
	
	@Override
	public void onClearCamFrame()
	{
		for (IWorldFrameObserver obs : observers)
		{
			obs.onClearWorldFrame();
			obs.onClearCamDetectionFrame();
		}
		for (IWorldFrameObserver obs : prioObservers)
		{
			obs.onClearWorldFrame();
			obs.onClearCamDetectionFrame();
		}
		synchronized (lastSeenBallSync)
		{
			lastSeenBall = new CamBall();
		}
		infoProcessor.stop();
		infoProcessor.start();
	}
	
	
	@Override
	public final void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public final void deinitModule()
	{
	}
	
	
	@Override
	public final void startModule() throws StartModuleException
	{
		start();
		
		geometryReceived = false;
		Geometry.refresh();
		
		if (!observers.isEmpty())
		{
			log.warn("There were observers left: " + observers);
			observers.clear();
		}
		if (!prioObservers.isEmpty())
		{
			log.warn("There were prioObservers left: " + prioObservers);
			prioObservers.clear();
		}
		infoProcessor.start();
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.warn("No timer found");
		}
	}
	
	
	@Override
	public final void stopModule()
	{
		stop();
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		infoProcessor.stop();
		onClearCamFrame();
		if (execService != null)
		{
			synchronized (execServiceSync)
			{
				execService.shutdown();
				execService = null;
			}
		}
	}
	
	
	/**
	 * @param configClient
	 */
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		synchronized (execServiceSync)
		{
			if (ownThread && (execService == null))
			{
				execService = Executors.newSingleThreadExecutor(new NamedThreadFactory("WP_worker"));
			}
		}
	}
	
	
	/**
	 * @return the postProcessors
	 */
	public List<IWfPostProcessor> getPostProcessors()
	{
		return postProcessors;
	}
}
