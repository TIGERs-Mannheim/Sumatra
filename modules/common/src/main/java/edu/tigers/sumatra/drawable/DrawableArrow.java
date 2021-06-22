/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


@Persistent
public class DrawableArrow extends ADrawable
{
	private IVector2 position;
	private IVector2 direction;
	private int arrowSize = 25;


	@SuppressWarnings("unused") // berkeley
	private DrawableArrow()
	{
	}


	public DrawableArrow(IVector2 position, IVector2 direction)
	{
		this.position = position;
		this.direction = direction;
	}


	public DrawableArrow(IVector2 position, IVector2 direction, Color color)
	{
		this.position = position;
		this.direction = direction;
		setColor(color);
	}


	public DrawableArrow(IVector2 position, IVector2 direction, Color color, int arrowSize)
	{
		this(position, direction, color);
		this.arrowSize = arrowSize;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		IVector2 guiPosition = tool.transformToGuiCoordinates(position, invert);
		IVector2 guiDestination = tool.transformToGuiCoordinates(position.addNew(direction), invert);

		drawArrow(g, (int) guiPosition.x(), (int) guiPosition.y(), (int) guiDestination.x(), (int) guiDestination.y(),
				tool.scaleXLength(arrowSize));

	}


	private void drawArrow(Graphics2D g1, int x1, int y1, int x2, int y2, int arrowTipSize)
	{
		Graphics2D g = (Graphics2D) g1.create();
		double dx = x2 - (double) x1;
		double dy = y2 - (double) y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.drawLine(0, 0, len - arrowTipSize, 0);
		g.fillPolygon(new int[] { len, len - arrowTipSize, len - arrowTipSize, len },
				new int[] { 0, -arrowTipSize, arrowTipSize, 0 }, 4);
	}
}
