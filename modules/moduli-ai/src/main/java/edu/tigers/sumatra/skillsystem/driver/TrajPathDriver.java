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
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableTrajectory2D;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathDriver extends PositionDriver implements ITrajPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger				log							= Logger.getLogger(TrajPathDriver.class.getName());
																						
	private TrajectoryWithTime<IVector2>	path;
														
														
	private double									targetAngle;
														
	@Configurable
	private static double						angleRemTimeMultiplier	= 1;
																						
	@Configurable
	private static double						minAngleDist				= 0.05;
																						
																						
	@Configurable
	private static double						maxAngleDist				= 0.5;
																						
	@Configurable(comment = "time [s]")
	private static double						positionMoveLookAhead	= 0.3;
																						
	@Configurable(comment = "move mode for sending positions", defValue = "POS_ON_TANGENT")
	private static EPositionMoveMode			positionMoveMode			= EPositionMoveMode.POS_ON_TANGENT;
																						
																						
	private enum EPositionMoveMode
	{
		POS_ON_SPLINE,
		POS_ON_TANGENT
	}
	
	
	static
	{
		ConfigRegistration.registerClass("skills", TrajPathDriver.class);
	}
	
	
	/**
	 */
	public TrajPathDriver()
	{
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.update(bot, aBot, wFrame);
		if (path == null)
		{
			return;
		}
		
		IVector2 curDest;
		switch (positionMoveMode)
		{
			case POS_ON_SPLINE:
			{
				curDest = (path.getPositionMM(wFrame.getTimestamp() + (long) (positionMoveLookAhead * 1e9)));
				break;
			}
			case POS_ON_TANGENT:
			{
				IVector2 pos = path.getPositionMM(wFrame.getTimestamp());
				IVector2 dest = pos
						.addNew(path.getVelocity(wFrame.getTimestamp())
								.multiplyNew(positionMoveLookAhead * 1000));
				curDest = dest;
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
		
		double orient;
		double diff = AngleMath.getShortestRotation(bot.getAngle(), targetAngle);
		double maxAngleChange = minAngleDist
				+ Math.max(0, maxAngleDist - minAngleDist
						- (angleRemTimeMultiplier * path.getRemainingTrajectoryTime(wFrame.getTimestamp())));
		orient = bot.getAngle() + (Math.signum(diff) * Math.min(Math.abs(diff), maxAngleChange));
		
		setDestination(new Vector3(curDest, orient));
		
		
		List<IDrawableShape> shapes = new ArrayList<>(1);
		shapes.add(new DrawableTrajectory2D(path.getTrajectory()));
		shapes.add(new DrawableBot(path.getTrajectory().getPositionMM(Double.MAX_VALUE), targetAngle, Color.magenta,
				Geometry.getBotRadius() + 25,
				bot.getCenter2DribblerDist()));
		shapes.add(new DrawableBot(path.getTrajectory().getPositionMM(0), orient, Color.cyan,
				Geometry.getBotRadius() + 20,
				bot.getCenter2DribblerDist()));
		setShapes(EShapesLayer.PATH, shapes);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TRAJ_PATH;
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		return super.getNextDestination(bot, wFrame);
	}
	
	
	/**
	 * @param path the path to set
	 */
	@Override
	public void setPath(final TrajectoryWithTime<IVector2> path, final IVector2 finalDestination,
			final double targetAngle)
	{
		this.path = path;
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
	
	
}
