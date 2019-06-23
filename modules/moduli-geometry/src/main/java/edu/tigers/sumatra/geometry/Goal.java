/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * This is a immutable representation of a goal.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Goal
{
	private final double width;
	private final double depth;
	private final Vector2f center;
	private final Vector2f leftPost;
	private final Vector2f rightPost;
	private final ILine line;
	
	
	/**
	 * @param width
	 * @param center
	 * @param depth
	 */
	public Goal(final double width, final IVector2 center, final double depth)
	{
		this.width = width;
		this.depth = depth;
		this.center = Vector2f.copy(center);
		
		leftPost = Vector2f.fromXY(center.x(), center.y() + (width / 2.0));
		rightPost = Vector2f.fromXY(center.x(), center.y() - (width / 2.0));
		line = Line.fromPoints(leftPost, rightPost);
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
	
	
	/**
	 * @return the goal line from left to right post
	 */
	public ILine getLine()
	{
		return line;
	}
	
	
	/**
	 * @param point
	 * @param margin
	 * @return
	 */
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		IRectangle goalRectangle = Rectangle.fromPoints(leftPost,
				rightPost.addNew(Vector2.fromX(Math.signum(center.x()) * depth)));
		return goalRectangle.isPointInShape(point, margin);
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
		return new Goal(width + 2 * yMargin, center, depth + 2 * xMargin);
	}
}
