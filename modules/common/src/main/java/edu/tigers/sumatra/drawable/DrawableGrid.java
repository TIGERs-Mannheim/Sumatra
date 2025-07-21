/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;


@RequiredArgsConstructor
public class DrawableGrid implements IDrawableShape
{
	private static final DecimalFormat DF = new DecimalFormat("00");
	private final List<IVector3> points;
	private final double cellExtentX;
	private final double cellExtentY;

	@Setter
	@Accessors(chain = true)
	private IColorPicker colorPicker = ColorPickerFactory.scaledSingleBlack(0, 0, 0, 1.0, 2);

	@Setter
	@Accessors(chain = true)
	private boolean drawNumbers;

	private List<IDrawableShape> cellShapes;
	private List<IDrawableShape> numberShapes;


	public static DrawableGrid generate(
			int numX,
			int numY,
			double width,
			double height,
			ToDoubleFunction<IVector2> ratingFn
	)
	{
		List<IVector2> points = new ArrayList<>();
		for (int iy = 0; iy < numY; iy++)
		{
			for (int ix = 0; ix < numX; ix++)
			{
				double x = (-height / 2) + (ix * (height / (numX - 1)));
				double y = (-width / 2) + (iy * (width / (numY - 1)));
				points.add(Vector2.fromXY(x, y));
			}
		}

		List<IVector3> ratedPoints = points.parallelStream()
				.map(p -> (IVector3) Vector3.from2d(p, ratingFn.applyAsDouble(p)))
				.toList();
		double cellExtentX = height / numX;
		double cellExtentY = width / numY;
		return new DrawableGrid(ratedPoints, cellExtentX, cellExtentY);
	}


	@Override
	public void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
		IDrawableShape.super.paintShape(g, tool, invert);

		if (cellShapes == null)
		{
			cellShapes = generateCellShapes();
		}
		if (numberShapes == null)
		{
			if (drawNumbers)
			{
				numberShapes = generateNumberShapes();
			} else
			{
				numberShapes = List.of();
			}
		}
		cellShapes.forEach(s -> s.paintShape(g, tool, invert));
		numberShapes.forEach(s -> s.paintShape(g, tool, invert));
	}


	private List<IDrawableShape> generateCellShapes()
	{
		return points.stream().map(this::createCell).toList();
	}


	private List<IDrawableShape> generateNumberShapes()
	{
		return points.stream().map(this::createNumber).toList();
	}


	private IDrawableShape createCell(IVector3 point)
	{
		Color color = colorPicker.getColor(point.z());
		IVector2 pos = point.getXYVector();
		Rectangle rectangle = Rectangle.fromCenter(pos, cellExtentX, cellExtentY);
		return new DrawableRectangle(rectangle)
				.setFill(true)
				.setColor(color);
	}


	private IDrawableShape createNumber(IVector3 point)
	{
		long number = Math.round(point.z() * 100);
		if (number >= 100)
		{
			return new DrawableAnnotation(point.getXYVector(), "");
		}
		String text = DF.format(number);
		return new DrawableAnnotation(point.getXYVector(), text)
				.withFontHeight(cellExtentX / 2)
				.withCenterHorizontally(true);
	}
}
