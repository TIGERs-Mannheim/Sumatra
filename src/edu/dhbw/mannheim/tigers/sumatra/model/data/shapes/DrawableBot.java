/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Outline of a bot with orientation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableBot implements IDrawableShape
{
	private final DrawableCircle	circle;
	private final DrawableLine		line;
	
	
	@SuppressWarnings("unused")
	private DrawableBot()
	{
		this(AVector2.ZERO_VECTOR, 0, Color.RED);
	}
	
	
	/**
	 * @param pos
	 * @param orientation
	 * @param color
	 */
	public DrawableBot(final IVector2 pos, final float orientation, final Color color)
	{
		this(pos, orientation, color, 1);
	}
	
	
	/**
	 * @param pos
	 * @param orientation
	 * @param color
	 * @param scale
	 */
	public DrawableBot(final IVector2 pos, final float orientation, final Color color, final float scale)
	{
		float radius = (AIConfig.getGeometry().getBotRadius() + 10) * scale;
		circle = new DrawableCircle(new Circle(pos, radius), color);
		line = new DrawableLine(
				new Line(pos, new Vector2(orientation).scaleTo(Geometry.getCenter2DribblerDistDefault() * scale)),
				color, false);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		circle.paintShape(g, fieldPanel, invert);
		line.paintShape(g, fieldPanel, invert);
	}
}
