/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableArc extends Arc
{
	private final ColorWrapper	color;
	
	
	/**
	 * 
	 */
	private DrawableArc()
	{
		super(AVector2.ZERO_VECTOR, 1, 0, 1);
		color = new ColorWrapper(Color.red);
	}
	
	
	/**
	 * @param arc
	 * @param color
	 */
	public DrawableArc(final Arc arc, final Color color)
	{
		super(arc);
		this.color = new ColorWrapper(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final IVector2 transBotPos = fieldPanel.transformToGuiCoordinates(center());
		float radius = fieldPanel.scaleXLength(radius());
		int drawingX = (int) (transBotPos.x() - radius);
		int drawingY = (int) (transBotPos.y() - radius);
		
		float startAngle = AngleMath
				.rad2deg((getStartAngle() + fieldPanel.getFieldTurn().getAngle()) - AngleMath.PI_HALF);
		float extendAngle = AngleMath.rad2deg(getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, Arc2D.PIE);
		g.setColor(color.getColor());
		g.draw(arcShape);
	}
	
}
