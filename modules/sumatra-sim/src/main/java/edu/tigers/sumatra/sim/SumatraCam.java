/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2015
 * Author(s): geforce
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;


import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.TimeSync;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.sim.physicUtility.CollisionHandler;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;


/**
 * Simulate vision in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraCam extends ACam implements Runnable
{
	private static final Logger		log					= Logger.getLogger(SumatraCam.class.getName());
																		
	private static long					dt						= 16_000_000;
	private static long					simDt					= 1_000_000;
	private long							simTime;
												
	private boolean						running				= true;
	private CountDownLatch				steppingLatch		= new CountDownLatch(0);
																		
	private double							simSpeed				= 1;
																		
	private ScheduledExecutorService	service				= null;
	private long							frameId				= 0;
	private final List<SumatraBot>	bots					= new CopyOnWriteArrayList<>();
	private ISimulatedBall				ball					= new SimulatedBall();
																		
	private CollisionHandler			collisionHandler	= new CollisionHandler();
	private final TimeSync				timeSync				= new TimeSync();
																		
	private MulticastUDPTransmitter	transmitter;
	private final boolean				export;
												
	private static String				targetAddr			= "224.5.23.5";
																		
	private static int					targetPort			= 10006;
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public SumatraCam(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
		
		export = subnodeConfiguration.getBoolean("export", false);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		simTime = 0;
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SumatraCam"));
		service.execute(this);
		if (export)
		{
			transmitter = new MulticastUDPTransmitter(0, targetAddr, targetPort);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		service.shutdownNow();
		if (transmitter != null)
		{
			try
			{
				service.awaitTermination(2, TimeUnit.SECONDS);
			} catch (InterruptedException e)
			{
				log.error("Timed out waiting for service to shut down", e);
			}
			transmitter.cleanup();
		}
	}
	
	
	/**
	 * @param bot
	 */
	public void registerBot(final SumatraBot bot)
	{
		bots.add(bot);
	}
	
	
	/**
	 * @param bot
	 */
	public void unregisterBot(final SumatraBot bot)
	{
		bots.remove(bot);
	}
	
	
	private void simulate(final long dt)
	{
		ball.update(dt / 1e9);
		for (SumatraBot bot : bots)
		{
			bot.update(dt / 1e9);
		}
		
		processBotCollisions();
		
		for (int i = 0; (i < (dt / simDt)) && (i < 100); i++)
		{
			double stepDt = simDt / 1e9;
			simTime += simDt;
			MotionContext mc = getMotionContext(simTime);
			ball.step(stepDt, mc);
			for (int j = 0; j < bots.size(); j++)
			{
				SumatraBot bot = bots.get(j);
				bot.step(stepDt, mc);
			}
		}
	}
	
	
	private void processBotCollisions()
	{
		List<SumatraBotPair> collidingBots = collisionHandler.getCollidingBots(bots);
		
		for (SumatraBotPair collidingPair : collidingBots)
		{
			collisionHandler.correctCollidingBots(collidingPair);
		}
	}
	
	
	private MotionContext getMotionContext(final long timestamp)
	{
		MotionContext mc = new MotionContext();
		for (SumatraBot bot : bots)
		{
			BotInfo bi = new BotInfo(bot.getBotId(), bot.getPos());
			bi.setVel(bot.getVel());
			bi.setKickSpeed(bot.getKickSpeed(timestamp));
			bi.setChip(bot.getMatchCtrl().getDevice() == EKickerDevice.CHIP);
			bi.setDribbleRpm(bot.getMatchCtrl().getDribbleSpeed());
			bi.setBallContact(bot.isBarrierInterrupted());
			mc.getBots().put(bot.getBotId(), bi);
		}
		return mc;
	}
	
	
	/**
	 * @return
	 */
	public final SSL_DetectionFrame createFrame()
	{
		simulate(dt);
		
		SSL_DetectionFrame.Builder dFrame = SSL_DetectionFrame.newBuilder();
		for (SumatraBot bot : bots)
		{
			if (bot.getSensoryPos().isPresent())
			{
				IVector3 pose = bot.getSensoryPos().get();
				SSL_DetectionRobot.Builder dBot = SSL_DetectionRobot.newBuilder();
				dBot.setOrientation((float) pose.z());
				dBot.setX((float) pose.x());
				dBot.setY((float) pose.y());
				dBot.setConfidence(1);
				dBot.setRobotId(bot.getBotId().getNumber());
				dBot.setPixelX(0);
				dBot.setPixelY(0);
				if (bot.getColor() == ETeamColor.BLUE)
				{
					dFrame.addRobotsBlue(dBot);
				} else
				{
					dFrame.addRobotsYellow(dBot);
				}
			}
		}
		
		SSL_DetectionBall.Builder dBall = SSL_DetectionBall.newBuilder();
		dBall.setConfidence(1);
		dBall.setPixelX(0);
		dBall.setPixelY(0);
		dBall.setX((float) ball.getPos().x());
		dBall.setY((float) ball.getPos().y());
		dBall.setZ((float) ball.getPos().z());
		dFrame.addBalls(dBall);
		
		
		dFrame.setCameraId(0);
		dFrame.setFrameNumber((int) frameId);
		dFrame.setTCapture(simTime / 1e9);
		dFrame.setTSent(simTime / 1e9);
		
		frameId++;
		
		return dFrame.build();
	}
	
	
	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				steppingLatch.await();
			} catch (InterruptedException e1)
			{
				return;
			}
			
			long t0 = System.nanoTime();
			try
			{
				final SSL_DetectionFrame frame = createFrame();
				notifyNewCameraFrame(frame, timeSync);
				exportVisionFrame();
			} catch (Throwable e)
			{
				log.error("Exception in SumatraCam.", e);
			}
			long t1 = System.nanoTime();
			
			if (!running)
			{
				steppingLatch = new CountDownLatch(1);
			} else
			{
				long mDt = (long) (dt / simSpeed);
				long sleep = (mDt - (t1 - t0));
				if (sleep > 0)
				{
					assert sleep < (long) 1e9;
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
		}
	}
	
	
	private void exportVisionFrame()
	{
		if (transmitter == null)
		{
			return;
		}
		try
		{
			SSL_DetectionFrame.Builder dFrame = SSL_DetectionFrame.newBuilder();
			for (SumatraBot sBot : bots)
			{
				IVector3 pose = sBot.getPos();
				SSL_DetectionRobot.Builder dBot = SSL_DetectionRobot.newBuilder();
				dBot.setOrientation((float) pose.z());
				dBot.setX((float) pose.x());
				dBot.setY((float) pose.y());
				dBot.setConfidence(1);
				dBot.setRobotId(sBot.getBotId().getNumber());
				dBot.setPixelX(0);
				dBot.setPixelY(0);
				if (sBot.getColor() == ETeamColor.BLUE)
				{
					dFrame.addRobotsBlue(dBot);
				} else
				{
					dFrame.addRobotsYellow(dBot);
				}
			}
			
			SSL_DetectionBall.Builder sslBall = SSL_DetectionBall.newBuilder();
			sslBall.setConfidence(1);
			sslBall.setPixelX(0);
			sslBall.setPixelY(0);
			sslBall.setX((float) ball.getPos().x());
			sslBall.setY((float) ball.getPos().y());
			sslBall.setZ((float) ball.getPos().z());
			dFrame.addBalls(sslBall);
			
			dFrame.setCameraId(0);
			dFrame.setFrameNumber((int) frameId);
			
			dFrame.setTCapture(System.currentTimeMillis() / 1000.0);
			dFrame.setTSent(System.currentTimeMillis() / 1000.0);
			
			SSL_WrapperPacket.Builder pkg = SSL_WrapperPacket.newBuilder();
			pkg.setDetection(dFrame);
			byte[] data = pkg.build().toByteArray();
			transmitter.send(data);
			
		} catch (Throwable err)
		{
			log.error("Exception in main loop", err);
		}
	}
	
	
	/**
	 * @param time
	 */
	public void reset(final long time)
	{
		boolean run = running;
		if (run)
		{
			pause();
		}
		simTime = time;
		notifyVisionLost();
		ball = new SimulatedBall();
		if (run)
		{
			play();
		}
	}
	
	
	/**
	 * 
	 */
	public void pause()
	{
		running = false;
		steppingLatch = new CountDownLatch(1);
	}
	
	
	/**
	 * 
	 */
	public void play()
	{
		running = true;
		steppingLatch.countDown();
	}
	
	
	/**
	 * @param count
	 */
	public void step(final int count)
	{
		steppingLatch.countDown();
	}
	
	
	@Override
	public void replaceBall(final IVector3 pos, final IVector3 vel)
	{
		ball.setPos(pos);
		ball.setVel(vel);
	}
	
	
	/**
	 * @return the bots
	 */
	public final List<SumatraBot> getBots()
	{
		return bots;
	}
	
	
	/**
	 * @return the ball
	 */
	public final ISimulatedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @return the simSpeed
	 */
	public double getSimSpeed()
	{
		return simSpeed;
	}
	
	
	/**
	 * @param simSpeed the simSpeed to set
	 */
	public void setSimSpeed(final double simSpeed)
	{
		this.simSpeed = simSpeed;
	}
	
	
	/**
	 * @return the simTime
	 */
	public long getSimTime()
	{
		return simTime;
	}
	
	
	/**
	 * @return the frameId
	 */
	public long getFrameId()
	{
		return frameId;
	}
}
