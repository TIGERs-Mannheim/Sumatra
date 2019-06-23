/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.UpdateSplineDecisionMakerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * a thread for a path finder<br>
 * it is triggered in a certain interval to refresh the path used by the bot
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class PathFinderThread implements Runnable, IWorldFrameConsumer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger							log					= Logger.getLogger(PathFinderThread.class
																									.getName());
	private final IPathFinder								pathFinder;
	private final Sisyphus									sisyphus;
	private final BotID										botId;
	private final UpdateSplineDecisionMakerFactory	updateSplineDecision;
	private final SumatraTimer								timer;
	
	private boolean											active				= true;
	private WorldFrame										wFrame				= null;
	private PathFinderInput									pathFinderInput	= null;
	private Path												currentPath			= null;
	private boolean											wasLastRambo		= false;
	
	private final Object										syncRun				= new Object();
	private CountDownLatch									latchStartStop		= new CountDownLatch(0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param sisyphus this is typically Sisyphus
	 * @param botId the bot which is handled by this thread
	 */
	public PathFinderThread(final Sisyphus sisyphus, final BotID botId)
	{
		this.sisyphus = sisyphus;
		this.botId = botId;
		pathFinder = new ERRTPlanner_WPC();
		updateSplineDecision = new UpdateSplineDecisionMakerFactory();
		
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
			predictor.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP Modul not found", err);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Must be called on termination
	 */
	public void onTermination()
	{
		AWorldPredictor predictor;
		try
		{
			predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP Modul not found", err);
		}
	}
	
	
	@Override
	public void onNewSimpleWorldFrame(final SimpleWorldFrame worldFrame)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wFrame)
	{
		if (wFrame.getTeamColor() == botId.getTeamColor())
		{
			this.wFrame = wFrame;
		}
	}
	
	
	@Override
	public void onStop()
	{
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		wFrame = null;
	}
	
	
	/**
	 * @param pathFinderInput
	 */
	public void start(final PathFinderInput pathFinderInput)
	{
		synchronized (syncRun)
		{
			this.pathFinderInput = pathFinderInput;
			active = true;
		}
		awaitRoundFinished();
	}
	
	
	/**
	 */
	public void stop()
	{
		synchronized (syncRun)
		{
			active = false;
			pathFinderInput = null;
			currentPath = null;
		}
		awaitRoundFinished();
	}
	
	
	private void awaitRoundFinished()
	{
		latchStartStop = new CountDownLatch(1);
		try
		{
			if (!latchStartStop.await(1, TimeUnit.SECONDS))
			{
				log.warn("PathFinderThread did not run correctly! Might be due to AI exceptions");
			}
		} catch (InterruptedException err)
		{
			log.error("Interupted while waiting for thread start", err);
		}
	}
	
	
	@Override
	public void run()
	{
		synchronized (syncRun)
		{
			// get local copies to assure that we are using the same object throughout this method
			final WorldFrame worldFrame = wFrame;
			final PathFinderInput localPathFinderInput = pathFinderInput;
			
			if ((worldFrame == null) || (localPathFinderInput == null) || !active
					|| (worldFrame.getTigerBotsAvailable().getWithNull(botId) == null))
			{
				// nothing to do atm
				latchStartStop.countDown();
				return;
			}
			
			final long frameId = worldFrame.getId().getFrameNumber();
			startTime(frameId);
			
			try
			{
				localPathFinderInput.update(worldFrame);
				Path newPath = generatePath(localPathFinderInput, worldFrame);
				setNewPathIfNecessary(newPath, localPathFinderInput);
			} catch (Exception e)
			{
				log.error("Exception in pathfinder thread", e);
			} finally
			{
				stopTime(frameId);
			}
			// we release the latch here, although there may have been an Exception.
			latchStartStop.countDown();
		}
	}
	
	
	/**
	 * Generate a path including spline
	 * 
	 * @param localPathFinderInput
	 * @param wFrame
	 * @return
	 */
	public Path generatePath(final PathFinderInput localPathFinderInput, final SimpleWorldFrame wFrame)
	{
		Path newPath = pathFinder.calcPath(localPathFinderInput);
		sisyphus.addAHermiteSpline(wFrame, newPath, localPathFinderInput);
		sisyphus.onPotentialNewPath(newPath);
		return newPath;
	}
	
	
	private void setNewPathIfNecessary(final Path newPath, final PathFinderInput localPathFinderInput)
	{
		if (currentPath == null)
		{
			setNewPath(newPath);
			return;
		}
		switch (updateSplineDecision.check(localPathFinderInput, currentPath, newPath))
		{
			case ENFORCE:
				setNewPath(newPath);
				break;
			case OPTIMIZATION_FOUND:
				if (!pathFinderInput.getMoveCon().isOptimizationWanted())
				{
					break;
				}
			case COLLISION_AHEAD:
			case VIOLATION:
				if (newPath.isRambo() && !wasLastRambo)
				{
					wasLastRambo = true;
					setNewPath(newPath);
				} else if (!newPath.isRambo())
				{
					setNewPath(newPath);
				}
				break;
			
			case NO_VIOLATION:
				break;
		}
	}
	
	
	private void setNewPath(final Path newPath)
	{
		currentPath = newPath;
		sisyphus.onNewPath(newPath);
		if (!newPath.isRambo())
		{
			wasLastRambo = false;
		}
	}
	
	
	private void startTime(final long id)
	{
		if (timer != null)
		{
			switch (botId.getTeamColor())
			{
				case YELLOW:
					timer.start(ETimable.PP_Y, id, botId.getNumberWithColorOffset());
					break;
				case BLUE:
					timer.start(ETimable.PP_B, id, botId.getNumberWithColorOffset());
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}
	
	
	private void stopTime(final long id)
	{
		if (timer != null)
		{
			switch (botId.getTeamColor())
			{
				case YELLOW:
					timer.stop(ETimable.PP_Y, id, botId.getNumberWithColorOffset());
					break;
				case BLUE:
					timer.stop(ETimable.PP_B, id, botId.getNumberWithColorOffset());
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
