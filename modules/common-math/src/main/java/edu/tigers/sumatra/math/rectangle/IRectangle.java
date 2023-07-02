/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import edu.tigers.sumatra.export.IJsonString;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;
import java.util.Random;


/**
 * Regular Rectangle interface.
 * All regular rectangle sides are either parallel to the X- or the Y-Axis.
 * Therefore, it is impossible to create skewed rectangles.
 *
 * @author Malte
 */
public interface IRectangle extends I2DShape, IJsonString
{
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the Y-Axis.
	 *
	 * @return
	 */
	double yExtent();


	/**
	 * Absolute value of the length of the side
	 * that is parallel to the X-Axis.
	 *
	 * @return
	 */
	double xExtent();


	/**
	 * @return max x among all 4 corners
	 */
	double maxX();


	/**
	 * @return min x among all 4 corners
	 */
	double minX();


	/**
	 * @return max y among all 4 corners
	 */
	double maxY();


	/**
	 * @return min y among all 4 corners
	 */
	double minY();


	/**
	 * Generates a random point within the shape.
	 *
	 * @param rnd
	 * @return
	 */
	IVector2 getRandomPointInShape(Random rnd);


	boolean isCircleInShape(ICircle circle);


	/**
	 * Center of the rectangle
	 *
	 * @return
	 */
	IVector2 center();


	/**
	 * Get a specified corner of the rectangle
	 *
	 * @param pos which corner
	 * @return IVector2 of the position of the selected corner
	 */
	IVector2 getCorner(ECorner pos);


	/**
	 * Get all four points of the rectangle.
	 * Starting at bottomLeft, going clockwise.
	 *
	 * @return List of corner points.
	 */
	List<IVector2> getCorners();

	/**
	 * Get the rectangle edges in clockwise order.
	 * Starting with the left edge.
	 *
	 * @return List of all edges
	 */
	List<ILineSegment> getEdges();

	/**
	 * Create a new rectangle with a given margin in each direction
	 *
	 * @param margin a positive or negative margin
	 * @return a new rectangle
	 */
	@Override
	IRectangle withMargin(double margin);

	/**
	 * Create a new rectangle with a given margin in each direction
	 *
	 * @param xMargin a positive or negative margin for x direction
	 * @param yMargin a positive or negative margin for y direction
	 * @return a new rectangle
	 */
	IRectangle withMarginXy(double xMargin, double yMargin);

	/**
	 * Get the nearest point inside the rectangle, if point is outside.<br>
	 * Use pointToBuildLine to move the point to the rectangle border
	 *
	 * @param point            the point to check and move
	 * @param pointToBuildLine a point to build a line for generating intersections on the rectangle border
	 * @return a point inside the rectangle
	 */
	IVector2 nearestPointInside(IVector2 point, IVector2 pointToBuildLine);

	/**
	 * @return a mirrored shape
	 */
	IRectangle mirror();


	/**
	 * Top -> posY
	 * Right -> posX
	 */
	enum ECorner
	{
		BOTTOM_LEFT(0),
		TOP_LEFT(1),
		TOP_RIGHT(2),
		BOTTOM_RIGHT(3);

		private final int index;


		ECorner(int index)
		{
			this.index = index;
		}


		public int getIndex()
		{
			return this.index;
		}

	}
}
