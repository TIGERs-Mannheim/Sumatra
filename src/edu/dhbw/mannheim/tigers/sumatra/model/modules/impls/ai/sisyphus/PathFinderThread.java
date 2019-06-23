/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Skills;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Collision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTPlanner_WPC;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.UpdateSplineDecisionMakerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * a thread for a path finder<br>
 * it is triggered in a certain interval to refresh the path used by the bot
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PathFinderThread implements Runnable
{
	
	
	private static final Logger					log								= Logger.getLogger(PathFinderThread.class
																										.getName());
	
	/**  */
	private static final int						STOPPING_PP_THREAD_DELAY	= 2000;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private IPathFinder								pathFinder;
	
	private UpdateSplineDecisionMakerFactory	updateSplineDecision;
	
	private Sisyphus									sisyphus;
	
	private BotID										botId;
	
	private Path										oldPath;
	
	private PathFinderInput							pathFinderInput;
	
	private boolean									wasLastRambo					= false;
	
	private int											avoidedCollisions				= 0;
	
	private boolean									active							= true;
	private CountDownLatch							countDownLatch					= new CountDownLatch(1);
	
	private final SplineTrajectoryGenerator	trajGeneratorMove				= new SplineTrajectoryGenerator();
	
	private ITimer										timer								= null;
	
	@SuppressWarnings("unused")
	private Collision									collision						= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param sisyphus Sisyphus reference
	 * @param botId the bot which is handled by this thread
	 * @param currentPath
	 */
	public PathFinderThread(Sisyphus sisyphus, BotID botId, Path currentPath)
	{
		this.sisyphus = sisyphus;
		this.botId = botId;
		oldPath = currentPath;
		pathFinder = new ERRTPlanner_WPC();
		updateSplineDecision = new UpdateSplineDecisionMakerFactory();
		
		try
		{
			timer = (ITimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.error("No timer found.");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void run()
	{
		try
		{
			if (pathFinderInput == null)
			{
				log.error("No pathFinderInput!");
				return;
			}
			PathFinderInput localPathFinderInput = pathFinderInput;
			startTime(localPathFinderInput.getwFrame().id);
			try
			{
				if (!isPathPlanningNeeded(localPathFinderInput))
				{
					if (active)
					{
						Path existingPath = localPathFinderInput.getExistingPathes().get(botId);
						existingPath.setOld(true);
						sisyphus.onNewPath(botId, existingPath, localPathFinderInput);
						active = false;
					}
					stopTime(localPathFinderInput.getwFrame().id);
					return;
				}
				active = true;
				
				if (oldPath.isOld())
				{
					oldPath.setTree(null);
					wasLastRambo = false;
					avoidedCollisions = 0;
					collision = null;
				}
				
				localPathFinderInput.setAvoidedCollisionsSeries(avoidedCollisions);
				// if ((collision != null) && !localPathFinderInput.getFieldInfo().isPointOK(collision.getPosition()))
				// {
				// localPathFinderInput.setObstacleFoundByLastSpline(collision.getPosition());
				// }
				Path newPath = pathFinder.calcPath(localPathFinderInput);
				if (!newPath.isRambo())
				{
					wasLastRambo = false;
				}
				addAHermiteSpline(newPath, localPathFinderInput);
				oldPath.setChanged(false);
				
				setNewPathIfNecessary(newPath, localPathFinderInput);
				
				sisyphus.onNewPath(botId, oldPath, localPathFinderInput);
				countDownLatch.countDown();
				stopTime(localPathFinderInput.getwFrame().id);
			} catch (RuntimeException err)
			{
				log.fatal("Exception in PathFinderThread.", err);
			}
			stopTime(localPathFinderInput.getwFrame().id);
		} catch (Exception e)
		{
			log.error("Error in pathfinder thread", e);
		}
	}
	
	
	private boolean isPathPlanningNeeded(PathFinderInput localPathFinderInput)
	{
		if ((localPathFinderInput == null)
				|| (!localPathFinderInput.isActive())
				|| ((System.nanoTime() - localPathFinderInput.getTimestamp()) > (TimeUnit.MILLISECONDS
						.toNanos(STOPPING_PP_THREAD_DELAY)))
				|| (!localPathFinderInput.getwFrame().tigerBotsAvailable.containsKey(botId)))
		{
			return false;
		}
		return true;
	}
	
	
	private void setNewPathIfNecessary(Path newPath, PathFinderInput localPathFinderInput)
	{
		if (!oldPath.isOld())
		{
			switch (updateSplineDecision.check(localPathFinderInput, oldPath, newPath))
			{
				case ENFORCE:
					setNewPath(newPath);
					break;
				case COLLISION_AHEAD:
					collision = oldPath.getFirstCollisionAt();
				case OPTIMIZATION_FOUND:
					if (!pathFinderInput.getMoveCon().isOptimizationWanted())
					{
						break;
					}
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
		} else
		{
			log.trace("New Spline - path 'isOld'");
			setNewPath(newPath);
		}
		avoidedCollisions = localPathFinderInput.getAvoidedCollisionsSeries();
	}
	
	
	private void setNewPath(Path newPath)
	{
		if ((oldPath.getTree() != null) && (newPath.getTree() == null))
		{
			newPath.setTree(oldPath.getTree());
		}
		oldPath = newPath;
		oldPath.setChanged(true);
	}
	
	
	/**
	 * wait until a new path was created
	 */
	public void waitForPath()
	{
		if ((pathFinderInput != null) && (pathFinderInput.getwFrame() != null))
		{
			if (!pathFinderInput.getwFrame().tigerBotsAvailable.containsKey(botId))
			{
				log.warn("waitForPath() was called for a disconnected bot!");
				return;
			}
		} else
		{
			log.error("PathFinderInput or its containing worldframe == null at waitForPath()!");
			return;
		}
		while ((oldPath != null) && oldPath.isOld())
		{
			countDownLatch = new CountDownLatch(1);
			try
			{
				if (!countDownLatch.await(2, TimeUnit.SECONDS))
				{
					log.warn("Timed out waiting for path", new Exception());
				}
			} catch (InterruptedException err)
			{
				log.info("Count down latch of sisyphus interrupted");
			}
		}
	}
	
	
	/**
	 * add a spline to a path
	 * 
	 * @param path
	 * @param pfi
	 */
	public void addAHermiteSpline(Path path, PathFinderInput pfi)
	{
		TrackedTigerBot bot = pfi.getwFrame().getTiger(pfi.getBotId());
		float dstOrient = pfi.getDstOrient();
		List<IVector2> mPath = new ArrayList<IVector2>();
		mPath.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		
		for (IVector2 p : path.getPath())
		{
			mPath.add(DistanceUnit.MILLIMETERS.toMeters(p));
		}
		
		Skills skillConfig = AIConfig.getSkills(bot.getBotType());
		if (pfi.getMoveCon().getSpeed() > 0)
		{
			trajGeneratorMove.setPositionTrajParams(pfi.getMoveCon().getSpeed(), skillConfig.getMaxLinearAcceleration());
		} else
		{
			trajGeneratorMove.setPositionTrajParams(skillConfig.getMaxLinearVelocity(),
					skillConfig.getMaxLinearAcceleration());
		}
		trajGeneratorMove.setRotationTrajParams(skillConfig.getMaxRotateVelocity(),
				skillConfig.getMaxRotateAcceleration());
		trajGeneratorMove.setSplineParams(skillConfig.getNormalAngleToSpeed());
		trajGeneratorMove.setReducePathScore(AIConfig.getOptimization().getPathReductionScore());
		
		// if (mPath.size() > 0)
		// {
		// mPath.remove(mPath.size() - 1);
		// }
		SplinePair3D trajectories = trajGeneratorMove.create(mPath, bot.getVel(), pfi.getMoveCon().getVelAtDestination(),
				bot.getAngle(), dstOrient, bot.getaVel(), 0);
		path.setHermiteSpline(trajectories);
	}
	
	
	private void startTime(FrameID frameId)
	{
		if (timer != null)
		{
			timer.start("PP", frameId);
		}
	}
	
	
	private void stopTime(FrameID frameId)
	{
		if (timer != null)
		{
			timer.stop("PP", frameId);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the pathFinder
	 */
	public final IPathFinder getPathFinder()
	{
		return pathFinder;
	}
	
	
	/**
	 * @param pathFinder the pathFinder to set
	 */
	public final void setPathFinder(IPathFinder pathFinder)
	{
		this.pathFinder = pathFinder;
	}
	
	
	/**
	 * @return the pathFinderInput
	 */
	public PathFinderInput getPathFinderInput()
	{
		return pathFinderInput;
	}
	
	
	/**
	 * @param pathFinderInput the pathFinderInput to set
	 */
	public void setPathFinderInput(PathFinderInput pathFinderInput)
	{
		this.pathFinderInput = pathFinderInput;
	}
	
	
	/**
	 * @return the path
	 */
	public Path getPath()
	{
		return oldPath;
	}
	
	
	/**
	 * @param path the path to set
	 */
	public void setPath(Path path)
	{
		oldPath = path;
	}
	
	
}
