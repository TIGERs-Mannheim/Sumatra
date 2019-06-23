/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableText implements IDrawableShape
{
	private final IVector2		pos;
	private final String			text;
	private final ColorWrapper	color;
	
	
	@SuppressWarnings("unused")
	private DrawableText()
	{
		pos = AVector2.ZERO_VECTOR;
		text = "";
		color = new ColorWrapper(Color.RED);
	}
	
	
	/**
	 * @param pos
	 * @param text
	 * @param color
	 */
	public DrawableText(final IVector2 pos, final String text, final Color color)
	{
		this.pos = pos;
		this.text = text;
		this.color = new ColorWrapper(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final IVector2 transPoint = fieldPanel.transformToGuiCoordinates(pos, invert);
		int pointSize = 3;
		final int drawingX = (int) transPoint.x() - (pointSize / 2);
		final int drawingY = (int) transPoint.y() - (pointSize / 2);
		
		Font font = new Font("", Font.PLAIN, 5);
		g.setFont(font);
		g.setColor(color.getColor());
		g.drawString(text, drawingX, drawingY);
	}
}
