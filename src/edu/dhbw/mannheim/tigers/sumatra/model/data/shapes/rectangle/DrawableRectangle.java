/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2015
 * Author(s): tilman
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * A Rectangle with a color
 * 
 * @author tilman
 */
@Persistent(version = 1)
public class DrawableRectangle extends Rectangle implements IDrawableShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ColorWrapper	color;
	private boolean		fill	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * For db only
	 */
	@SuppressWarnings("unused")
	private DrawableRectangle()
	{
		super(new Rectangle(AVector2.ZERO_VECTOR, new Vector2(1, 1)));
	}
	
	
	/**
	 * @param rec
	 */
	public DrawableRectangle(final IRectangle rec)
	{
		super(rec);
		setColor(Color.red);
	}
	
	
	/**
	 * A Rectangle that can be drawn, duh!
	 * 
	 * @param rec
	 * @param color
	 */
	public DrawableRectangle(final IRectangle rec, final Color color)
	{
		super(rec);
		setColor(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 topLeft = fieldPanel.transformToGuiCoordinates(topLeft(), invert);
		final IVector2 bottomRight = fieldPanel.transformToGuiCoordinates(bottomRight(), invert);
		
		int x = (int) (topLeft.x() < bottomRight.x() ? topLeft.x() : bottomRight.x());
		int y = (int) (topLeft.y() < bottomRight.y() ? topLeft.y() : bottomRight.y());
		
		int width = Math.abs((int) (bottomRight.x() - topLeft.x()));
		int height = Math.abs((int) (bottomRight.y() - topLeft.y()));
		
		g.setColor(getColor());
		g.setStroke(new BasicStroke(1));
		g.drawRect(x, y, width, height);
		if (fill)
		{
			g.fillRect(x, y, width, height);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the color
	 */
	public Color getColor()
	{
		return color.getColor();
	}
	
	
	/**
	 * @param color the color to set
	 */
	public void setColor(final Color color)
	{
		this.color = new ColorWrapper(color);
	}
	
	
	/**
	 * @param fill
	 */
	public void setFill(final boolean fill)
	{
		this.fill = fill;
	}
}
