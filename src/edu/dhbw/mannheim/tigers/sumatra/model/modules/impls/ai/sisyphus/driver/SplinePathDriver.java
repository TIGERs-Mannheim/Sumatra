/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.DrawableSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * PathDriver for all types of ISpline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SplinePathDriver extends PositionDriver
{
	private ISpline						spline;
	
	@Configurable(comment = "time [s]")
	private static float					positionMoveLookAhead	= 0.3f;
	
	@Configurable(comment = "move mode for sending positions")
	private static EPositionMoveMode	positionMoveMode			= EPositionMoveMode.POS_ON_TANGENT;
	
	private float							forcedEndTime				= 0.0f;
	
	
	private enum EPositionMoveMode
	{
		POS_ON_SPLINE,
		POS_ON_TANGENT
	}
	
	
	/**
	 * @param spline
	 */
	public SplinePathDriver(final ISpline spline)
	{
		this(spline, 0);
	}
	
	
	/**
	 * @param spline
	 * @param forcedEndTime
	 */
	public SplinePathDriver(final ISpline spline, final float forcedEndTime)
	{
		this.spline = spline;
		this.forcedEndTime = forcedEndTime;
		addSupportedCommand(ECommandType.VEL);
		addSupportedCommand(ECommandType.POS_VEL);
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		float ct = spline.getCurrentTime();
		float p = 0;
		
		if (forcedEndTime > spline.getTotalTime())
		{
			p = ct / forcedEndTime;
			ct = p * spline.getTotalTime();
		}
		switch (positionMoveMode)
		{
			case POS_ON_SPLINE:
			{
				float t = Math.min(ct + positionMoveLookAhead, spline.getTotalTime());
				setDestination(spline.getPositionByTime(t));
				break;
			}
			case POS_ON_TANGENT:
			{
				float t = Math.min(ct, spline.getTotalTime());
				IVector3 pos = spline.getPositionByTime(t);
				IVector2 dest = pos.getXYVector().addNew(
						spline.getVelocityByTime(t).getXYVector().multiply(positionMoveLookAhead * 1000));
				setDestination(new Vector3(dest, pos.z()));
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
		return super.getNextDestination(bot, wFrame);
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		float t = spline.getCurrentTime();
		float p = 0;
		
		if (forcedEndTime > spline.getTotalTime())
		{
			p = t / forcedEndTime;
			t = p * spline.getTotalTime();
		}
		
		IVector3 globalVel = spline.getVelocityByTime(t);
		return globalVel;
	}
	
	
	@Override
	public void setMovingSpeed(final EMovingSpeed speed)
	{
		// should have been handled earlier in spline generation
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePath(bot, shapes);
		shapes.add(new DrawableSpline(spline, 0));
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		// draw current theoretical pos on spline
		float radius = AIConfig.getGeometry().getBotRadius() + 30;
		IVector3 pos = spline.getPositionByTime(spline.getCurrentTime());
		shapes.add(new DrawableCircle(
				new Circle(pos.getXYVector(), radius),
				Color.cyan));
		shapes.add(new DrawableLine(new Line(pos.getXYVector(), new Vector2(pos.z()).scaleTo(radius)), Color.cyan, false));
	}
	
	
	/**
	 * @return the spline
	 */
	public final ISpline getSpline()
	{
		return spline;
	}
	
	
	/**
	 * @param spline the spline to set
	 */
	public final void setSpline(final ISpline spline)
	{
		this.spline = spline;
	}
}
