/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * An obstacle in a tube shape
 */
@Persistent
public class TubeObstacle implements IObstacle
{
	private final Tube tube;
	private transient DrawableTube drawableTube = null;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private TubeObstacle()
	{
		tube = Tube.create(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, 0);
	}
	
	
	public TubeObstacle(final Tube tube)
	{
		this.tube = tube;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return tube.isPointInShape(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		initDrawableTube();
		drawableTube.paintShape(g, tool, invert);
	}
	
	
	private void initDrawableTube()
	{
		if (drawableTube == null)
		{
			drawableTube = new DrawableTube(tube);
		}
	}
}
