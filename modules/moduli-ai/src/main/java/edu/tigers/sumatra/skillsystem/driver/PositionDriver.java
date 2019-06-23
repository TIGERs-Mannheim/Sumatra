/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This driver will drive straight to its destination, but considers far distances
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionDriver extends ABaseDriver
{
	
	private IVector2	destination			= null;
	private IVector3	nextDestination	= null;
	private double		orientation			= 0;
	private boolean	allowOutsideField	= false;
	
	
	@Configurable(comment = "max dist [mm] that destination may differ from bot pos. If greater, dest is modified.", spezis = {
			"", "TIGER_V3" }, defValueSpezis = { "1000", "500" })
	private double		maxDistance			= Double.MAX_VALUE;
	
	
	private double		maxDist				= maxDistance;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", PositionDriver.class);
	}
	
	
	/**
	 * 
	 */
	public PositionDriver()
	{
		addSupportedCommand(EBotSkill.GLOBAL_POSITION);
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		if (destination == null)
		{
			return new Vector3(bot.getPos(), bot.getAngle());
		}
		IVector2 dest = destination;
		double dist = GeoMath.distancePP(dest, bot.getPos());
		if (dist > maxDist)
		{
			dest = GeoMath.stepAlongLine(bot.getPos(), dest, maxDist);
		}
		if (!allowOutsideField && !Geometry.getFieldWBorders().isPointInShape(dest, -Geometry.getBotRadius()))
		{
			// List<IVector2> intersects = Geometry.getFieldWBorders()
			// .lineIntersections(Line.newLine(bot.getPos(), dest));
			// Rectangle rect = new Rectangle(bot.getPos(), dest);
			// dest = Geometry.getFieldWBorders().nearestPointInside(dest);
			// for (IVector2 inter : intersects)
			// {
			// if (rect.isPointInShape(inter))
			// {
			// dest = inter;
			// break;
			// }
			// }
		}
		nextDestination = new Vector3(dest, orientation);
		return nextDestination;
	}
	
	
	@Override
	protected void onFirstUpdate(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.onFirstUpdate(bot, aBot, wFrame);
		maxDist = maxDistance;
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
	public double getOrientation()
	{
		return orientation;
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(final double orientation)
	{
		this.orientation = orientation;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.POSITION;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.update(bot, aBot, wFrame);
		List<IDrawableShape> shapes = new ArrayList<>(2);
		if (destination != null)
		{
			shapes.add(new DrawableBot(destination, orientation, Color.magenta, Geometry.getBotRadius(),
					bot.getCenter2DribblerDist()));
		}
		if (nextDestination != null)
		{
			shapes.add(
					new DrawableBot(nextDestination.getXYVector(), nextDestination.z(), Color.red, Geometry.getBotRadius(),
							bot.getCenter2DribblerDist()));
			shapes.add(new DrawableLine(Line.newLine(bot.getPos(), nextDestination.getXYVector()), Color.PINK));
		}
		setShapes(EShapesLayer.POSITION_DRIVER, shapes);
	}
	
	
	/**
	 * @return the maxDist
	 */
	public double getMaxDist()
	{
		return maxDist;
	}
	
	
	/**
	 * @param maxDist the maxDist to set
	 */
	public void setMaxDist(final double maxDist)
	{
		this.maxDist = maxDist;
	}
	
	
	/**
	 * @return the allowOutsideField
	 */
	public boolean isAllowOutsideField()
	{
		return allowOutsideField;
	}
	
	
	/**
	 * @param allowOutsideField the allowOutsideField to set
	 */
	public void setAllowOutsideField(final boolean allowOutsideField)
	{
		this.allowOutsideField = allowOutsideField;
	}
}
