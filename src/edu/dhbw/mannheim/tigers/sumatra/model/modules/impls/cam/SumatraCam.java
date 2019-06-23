/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2015
 * Author(s): geforce
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SumatraBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.IBallReplacer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Simulate vision in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraCam extends ACam implements IBallReplacer, Runnable
{
	private static final Logger		log		= Logger.getLogger(SumatraCam.class.getName());
	
	@Configurable(comment = "Vision Speed")
	private static float					dt			= 0.02f;
	@Configurable(comment = "Simulation Speed")
	private static float					simDt		= 0.001f;
	
	private ScheduledExecutorService	service	= null;
	private long							frameId	= 0;
	private final List<SumatraBot>	bots		= new ArrayList<>();
	private final ISimulatedBall		ball		= new SimulatedBall();
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public SumatraCam(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SumatraCam"));
		service.scheduleAtFixedRate(this, 1000, (long) (dt * 1000), TimeUnit.MILLISECONDS);
	}
	
	
	@Override
	public void stopModule()
	{
		service.shutdown();
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
	
	
	private void simulate()
	{
		for (int i = 0; i < (dt / simDt); i++)
		{
			ball.step(simDt);
			for (int j = 0; j < bots.size(); j++)
			{
				SumatraBot bot = bots.get(j);
				
				bot.step(simDt);
				bot.ballInteraction(ball);
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public final CamDetectionFrame createFrame()
	{
		simulate();
		
		long time = SumatraClock.nanoTime();
		List<CamBall> balls = new ArrayList<>();
		balls.add(ball.getCamBall());
		List<CamRobot> yellowBots = new ArrayList<>();
		List<CamRobot> blueBots = new ArrayList<>();
		for (SumatraBot bot : bots)
		{
			CamRobot robot = new CamRobot(1, bot.getBotID().getNumber(), bot.getPos().x(), bot.getPos().y(), bot
					.getPos().z(), 0,
					0, 0, time, 0);
			if (bot.getBotID().getTeamColor() == ETeamColor.YELLOW)
			{
				yellowBots.add(robot);
			} else
			{
				blueBots.add(robot);
			}
		}
		
		final CamDetectionFrame frame = new CamDetectionFrame(time, time, time, 0, frameId++, balls, yellowBots,
				blueBots);
		
		return frame;
	}
	
	
	@Override
	public void run()
	{
		try
		{
			final CamDetectionFrame frame = createFrame();
			notifyNewCameraFrame(frame);
		} catch (Exception e)
		{
			log.error("Exception in SumatraCam.", e);
		}
	}
	
	
	@Override
	public void replaceBall(final IVector2 pos)
	{
		ball.setPos(new Vector3(pos, 0));
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 */
	public void replaceBall(final IVector2 pos, final IVector2 vel)
	{
		ball.setVel(vel);
		replaceBall(pos);
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
}
