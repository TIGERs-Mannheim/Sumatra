/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectory2D;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * PathDriver for all types of ISpline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SplinePathDriver extends PositionDriver
{
	
	private ITrajectory<IVector3>		spline;
	
	@Configurable(comment = "time [s]")
	private static double				positionMoveLookAhead	= 0.3;
	
	@Configurable(comment = "move mode for sending positions")
	private static EPositionMoveMode	positionMoveMode			= EPositionMoveMode.POS_ON_TANGENT;
	
	private double							forcedEndTime				= 0.0;
	
	private long							startTime					= 0;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", SplinePathDriver.class);
	}
	
	
	private enum EPositionMoveMode
	{
		POS_ON_SPLINE,
		POS_ON_TANGENT
	}
	
	
	/**
	 * @param spline
	 */
	public SplinePathDriver(final ITrajectory<IVector3> spline)
	{
		this(spline, 0);
	}
	
	
	/**
	 * @param spline
	 * @param forcedEndTime
	 */
	public SplinePathDriver(final ITrajectory<IVector3> spline, final double forcedEndTime)
	{
		this.spline = spline;
		this.forcedEndTime = forcedEndTime;
		clearSupportedCommands();
		addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		double ct = (wFrame.getTimestamp() - startTime) / 1e9;
		double p = 0;
		
		if (forcedEndTime > spline.getTotalTime())
		{
			p = ct / forcedEndTime;
			ct = p * spline.getTotalTime();
		}
		switch (positionMoveMode)
		{
			case POS_ON_SPLINE:
			{
				double t = Math.min(ct + positionMoveLookAhead, spline.getTotalTime());
				setDestination(spline.getPositionMM(t).getXYVector());
				setOrientation(spline.getPosition(t).z());
				break;
			}
			case POS_ON_TANGENT:
			{
				double t = Math.min(ct, spline.getTotalTime());
				IVector2 pos = spline.getPositionMM(t).getXYVector();
				double orient = spline.getPosition(t).z();
				IVector2 dest = pos.getXYVector().addNew(
						spline.getVelocity(t).getXYVector().multiplyNew(positionMoveLookAhead * 1000));
				setDestination(new Vector3(dest, orient));
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
		return super.getNextDestination(bot, wFrame);
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		double t = (wFrame.getTimestamp() - startTime) / 1e9;
		double p = 0;
		
		if (forcedEndTime > spline.getTotalTime())
		{
			p = t / forcedEndTime;
			t = p * spline.getTotalTime();
		}
		
		IVector3 globalVel = spline.getVelocity(t);
		// if (t < (spline.getTotalTime() / 2))
		// {
		// globalVel = new Vector3(globalVel.getXYVector(), 0);
		// } else
		// {
		// globalVel = new Vector3(globalVel.getXYVector(), spline.getVelocity(t - (spline.getTotalTime() / 2)).z());
		// }
		return globalVel;
	}
	
	
	/**
	 * @return the spline
	 */
	public final ITrajectory<IVector3> getSpline()
	{
		return spline;
	}
	
	
	/**
	 * @param spline the spline to set
	 */
	public final void setSpline(final ITrajectory<IVector3> spline)
	{
		this.spline = spline;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.update(bot, aBot, wFrame);
		
		if (startTime == 0)
		{
			startTime = wFrame.getTimestamp();
		}
		
		double ct = (wFrame.getTimestamp() - startTime) / 1e9;
		List<IDrawableShape> shapes = new ArrayList<>(3);
		// draw current theoretical pos on spline
		double radius = Geometry.getBotRadius() + 30;
		IVector2 pos = spline.getPositionMM(ct).getXYVector();
		double orient = spline.getPosition(ct).z();
		shapes.add(new DrawableCircle(
				new Circle(pos.getXYVector(), radius),
				Color.cyan));
		shapes.add(
				new DrawableLine(new Line(pos.getXYVector(), new Vector2(orient).scaleTo(radius)), Color.cyan, false));
		shapes.add(new DrawableTrajectory2D(spline));
		setShapes(EShapesLayer.SPLINES, shapes);
		
		TrajectoryWithTime<IVector3> twt = new TrajectoryWithTime<>(spline, startTime);
		aBot.setCurrentTrajectory(Optional.of(twt));
	}
	
	
	/**
	 * @return the startTime
	 */
	protected long getStartTime()
	{
		return startTime;
	}
}
