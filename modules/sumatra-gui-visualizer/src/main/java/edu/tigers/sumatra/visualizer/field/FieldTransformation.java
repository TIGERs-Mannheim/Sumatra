/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field;

import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Data;

import java.awt.Color;


@SuppressWarnings("SuspiciousNameCombination") // x/y and width/height/length not always consistent
@Data
public class FieldTransformation implements IDrawableTool
{
	private static final Color FIELD_COLOR = new Color(0, 160, 30);
	private static final Color FIELD_COLOR_DARK = new Color(77, 77, 77);

	private static final int FIELD_WIDTH = 10000;

	private double fieldGlobalWidth = Geometry.getFieldWidth();
	private double fieldGlobalLength = Geometry.getFieldLength();
	private double fieldGlobalBoundaryWidth = Geometry.getBoundaryWidth();

	private EFieldTurn fieldTurn = EFieldTurn.NORMAL;
	private boolean darkMode = false;
	private double scale = 1.0;


	/**
	 * Transforms a global(field)position into a gui position.
	 *
	 * @param globalPoint
	 * @return guiPosition
	 */
	private IVector2 transformToGuiCoordinates(IVector2 globalPoint)
	{
		IVector2 translation = Vector2.fromXY(fieldGlobalLength, fieldGlobalWidth)
				.multiply(0.5)
				.add(Vector2.fromXY(fieldGlobalBoundaryWidth, fieldGlobalBoundaryWidth));
		IVector2 translatedPoint = globalPoint.addNew(translation);
		return turnPoint(scaleGlobalToGui(translatedPoint));
	}


	@Override
	public IVector2 transformToGuiCoordinates(IVector2 globalPoint, boolean invert)
	{
		return transformToGuiCoordinates(globalPoint.multiplyNew(invert ? -1 : 1));
	}


	@Override
	public double transformToGuiAngle(double globalAngle, boolean invert)
	{
		double offset = switch (fieldTurn)
				{
					case NORMAL -> AngleMath.PI;
					case T90 -> -AngleMath.PI_HALF;
					case T180 -> 0;
					case T270 -> AngleMath.PI_HALF;
				};
		return globalAngle + (invert ? AngleMath.PI : 0) + offset;
	}


	/**
	 * Transforms a gui position into a global(field)position.
	 *
	 * @param guiPoint
	 * @return globalPosition
	 */
	public IVector2 transformToGlobalCoordinates(IVector2 guiPoint)
	{
		IVector2 guiPointTurned = turnPoint(guiPoint);
		IVector2 translation = Vector2.fromXY(fieldGlobalLength, fieldGlobalWidth)
				.multiply(0.5)
				.add(Vector2.fromXY(fieldGlobalBoundaryWidth, fieldGlobalBoundaryWidth));
		return scaleGuiToGlobal(guiPointTurned)
				.subtractNew(translation);
	}


	private IVector2 turnPoint(final IVector2 point)
	{
		final int width = getFieldTotalWidth();
		final int height = getFieldTotalHeight();
		return switch (fieldTurn)
				{
					case NORMAL -> Vector2.fromXY(height - point.x(), point.y());
					case T90 -> Vector2.fromXY(point.y(), point.x());
					case T180 -> Vector2.fromXY(point.x(), width - point.y());
					case T270 -> Vector2.fromXY(width - point.y(), height - point.x());
				};
	}


	@Override
	public int scaleGlobalToGui(double length)
	{
		double scaleFactor = getFieldWidth() / fieldGlobalWidth;
		return (int) Math.round(length * scaleFactor);
	}


	private IVector2 scaleGlobalToGui(IVector2 point)
	{
		return Vector2.fromXY(scaleGlobalToGui(point.x()), scaleGlobalToGui(point.y()));
	}


	private int scaleGuiToGlobal(double length)
	{
		double scaleFactor = fieldGlobalWidth / getFieldWidth();
		return (int) Math.round(length * scaleFactor);
	}


	private IVector2 scaleGuiToGlobal(IVector2 point)
	{
		return Vector2.fromXY(scaleGuiToGlobal(point.x()), scaleGuiToGlobal(point.y()));
	}


	private int getFieldTotalWidth()
	{
		return getFieldWidth() + 2 * getPanelBoundaryWidth();
	}


	private int getFieldTotalHeight()
	{
		return getFieldHeight() + 2 * getPanelBoundaryWidth();
	}


	private double getFieldRatio()
	{
		return fieldGlobalLength / fieldGlobalWidth;
	}


	public double getFieldTotalRatio()
	{
		return (fieldGlobalLength + 2 * fieldGlobalBoundaryWidth) /
				(fieldGlobalWidth + 2 * fieldGlobalBoundaryWidth);
	}


	private int getFieldHeight()
	{
		return (int) Math.round(getFieldRatio() * FIELD_WIDTH);
	}


	private int getFieldWidth()
	{
		return FIELD_WIDTH;
	}


	private int getPanelBoundaryWidth()
	{
		return scaleGlobalToGui(fieldGlobalBoundaryWidth);
	}


	public double getFieldScale(int width, int height)
	{
		double heightScaleFactor;
		double widthScaleFactor;
		if (width > height)
		{
			heightScaleFactor = (double) height / getFieldTotalWidth();
			widthScaleFactor = (double) width / getFieldTotalHeight();
		} else
		{
			heightScaleFactor = ((double) height) / getFieldTotalHeight();
			widthScaleFactor = ((double) width) / getFieldTotalWidth();
		}
		scale = Math.min(heightScaleFactor, widthScaleFactor);
		return scale;
	}


	@Override
	public Color getFieldColor()
	{
		return darkMode ? FIELD_COLOR_DARK : FIELD_COLOR;
	}
}
