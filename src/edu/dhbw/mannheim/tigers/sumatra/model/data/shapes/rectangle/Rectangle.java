/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Mutable implementation of {@link IRectangle}.
 * 
 * @author Malte
 */
@Persistent
public class Rectangle extends ARectangle
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** is always in the top left corner of the rectangle */
	private final IVector2	topLeft;
	
	/** x */
	private final float		xExtend;
	/** y */
	private final float		yExtend;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private Rectangle()
	{
		this(AVector2.ZERO_VECTOR, new Vector2(1, 1));
	}
	
	
	/**
	 * Creates a new Rectangle. Length and width must be positive
	 * and greater then zero.
	 * 
	 * @param topLeft which is used to define the position of the rectangle on the field.
	 *           Is always left-top.
	 * @param rectangleLength the length (x-axis)
	 * @param rectangleWidth the width (y-axis)
	 * @throws IllegalArgumentException when length or width is negative or zero.
	 */
	public Rectangle(final IVector2 topLeft, final float rectangleLength, final float rectangleWidth)
	{
		this.topLeft = new Vector2(topLeft);
		if ((rectangleLength <= 0) || (rectangleWidth <= 0))
		{
			throw new IllegalArgumentException("Lenght or width cannot be negative or zero.");
		}
		yExtend = rectangleWidth;
		xExtend = rectangleLength;
	}
	
	
	/**
	 * Creates new Rectangle from two points. Have to be counter side corners.
	 * 
	 * @param p1
	 * @param p2
	 * @throws IllegalArgumentException when length or width is zero.
	 * @author DionH
	 */
	public Rectangle(final IVector2 p1, final IVector2 p2)
	{
		topLeft = new Vector2(SumatraMath.min(p1.x(), p2.x()), SumatraMath.max(p1.y(), p2.y()));
		float xExtend = Math.abs(p1.x() - p2.x());
		float yExtend = Math.abs(p1.y() - p2.y());
		
		if ((xExtend == 0) || (yExtend == 0))
		{
			xExtend = 1e-6f;
			yExtend = 1e-6f;
			// throw new IllegalArgumentException("Lenght or width cannot be negative: " + p1 + ", " + p2);
		}
		this.xExtend = xExtend;
		this.yExtend = yExtend;
	}
	
	
	/**
	 * @param p0
	 * @param p1
	 * @param radius
	 * @return
	 */
	public static Rectangle aroundLine(final IVector2 p0, final IVector2 p1, final float radius)
	{
		IVector2 dir;
		if (p0.equals(p1))
		{
			dir = AVector2.X_AXIS;
		} else
		{
			dir = p0.subtractNew(p1);
		}
		IVector2 orthDir = dir.turnNew(AngleMath.PI_HALF).scaleTo(radius);
		IVector2 p2 = p1.addNew(dir.scaleToNew(-radius));
		IVector2 p3 = p0.addNew(dir.scaleToNew(radius));
		return new Rectangle(p3.addNew(orthDir), p2.addNew(orthDir.turnNew(AngleMath.PI)));
	}
	
	
	/**
	 * @param rec
	 */
	public Rectangle(final IRectangle rec)
	{
		topLeft = new Vector2(rec.topLeft());
		xExtend = rec.xExtend();
		yExtend = rec.yExtend();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public float yExtend()
	{
		return yExtend;
	}
	
	
	@Override
	public float xExtend()
	{
		return xExtend;
	}
	
	
	@Override
	public IVector2 topLeft()
	{
		return topLeft;
	}
	
	
	@Override
	public IVector2 topRight()
	{
		return new Vector2(topLeft.x() + xExtend, topLeft.y());
	}
	
	
	@Override
	public IVector2 bottomLeft()
	{
		return new Vector2f(topLeft.x(), topLeft.y() - yExtend);
	}
	
	
	@Override
	public IVector2 bottomRight()
	{
		return new Vector2f(topLeft.x() + xExtend, topLeft.y() - yExtend);
	}
	
	
	@Override
	public IVector2 getMidPoint()
	{
		return new Vector2(topLeft.x() + (xExtend / 2), topLeft.y() - (yExtend / 2));
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		List<IVector2> corners = new ArrayList<IVector2>();
		
		corners.add(new Vector2(topLeft));
		corners.add(bottomLeft());
		corners.add(bottomRight());
		corners.add(topRight());
		
		return corners;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		return nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		Circle circle = new Circle(getMidPoint(), Math.max(yExtend, xExtend));
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 topLeft = fieldPanel.transformToGuiCoordinates(topLeft(), invert);
		final IVector2 bottomRight = fieldPanel.transformToGuiCoordinates(bottomRight(), invert);
		
		int x = (int) (topLeft.x() < bottomRight.x() ? topLeft.x() : bottomRight.x());
		int y = (int) (topLeft.y() < bottomRight.y() ? topLeft.y() : bottomRight.y());
		
		int width = Math.abs((int) (bottomRight.x() - topLeft.x()));
		int height = Math.abs((int) (bottomRight.y() - topLeft.y()));
		
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(1));
		g.drawRect(x, y, width, height);
	}
}
