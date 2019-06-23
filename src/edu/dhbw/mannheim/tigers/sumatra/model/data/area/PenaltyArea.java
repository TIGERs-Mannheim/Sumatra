/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.area;

import java.awt.Color;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.IEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	private final Line				penaltyAreaFrontLine;
	
	private final Circle				penaltyCirclePos;
	private final Circle				penaltyCircleNeg;
	private final Vector2f			penaltyCirclePosCentre;
	private final Vector2f			penaltyCircleNegCentre;
	
	private final Rectangle			penaltyRectangle;
	// private final Rectangle behindPenaltyRectangle;
	private final Rectangle			field;
	
	/** for nearestPointOutside only; check for closeness to front line */
	private static final float		PRECISION	= 0.05f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param owner
	 * @param config
	 */
	public PenaltyArea(final ETeam owner, final Configuration config)
	{
		ETeam.assertOneTeam(owner);
		this.owner = owner;
		
		
		radiusOfPenaltyArea = config.getFloat("field.distanceToPenaltyArea");
		float lengthOfPenaltyAreaFrontLine = config.getFloat("field.lengthOfPenaltyAreaFrontLine");
		lengthOfPenaltyAreaFrontLineHalf = lengthOfPenaltyAreaFrontLine / 2;
		float distanceToPenaltyMark = config.getFloat("field.distanceToPenaltyMark");
		
		final float fieldLength = config.getFloat("field.length");
		final float fieldWidth = config.getFloat("field.width");
		field = new Rectangle(new Vector2(-fieldLength / 2, -fieldWidth / 2),
				new Vector2(fieldLength / 2, fieldWidth / 2));
		
		
		if (owner == ETeam.TIGERS)
		{
			goalCenter = new Vector2f(-fieldLength / 2, 0);
			penaltyMark = new Vector2f(goalCenter.x() + distanceToPenaltyMark, goalCenter.y());
			penaltyAreaFrontLine = new Line(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()),
					new Vector2(0, 1));
			penaltyRectangle = new Rectangle(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()
					+ lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfPenaltyAreaFrontLineHalf));
			// behindPenaltyRectangle = new Rectangle(new Vector2(goalCenter.x() + radiusOfPenaltyArea, goalCenter.y()
			// + lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
			// - lengthOfPenaltyAreaFrontLineHalf));
			
		} else
		{
			goalCenter = new Vector2f(fieldLength / 2, 0);
			penaltyMark = new Vector2f(goalCenter.x() - distanceToPenaltyMark, goalCenter.y());
			penaltyAreaFrontLine = new Line(new Vector2(goalCenter.x() - radiusOfPenaltyArea, goalCenter.y()),
					new Vector2(0, 1));
			penaltyRectangle = new Rectangle(new Vector2(goalCenter.x() - radiusOfPenaltyArea, goalCenter.y()
					+ lengthOfPenaltyAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfPenaltyAreaFrontLineHalf));
		}
		
		// all the stuff that doesn't differ for both teams,
		
		// the center of the quarterCircles are on the goal line at the height of the two ends of the parallel
		// frontline
		penaltyCirclePosCentre = new Vector2f(goalCenter.x(), goalCenter.y() + lengthOfPenaltyAreaFrontLineHalf);
		penaltyCirclePos = new Circle(penaltyCirclePosCentre, radiusOfPenaltyArea);
		
		penaltyCircleNegCentre = new Vector2f(goalCenter.x(), goalCenter.y() - lengthOfPenaltyAreaFrontLineHalf);
		penaltyCircleNeg = new Circle(penaltyCircleNegCentre, radiusOfPenaltyArea);
		
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
	public IVector2 stepAlongPenArea(final float length)
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
	public boolean isPointInShape(final IVector2 point)
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
	public boolean isPointInShape(final IVector2 point, final float margin)
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
			// TIGERS
			if (margin != 0)
			{
				Rectangle marginRectangle = new Rectangle(penaltyRectangle.bottomLeft(), new Vector2(penaltyRectangle
						.bottomLeft().addNew(new Vector2(margin, 0)).x, penaltyRectangle.topRight().y()));
				if (owner == ETeam.OPPONENTS)
				{
					marginRectangle = new Rectangle(penaltyRectangle.bottomRight(), new Vector2(penaltyRectangle
							.bottomRight()
							.addNew(new Vector2(-margin, 0)).x, penaltyRectangle.topLeft().y()));
				}
				if (margin > 0)
				{
					if (penaltyRectangle.isPointInShape(point) || marginRectangle.isPointInShape(point))
					{
						return true;
					}
				} else
				{
					if (penaltyRectangle.isPointInShape(point) && !marginRectangle.isPointInShape(point))
					{
						return true;
					}
				}
			} else
			{
				if (penaltyRectangle.isPointInShape(point))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * @see #isLineIntersectingShape(ILine, float)
	 */
	@Override
	@Deprecated
	public boolean isLineIntersectingShape(final ILine line)
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
	public boolean isLineIntersectingShape(final ILine line, final float securityDistance)
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
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return nearestPointOutside(point, 0);
	}
	
	
	private Rectangle getPenaltyRectangleWithMargin(final float margin)
	{
		IVector2 bottomRight = new Vector2(goalCenter.x(), lengthOfPenaltyAreaFrontLineHalf);
		final IVector2 topleft;
		if (owner == ETeam.TIGERS)
		{
			topleft = new Vector2(goalCenter.x() + radiusOfPenaltyArea + margin, -lengthOfPenaltyAreaFrontLineHalf);
		} else
		{
			topleft = new Vector2(goalCenter.x() - radiusOfPenaltyArea - margin, -lengthOfPenaltyAreaFrontLineHalf);
		}
		
		Rectangle rectMargin = new Rectangle(topleft, bottomRight);
		return rectMargin;
	}
	
	
	private IVector2 getPointInsideField(final IVector2 point)
	{
		IVector2 preprocessedPoint = point;
		float maxX = AIConfig.getGeometry().getFieldLength() / 2;
		if ((owner == ETeam.TIGERS) && (point.y() < -maxX))
		{
			preprocessedPoint = new Vector2(maxX, point.y());
		} else if ((owner == ETeam.OPPONENTS) && (point.y() > maxX))
		{
			preprocessedPoint = new Vector2(maxX, point.y());
		}
		return preprocessedPoint;
	}
	
	
	/**
	 * Calculate the nearest point outside of the penalty area with a given margin.
	 * 
	 * @param point
	 * @param margin
	 * @return nearest point outside of penalty area
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final float margin)
	{
		IVector2 preprocessedPoint = getPointInsideField(point);
		
		if (!isPointInShape(preprocessedPoint, margin))
		{
			return preprocessedPoint;
		}
		
		
		if (getPenaltyRectangleWithMargin(margin).isPointInShape(preprocessedPoint))
		{
			if (owner == ETeam.TIGERS)
			{
				return new Vector2(goalCenter.x() + radiusOfPenaltyArea + margin, preprocessedPoint.y());
			}
			return new Vector2(goalCenter.x() - radiusOfPenaltyArea - margin, preprocessedPoint.y());
		}
		
		// vector from middle of one quarter circle to given point has to be scaled to circleRadius + botRadius
		// but first: positive or negative circle has to be found out
		if (((owner == ETeam.OPPONENTS) && (preprocessedPoint.y() > 0))
				|| ((owner == ETeam.TIGERS) && (preprocessedPoint.y() > 0)))
		{
			final Vector2 pToPVector = preprocessedPoint.subtractNew(penaltyCirclePosCentre);
			pToPVector.scaleTo((radiusOfPenaltyArea + margin));
			return pToPVector.addNew(penaltyCirclePosCentre);
		}
		final Vector2 pToPVector = preprocessedPoint.subtractNew(penaltyCircleNegCentre);
		pToPVector.scaleTo((radiusOfPenaltyArea + margin));
		return pToPVector.addNew(penaltyCircleNegCentre);
	}
	
	
	/**
	 * Warn: Under construction, untested and exceptional return value if null
	 * 
	 * @param p1
	 * @param p2
	 * @param margin
	 * @return
	 */
	public IVector2 getLineIntersection(final IVector2 p1, final IVector2 p2, final float margin)
	{
		ILine line = Line.newLine(p1, p2);
		float frontX = penaltyAreaFrontLine.supportVector().x();
		ILine frontLineWithMargin = Line.newLine(new Vector2(frontX - (Math.signum(frontX) * margin),
				-lengthOfPenaltyAreaFrontLineHalf), new Vector2(frontX - (Math.signum(frontX) * margin),
				-lengthOfPenaltyAreaFrontLineHalf));
		try
		{
			IVector2 frontLineIntersect = GeoMath.intersectionPoint(frontLineWithMargin, line);
			if (Math.abs(frontLineIntersect.y()) < lengthOfPenaltyAreaFrontLineHalf)
			{
				return frontLineIntersect;
			}
		} catch (MathException err)
		{
			throw new IllegalArgumentException("Given line is parallel to front line.");
		}
		
		Circle circlePosMargin = new Circle(penaltyCirclePos.center(), penaltyCirclePos.radius() + margin);
		Rectangle circlePosQuarterRect = new Rectangle(penaltyCirclePos.center(), new Vector2(frontLineWithMargin
				.supportVector().x(), lengthOfPenaltyAreaFrontLineHalf + penaltyCirclePos.radius() + margin));
		List<IVector2> intersections = circlePosMargin.lineIntersections(line);
		for (IVector2 intersect : intersections)
		{
			if (circlePosQuarterRect.isPointInShape(intersect))
			{
				return intersect;
			}
		}
		
		Circle circleNegMargin = new Circle(penaltyCircleNeg.center(), penaltyCircleNeg.radius() + margin);
		Rectangle circleNegQuarterRect = new Rectangle(penaltyCircleNeg.center(), new Vector2(frontLineWithMargin
				.supportVector().x(), -(lengthOfPenaltyAreaFrontLineHalf + penaltyCircleNeg.radius() + margin)));
		intersections = circleNegMargin.lineIntersections(line);
		for (IVector2 intersect : intersections)
		{
			if (circleNegQuarterRect.isPointInShape(intersect))
			{
				return intersect;
			}
		}
		
		assert false : "Given line is strange. It does not cut";
		// FIXME return some valid value
		return null;
	}
	
	
	/**
	 * Nearest point on line from {@link IVector2} to {@link IVector2}, which is outside of this penalty
	 * Area, but inside the field plus botRadius and towards {@link IVector2}.
	 * If {@link IVector2} is behind the PenaltyArea, behavior is undefined.
	 * 
	 * @param point
	 * @param pointToBuildLine
	 * @param margin
	 * @return point outside the area
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine, final float margin)
	{
		if (!isPointInShape(point, margin))
		{
			float xOfGoalLine = goalCenter.x();
			if (((owner == ETeam.TIGERS) && (xOfGoalLine > point.x()))
					|| ((owner == ETeam.OPPONENTS) && (xOfGoalLine < point.x())))
			{
				float yOfPenAreaBeginPlusMargin = getLengthOfPenaltyAreaFrontLineHalf()
						+ getRadiusOfPenaltyArea() + margin;
				if (point.y() > 0)
				{
					return new Vector2(xOfGoalLine, yOfPenAreaBeginPlusMargin);
				}
				return new Vector2(xOfGoalLine, -yOfPenAreaBeginPlusMargin);
			}
			return point;
		}
		
		final Vector2 p2pVector = pointToBuildLine.subtractNew(point);
		
		if (p2pVector.isZeroVector())
		{
			return nearestPointOutside(point, margin);
		}
		
		// for intersection calculations
		final Line p2pLine = new Line(point, p2pVector);
		
		
		// intersection on pos. circle
		Circle circlePosMargin = new Circle(penaltyCirclePos.center(), penaltyCirclePos.radius() + margin);
		List<IVector2> intersections = circlePosMargin.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on pos. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() >= lengthOfPenaltyAreaFrontLineHalf)
					&& field.isPointInShape(intersection))
			{
				return intersection;
			}
		}
		
		
		// intersection is on neg. circle
		Circle circleNegMargin = new Circle(penaltyCircleNeg.center(), penaltyCircleNeg.radius() + margin);
		intersections = circleNegMargin.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on neg. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() <= -lengthOfPenaltyAreaFrontLineHalf)
					&& field.isPointInShape(intersection))
			{
				return intersection;
			}
		}
		
		// intersection is on rectangle
		intersections = getPenaltyRectangleWithMargin(margin).lineIntersection(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			if (owner == ETeam.TIGERS)
			{
				// intersection needs to be on frontLine of penaltyArea
				if ((Math.abs(intersection.x() - (goalCenter.x() + radiusOfPenaltyArea + margin)) < PRECISION))
				{
					return intersection;
				}
			} else
			{
				// intersection needs to be on frontLine of penaltyArea
				if ((Math.abs(intersection.x() - (goalCenter.x() - radiusOfPenaltyArea - margin)) < PRECISION))
				{
					return intersection;
				}
			}
		}
		assert false : "Unexpected state in nearestPointOutside! " + point;
		// if something went wrong, which should not happen if pointToBuild line is not behind the Area
		return point;
	}
	
	
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
	public final Line getPenaltyAreaFrontLine()
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
	public final Circle getPenaltyCirclePos()
	{
		return penaltyCirclePos;
	}
	
	
	/**
	 * @return the penaltyCircleNeg
	 */
	public final Circle getPenaltyCircleNeg()
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
	public final Rectangle getPenaltyRectangle()
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
	
	
	/**
	 * Visual test for penalty area
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public void testPenArea(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		float flen = AIConfig.getGeometry().getFieldLength() + 400;
		float fwidth = AIConfig.getGeometry().getFieldWidth() + 400;
		float step = 50;
		PenaltyArea po = AIConfig.getGeometry().getPenaltyAreaOur();
		PenaltyArea pt = AIConfig.getGeometry().getPenaltyAreaTheir();
		for (float x = -flen / 2; x < (flen / 2); x += step)
		{
			for (float y = -fwidth / 2; y < (fwidth / 2); y += step)
			{
				IVector2 point = new Vector2(x, y);
				Color color = po.isPointInShape(point, 200) ? Color.GREEN : Color.RED;
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
						.add(new DrawablePoint(point, color));
				IVector2 no = pt.nearestPointOutside(point, new Vector2(0, 3000), 200);
				if (!no.equals(point))
				{
					// newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
					// .add(new DrawableLine(Line.newLine(point, no), Color.red, false));
					newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
							.add(new DrawablePoint(no, Color.GREEN));
				} else
				{
					newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.UNSORTED)
							.add(new DrawablePoint(point, Color.BLACK));
				}
				
				newTacticalField
						.getDrawableShapes()
						.get(EDrawableShapesLayer.UNSORTED)
						.add(new DrawablePoint(point, GeoMath.p2pVisibilityBall(baseAiFrame.getWorldFrame(), point,
								new Vector2(), 200) ? Color.GREEN : Color.RED));
				
			}
		}
	}
}
