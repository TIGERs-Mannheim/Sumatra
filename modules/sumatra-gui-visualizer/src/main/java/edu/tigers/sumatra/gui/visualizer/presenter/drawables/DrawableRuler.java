/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.drawables;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Graphics2D;
import java.util.List;


public class DrawableRuler implements IDrawableShape
{
	@Configurable(comment = "Factor for annotation font height", defValue = "10.0")
	private static double factorFontHeight = 10;
	@Configurable(comment = "Factor for ruler stroke strength", defValue = "1.5")
	private static double factorStrokeStrength = 1.5;
	@Configurable(comment = "[deg] Angle between 0° and 45° to start stacking annotations", defValue = "35")
	private static int stackingAngle = 35;

	static
	{
		ConfigRegistration.registerClass("user", DrawableRuler.class);
	}

	private final IDrawableShape shape;
	private final ILineSegment rulerLine;
	private final String lineDescription;


	public DrawableRuler(IVector2 start, IVector2 end)
	{
		IVector2 start2End = end.subtractNew(start);
		lineDescription = String.format(" %.1f / %.1f %nlength: %.1f / angle: %.2f / %.1f°", start2End.x(), start2End.y(),
				start2End.getLength2(), start2End.getAngle(), AngleMath.rad2deg(start2End.getAngle()));
		rulerLine = Lines.segmentFromPoints(start, end);
		shape = new DrawableLine(rulerLine);
	}


	private EFieldRotation getFieldRotation(EFieldTurn turn)
	{
		double fieldAngle = turn.getAngle();
		if (fieldAngle > AngleMath.PI_HALF)
		{
			fieldAngle -= AngleMath.PI;
		}
		return fieldAngle < AngleMath.PI_QUART ? EFieldRotation.HORIZONTAL : EFieldRotation.VERTICAL;
	}


	private List<DrawableAnnotation> findBestAnnotationPlacement(IVector2 start, IVector2 end, EFieldRotation rotation,
			int fontHeight)
	{
		double rulerAngle = Math.abs(rulerLine.directionVector().getAngle());
		if (rotation == EFieldRotation.HORIZONTAL)
		{
			double minAngleToHorizontal = rulerAngle > AngleMath.PI_HALF ? AngleMath.PI - rulerAngle : rulerAngle;
			if (minAngleToHorizontal < AngleMath.deg2rad(stackingAngle) || 6 * fontHeight > end.subtractNew(start)
					.getLength2())
			{
				double y = Math.min(start.y(), end.y());

				return List.of(
						new DrawableAnnotation(Vector2.fromXY(start.x(), y - 2.5 * fontHeight),
								String.format("Start: %.1f / %.1f", start.x(), start.y())),
						new DrawableAnnotation(Vector2.fromXY(start.x(), y - 1.5 * fontHeight),
								String.format("End: %.1f / %.1f", end.x(), end.y())),
						new DrawableAnnotation(Vector2.fromXY(start.x(), y - 4.5 * fontHeight), "dist:" + lineDescription)
				);
			}
		} else
		{
			if (SumatraMath.isBetween(rulerAngle, AngleMath.deg2rad(90.0 - stackingAngle),
					AngleMath.deg2rad(90.0 + stackingAngle)) || 6 * fontHeight > end.subtractNew(start).getLength2())
			{
				double x = Math.min(start.x(), end.x());
				return List.of(
						new DrawableAnnotation(Vector2.fromXY(x - 1.5 * fontHeight, start.y()),
								String.format("Start: %.1f / %.1f", start.x(), start.y())),
						new DrawableAnnotation(Vector2.fromXY(x - 2.5 * fontHeight, start.y()),
								String.format("End: %.1f   / %.1f", end.x(), end.y())),
						new DrawableAnnotation(Vector2.fromXY(x - 4.5 * fontHeight, start.y()), "dist:" + lineDescription)
				);
			}
		}
		return List.of(
				new DrawableAnnotation(start, String.format(" %.1f / %.1f", start.x(), start.y())),
				new DrawableAnnotation(end, String.format(" %.1f / %.1f", end.x(), end.y())),
				new DrawableAnnotation(start.addNew(end.subtractNew(start).multiplyNew(0.5)), lineDescription)
		);
	}


	@Override
	public void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
		shape.setStrokeWidth(factorStrokeStrength / tool.getScale()).paintShape(g, tool, invert);
		int fontHeight = (int) (factorFontHeight / tool.getScale());
		List<DrawableAnnotation> annotations = findBestAnnotationPlacement(rulerLine.getPathStart(), rulerLine.getPathEnd(),
				getFieldRotation(tool.getFieldTurn()), fontHeight);
		annotations.forEach(a -> a.withFontHeight(fontHeight).paintShape(g, tool, invert));
	}


	private enum EFieldRotation
	{
		HORIZONTAL,
		VERTICAL
	}
}
