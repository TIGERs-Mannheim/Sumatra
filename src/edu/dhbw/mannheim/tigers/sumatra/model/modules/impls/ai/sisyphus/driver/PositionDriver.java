/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This driver will drive straight to its destination, but considers far distances
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionDriver extends ABaseDriver
{
	private IVector2		destination			= null;
	private IVector3		nextDestination	= Vector3.ZERO_VECTOR;
	private float			orientation			= 0;
	
	
	@Configurable(comment = "max dist [mm] that destination may differ from bot pos. If greater, dest is modified.", spezis = {
			"", "TIGER_V3" })
	private static float	maxDistanceFast	= 400;
	
	@Configurable(comment = "Max dist [mm] in slow mode", spezis = { "", "TIGER_V3" })
	private static float	maxDistanceSlow	= 200;
	
	@Configurable(comment = "Max dist [mm] in faster mode", spezis = { "", "TIGER_V3" })
	private static float	maxDistanceFaster	= 500;
	
	
	@SuppressWarnings("unused")
	private float			maxDistance			= maxDistanceFast;
	
	
	/**
	 * 
	 */
	public PositionDriver()
	{
		addSupportedCommand(ECommandType.POS);
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (destination == null)
		{
			return new Vector3(bot.getPos(), bot.getAngle());
		}
		IVector2 dest = destination;
		float dist = GeoMath.distancePP(dest, bot.getPos());
		if (dist > maxDistance)
		{
			dest = GeoMath.stepAlongLine(bot.getPos(), dest, maxDistance);
		}
		if (!AIConfig.getGeometry().getFieldWBorders().isPointInShape(dest, -AIConfig.getGeometry().getBotRadius()))
		{
			try
			{
				List<IVector2> intersects = AIConfig.getGeometry().getFieldWBorders()
						.getIntersectionPoints(Line.newLine(bot.getPos(), dest));
				Rectangle rect = new Rectangle(bot.getPos(), dest);
				for (IVector2 inter : intersects)
				{
					if (rect.isPointInShape(inter))
					{
						dest = inter;
						break;
					}
				}
			} catch (MathException err)
			{
				dest = AIConfig.getGeometry().getFieldWBorders().nearestPointInside(dest);
			}
		}
		nextDestination = new Vector3(dest, orientation);
		return nextDestination;
	}
	
	
	@Override
	public void setMovingSpeed(final EMovingSpeed speed)
	{
		switch (speed)
		{
			case FAST:
				maxDistance = maxDistanceFaster;
				break;
			case NORMAL:
				maxDistance = maxDistanceFast;
				break;
			case SLOW:
				maxDistance = maxDistanceSlow;
				break;
			case UNLIMITED:
				maxDistance = Float.MAX_VALUE;
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * @return the destination
	 */
	public IVector2 getDestination()
	{
		return destination;
	}
	
	
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(final IVector2 destination)
	{
		this.destination = destination;
	}
	
	
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(final IVector3 destination)
	{
		this.destination = destination.getXYVector();
		orientation = destination.z();
	}
	
	
	/**
	 * @return the orientation
	 */
	public float getOrientation()
	{
		return orientation;
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(final float orientation)
	{
		this.orientation = orientation;
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePath(bot, shapes);
		if (destination != null)
		{
			shapes.add(new DrawableBot(destination, orientation, Color.magenta));
		}
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		shapes.add(new DrawableBot(nextDestination.getXYVector(), nextDestination.z(), Color.red));
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.POSITION;
	}
}
