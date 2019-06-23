/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 28, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallTrajDriver extends TrajPathDriverV2 implements IKickPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(KickBallTrajDriver.class.getName());
	
	private final KickSkillCalc	calc;
	
	
	/**
	 * @param receiver
	 */
	public KickBallTrajDriver(final DynamicPosition receiver)
	{
		calc = new KickSkillCalc(receiver);
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		getMoveCon().setOptimizeOrientation(false);
		
		calc.setMoveCon(getMoveCon());
		calc.getObstacleGen().setUsePenAreaOur(!getMoveCon().isPenaltyAreaAllowedOur());
		calc.getObstacleGen().setUsePenAreaTheir(!getMoveCon().isPenaltyAreaAllowedTheir());
		
		TrajectoryWithTime<IVector2> path = calc.calculatePath(bot, wFrame);
		
		List<IDrawableShape> shapes = new ArrayList<>();
		List<IDrawableShape> shapesDebug = new ArrayList<>();
		shapesDebug.add(new DrawableText(bot.getPos(), calc.getState(), Color.black));
		
		setPath(path, calc.getFinderInput().getDest(), calc.getFinderInput().getTargetAngle());
		
		// painting
		List<IDrawableShape> obsShapes = new ArrayList<>(calc.getObstacles());
		shapesDebug.add(new DrawableBot(calc.getUnFilteredDest().getXYVector(), calc.getUnFilteredDest().z(), Color.black,
				Geometry.getBotRadius(), bot.getCenter2DribblerDist()));
		shapes.add(new DrawableLine(Line.newLine(wFrame.getBall().getPos(), calc.getReceiver()), Color.blue));
		
		// set current trajectory for bot
		if (path != null)
		{
			ITrajectory<IVector3> traj = new TrajectoryXyw(path.getTrajectory(),
					TrajectoryGenerator.generateRotationTrajectoryStub(calc.getFinderInput().getTargetAngle(), null));
			TrajectoryWithTime<IVector3> twt = new TrajectoryWithTime<>(traj, path.gettStart());
			aBot.setCurrentTrajectory(Optional.ofNullable(twt));
		} else
		{
			DrawableCircle dc = new DrawableCircle(bot.getPos(), 100, Color.pink);
			dc.setFill(true);
			shapes.add(dc);
		}
		
		setShapes(EShapesLayer.TRAJ_PATH_OBSTACLES, obsShapes);
		setShapes(EShapesLayer.TRAJ_PATH_DEBUG, new ArrayList<>(calc.getFinderInput().getDebugShapes()));
		setShapes(EShapesLayer.KICK_SKILL, shapes);
		setShapes(EShapesLayer.KICK_SKILL_DEBUG, shapesDebug);
		
		super.update(bot, aBot, wFrame);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_TRAJ;
	}
	
	
	@Override
	public boolean isEnableDribbler()
	{
		return calc.isEnableDribbler();
	}
	
	
	@Override
	public boolean armKicker()
	{
		return calc.isArmKicker();
	}
	
	
	/**
	 * @param distBehindBallHitTarget the distBehindBallHitTarget to set
	 */
	public void setDistBehindBallHitTarget(final double distBehindBallHitTarget)
	{
		calc.setDistBehindBallHitTarget(distBehindBallHitTarget);
	}
	
	
	@Override
	public void setMoveMode(final EMoveMode moveMode)
	{
		calc.setMoveMode(moveMode);
	}
	
	
	/**
	 * @param ready4Kick
	 */
	@Override
	public void setRoleReady4Kick(final boolean ready4Kick)
	{
		calc.setRoleReady4Kick(ready4Kick);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public boolean isSkillReady4Kick()
	{
		return calc.isSkillReady4Kick();
	}
	
	
	/**
	 * @param dest
	 */
	@Override
	public void setDestForAvoidingOpponent(final IVector2 dest)
	{
		calc.setDestForAvoidingOpponent(dest);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void unsetDestForAvoidingOpponent()
	{
		calc.unsetDestForAvoidingOpponent();
	}
	
	
	@Override
	public void setProtectPos(final IVector2 pos)
	{
		calc.setProtectPos(pos);
	}
}
