/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableBorderText implements IDrawableShape
{
	private final IVector2 pos;
	private final String text;
	private final Color color;
	private int fontSize = 10;
	
	
	@SuppressWarnings("unused")
	private DrawableBorderText()
	{
		pos = Vector2f.ZERO_VECTOR;
		text = "";
		color = Color.red;
	}
	
	
	/**
	 * @param pos
	 * @param text
	 * @param color
	 */
	public DrawableBorderText(final IVector2 pos, final String text, final Color color)
	{
		this.pos = pos;
		this.text = text;
		this.color = color;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.scale(1f / tool.getScaleFactor(), 1.0 / tool.getScaleFactor());
		g.translate(-tool.getFieldOriginX(), -tool.getFieldOriginY());
		
		final IVector2 transPoint = pos;
		int pointSize = 3;
		final int drawingX = (int) transPoint.x() - (pointSize / 2);
		final int drawingY = (int) transPoint.y() - (pointSize / 2);
		
		Font font = new Font("", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, drawingX, drawingY);
		
		g.translate(tool.getFieldOriginX(), tool.getFieldOriginY());
		g.scale(tool.getScaleFactor(), tool.getScaleFactor());
	}
	
	
	/**
	 * @return the fontSize
	 */
	public final int getFontSize()
	{
		return fontSize;
	}
	
	
	/**
	 * @param fontSize the fontSize to set
	 */
	public final void setFontSize(final int fontSize)
	{
		this.fontSize = fontSize;
	}
	
}
