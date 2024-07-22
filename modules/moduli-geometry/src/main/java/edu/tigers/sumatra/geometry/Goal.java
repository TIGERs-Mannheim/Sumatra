/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.List;


/**
 * This is an immutable representation of a goal.
 */
public class Goal
{
	private final double width;
	private final double depth;
	private final Vector2f center;
	private final Vector2f leftPost;
	private final Vector2f rightPost;
	private final ILine line;
	private final ILineSegment goalLine;
	private final ILineSegment lineSegment;
	private final IRectangle rectangle;


	public Goal(IVector2 center, double width, double depth, double fieldWidth)
	{
		this.width = width;
		this.depth = depth;
		this.center = Vector2f.copy(center);

		leftPost = Vector2f.fromXY(center.x(), center.y() + (width / 2.0));
		rightPost = Vector2f.fromXY(center.x(), center.y() - (width / 2.0));
		line = Lines.lineFromPoints(leftPost, rightPost);
		goalLine = Lines.segmentFromPoints(Vector2.fromXY(center.x(), -fieldWidth / 2.0),
				Vector2.fromXY(center.x(), fieldWidth / 2.0));
		lineSegment = Lines.segmentFromPoints(leftPost, rightPost);
		rectangle = Rectangle.fromPoints(leftPost,
				rightPost.addNew(Vector2.fromX(Math.signum(center.x()) * depth)));
	}


	/**
	 * @return the width of the goal.
	 */
	public double getWidth()
	{
		return width;
	}


	/**
	 * @return the depth of the goal
	 */
	public double getDepth()
	{
		return depth;
	}


	/**
	 * @return the vector of the goal.
	 */
	public Vector2f getCenter()
	{
		return center;
	}


	/**
	 * @return the postion of the left goal post.
	 */
	public Vector2f getLeftPost()
	{
		return leftPost;

	}


	/**
	 * @return the postion of the right goal post.
	 */
	public Vector2f getRightPost()
	{
		return rightPost;
	}


	public List<IVector2> getGoalPosts()
	{
		return List.of(
				getLeftPost(),
				getRightPost()
		);
	}


	/**
	 * @return the unbound goal line
	 */
	public ILine getLine()
	{
		return line;
	}


	/**
	 * @return the goal line segment from left to right field corner
	 */
	public ILineSegment getGoalLine()
	{
		return goalLine;
	}


	/**
	 * @return the line segment from left to right post
	 */
	public ILineSegment getLineSegment()
	{
		return lineSegment;
	}


	public IRectangle getRectangle()
	{
		return rectangle;
	}


	public List<IVector2> getCorners()
	{
		return List.of(
				leftPost.addMagnitude(Vector2.fromXY(depth, 0)),
				rightPost.addMagnitude(Vector2.fromXY(depth, 0))
		);
	}


	/**
	 * @param point
	 * @param margin
	 * @return
	 */
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return rectangle.withMargin(margin).isPointInShape(point);
	}


	/**
	 * @param point
	 * @return
	 */
	public boolean isPointInShape(final IVector2 point)
	{
		return rectangle.isPointInShape(point);
	}


	/**
	 * @param margin [mm]
	 * @return
	 */
	public Goal withMargin(double margin)
	{
		return withMargin(margin, margin);
	}


	/**
	 * @param xMargin [mm]
	 * @param yMargin [mm]
	 * @return
	 */
	public Goal withMargin(double xMargin, double yMargin)
	{
		return new Goal(center, width + 2 * yMargin, depth + 2 * xMargin, goalLine.getLength());
	}


	/**
	 * This methods calculates the point where the bisector("Winkelhalbierende") of the angle(alpha) at p1 cuts the
	 * goal line.
	 * <pre>
	 *     bisector
	 *  gl----x----gr
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *     source
	 * </pre>
	 *
	 * @param source
	 * @return bisector
	 */
	public IVector2 bisection(IVector2 source)
	{
		return TriangleMath.bisector(source, leftPost, rightPost);
	}
}
