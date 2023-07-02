/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;


/**
 * Draw a text at a certain position with an offset.
 * The center position is given in field coordinates. The offset is given
 * in graphics coordinates (X right, Y down).
 * The offset text will thus always appear at the same location regardless
 * of field orientation.
 */
@Persistent
public class DrawableAnnotation implements IDrawableShape
{
	private final IVector2 center;
	private final String text;

	private IVector2 offset = Vector2f.ZERO_VECTOR;
	private boolean centerHorizontally = false;
	private Color color = Color.BLACK;
	private int fontHeight = 50;
	private boolean bold = false;


	@SuppressWarnings("unused")
	private DrawableAnnotation()
	{
		center = Vector2f.ZERO_VECTOR;
		text = "";
	}


	/**
	 * @param center
	 * @param text
	 */
	public DrawableAnnotation(final IVector2 center, final String text)
	{
		super();
		this.center = center;
		this.text = text;
	}


	/**
	 * @param center
	 * @param text
	 * @param centerHorizontal
	 */
	public DrawableAnnotation(final IVector2 center, final String text, final boolean centerHorizontal)
	{
		super();
		this.center = center;
		this.text = text;
		centerHorizontally = centerHorizontal;
	}


	/**
	 * @param center
	 * @param text
	 * @param offset
	 */
	public DrawableAnnotation(final IVector2 center, final String text, final IVector2 offset)
	{
		super();
		this.center = center;
		this.text = text;
		this.offset = offset;
	}


	/**
	 * @param center
	 * @param text
	 * @param color
	 */
	public DrawableAnnotation(final IVector2 center, final String text, final Color color)
	{
		super();
		this.center = center;
		this.text = text;
		this.color = color;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		Font font = new Font("", bold ? Font.BOLD : Font.PLAIN, tool.scaleGlobalToGui(fontHeight));

		g.setFont(font);
		g.setColor(color);

		final IVector2 transPoint = tool.transformToGuiCoordinates(center, invert);

		String[] lines = text.split("\n");
		int numLines = lines.length;
		double maxWidth = 0;
		for (String line : lines)
		{
			maxWidth = Math.max(maxWidth, g.getFontMetrics(font).stringWidth(line));
		}

		double lineHeight = g.getFontMetrics(font).getHeight();
		double textHeight = lineHeight * numLines;

		double drawingX = transPoint.x() + tool.scaleGlobalToGui(offset.x());
		double drawingY = transPoint.y() + tool.scaleGlobalToGui(offset.y());

		drawingY += (textHeight / 2) - g.getFontMetrics(font).getDescent();

		if (offset.x() < 0)
		{
			drawingX -= maxWidth;
		}

		if (centerHorizontally)
		{
			if (offset.x() < 0)
			{
				drawingX += maxWidth / 2;
			} else
			{
				drawingX -= maxWidth / 2;
			}
		}

		drawingY -= (numLines - 1) * lineHeight;

		for (String txt : lines)
		{
			g.drawString(txt, (float) drawingX, (float) drawingY);
			drawingY += lineHeight;
		}
	}


	/**
	 * @param color the color to set
	 */
	@Override
	public final DrawableAnnotation setColor(final Color color)
	{
		this.color = color;
		return this;
	}


	/**
	 * @param fontHeight the fontHeight to set in [mm]
	 * @return
	 */
	public final DrawableAnnotation withFontHeight(final int fontHeight)
	{
		this.fontHeight = fontHeight;
		return this;
	}


	public DrawableAnnotation withOffsetX(double offsetX)
	{
		return withOffset(Vector2f.fromX(offsetX));
	}


	public DrawableAnnotation withOffsetY(double offsetY)
	{
		return withOffset(Vector2f.fromY(offsetY));
	}


	/**
	 * @param offset the offset to set
	 * @return
	 */
	public DrawableAnnotation withOffset(final IVector2 offset)
	{
		this.offset = offset;
		return this;
	}


	/**
	 * @param centerHorizontally the centerHorizontally to set
	 * @return
	 */
	public DrawableAnnotation withCenterHorizontally(final boolean centerHorizontally)
	{
		this.centerHorizontally = centerHorizontally;
		return this;
	}


	public DrawableAnnotation withBold(final boolean bold)
	{
		this.bold = bold;
		return this;
	}
}
