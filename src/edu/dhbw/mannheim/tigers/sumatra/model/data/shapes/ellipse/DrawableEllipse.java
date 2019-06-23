/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 18, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.persistence.Embeddable;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * This is the drawable ellipse that fits to the normal Ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Embeddable
public class DrawableEllipse extends Ellipse implements IDrawableShape, Shape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log			= Logger.getLogger(DrawableEllipse.class.getName());
	private Color						color			= Color.red;
	private IVector2					curveStart	= GeoMath.INIT_VECTOR;
	private float						curvelength	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param ellipse
	 * @param color
	 */
	public DrawableEllipse(final IEllipse ellipse, final Color color)
	{
		super(ellipse);
		this.color = color;
	}
	
	
	/**
	 * @param ellipse
	 */
	public DrawableEllipse(final IEllipse ellipse)
	{
		super(ellipse);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paintShape(Graphics2D g)
	{
		g.setStroke(new BasicStroke(1));
		g.setColor(color);
		
		Rectangle r = getBounds();
		
		g.rotate(-getTurnAngle(), r.x + (r.width / 2), r.y + (r.height / 2));
		g.drawOval(r.x, r.y, r.width, r.height);
		g.rotate(getTurnAngle(), r.x + (r.width / 2), r.y + (r.height / 2));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the curvelength
	 */
	public final float getCurvelength()
	{
		return curvelength;
	}
	
	
	/**
	 * @return the curveStart
	 */
	public final IVector2 getCurveStart()
	{
		return curveStart;
	}
	
	
	/**
	 * Do not draw the complete ellipse, but only the curve specified by start and length
	 * 
	 * @param start point on the ellipse
	 * @param length how long should the curve be? may also be negative
	 */
	public final void setCurve(final IVector2 start, final float length)
	{
		curvelength = length;
		curveStart = start;
	}
	
	
	@Override
	public Rectangle getBounds()
	{
		IVector2 center = FieldPanel.transformToGuiCoordinates(getCenter());
		int x = (int) (center.x() - FieldPanel.scaleXLength(getRadiusY()));
		int y = (int) (center.y() - FieldPanel.scaleYLength(getRadiusX()));
		int width = FieldPanel.scaleXLength(getRadiusY()) * 2;
		int height = FieldPanel.scaleYLength(getRadiusX()) * 2;
		return new Rectangle(x, y, width, height);
	}
	
	
	@Override
	public Rectangle2D getBounds2D()
	{
		return new Rectangle(getBounds());
	}
	
	
	@Override
	public boolean contains(double x, double y)
	{
		return isPointInShape(new Vector2(x, y));
	}
	
	
	@Override
	public boolean contains(Point2D p)
	{
		return isPointInShape(new Vector2(p.getX(), p.getY()));
	}
	
	
	@Override
	public boolean intersects(double x, double y, double w, double h)
	{
		// we are lazy ;)
		return getBounds().intersects(x, y, w, h);
	}
	
	
	@Override
	public boolean intersects(Rectangle2D r)
	{
		return getBounds().intersects(r);
	}
	
	
	@Override
	public boolean contains(double x, double y, double w, double h)
	{
		return getBounds().contains(x, y, w, h);
	}
	
	
	@Override
	public boolean contains(Rectangle2D r)
	{
		return getBounds().contains(r);
	}
	
	
	@Override
	public PathIterator getPathIterator(AffineTransform at)
	{
		return getPathIterator(at, 4);
	}
	
	
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness)
	{
		if (curveStart.equals(GeoMath.INIT_VECTOR))
		{
			return new FlatteningPathIterator(new EllipsePathIterator(at, this), flatness);
		}
		return new FlatteningPathIterator(new EllipsePathIterator(at, this, curveStart, curvelength), flatness);
	}
	
	/**
	 * A PathIterator is used to draw the shape
	 * This class is not used atm, because I found out that drawing an ellipse is quite easy with
	 * {@link Graphics2D#drawOval(int, int, int, int)} and {@link Graphics2D#rotate(double, double, double)}
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Deprecated
	private static class EllipsePathIterator implements PathIterator
	{
		private static final float		EQUALS_TOLERANCE	= 1f;
		private static final float		STEP_SIZE			= 5;
		private static final long		EMERGENCY_EXIT		= 10000;
		private final IEllipse			ellipse;
		private IVector2					curPos;
		private IVector2					lastPos;
		private boolean					done					= false;
		private boolean					firstSegment		= true;
		/** count the steps and exit, if EMERGENCY_EXIT reached */
		private long						emergencyCounter;
		private final AffineTransform	at;
		
		private float						stepsLeft			= 0;
		private boolean					limitedSteps		= true;
		
		private final float				stepSize;
		
		
		/**
		 * 
		 * @param at
		 * @param ellipse
		 * @param curveStart
		 * @param curveLength
		 */
		public EllipsePathIterator(AffineTransform at, IEllipse ellipse, IVector2 curveStart, float curveLength)
		{
			this.ellipse = ellipse;
			curPos = curveStart;
			lastPos = curPos;
			emergencyCounter = 0;
			this.at = at;
			if (curveLength < 0)
			{
				stepSize = -STEP_SIZE;
				stepsLeft = -curveLength;
			} else
			{
				stepSize = STEP_SIZE;
				stepsLeft = curveLength;
			}
		}
		
		
		/**
		 * 
		 * @param at
		 * @param ellipse
		 */
		public EllipsePathIterator(AffineTransform at, IEllipse ellipse)
		{
			this(at, ellipse, ellipse.getCenter()
					.addNew(new Vector2(0, ellipse.getRadiusY()).turn(ellipse.getTurnAngle())), 0);
			limitedSteps = false;
		}
		
		
		@Override
		public int getWindingRule()
		{
			return WIND_NON_ZERO;
		}
		
		
		@Override
		public boolean isDone()
		{
			return done;
		}
		
		
		@Override
		public void next()
		{
			IVector2 newPos = ellipse.stepOnCurve(curPos, stepSize);
			if (limitedSteps)
			{
				stepsLeft -= GeoMath.distancePP(newPos, curPos);
			}
			curPos = newPos;
			
			// done if first pos reached or if done was already true
			if (limitedSteps)
			{
				done = done || (stepsLeft <= 0);
			} else
			{
				done = (done || curPos.equals(lastPos, EQUALS_TOLERANCE));
			}
		}
		
		
		@Override
		public int currentSegment(float[] coords)
		{
			emergencyCounter++;
			IVector2 transP = FieldPanel.transformToGuiCoordinates(curPos);
			if (at != null)
			{
				transP = transP.addNew(new Vector2(at.getTranslateX(), at.getTranslateY()));
			}
			coords[0] = transP.x();
			coords[1] = transP.y();
			if (firstSegment)
			{
				firstSegment = false;
				return SEG_MOVETO;
			}
			if (emergencyCounter > EMERGENCY_EXIT)
			{
				// we looped so long... lets exit now, or we might never exit...
				done = true;
				log.warn("Emergency exit. We were nearly in an endless loop");
			}
			return SEG_LINETO;
		}
		
		
		@Override
		public int currentSegment(double[] coords)
		{
			float[] fcoords = new float[coords.length];
			int result = currentSegment(fcoords);
			for (int i = 0; i < fcoords.length; i++)
			{
				coords[i] = fcoords[i];
			}
			return result;
		}
	}
}
