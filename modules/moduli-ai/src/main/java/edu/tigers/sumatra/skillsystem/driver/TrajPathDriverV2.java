/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathDriverV2 extends ABaseDriver implements ITrajPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger				log							= Logger.getLogger(TrajPathDriverV2.class.getName());
	
	private TrajectoryWithTime<IVector2>	path;
	
	
	private IVector3								destination					= null;
	private IVector2								finalDestination			= null;
	private double									targetAngle;
	
	@Configurable
	private static boolean						relativeVelLimit			= true;
	
	@Configurable
	private static double						upperRotationSpeed		= 10;
	@Configurable
	private static double						lowerRotationSpeed		= 1;
	@Configurable
	private static double						upperRotationTrajTime	= 2;
	@Configurable
	private static double						lowerRotationTrajTime	= 0.5;
	
	
	private double									minFwdBwdTime				= 0.3;
	
	private boolean								standing						= true;
	private long									tStandingLast				= 0;
	
	private double									limVelSide					= 1.5;
	private double									limAccSide					= 2.5;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", TrajPathDriverV2.class);
	}
	
	
	/**
	 */
	public TrajPathDriverV2()
	{
		addSupportedCommand(EBotSkill.GLOBAL_POSITION);
		addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	private double getMaxRotationSpeed(final double trajTime)
	{
		if (trajTime >= upperRotationTrajTime)
		{
			return lowerRotationSpeed;
		}
		if (trajTime <= lowerRotationTrajTime)
		{
			return upperRotationSpeed;
		}
		double relTime = (trajTime - lowerRotationTrajTime) / (upperRotationTrajTime - lowerRotationTrajTime);
		double speed = lowerRotationSpeed + ((upperRotationSpeed - lowerRotationSpeed) * (1 - relTime));
		return speed;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.update(bot, aBot, wFrame);
		if (path == null)
		{
			removeSupportedCommand(EBotSkill.GLOBAL_POSITION);
			addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
			return;
		}
		addSupportedCommand(EBotSkill.GLOBAL_POSITION);
		removeSupportedCommand(EBotSkill.LOCAL_VELOCITY);
		
		// FIXME can not be used atm...
		getMaxRotationSpeed(path.getRemainingTrajectoryTime(wFrame.getTimestamp()));
		
		IVector2 curDest = path.getNextDestination(wFrame.getTimestamp());
		
		double curTargetAngle = targetAngle;
		if (getMoveCon().isOptimizeOrientation())
		{
			if (tStandingLast == 0)
			{
				tStandingLast = wFrame.getTimestamp();
			}
			if (standing && (bot.getVel().getLength2() > 0.3))
			{
				standing = false;
			}
			double diff = (wFrame.getTimestamp() - tStandingLast) / 1e9;
			if (diff > 0.2)
			{
				if (!standing && (bot.getVel().getLength2() < 0.1))
				{
					standing = true;
					tStandingLast = wFrame.getTimestamp();
				}
			}
			
			double curAngle = bot.getAngle();
			double maxAccW = getMoveCon().getMoveConstraints().getAccMaxW();
			double maxBrkW = maxAccW;
			double maxVelW = getMoveCon().getMoveConstraints().getVelMaxW();
			
			double remTime = path.getRemainingTrajectoryTime(wFrame.getTimestamp());
			
			// try to drive forward or backward
			double orient;
			IVector2 dir = path.getVelocity(wFrame.getTimestamp() + (long) 2e8);
			if ((dir.getLength() < 0.3))
			{
				dir = curDest.subtractNew(bot.getPos());
				if (dir.getLength2() < 50)
				{
					dir = AVector2.ZERO_VECTOR;
				}
			}
			if (dir.isZeroVector())
			{
				orient = targetAngle;
			} else if (Math.abs(AngleMath.difference(dir.getAngle(), bot.getAngle())) < (AngleMath.PI_HALF + 0.3))
			{
				orient = dir.getAngle();
			} else
			{
				orient = dir.multiplyNew(-1).getAngle();
			}
			ITrajectory<Double> trajW = new BangBangTrajectory1DOrient(
					curAngle,
					orient,
					bot.getaVel(),
					maxAccW,
					maxBrkW,
					maxVelW);
			ITrajectory<Double> trajBrk = new BangBangTrajectory1DOrient(
					orient,
					targetAngle,
					bot.getaVel(),
					maxAccW,
					maxBrkW,
					maxVelW);
			
			double curVel = path.getVelocity(wFrame.getTimestamp()).getLength2();
			double velThreshold = 2;
			double timeOffset = 0;
			if (curVel < velThreshold)
			{
				double curAcc = path.getAcceleration(wFrame.getTimestamp()).getLength2();
				timeOffset = (velThreshold - curVel) / curAcc;
			}
			
			double turnTime = trajW.getTotalTime() + trajBrk.getTotalTime() + minFwdBwdTime + timeOffset;
			
			if (((turnTime >= remTime))) // || (curVel < 0.5)))
			{
				// need to break to reach orientation in time!
				curTargetAngle = targetAngle;
			} else
			{
				curTargetAngle = orient;
				if (standing && (Math.abs(AngleMath.difference(curTargetAngle, curAngle)) > 0.4))
				{
					curDest = bot.getPos();
				} else
				{
					standing = false;
				}
			}
			
		}
		
		if (relativeVelLimit)
		{
			IVector2 botVel = path.getVelocity(wFrame.getTimestamp());
			if (botVel.getLength() > 0.1)
			{
				double angleDiff = Math.abs(AngleMath.difference(botVel.getAngle(), bot.getAngle()));
				if (angleDiff > AngleMath.PI_HALF)
				{
					angleDiff = AngleMath.PI - angleDiff;
				}
				double relFwd = (1 - (angleDiff / AngleMath.PI_HALF));
				double maxVel = getMoveCon().getMoveConstraints().getVelMax();
				double maxSideVel = limVelSide;
				double limVel = maxSideVel + ((maxVel - maxSideVel) * relFwd);
				getMoveCon().getMoveConstraints().setVelMax(limVel);
				
				// only limit acc if vel high
				if (botVel.getLength() > 1.0)
				{
					double maxAcc = getMoveCon().getMoveConstraints().getAccMax();
					double maxAccSide = limAccSide;
					double limAcc = maxAccSide + ((maxAcc - maxAccSide) * relFwd);
					getMoveCon().getMoveConstraints().setAccMax(limAcc);
				}
			}
		}
		
		destination = new Vector3(curDest, curTargetAngle);
		
		List<IDrawableShape> shapes = new ArrayList<>(1);
		List<IDrawableShape> shapesDebug = new ArrayList<>(1);
		double tLower = 1;
		TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(path.getTrajectory(), 0,
				Math.min(tLower, path.getTrajectory().getTotalTime()));
		shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.green));
		if (path.getTrajectory().getTotalTime() > tLower)
		{
			TrajectoryWrapper<IVector2> trajWrapperUpper = new TrajectoryWrapper<>(path.getTrajectory(), tLower,
					path.getTrajectory().getTotalTime());
			shapes.add(new DrawableTrajectoryPath(trajWrapperUpper, Color.red));
		}
		
		shapes.add(new DrawableBot(finalDestination, targetAngle, Color.magenta,
				Geometry.getBotRadius() + 25,
				Geometry.getBotRadius() + 25));
		
		shapesDebug.add(new DrawableBot(curDest, curTargetAngle, Color.red,
				Geometry.getBotRadius() + 20,
				Geometry.getBotRadius() + 20));
		
		if (GeoMath.distancePP(curDest, finalDestination) > 10)
		{
			shapesDebug.add(new DrawableLine(Line.newLine(curDest, finalDestination), Color.magenta, false));
		}
		
		setShapes(EShapesLayer.PATH, shapes);
		setShapes(EShapesLayer.PATH_DEBUG, shapesDebug);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TRAJ_PATH;
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		getMoveCon().setEmergencyBreak(false);
		if (destination == null)
		{
			return new Vector3(bot.getPos(), bot.getAngle());
		}
		return destination;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		// emergency break
		getMoveCon().setEmergencyBreak(true);
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public IVector3 getNextLocalVelocity(final ITrackedBot bot, final WorldFrame wFrame, final double dt)
	{
		return getNextVelocity(bot, wFrame);
	}
	
	
	/**
	 * @param path the path to set
	 */
	@Override
	public void setPath(final TrajectoryWithTime<IVector2> path, final IVector2 finalDestination,
			final double targetAngle)
	{
		this.path = path;
		this.finalDestination = finalDestination;
		this.targetAngle = targetAngle;
	}
	
	
	/**
	 * @return the path
	 */
	@Override
	public TrajectoryWithTime<IVector2> getPath()
	{
		return path;
	}
	
	
	/**
	 * @return the minFwdBwdTime
	 */
	public double getMinFwdBwdTime()
	{
		return minFwdBwdTime;
	}
	
	
	/**
	 * @param minFwdBwdTime the minFwdBwdTime to set
	 */
	public void setMinFwdBwdTime(final double minFwdBwdTime)
	{
		this.minFwdBwdTime = minFwdBwdTime;
	}
	
	
	/**
	 * @param limVelSide the limVelSide to set
	 */
	public void setLimVelSide(final double limVelSide)
	{
		this.limVelSide = limVelSide;
	}
	
	
	/**
	 * @param limAccSide the limAccSide to set
	 */
	public void setLimAccSide(final double limAccSide)
	{
		this.limAccSide = limAccSide;
	}
}
