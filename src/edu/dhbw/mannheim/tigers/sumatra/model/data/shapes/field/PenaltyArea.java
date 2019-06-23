/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.IEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Linef;


/**
 * Class representing a penalty area
 * The PenaltyArea is built out of one rectangle in the middle, with two quarter circles on the top and the
 * bottom half
 * 
 * @author Malte, Frieder
 */
public class PenaltyArea implements I2DShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(PenaltyArea.class.getName());
	
	private final ETeam				owner;
	private final Vector2f			goalCenter;
	private final Vector2f			penaltyMark;
	
	/** radius of the two, small quarter circles at the sides of the penalty area. */
	private final float				radiusOfPenaltyArea;
	
	/** the length of the short line of the penalty area, that is parallel to the goal line */
	private final float				lengthOfPenaltyAreaFrontLineHalf;
	
	/** needs to checked, if y<=175 && y>=-175 **/
	private final Linef				penaltyAreaFrontLine;
	
	private final Circlef			penaltyCirclePos;
	private final Circlef			penaltyCircleNeg;
	private final Vector2f			penaltyCirclePosCentre;
	private final Vector2f			penaltyCircleNegCentre;
	
	private final Rectanglef		penaltyRectangle;
	// private final Rectanglef behindPenaltyRectangle;
	private final Rectanglef		field;
	
	private final float				botRadius;
	/** for nearestPointOutside only; check for closeness to front line */
	private static final float		PRECISION	= 0.05f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param owner
	 * @param config
	 */
	public PenaltyArea(ETeam owner, Configuration config)
	{
		ETeam.assertOneTeam(owner);
		this.owner = owner;
		
		
		radiusOfPenaltyArea = config.getFloat("field.distanceToPenaltyArea");
		float lengthOfPenaltyAreaFrontLine = config.getFloat("field.lengthOfPenaltyAreaFrontLine");
		lengthOfPenaltyAreaFrontLineHalf = lengthOfPenaltyAreaFrontLine / 2;
		float distanceToPenaltyMark = config.getFloat("field.distanceToPenaltyMark");
		botRadius = config.getFloat("botRadius");
		
		final float fieldLength = config.getFloat("field.length");
		final float fieldWidth = config.getFloat("field.width");
		field = new Rectanglef(new Vector2(-fieldLength / 2, -fieldWidth / 2), new Vector2(fieldLength / 2,
				fieldWidth / 2));
		
		
		if (owner == ETeam.TIGERS)
		{
			goalCenter = new Vector2f(-fieldLength / 2, 0);
			penaltyMark = new Vector2f(goalCenter.x() + distanceToPenaltyMark, goalCenter.y());
			penaltyAreaFrontLine = new Linef(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()),
					new Vector2(0, 1));
			penaltyRectangle = new Rectanglef(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()
					+ lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfPenaltyAreaFrontLineHalf));
			// behindPenaltyRectangle = new Rectanglef(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()
			// + lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
			// - lengthOfPenaltyAreaFrontLineHalf));
			
		} else
		{
			goalCenter = new Vector2f(fieldLength / 2, 0);
			penaltyMark = new Vector2f(goalCenter.x() - distanceToPenaltyMark, goalCenter.y());
			penaltyAreaFrontLine = new Linef(new Vector2(goalCenter.x() - radiusOfPenaltyArea, goalCenter.y()),
					new Vector2(0, 1));
			penaltyRectangle = new Rectanglef(new Vector2(goalCenter.x() - radiusOfPenaltyArea, goalCenter.y()
					+ lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfPenaltyAreaFrontLineHalf));
		}
		
		// all the stuff that doesn't differ for both teams,
		
		// the center of the quarterCircles are on the goal line at the height of the two ends of the parallel
		// frontline
		penaltyCirclePosCentre = new Vector2f(goalCenter.x(), goalCenter.y() + lengthOfPenaltyAreaFrontLineHalf);
		penaltyCirclePos = new Circlef(penaltyCirclePosCentre, radiusOfPenaltyArea);
		
		penaltyCircleNegCentre = new Vector2f(goalCenter.x(), goalCenter.y() - lengthOfPenaltyAreaFrontLineHalf);
		penaltyCircleNeg = new Circlef(penaltyCircleNegCentre, radiusOfPenaltyArea);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Not yet implemented, not necessary.
	 */
	@Override
	@Deprecated
	public float getArea()
	{
		return -1;
	}
	
	
	/**
	 * Get the perimeter (Umfang) of this penalty area (only front curve)
	 * 
	 * @return
	 */
	public float getPerimeterFrontCurve()
	{
		return (penaltyCircleNeg.radius() * AngleMath.PI) + penaltyRectangle.yExtend();
	}
	
	
	/**
	 * Step along the front curve of the penalty area. Starts in positive half on goal line. <br>
	 * Note: Only tested for our penalty area :P
	 * 
	 * @param length [mm]
	 * @return
	 */
	public IVector2 stepAlongPenArea(float length)
	{
		float perimeterQuarterCircle = (penaltyCircleNeg.radius() * AngleMath.PI) / 2;
		int toggle = -1;
		if (getOwner() == ETeam.OPPONENTS)
		{
			toggle = 1;
		}
		
		if (length <= perimeterQuarterCircle)
		{
			IEllipse el = new Ellipse(penaltyCirclePos.center(), penaltyCirclePos.radius(), penaltyCirclePos.radius());
			return GeoMath.stepAlongEllipse(el,
					new Vector2(0, (penaltyRectangle.yExtend() / 2) + penaltyCirclePos.radius()).add(goalCenter), toggle
							* length);
		} else if (length <= (perimeterQuarterCircle + penaltyRectangle.yExtend()))
		{
			return GeoMath.stepAlongLine(new Vector2(penaltyAreaFrontLine.supportVector().x(),
					penaltyRectangle.yExtend() / 2),
					new Vector2(penaltyAreaFrontLine.supportVector().x(), -penaltyRectangle.yExtend() / 2), length
							- perimeterQuarterCircle);
		} else if (length <= ((perimeterQuarterCircle * 2) + penaltyRectangle.yExtend()))
		{
			IEllipse el = new Ellipse(penaltyCircleNeg.center(), penaltyCircleNeg.radius(), penaltyCircleNeg.radius());
			return GeoMath.stepAlongEllipse(el,
					new Vector2(penaltyAreaFrontLine.supportVector().x(), -penaltyRectangle.yExtend() / 2), toggle
							* (length - perimeterQuarterCircle - penaltyRectangle.yExtend()));
		} else
		{
			log.warn("Tried to step too long along penalty area: " + length);
			return new Vector2(getGoalCenter().x(), -((getPenaltyRectangle().yExtend() / 2) + getPenaltyCircleNeg()
					.radius()));
		}
	}
	
	
	@Override
	public boolean isPointInShape(IVector2 point)
	{
		if (field.isPointInShape(point))
		{
			if (penaltyCirclePos.isPointInShape(point))
			{
				return true;
			}
			
			if (penaltyCircleNeg.isPointInShape(point))
			{
				return true;
			}
			if (penaltyRectangle.isPointInShape(point))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * isPointInShape with a margin around the shape
	 * 
	 * @param point
	 * @param margin
	 * @return
	 */
	public boolean isPointInShape(IVector2 point, float margin)
	{
		if (field.isPointInShape(point))
		{
			if (penaltyCirclePos.isPointInShape(point, margin))
			{
				return true;
			}
			
			if (penaltyCircleNeg.isPointInShape(point, margin))
			{
				return true;
			}
			if (penaltyRectangle.isPointInShape(point, margin))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @see #isLineIntersectingShape(ILine, float)
	 */
	@Override
	@Deprecated
	public boolean isLineIntersectingShape(ILine line)
	{
		return isLineIntersectingShape(line, 0);
	}
	
	
	/**
	 * If you want to use this, you should first review the implementation!!
	 * 
	 * @param line
	 * @param securityDistance
	 * @return
	 */
	@Deprecated
	public boolean isLineIntersectingShape(ILine line, float securityDistance)
	{
		// ### check if line lies on PenaltyAreaFrontLine
		if (line.isVertical())
		{
			try
			{
				if (SumatraMath.isEqual(line.getXValue(0), (goalCenter.x() + radiusOfPenaltyArea)))
				{
					// line equals PenaltyAreaFrontLine
					return true;
				}
			} catch (final MathException err)
			{
				// nothing to be done
			}
		}
		
		// / ### check if line intersects PenaltyAreaFrontLine
		try
		{
			final Vector2 intersection = GeoMath.intersectionPoint(line, penaltyAreaFrontLine);
			if ((intersection.y >= -lengthOfPenaltyAreaFrontLineHalf)
					&& (intersection.y <= lengthOfPenaltyAreaFrontLineHalf))
			{
				return true;
			}
		} catch (final MathException err)
		{
			// nothing to be done
		}
		
		try
		{
			final Vector2 intersection = GeoMath.intersectionPoint(line, new Line(goalCenter, new Vector2(0, 1)));
			float length = (radiusOfPenaltyArea + lengthOfPenaltyAreaFrontLineHalf);
			if (owner == ETeam.TIGERS)
			{
				if ((intersection.y() >= length) && (line.getSlope() >= 0))
				{
					return false;
				}
				if ((intersection.y() <= -length) && (line.getSlope() <= 0))
				{
					return false;
				}
			} else
			{
				if ((intersection.y() >= length) && (line.getSlope() <= 0))
				{
					return false;
				}
				if ((intersection.y() <= -length) && (line.getSlope() >= 0))
				{
					return false;
				}
			}
		} catch (MathException err1)
		{
			// nothing to be done
		}
		
		try
		{
			if (GeoMath.isLineInterceptingCircle(penaltyCirclePosCentre, radiusOfPenaltyArea + securityDistance,
					line.getSlope(), line.getYIntercept())
					|| GeoMath.isLineInterceptingCircle(penaltyCircleNegCentre, radiusOfPenaltyArea + securityDistance,
							line.getSlope(), line.getYIntercept()))
			{
				log.warn("in way");
				return true;
			}
		} catch (final MathException err)
		{
			// nothing to be done
		}
		
		return false;
	}
	
	
	/**
	 * While in the rectangle, the point outside is on the line which goes through {@link IVector2} and is parallel to
	 * the x-axis + BotRadius. While in one of the circles, the point outside is on the line which goes through
	 * {@link IVector2} and the center of the circle + BotRadius.
	 * If this addition of the botRadius is changed, please consider changing
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalculator}.
	 * 
	 * @param point
	 * @return point outside the Area
	 */
	@Override
	public IVector2 nearestPointOutside(IVector2 point)
	{
		if (!isPointInShape(point))
		{
			return point;
		}
		
		if (penaltyRectangle.isPointInShape(point))
		{
			if (owner == ETeam.TIGERS)
			{
				return new Vector2(goalCenter.x() + radiusOfPenaltyArea + botRadius, point.y());
			}
			return new Vector2(goalCenter.x() - radiusOfPenaltyArea - botRadius, point.y());
		}
		
		// vector from middle of one quarter circle to given point has to be scaled to circleRadius + botRadius
		// but first: positive or negative circle has to be found out
		if (point.y() > 0)
		{
			final Vector2 pToPVector = new Vector2(point.x() - penaltyCirclePosCentre.x(), point.y()
					- penaltyCirclePosCentre.y());
			pToPVector.scaleTo(radiusOfPenaltyArea + botRadius);
			return new Vector2(pToPVector.x + penaltyCirclePosCentre.x(), pToPVector.y + penaltyCirclePosCentre.y());
		}
		final Vector2 pToPVector = new Vector2(point.x() - penaltyCircleNegCentre.x(), point.y()
				- penaltyCircleNegCentre.y());
		pToPVector.scaleTo(radiusOfPenaltyArea + botRadius);
		return new Vector2(pToPVector.x + penaltyCircleNegCentre.x(), pToPVector.y + penaltyCircleNegCentre.y());
	}
	
	
	/**
	 * Nearest point on line from {@link IVector2} to {@link IVector2}, which is outside of this penalty
	 * Area, but inside the field plus botRadius and towards {@link IVector2}.
	 * If {@link IVector2} is behind the PenaltyArea, behavior is undefined.
	 * 
	 * If this addition of the botRadius is changed, please consider changing
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalculator}.
	 * 
	 * @param point
	 * @param pointToBuildLine
	 * @return point outside the area
	 */
	
	public IVector2 nearestPointOutside(IVector2 point, IVector2 pointToBuildLine)
	{
		if (!isPointInShape(point))
		{
			return point;
		}
		
		final Vector2 p2pVector = new Vector2(pointToBuildLine.x() - point.x(), pointToBuildLine.y() - point.y());
		final int directionCounterP2PVector = directionCounter(p2pVector);
		
		// for intersection calculations
		final Line p2pLine = new Line(point, p2pVector);
		
		
		// intersection on pos. circle
		List<IVector2> intersections = penaltyCirclePos.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on pos. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() >= lengthOfPenaltyAreaFrontLineHalf)
					&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
							intersection.y() - point.y()))) && field.isPointInShape(intersection))
			{
				return scaleVector(intersection, point, pointToBuildLine);
			}
		}
		
		
		// intersection is on neg. circle
		intersections = penaltyCircleNeg.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on neg. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() <= -lengthOfPenaltyAreaFrontLineHalf)
					&& field.isPointInShape(intersection)
					&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
							intersection.y() - point.y()))) && field.isPointInShape(intersection))
			{
				return scaleVector(intersection, point, pointToBuildLine);
			}
		}
		
		// intersection is on rectangle
		intersections = penaltyRectangle.lineIntersection(p2pLine);
		
		switch (intersections.size())
		{
		// p2pLine is on one side of the rectangle
			case 3:
				for (int i = 0; i < intersections.size(); i += 2)
				{
					final IVector2 intersection = intersections.get(i);
					if (owner == ETeam.TIGERS)
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() + radiusOfPenaltyArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					} else
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() - radiusOfPenaltyArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					}
				}
				break;
			case 1:
			case 2:
				for (final IVector2 intersection : intersections)
				{
					if (owner == ETeam.TIGERS)
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() + radiusOfPenaltyArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					} else
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() - radiusOfPenaltyArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					}
					
				}
		}
		
		// if something went wrong, which should not happen if pointToBuild line is not behind the Area
		return point;
	}
	
	
	/**
	 * 
	 * For {@link #nearestPointOutside(IVector2 point, IVector2 pointToBuildLine)} only.
	 * Scales Vector from Point to intersection to a Vector from point to intersection + botRadius.
	 * 
	 * @return
	 */
	private Vector2 scaleVector(IVector2 intersection, IVector2 point, IVector2 pointToBuildLine)
	{
		if (intersection.equals(point, PRECISION))
		{
			final Vector2 conVector = new Vector2(pointToBuildLine.x() - point.x(), pointToBuildLine.y() - point.y());
			conVector.scaleTo(botRadius);
			return new Vector2(conVector.x() + point.x(), conVector.y() + point.y());
		}
		final Vector2 conVector = new Vector2(intersection.x() - point.x(), intersection.y() - point.y());
		conVector.scaleTo(conVector.getLength2() + botRadius);
		return new Vector2(conVector.x() + point.x(), conVector.y() + point.y());
	}
	
	
	/**
	 * 
	 * @param vector
	 * @return 0: x,y <= 0
	 *         1: x <= 0 , y > 0
	 *         2: x > 0 , y <= 0
	 *         3: x,y > 0
	 */
	private int directionCounter(IVector2 vector)
	{
		
		int counter = 0;
		
		if (vector.x() > 0)
		{
			counter += 2;
		}
		
		if (vector.y() > 0)
		{
			counter += 1;
		}
		
		return counter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the penaltyMark in this penaltyArea
	 */
	public IVector2 getPenaltyMark()
	{
		return penaltyMark;
	}
	
	
	/**
	 * @return the penaltyAreaFrontLine
	 */
	public final Linef getPenaltyAreaFrontLine()
	{
		return penaltyAreaFrontLine;
	}
	
	
	/**
	 * @return the owner
	 */
	public final ETeam getOwner()
	{
		return owner;
	}
	
	
	/**
	 * @return the radiusOfPenaltyArea
	 */
	public final float getRadiusOfPenaltyArea()
	{
		return radiusOfPenaltyArea;
	}
	
	
	/**
	 * @return the lengthOfPenaltyAreaFrontLineHalf
	 */
	public final float getLengthOfPenaltyAreaFrontLineHalf()
	{
		return lengthOfPenaltyAreaFrontLineHalf;
	}
	
	
	/**
	 * @return the penaltyCirclePos
	 */
	public final Circlef getPenaltyCirclePos()
	{
		return penaltyCirclePos;
	}
	
	
	/**
	 * @return the penaltyCircleNeg
	 */
	public final Circlef getPenaltyCircleNeg()
	{
		return penaltyCircleNeg;
	}
	
	
	/**
	 * @return the penaltyCirclePosCentre
	 */
	public final Vector2f getPenaltyCirclePosCentre()
	{
		return penaltyCirclePosCentre;
	}
	
	
	/**
	 * @return the penaltyCircleNegCentre
	 */
	public final Vector2f getPenaltyCircleNegCentre()
	{
		return penaltyCircleNegCentre;
	}
	
	
	/**
	 * @return the penaltyRectangle
	 */
	public final Rectanglef getPenaltyRectangle()
	{
		return penaltyRectangle;
	}
	
	
	/**
	 * @return the goalCenter
	 */
	public final Vector2f getGoalCenter()
	{
		return goalCenter;
	}
}
