/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.thread.SimulatedScheduledExecutorService;
import edu.tigers.sumatra.timer.ATimer;
import edu.tigers.sumatra.timer.ETimable;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * a thread for a path finder<br>
 * it is triggered in a certain interval to refresh the path used by the bot
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public final class PathFinderThread
{
	
	
	private static final Logger							log					= Logger.getLogger(PathFinderThread.class
																									.getName());
																									
	private WorldFrameWrapper								wFrameWrapper		= null;
	private final IWorldFrameObserver					wfConsumer			= new WorldFrameConsumer();
																							
	private final Map<Sisyphus, ScheduledFuture<?>>	futures				= new ConcurrentHashMap<>();
																							
																							
	// ### util ###
	private ScheduledExecutorService						scheduler			= new SimulatedScheduledExecutorService();
	private SumatraTimer										timer					= null;
																							
																							
	@Configurable(comment = "Number of threads in thread pool for pathplanning. Needs moduli restart.")
	private static int										numberOfThreads	= 2;
																							
																							
	static
	{
		ConfigRegistration.registerClass("sisyphus", PathFinderThread.class);
	}
	
	
	/**
	 */
	public PathFinderThread()
	{
	}
	
	
	/**
	 */
	public void startScheduler()
	{
		SumatraTimer timer = null;
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.error("No timer found.");
		}
		this.timer = timer;
		
		AWorldPredictor predictor;
		try
		{
			predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(wfConsumer);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP Modul not found", err);
		}
		
		scheduler = Executors.newScheduledThreadPool(numberOfThreads, new NamedThreadFactory(
				"Pathplanner"));
	}
	
	
	/**
	 */
	public void stopScheduler()
	{
		AWorldPredictor predictor;
		try
		{
			predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(wfConsumer);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP Modul not found", err);
		}
		
		scheduler.shutdown();
		
		try
		{
			scheduler.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException err)
		{
			log.warn("PathFinderThread could not be stopped correctly.");
		}
	}
	
	
	/**
	 * Start Pathplanning and return
	 * 
	 * @param sisyphus
	 */
	public void start(final Sisyphus sisyphus)
	{
		ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runner(sisyphus), 0,
				sisyphus.getPathPlanningInterval(),
				TimeUnit.MILLISECONDS);
		futures.put(sisyphus, future);
	}
	
	
	/**
	 * @param sisyphus
	 */
	public void stop(final Sisyphus sisyphus)
	{
		ScheduledFuture<?> future = futures.remove(sisyphus);
		if (future == null)
		{
			log.warn("A thread for given sisyphus (" + sisyphus.getCustomId() + ") is not known.");
		} else
		{
			boolean canceled = future.cancel(false);
			if (!canceled)
			{
				try
				{
					future.get();
					log.warn("Could not cancel scheduled runner for sisyphus " + sisyphus.getCustomId());
				} catch (InterruptedException | ExecutionException err)
				{
					log.warn("Could not cancel scheduled runner for sisyphus " + sisyphus.getCustomId(), err);
				}
			}
		}
	}
	
	
	private void startTime(final long id, final int customId)
	{
		if (timer != null)
		{
			timer.start(ETimable.PATHPLANNING, id, customId);
		}
	}
	
	
	private void stopTime(final long id, final int customId)
	{
		if (timer != null)
		{
			timer.stop(ETimable.PATHPLANNING, id, customId);
		}
	}
	
	
	/**
	 * @param wfw
	 */
	public void setWorldframe(final WorldFrameWrapper wfw)
	{
		wFrameWrapper = wfw;
	}
	
	private class Runner implements Runnable
	{
		private final Sisyphus sisyphus;
		
		
		/**
		 * @param sisyphus
		 */
		public Runner(final Sisyphus sisyphus)
		{
			this.sisyphus = sisyphus;
		}
		
		
		@Override
		public void run()
		{
			try
			{
				if (wFrameWrapper == null)
				{
					return;
				}
				
				// get local copies to assure that we are using the same object throughout this method
				final BotID botId = sisyphus.getBotId();
				final WorldFrame worldFrame = wFrameWrapper.getWorldFrame(botId.getTeamColor());
				final PathFinderInput localPathFinderInput = sisyphus.getPathFinderInput();
				
				if ((worldFrame == null) || (localPathFinderInput == null))
				// || (!worldFrame.getTigerBotsAvailable().containsKey(botId)))
				{
					// nothing to do atm
					return;
				}
				
				final long frameId = worldFrame.getId();
				startTime(frameId, sisyphus.getCustomId());
				
				try
				{
					localPathFinderInput.getFieldInfo().updateWorldFrame(worldFrame);
					IPath newPath = sisyphus.getPathFinder().calcPath(localPathFinderInput);
					sisyphus.onPotentialNewPath(newPath);
					if ((sisyphus.getCurrentPath() == null)
							|| sisyphus.getPathFilter().accept(localPathFinderInput, newPath, sisyphus.getCurrentPath()))
					{
						sisyphus.setCurrentPath(newPath);
						sisyphus.onNewPath(newPath);
					}
				} finally
				{
					stopTime(frameId, sisyphus.getCustomId());
				}
			} catch (Exception err)
			{
				log.error("Exception in patherfinder thread", err);
			}
		}
	}
	
	private class WorldFrameConsumer implements IWorldFrameObserver
	{
		@Override
		public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
		{
			PathFinderThread.this.wFrameWrapper = wFrameWrapper;
		}
	}
}
