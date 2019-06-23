/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.tigers.sumatra.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotStatus2Vision extends AModule implements Runnable, IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger			log				= Logger.getLogger(BotStatus2Vision.class.getName());
	
	private static final int				PORT				= 10006;
	
	/** */
	public static final String				MODULE_TYPE		= "BotStatus2Vision";
	/** */
	public static final String				MODULE_ID		= "BotStatus2Vision";
	
	private MulticastUDPTransmitter		transmitter;
	private int									frameId			= 0;
	private ABotManager						botManager;
	private ScheduledExecutorService		service;
	private final List<QueueData>			sendingQueue	= Collections.synchronizedList(new ArrayList<>());
	private ExtendedCamDetectionFrame	camFrame			= null;
	
	private static final double			DELAY				= 0.0;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param config
	 */
	public BotStatus2Vision(final SubnodeConfiguration config)
	{
	}
	
	
	@Override
	public void run()
	{
		try
		{
			SSL_DetectionFrame.Builder dFrame = SSL_DetectionFrame.newBuilder();
			for (IBot bot : botManager.getAllBots().values())
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
			
			if (camFrame != null)
			{
				for (CamBall cBall : camFrame.getBalls())
				{
					SSL_DetectionBall.Builder ball = SSL_DetectionBall.newBuilder();
					ball.setConfidence(1);
					ball.setPixelX((float) cBall.getPixel().x());
					ball.setPixelY((float) cBall.getPixel().y());
					ball.setX((float) cBall.getPos().x());
					ball.setY((float) cBall.getPos().y());
					ball.setZ((float) cBall.getPos().z());
					dFrame.addBalls(ball);
				}
			}
			
			dFrame.setCameraId(0);
			dFrame.setFrameNumber(frameId);
			dFrame.setTCapture(System.currentTimeMillis() / 1000.0);
			dFrame.setTSent(System.currentTimeMillis() / 1000.0);
			
			sendingQueue.add(new QueueData(System.nanoTime(), dFrame));
			
			while (!sendingQueue.isEmpty())
			{
				QueueData qd = sendingQueue.get(0);
				if (qd.t < (System.nanoTime() - (long) (DELAY * 1e9)))
				{
					SSL_WrapperPacket.Builder pkg = SSL_WrapperPacket.newBuilder();
					pkg.setDetection(qd.dFrame);
					byte[] data = pkg.build().toByteArray();
					transmitter.send(data);
					sendingQueue.remove(0);
				} else
				{
					break;
				}
			}
			
			frameId++;
		} catch (Throwable err)
		{
			log.error("Exception in main loop", err);
		}
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		camFrame = frame;
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		transmitter = new MulticastUDPTransmitter(0, "224.5.23.2", PORT);
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find botManager", err);
		}
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp", err);
		}
		
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("BotStatus2Vision"));
		service.scheduleAtFixedRate(this, 0, 16, TimeUnit.MILLISECONDS);
		
		
	}
	
	
	@Override
	public void stopModule()
	{
		service.shutdown();
		try
		{
			Validate.isTrue(service.awaitTermination(1, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find wp", err);
		}
		transmitter.cleanup();
	}
	
	private static class QueueData
	{
		long								t;
		SSL_DetectionFrame.Builder	dFrame;
		
		
		/**
		 * @param t
		 * @param dFrame
		 */
		public QueueData(final long t, final SSL_DetectionFrame.Builder dFrame)
		{
			super();
			this.t = t;
			this.dFrame = dFrame;
		}
	}
}
