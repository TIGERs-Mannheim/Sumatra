/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.OpponentPenaltyArea;
import edu.tigers.sumatra.geometry.PenaltyArea;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class PenaltyAreaObstacle implements IObstacle
{
	private final double radius;
	private final ETeam team;
	private transient IPenaltyArea penaltyArea;
	private transient List<IDrawableShape> shapes = new ArrayList<>();
	
	
	@SuppressWarnings("unused")
	private PenaltyAreaObstacle()
	{
		radius = 0;
		team = ETeam.UNKNOWN;
	}
	
	
	/**
	 * @param team the team this penalty area belongs to
	 * @param radius the radius (with margin) of the penalty area
	 */
	public PenaltyAreaObstacle(final ETeam team, final double radius)
	{
		this.radius = radius;
		this.team = team;
		Validate.isTrue(team == ETeam.OPPONENTS || team == ETeam.TIGERS);
	}
	
	
	private void ensureInitialized()
	{
		if (penaltyArea == null)
		{
			if (team == ETeam.OPPONENTS)
			{
				penaltyArea = new OpponentPenaltyArea(radius, Geometry.getPenaltyAreaOur().getFrontLineLength());
			} else
			{
				penaltyArea = new PenaltyArea(radius, Geometry.getPenaltyAreaOur().getFrontLineLength());
			}
		}
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		ensureInitialized();
		return penaltyArea.isPointInShapeOrBehind(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		ensureInitialized();
		Color color = Color.BLACK;
		if (shapes.isEmpty())
		{
			shapes.add(new DrawableArc(penaltyArea.getArcPos(), color));
			shapes.add(new DrawableArc(penaltyArea.getArcNeg(), color));
			shapes.add(new DrawableRectangle(penaltyArea.getInnerRectangle(), color));
		}
		shapes.forEach(shape -> shape.paintShape(g, tool, invert));
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		// not supported
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		ensureInitialized();
		return penaltyArea.withMargin(margin).isPointInShapeOrBehind(point);
	}
	
	
	@Override
	public void setStrokeWidth(final double strokeWidth)
	{
		// not supported
	}
	
	
	@Override
	public boolean isWorthBrakingFor()
	{
		return true;
	}
}
