/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.AsyncExecution;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallChillTrajDriver extends TrajPathDriver implements IKickPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(KickBallChillTrajDriver.class.getName());
	
	@Configurable
	private static float				dist2Ball		= 200;
	
	@Configurable
	private static float				doneTolerance	= 50;
	
	private final DynamicPosition	receiver;
	private TrajPathFinderInput	finderInput		= new TrajPathFinderInput();
	private ObstacleGenerator		obsGen			= new ObstacleGenerator();
	private AsyncExecution			asynExecution	= null;
	private TrajPath					curPath			= null;
	private boolean					penAreaAllowed	= false;
	
	
	/**
	 * @param receiver
	 */
	public KickBallChillTrajDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
		
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (asynExecution == null)
		{
			asynExecution = new AsyncExecution(bot.getTeamColor());
		}
		asynExecution.executeAsynchronously(() -> calcPath(bot, wFrame));
		
		setPath(curPath);
	}
	
	
	private void calcPath(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		List<IDrawableShape> shapes = new ArrayList<>(1);
		
		// set obstacles
		obsGen.setUseBall(false);
		List<IObstacle> obstacles = new ArrayList<>();
		float toBallDist = (AIConfig.getGeometry().getBallRadius() + bot.getBot().getCenter2DribblerDist()) - 10;
		obstacles.add(new CatchBallObstacle(wFrame.getBall(), toBallDist, receiver, 8));
		obsGen.setUsePenAreaOur(penAreaAllowed);
		// obstacles.addAll(obsGen.generateObstacles(wFrame, bot.getId()));
		
		// update destination
		IVector2 ballStill = wFrame.getBall().getPosByVel(0);
		IVector2 finalDest = GeoMath.stepAlongLine(ballStill, receiver, -dist2Ball);
		float orientation = receiver.subtractNew(finalDest).getAngle();
		shapes.add(new DrawableBot(finalDest, receiver.subtractNew(finalDest).getAngle(), Color.magenta));
		
		// update path finder inputs
		finderInput.setForcePathAfter(0.5f);
		finderInput.setTrajOffset(0.2f);
		finderInput.setObstacles(obstacles);
		finderInput.setTrackedBot(bot);
		finderInput.setTargetAngle(orientation);
		finderInput.setDest(finalDest);
		
		// find path
		curPath = bot.getBot().getPathFinder().calcPath(finderInput);
		
		// check if we are done
		float dist2Dest = GeoMath.distancePP(finalDest, bot.getPos());
		if (dist2Dest < doneTolerance)
		{
			setDone(true);
		}
		
		setShapes(shapes);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_CHILL_TRAJ;
	}
	
	
	@Override
	public boolean isDone()
	{
		return (curPath != null) && (curPath.getRemainingTime() == 0);
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
		penAreaAllowed = allowed;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return true;
	}
}
