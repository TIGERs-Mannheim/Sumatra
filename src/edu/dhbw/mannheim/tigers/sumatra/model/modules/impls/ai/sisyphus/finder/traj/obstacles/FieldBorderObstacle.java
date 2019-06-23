/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class FieldBorderObstacle implements IObstacle
{
	private final Rectangle	field;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private FieldBorderObstacle()
	{
		field = null;
	}
	
	
	/**
	 * @param field
	 */
	public FieldBorderObstacle(final Rectangle field)
	{
		this.field = field;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		field.paintShape(g, fieldPanel, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		return !field.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		return field.nearestPointInside(point, AIConfig.getGeometry().getBotRadius());
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> obstacles)
	{
	}
}
