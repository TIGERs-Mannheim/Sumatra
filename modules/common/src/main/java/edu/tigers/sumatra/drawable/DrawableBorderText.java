/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.util.ScalingUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableBorderText implements IDrawableShape
{
	private final IVector2 pos;
	private final String text;
	private final Color color;
	private EFontSize fontSizeType = EFontSize.SMALL;


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
		final IVector2 transPoint = Vector2.fromXY(ScalingUtil.scale(pos.x()), ScalingUtil.scale(pos.y()));
		int pointSize = 3;
		final int drawingX = (int) transPoint.x() - (pointSize / 2);
		final int drawingY = (int) transPoint.y() - (pointSize / 2);

		int fontSize = ScalingUtil.getFontSize(fontSizeType);
		Font font = new Font("", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, drawingX, drawingY);
	}


	/**
	 * @param fontSizeType the fontSize to set
	 */
	public final DrawableBorderText setFontSize(final EFontSize fontSizeType)
	{
		this.fontSizeType = fontSizeType;
		return this;
	}


	@Override
	public boolean isBorderText()
	{
		return true;
	}
}
