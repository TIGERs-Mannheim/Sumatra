/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;


/**
 * Drawable trajectory path. Uses sampled points on a bang bang trajectory.
 */
@Persistent
public class DrawableTrajectoryArea extends ADrawableWithStroke
{
	private static final double STEP_SIZE = 0.05;

	private final List<ILineSegment> lines = new ArrayList<>();
	private final IVector2 pathStart;
	private final IVector2 pathEnd;


	@SuppressWarnings("unused")
	private DrawableTrajectoryArea()
	{
		pathStart = Vector2.zero();
		pathEnd = Vector2.zero();
	}


	public DrawableTrajectoryArea(ITrajectory<? extends IVector> trajXY, DoubleUnaryOperator widthFunction)
	{
		pathStart = trajXY.getPositionMM(0.0).getXYVector();
		pathEnd = trajXY.getPositionMM(trajXY.getTotalTime()).getXYVector();
		for (double t = 0; t < (trajXY.getTotalTime() - STEP_SIZE); t += STEP_SIZE)
		{
			IVector2 pos = trajXY.getPositionMM(t).getXYVector();
			IVector2 vel = trajXY.getVelocity(t).getXYVector();
			if (!vel.isZeroVector())
			{
				double dist = widthFunction.applyAsDouble(vel.getLength2());
				IVector2 start = pos.addNew(vel.turnNew(AngleMath.DEG_090_IN_RAD).scaleTo(dist));
				IVector2 end = pos.addNew(vel.turnNew(-AngleMath.DEG_090_IN_RAD).scaleTo(dist));
				ILineSegment line = Lines.segmentFromPoints(start, end);
				lines.add(line);
			}
		}
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		GeneralPath drawPathStart = new GeneralPath();
		GeneralPath drawPathEnd = new GeneralPath();

		IVector2 guiStart = tool.transformToGuiCoordinates(pathStart, invert);
		drawPathStart.moveTo(guiStart.x(), guiStart.y());
		drawPathEnd.moveTo(guiStart.x(), guiStart.y());

		for (ILineSegment line : lines)
		{
			GeneralPath drawPathLine = new GeneralPath();
			IVector2 start = tool.transformToGuiCoordinates(line.getPathStart(), invert);
			IVector2 end = tool.transformToGuiCoordinates(line.getPathEnd(), invert);
			drawPathLine.moveTo(start.x(), start.y());
			drawPathLine.lineTo(end.x(), end.y());
			g.draw(drawPathLine);

			drawPathStart.lineTo(start.x(), start.y());
			drawPathEnd.lineTo(end.x(), end.y());
		}

		IVector2 guiEnd = tool.transformToGuiCoordinates(pathEnd, invert);
		drawPathStart.lineTo(guiEnd.x(), guiEnd.y());
		drawPathEnd.lineTo(guiEnd.x(), guiEnd.y());

		g.draw(drawPathStart);
		g.draw(drawPathEnd);
	}
}
