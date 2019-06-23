/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.refbox.RefBox;
import edu.tigers.sumatra.referee.source.refbox.time.ITimeProvider;
import edu.tigers.sumatra.sim.util.CollisionHandler;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;


/**
 * Simulate vision in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author AndreR <andre@ryll.cc>
 */
public class SumatraSimulator extends AVisionFilter implements Runnable, ITimeProvider
{
	private static final Logger log = Logger
			.getLogger(SumatraSimulator.class.getName());
	
	private static final long CAM_DT = 16_000_000;
	private static final long SIM_DT = 1_000_000;
	private static final double SIMULATION_BUFFER_TIME = 5;
	private final Map<BotID, SumatraBot> bots = new ConcurrentHashMap<>();
	private long simTime;
	private boolean running = true;
	private CountDownLatch steppingLatch = new CountDownLatch(0);
	private double simSpeed = 1;
	private boolean simulate = true;
	private ScheduledExecutorService service = null;
	private long frameId = 0;
	private ISimulatedBall ball = new SimulatedBall();
	private CollisionHandler collisionHandler = new CollisionHandler();
	
	private final Deque<FilteredVisionFrame> stateBuffer = new ArrayDeque<>();
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public SumatraSimulator(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		simTime = 0;
		frameId = 0;
		running = true;
		steppingLatch = new CountDownLatch(0);
		simSpeed = 1;
		simulate = true;
		ball = new SimulatedBall();
		stateBuffer.clear();
		
		if (!bots.isEmpty())
		{
			log.warn("Bots list is not empty on startup: " + bots);
		}
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SumatraCam"));
		service.execute(this);
		
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			RefBox refBox = (RefBox) referee.getSource(ERefereeMessageSource.INTERNAL_REFBOX);
			refBox.setTimeProvider(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find AReferee module.", e);
		}
		
	}
	
	
	@Override
	public void stopModule()
	{
		service.shutdownNow();
		try
		{
			Validate.isTrue(service.awaitTermination(1, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	/**
	 * @param bot
	 */
	void registerBot(final SumatraBot bot)
	{
		if (bots.containsKey(bot.getBotId()))
		{
			log.warn("Registering existing bot! Skipping. " + bot);
		} else
		{
			bots.put(bot.getBotId(), bot);
		}
	}
	
	
	/**
	 * @param bot
	 */
	void unregisterBot(final SumatraBot bot)
	{
		SumatraBot removedBot = bots.remove(bot.getBotId());
		if (removedBot != bot)
		{
			log.warn("Unregistered bot does not match requested one: " + bot + " != " + removedBot);
		}
	}
	
	
	private void simulate(final long dt)
	{
		processBotCollisions();
		storeFrameInStateBuffer();
		
		for (int i = 0; (i < (dt / SIM_DT)) && (i < 100); i++)
		{
			double stepDt = SIM_DT / 1e9;
			simTime += SIM_DT;
			MotionContext mc = getMotionContext();
			ball.step(stepDt, mc);
			for (SumatraBot bot : bots.values())
			{
				bot.update(stepDt, simTime);
				bot.step(stepDt, mc);
			}
		}
		
	}
	
	
	private void storeFrameInStateBuffer()
	{
		FilteredVisionFrame latestState = stateBuffer.peekLast();
		if ((latestState == null) || (latestState.getTimestamp() < simTime))
		{
			stateBuffer.addLast(createFilteredFrame());
			
			while ((stateBuffer.peekFirst() != null)
					&& (((stateBuffer.peekLast().getTimestamp() - stateBuffer.peekFirst().getTimestamp())
							/ 1e9) >= SIMULATION_BUFFER_TIME))
			{
				stateBuffer.removeFirst();
			}
		}
	}
	
	
	private void processBotCollisions()
	{
		List<SumatraBotPair> collidingBots = collisionHandler.getCollidingBots(new ArrayList<>(bots.values()));
		
		for (SumatraBotPair collidingPair : collidingBots)
		{
			collisionHandler.correctCollidingBots(collidingPair);
		}
	}
	
	
	private MotionContext getMotionContext()
	{
		MotionContext mc = new MotionContext();
		for (SumatraBot bot : bots.values())
		{
			BotInfo bi = new BotInfo(bot.getBotId(), bot.getPos());
			bi.setVel(bot.getVel());
			bi.setKickSpeed(bot.getKickSpeed());
			bi.setChip(bot.getMatchCtrl().getSkill().getDevice() == EKickerDevice.CHIP);
			bi.setDribbleRpm(bot.getDribblerSpeed());
			bi.setBallContact(bot.isBarrierInterrupted());
			bi.setCenter2DribblerDist(bot.getCenter2DribblerDist());
			mc.getBots().put(bot.getBotId(), bi);
		}
		mc.getBallInfo().setPos(ball.getState().getPos());
		return mc;
	}
	
	
	/**
	 * @return
	 */
	public final FilteredVisionFrame createFilteredFrame()
	{
		List<FilteredVisionBot> filteredBots = new ArrayList<>();
		
		for (SumatraBot bot : bots.values())
		{
			if (bot.getSensoryPos().isPresent() && bot.getSensoryVel().isPresent())
			{
				IVector3 pose = bot.getSensoryPos().get();
				IVector3 vel = bot.getSensoryVel().get();
				FilteredVisionBot fBot = FilteredVisionBot.Builder.create()
						.withPos(pose.getXYVector())
						.withVel(vel.getXYVector())
						.withOrientation(pose.z())
						.withAVel(vel.z())
						.withId(bot.getBotId())
						.withQuality(1.0)
						.build();
				filteredBots.add(fBot);
			}
		}
		
		BallTrajectoryState ballState = ball.getState();
		FilteredVisionBall filteredBall = FilteredVisionBall.Builder.create()
				.withPos(ballState.getPos())
				.withVel(ballState.getVel())
				.withAcc(ballState.getAcc())
				.withLastVisibleTimestamp(simTime)
				.withIsChipped(ballState.isChipped())
				.withvSwitch(ballState.getvSwitchToRoll())
				.build();
		
		FilteredVisionFrame frame = FilteredVisionFrame.Builder.create()
				.withBots(filteredBots)
				.withBall(filteredBall)
				.withId(frameId)
				.withTimestamp(simTime)
				.build();
		
		frameId++;
		
		return frame;
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
				Thread.currentThread().interrupt();
				return;
			}
			
			long t0 = System.nanoTime();
			try
			{
				if (simulate)
				{
					simulate(CAM_DT);
				} else
				{
					simulate = true;
				}
				publishFilteredVisionFrame(createFilteredFrame());
			} catch (Exception e)
			{
				log.error("Exception in SumatraCam.", e);
			}
			long t1 = System.nanoTime();
			
			if (!running)
			{
				steppingLatch = new CountDownLatch(1);
			} else
			{
				long mDt = (long) (CAM_DT / simSpeed);
				long sleep = mDt - (t1 - t0);
				if (sleep > 0)
				{
					assert sleep < (long) 1e9;
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
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
		stateBuffer.clear();
		ball = new SimulatedBall();
		if (run)
		{
			play();
		}
	}
	
	
	/**
	 * Pause cam
	 */
	public void pause()
	{
		running = false;
	}
	
	
	/**
	 * Start cam
	 */
	public void play()
	{
		running = true;
		steppingLatch.countDown();
	}
	
	
	/**
	 * Do one step
	 */
	public void step()
	{
		steppingLatch.countDown();
	}
	
	
	public boolean isRunning()
	{
		return running;
	}
	
	
	/**
	 * Step backwards
	 */
	public void stepBack()
	{
		FilteredVisionFrame frame = stateBuffer.pollLast();
		if (frame != null)
		{
			for (SumatraBot bot : bots.values())
			{
				Optional<FilteredVisionBot> filtBot = frame.getBots().stream()
						.filter(b -> b.getBotID().equals(bot.getBotId()))
						.findFirst();
				
				if (!filtBot.isPresent())
				{
					continue;
				}
				
				bot.setPos(Vector3.from2d(filtBot.get().getPos(), filtBot.get().getOrientation()));
				bot.setVel(Vector3.from2d(filtBot.get().getVel(), filtBot.get().getAngularVel()));
			}
			
			FilteredVisionBall filteredState = frame.getBall();
			BallTrajectoryState ballState = BallTrajectoryState.aBallState()
					.withPos(filteredState.getPos())
					.withVel(filteredState.getVel())
					.withAcc(filteredState.getAcc())
					.withChipped(filteredState.isChipped())
					.withVSwitchToRoll(filteredState.getVSwitch())
					.build();
			
			ball.setState(ballState);
			
			simTime = frame.getTimestamp();
			simulate = false;
			steppingLatch.countDown();
		}
	}
	
	
	@Override
	public void resetBall(final IVector3 pos, final IVector3 vel)
	{
		ABallTrajectory traj = BallFactory
				.createTrajectoryFromKick(pos.getXYVector(), vel.multiplyNew(1e3), vel.z() > 0);
		
		ball.setState(traj.getMilliStateAtTime(0));
	}
	
	
	/**
	 * @return the bots
	 */
	public final Collection<SumatraBot> getBots()
	{
		return Collections.unmodifiableCollection(bots.values());
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
	
	
	@Override
	protected void updateCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		// Simulator generates own data and thus happily ignores any input here
	}
	
	
	@Override
	public long getTimeInMicroseconds()
	{
		return simTime / 1000;
	}
}
