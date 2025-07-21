/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;

import java.awt.Graphics2D;


/**
 * The field background with a color that depends on the darkMode flag.
 */
@Getter
public class DrawableFieldBackground implements IDrawableShape
{
	private IRectangle fieldWithBorder;
	private double boundaryWidth;


	/**
	 * @param rec
	 * @param boundaryWidth
	 */
	public DrawableFieldBackground(final IRectangle rec, double boundaryWidth)
	{
		fieldWithBorder = rec.withMargin(boundaryWidth);
		this.boundaryWidth = boundaryWidth;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		IVector2 topLeftGlobal = fieldWithBorder.center()
				.addNew(Vector2.fromXY(-fieldWithBorder.xExtent() / 2, -fieldWithBorder.yExtent() / 2));
		IVector2 bottomRightGlobal = fieldWithBorder.center()
				.addNew(Vector2.fromXY(fieldWithBorder.xExtent() / 2, fieldWithBorder.yExtent() / 2));

		final IVector2 topLeft = tool.transformToGuiCoordinates(topLeftGlobal, invert);
		final IVector2 bottomRight = tool.transformToGuiCoordinates(bottomRightGlobal, invert);

		int x = (int) (Math.min(topLeft.x(), bottomRight.x()));
		int y = (int) (Math.min(topLeft.y(), bottomRight.y()));

		int width = Math.abs((int) (bottomRight.x() - topLeft.x()));
		int height = Math.abs((int) (bottomRight.y() - topLeft.y()));

		g.setColor(tool.getFieldColor());
		g.fillRect(x, y, width, height);
	}
}
