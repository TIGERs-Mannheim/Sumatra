/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * A Tube with a color
 */
@Persistent(version = 2)
public class DrawableTube extends ADrawableWithStroke
{
	private ITube tube;
	private boolean fill;


	/**
	 * for DB only
	 */
	@SuppressWarnings("unused")
	private DrawableTube()
	{
		tube = Tube.create(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, 1);
	}


	/**
	 * @param tube
	 */
	public DrawableTube(final ITube tube)
	{
		this.tube = tube;
	}


	/**
	 * @param tube
	 * @param color
	 */
	public DrawableTube(final ITube tube, final Color color)
	{
		this.tube = tube;
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		double radius = tool.scaleGlobalToGui(tube.radius());
		final IVector2 startCenter = tool.transformToGuiCoordinates(tube.startCenter(), invert);
		final IVector2 endCenter = tool.transformToGuiCoordinates(tube.endCenter(), invert);
		ILineSegment segment = Lines.segmentFromPoints(startCenter, endCenter);
		List<IVector2> corners = new ArrayList<>();
		corners.add(startCenter.addNew(segment.directionVector().getNormalVector().scaleTo(radius)));
		corners.add(endCenter.addNew(segment.directionVector().getNormalVector().scaleTo(radius)));
		corners.add(startCenter.addNew(segment.directionVector().getNormalVector().scaleTo(-radius)));
		corners.add(endCenter.addNew(segment.directionVector().getNormalVector().scaleTo(-radius)));
		int[] xVals = new int[4];
		int[] yVals = new int[4];
		int i = 0;
		for (IVector2 point : corners)
		{
			xVals[i] = (int) point.x();
			yVals[i] = (int) point.y();
			i++;
		}

		if (fill)
		{
			g.fillArc((int) (startCenter.x() - radius), (int) (startCenter.y() - radius),
					(int) radius * 2, (int) radius * 2, 0, 360);
			g.fillArc((int) (endCenter.x() - radius), (int) (endCenter.y() - radius),
					(int) radius * 2, (int) radius * 2, 0, 360);
			g.fillPolygon(xVals, yVals, 4);
			return;
		}
		int startAngle = 0;
		int arcAngle = 360;
		Optional<Double> angle = segment.directionVector().getNormalVector().angleTo(Vector2.fromX(1));
		if (angle.isPresent())
		{
			startAngle = (int) AngleMath.rad2deg(angle.get());
			arcAngle = 180;
		}

		g.drawArc((int) (startCenter.x() - radius), (int) (startCenter.y() - radius),
				(int) radius * 2, (int) radius * 2, startAngle, arcAngle);
		g.drawArc((int) (endCenter.x() - radius), (int) (endCenter.y() - radius), (int) radius * 2,
				(int) radius * 2, -180 + startAngle, arcAngle);
		g.drawLine((int) corners.get(0).x(), (int) corners.get(0).y(), (int) corners.get(1).x(),
				(int) corners.get(1).y());
		g.drawLine((int) corners.get(2).x(), (int) corners.get(2).y(), (int) corners.get(3).x(),
				(int) corners.get(3).y());
	}


	@Override
	public DrawableTube setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}
}
