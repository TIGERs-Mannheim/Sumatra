/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;


/**
 * Draw a fancy planar curve :)
 */
public class DrawablePlanarCurve extends ADrawableWithStroke
{
	private final List<DrawSegment> segments = new ArrayList<>();
	private final IVector2 start;


	/**
	 * Draw a planar curve
	 *
	 * @param curve
	 */
	public DrawablePlanarCurve(PlanarCurve curve)
	{
		start = curve.getPos(curve.getTStart());

		for (var segment : curve.getSegments())
		{
			switch (segment.getType())
			{
				case FIRST_ORDER ->
				{
					IVector2 p2 = segment.getPosition(segment.getDuration());
					segments.add(new DrawSegment(null, p2));
				}
				case SECOND_ORDER ->
				{
					var timeScale = segment.getDuration();
					var v0 = segment.getVel().multiplyNew(timeScale);
					var p0 = segment.getPos();
					var p1 = v0.multiplyNew(0.5).add(p0);
					var p2 = segment.getPosition(segment.getDuration());
					segments.add(new DrawSegment(p1, p2));
				}
				case POINT ->
				{
					// No segments
				}
			}
		}
	}


	/**
	 * Draw a trajectory using a planar curve
	 *
	 * @param trajXY
	 */
	public DrawablePlanarCurve(ITrajectory<? extends IVector> trajXY)
	{
		start = trajXY.getPositionMM(0).getXYVector();
		double tStart = 0;
		var timeSections = trajXY.getTimeSections().stream().distinct().sorted().toList();
		for (var t : timeSections)
		{
			var tDuration = t - tStart;
			if (SumatraMath.isZero(tDuration))
			{
				tStart = t;
				continue;
			}
			if (trajXY.getAcceleration(tStart + 0.5 * tDuration).getXYVector().isZeroVector())
			{
				var p2 = trajXY.getPositionMM(tStart + tDuration).getXYVector();
				segments.add(new DrawSegment(null, p2));
			} else
			{
				var v0 = trajXY.getVelocity(tStart).getXYVector().multiplyNew(tDuration * 1e3);
				var p0 = trajXY.getPositionMM(tStart).getXYVector();
				var p1 = v0.multiplyNew(0.5).add(p0);
				var p2 = trajXY.getPositionMM(tStart + tDuration).getXYVector();
				segments.add(new DrawSegment(p1, p2));
			}
			tStart = t;
		}
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final GeneralPath drawPath = new GeneralPath();
		IVector2 startTrans = tool.transformToGuiCoordinates(start, invert);
		drawPath.moveTo(startTrans.x(), startTrans.y());

		for (var segment : segments)
		{
			if (segment.p1 != null)
			{
				var p1Trans = tool.transformToGuiCoordinates(segment.p1, invert);
				var p2Trans = tool.transformToGuiCoordinates(segment.p2, invert);
				drawPath.quadTo(p1Trans.x(), p1Trans.y(), p2Trans.x(), p2Trans.y());
			} else
			{
				var p2Trans = tool.transformToGuiCoordinates(segment.p2, invert);
				drawPath.lineTo(p2Trans.x(), p2Trans.y());
			}
		}
		g.draw(drawPath);

	}


	@Value
	@RequiredArgsConstructor
	private static class DrawSegment
	{
		IVector2 p1;
		@NonNull
		IVector2 p2;
	}
}
