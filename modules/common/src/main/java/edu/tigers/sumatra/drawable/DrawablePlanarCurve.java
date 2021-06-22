/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveState;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;


/**
 * Draw a fancy planar curve :)
 */
@Persistent
public class DrawablePlanarCurve extends ADrawableWithStroke
{
	private final List<IVector2> points = new ArrayList<>();


	@SuppressWarnings("unused")
	private DrawablePlanarCurve()
	{
	}


	/**
	 * Draw a planar curve with default stepSize and anglePrecision.
	 *
	 * @param curve
	 */
	public DrawablePlanarCurve(final PlanarCurve curve)
	{
		this(curve, 0.2, 0.1);
	}


	/**
	 * Draw planar curve sampled at equal intervals of stepSize, skipping points with angle deviation below anglePrecision.
	 *
	 * @param curve
	 * @param stepSize       in [s]
	 * @param anglePrecision in [rad]
	 */
	public DrawablePlanarCurve(final PlanarCurve curve, final double stepSize, final double anglePrecision)
	{
		IVector2 vLast = null;

		for (double t = 0; t < (curve.getTEnd() - stepSize); t += stepSize)
		{
			PlanarCurveState state = curve.getState(t);
			IVector2 pos = state.getPos();
			IVector2 vel = state.getVel();

			if (shouldAddPoint(vLast, vel, anglePrecision))
			{
				points.add(pos);
				vLast = vel;
			}

			if (t > stepSize * 1000)
			{
				break;
			}
		}

		points.add(curve.getState(curve.getTEnd()).getPos());
	}


	private boolean shouldAddPoint(IVector2 vLast, IVector2 vCur, double anglePrecision)
	{
		if (vLast == null)
		{
			// first point
			return true;
		}

		var vDiff = vCur.angleToAbs(vLast);
		if (vDiff.isEmpty())
		{
			// vCur or vLast is zero, this only happens at the beginning or end of the curve
			return true;
		}
		return vDiff.get() > anglePrecision;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final GeneralPath drawPath = new GeneralPath();
		IVector2 pLast = points.get(0);
		IVector2 posTrans = tool.transformToGuiCoordinates(pLast, invert);
		drawPath.moveTo(posTrans.x(), posTrans.y());

		for (int i = 1; i < points.size(); i++)
		{
			IVector2 pos = points.get(i);
			posTrans = tool.transformToGuiCoordinates(pos, invert);
			drawPath.lineTo(posTrans.x(), posTrans.y());

			if (VectorMath.distancePP(pLast, pos) > 0.2)
			{
				pLast = pos;
			}
		}
		g.draw(drawPath);
	}
}
