/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): DanielAl
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.area;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
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


/**
 * Class representing the area around the penalty area with extra space for freekick
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class FreekickArea implements I2DShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(FreekickArea.class.getName());
	
	private static final float		FREEKICK_RULE_DISTANCE	= 200;
	
	private final ETeam				owner;
	private final Vector2f			goalCenter;
	
	/** radius of the two, small quarter circles at the sides of the penalty area. */
	private final float				radiusOfFreekickArea;
	
	/** the length of the short line of the penalty area, that is parallel to the goal line */
	private final float				lengthOfFreekickAreaFrontLineHalf;
	
	/** needs to checked, if y<=175 && y>=-175 **/
	private final Line				freekickAreaFrontLine;
	
	private final Circle				freekickCirclePos;
	private final Circle				freekickCircleNeg;
	private final Vector2f			freekickCirclePosCentre;
	private final Vector2f			freekickCircleNegCentre;
	
	private final Rectangle			freekickRectangle;
	private final Rectangle			field;
	
	private final float				botRadius;
	/** for nearestPointOutside only; check for closeness to front line */
	private static final float		PRECISION					= 0.05f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param owner
	 * @param config
	 */
	public FreekickArea(ETeam owner, Configuration config)
	{
		ETeam.assertOneTeam(owner);
		this.owner = owner;
		
		radiusOfFreekickArea = config.getFloat("field.distanceToPenaltyArea") + FREEKICK_RULE_DISTANCE;
		float lengthOfFreekickAreaFrontLine = config.getFloat("field.lengthOfPenaltyAreaFrontLine");
		lengthOfFreekickAreaFrontLineHalf = lengthOfFreekickAreaFrontLine / 2;
		botRadius = config.getFloat("botRadius");
		
		final float fieldLength = config.getFloat("field.length");
		final float fieldWidth = config.getFloat("field.width");
		field = new Rectangle(new Vector2(-fieldLength / 2, -fieldWidth / 2),
				new Vector2(fieldLength / 2, fieldWidth / 2));
		
		
		if (owner == ETeam.TIGERS)
		{
			goalCenter = new Vector2f(-fieldLength / 2, 0);
			freekickAreaFrontLine = new Line(new Vector2(goalCenter.x() + radiusOfFreekickArea, goalCenter.y()),
					new Vector2(0, 1));
			freekickRectangle = new Rectangle(new Vector2(goalCenter.x() + radiusOfFreekickArea, goalCenter.y()
					+ lengthOfFreekickAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfFreekickAreaFrontLineHalf));
		} else
		{
			goalCenter = new Vector2f(fieldLength / 2, 0);
			freekickAreaFrontLine = new Line(new Vector2(goalCenter.x() - radiusOfFreekickArea, goalCenter.y()),
					new Vector2(0, 1));
			freekickRectangle = new Rectangle(new Vector2(goalCenter.x() - radiusOfFreekickArea, goalCenter.y()
					+ lengthOfFreekickAreaFrontLineHalf), new Vector2(goalCenter.x(), goalCenter.y()
					- lengthOfFreekickAreaFrontLineHalf));
		}
		
		// all the stuff that doesn't differ for both teams,
		
		// the center of the quarterCircles are on the goal line at the height of the two ends of the parallel
		// frontline
		freekickCirclePosCentre = new Vector2f(goalCenter.x(), goalCenter.y() + lengthOfFreekickAreaFrontLineHalf);
		freekickCirclePos = new Circle(freekickCirclePosCentre, radiusOfFreekickArea);
		
		freekickCircleNegCentre = new Vector2f(goalCenter.x(), goalCenter.y() - lengthOfFreekickAreaFrontLineHalf);
		freekickCircleNeg = new Circle(freekickCircleNegCentre, radiusOfFreekickArea);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Step along the front curve of the penalty area. Starts in positive half on goal line. <br>
	 * Note: Only tested for our penalty area :P
	 * 
	 * @param length [mm]
	 * @return
	 */
	public IVector2 stepAlongArea(float length)
	{
		float perimeterQuarterCircle = (freekickCircleNeg.radius() * AngleMath.PI) / 2;
		int toggle = -1;
		if (getOwner() == ETeam.OPPONENTS)
		{
			toggle = 1;
		}
		
		if (length <= perimeterQuarterCircle)
		{
			IEllipse el = new Ellipse(freekickCirclePos.center(), freekickCirclePos.radius(), freekickCirclePos.radius());
			return GeoMath.stepAlongEllipse(el,
					new Vector2(0, (freekickRectangle.yExtend() / 2) + freekickCirclePos.radius()).add(goalCenter), toggle
							* length);
		} else if (length <= (perimeterQuarterCircle + freekickRectangle.yExtend()))
		{
			return GeoMath.stepAlongLine(
					new Vector2(freekickAreaFrontLine.supportVector().x(), freekickRectangle.yExtend() / 2), new Vector2(
							freekickAreaFrontLine.supportVector().x(), -freekickRectangle.yExtend() / 2), length
							- perimeterQuarterCircle);
		} else if (length <= ((perimeterQuarterCircle * 2) + freekickRectangle.yExtend()))
		{
			IEllipse el = new Ellipse(freekickCircleNeg.center(), freekickCircleNeg.radius(), freekickCircleNeg.radius());
			return GeoMath.stepAlongEllipse(el,
					new Vector2(freekickAreaFrontLine.supportVector().x(), -freekickRectangle.yExtend() / 2), toggle
							* (length - perimeterQuarterCircle - freekickRectangle.yExtend()));
		} else
		{
			log.warn("Tried to step too long along penalty area: " + length);
			return new Vector2(getGoalCenter().x(), -((getFreekickRectangle().yExtend() / 2) + getFreekickCircleNeg()
					.radius()));
		}
	}
	
	
	@Override
	public boolean isPointInShape(IVector2 point)
	{
		if (field.isPointInShape(point))
		{
			if (freekickCirclePos.isPointInShape(point))
			{
				return true;
			}
			
			if (freekickCircleNeg.isPointInShape(point))
			{
				return true;
			}
			if (freekickRectangle.isPointInShape(point))
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
			if (freekickCirclePos.isPointInShape(point, margin))
			{
				return true;
			}
			
			if (freekickCircleNeg.isPointInShape(point, margin))
			{
				return true;
			}
			if (freekickRectangle.isPointInShape(point, margin))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 */
	@Override
	@Deprecated
	public boolean isLineIntersectingShape(ILine line)
	{
		return false;
	}
	
	
	/**
	 * While in the rectangle, the point outside is on the line which goes through {@link IVector2} and is parallel to
	 * the x-axis + BotRadius. While in one of the circles, the point outside is on the line which goes through
	 * {@link IVector2} and the center of the circle + BotRadius.
	 * If this addition of the botRadius is changed, please consider changing
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalc}.
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
		
		if (freekickRectangle.isPointInShape(point))
		{
			if (owner == ETeam.TIGERS)
			{
				return new Vector2(goalCenter.x() + radiusOfFreekickArea + botRadius, point.y());
			}
			return new Vector2(goalCenter.x() - radiusOfFreekickArea - botRadius, point.y());
		}
		
		// vector from middle of one quarter circle to given point has to be scaled to circleRadius + botRadius
		// but first: positive or negative circle has to be found out
		if (point.y() > 0)
		{
			final Vector2 pToPVector = new Vector2(point.x() - freekickCirclePosCentre.x(), point.y()
					- freekickCirclePosCentre.y());
			pToPVector.scaleTo(radiusOfFreekickArea + botRadius);
			return new Vector2(pToPVector.x + freekickCirclePosCentre.x(), pToPVector.y + freekickCirclePosCentre.y());
		}
		final Vector2 pToPVector = new Vector2(point.x() - freekickCircleNegCentre.x(), point.y()
				- freekickCircleNegCentre.y());
		pToPVector.scaleTo(radiusOfFreekickArea + botRadius);
		return new Vector2(pToPVector.x + freekickCircleNegCentre.x(), pToPVector.y + freekickCircleNegCentre.y());
	}
	
	
	/**
	 * Nearest point on line from {@link IVector2} to {@link IVector2}, which is outside of this penalty
	 * Area, but inside the field plus botRadius and towards {@link IVector2}.
	 * If {@link IVector2} is behind the PenaltyArea, behavior is undefined.
	 * 
	 * If this addition of the botRadius is changed, please consider changing
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalc}.
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
		List<IVector2> intersections = freekickCirclePos.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on pos. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() >= lengthOfFreekickAreaFrontLineHalf)
					&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
							intersection.y() - point.y()))) && field.isPointInShape(intersection))
			{
				return scaleVector(intersection, point, pointToBuildLine);
			}
		}
		
		
		// intersection is on neg. circle
		intersections = freekickCircleNeg.lineIntersections(p2pLine);
		
		for (final IVector2 intersection : intersections)
		{
			// test if intersection is in field, on neg. quarter circle and direction is toward pointToBuildLine
			if ((intersection.y() <= -lengthOfFreekickAreaFrontLineHalf)
					&& field.isPointInShape(intersection)
					&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
							intersection.y() - point.y()))) && field.isPointInShape(intersection))
			{
				return scaleVector(intersection, point, pointToBuildLine);
			}
		}
		
		// intersection is on rectangle
		intersections = freekickRectangle.lineIntersection(p2pLine);
		
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
						if ((Math.abs(intersection.x() - (goalCenter.x() + radiusOfFreekickArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					} else
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() - radiusOfFreekickArea)) < PRECISION)
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
						if ((Math.abs(intersection.x() - (goalCenter.x() + radiusOfFreekickArea)) < PRECISION)
								&& (directionCounterP2PVector == directionCounter(new Vector2(intersection.x() - point.x(),
										intersection.y() - point.y()))))
						{
							return scaleVector(intersection, point, pointToBuildLine);
						}
					} else
					{
						// intersection needs to be on frontLine of penaltyArea
						if ((Math.abs(intersection.x() - (goalCenter.x() - radiusOfFreekickArea)) < PRECISION)
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
	 * @return the freekickAreaFrontLine
	 */
	public final Line getFreekickAreaFrontLine()
	{
		return freekickAreaFrontLine;
	}
	
	
	/**
	 * @return the owner
	 */
	public final ETeam getOwner()
	{
		return owner;
	}
	
	
	/**
	 * @return the radiusOfFreekickArea
	 */
	public final float getRadiusOfFreekickArea()
	{
		return radiusOfFreekickArea;
	}
	
	
	/**
	 * @return the lengthOfFreekickAreaFrontLineHalf
	 */
	public final float getLengthOfFreekickAreaFrontLineHalf()
	{
		return lengthOfFreekickAreaFrontLineHalf;
	}
	
	
	/**
	 * @return the freekickCirclePos
	 */
	public final Circle getFreekickCirclePos()
	{
		return freekickCirclePos;
	}
	
	
	/**
	 * @return the freekickCircleNeg
	 */
	public final Circle getFreekickCircleNeg()
	{
		return freekickCircleNeg;
	}
	
	
	/**
	 * @return the freekickCirclePosCentre
	 */
	public final Vector2f getFreekickCirclePosCentre()
	{
		return freekickCirclePosCentre;
	}
	
	
	/**
	 * @return the freekickCircleNegCentre
	 */
	public final Vector2f getFreekickCircleNegCentre()
	{
		return freekickCircleNegCentre;
	}
	
	
	/**
	 * @return the freekickRectangle
	 */
	public final Rectangle getFreekickRectangle()
	{
		return freekickRectangle;
	}
	
	
	/**
	 * @return the goalCenter
	 */
	public final Vector2f getGoalCenter()
	{
		return goalCenter;
	}
	
	
	@Deprecated
	@Override
	public float getArea()
	{
		return 0;
	}
}
