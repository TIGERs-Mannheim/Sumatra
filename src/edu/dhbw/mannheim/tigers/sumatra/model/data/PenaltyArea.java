/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;

/**
 * Class representing a penalty area
 * 
 * @author Malte, Frieder
 * 
 */
public class PenaltyArea implements I2DShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ETeam owner;
	private final Vector2f goalCenter;
	/** radius of the two, small quarter circles at the sides of the penalty area. */
	private final float distanceToPenaltyArea;
	/** the length of the short line of the penalty area, that is parallel to the goal line */
	@SuppressWarnings("unused")
	private final float lengthOfPenaltyAreaFrontLine;
	/** needs to checked, if y<=175 && y>=-175**/
	private final Linef PenaltyAreaFrontLine;
	@SuppressWarnings("unused")
	private final Circlef PenaltyCirclePos;
	@SuppressWarnings("unused")
	private final Circlef PenaltyCircleNeg;
	private final Rectanglef penaltyRectangle;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	//FIXME: Don't use hard coded values in here!
	public PenaltyArea(ETeam owner, IVector2 goalCenter, float disToPenArea, float lengthOfFrontLine)
	{
		ETeam.assertOneTeam(owner);
		this.owner = owner;
		this.distanceToPenaltyArea = disToPenArea;
		this.lengthOfPenaltyAreaFrontLine = lengthOfFrontLine;
		this.goalCenter = new Vector2f(goalCenter);
		if(owner == ETeam.TIGERS)
		{
			this.PenaltyAreaFrontLine = new Linef(new Vector2(goalCenter.x()+450, goalCenter.y()), new Vector2(0 , 1)); 
			this.PenaltyCirclePos = new Circlef(new Vector2(goalCenter.x()+175, goalCenter.y()), distanceToPenaltyArea);
			this.PenaltyCircleNeg = new Circlef(new Vector2(goalCenter.x()-175, goalCenter.y()), distanceToPenaltyArea);
			this.penaltyRectangle = new Rectanglef(new Vector2(goalCenter.x()+175, goalCenter.y()+450), new Vector2(goalCenter.x()-175, goalCenter.y()));
		}
		else
		{
			this.PenaltyAreaFrontLine = new Linef(new Vector2(goalCenter.x()-450, goalCenter.y()), new Vector2(0 , 1));
			this.PenaltyCirclePos = new Circlef(new Vector2(goalCenter.x()+175, goalCenter.y()), distanceToPenaltyArea);
			this.PenaltyCircleNeg = new Circlef(new Vector2(goalCenter.x()-175, goalCenter.y()), distanceToPenaltyArea);
			this.penaltyRectangle = new Rectanglef(new Vector2(goalCenter.x()+175, goalCenter.y()-450), new Vector2(goalCenter.x()-175, goalCenter.y()));
		}
	}
	
//	public PenaltyArea(ETeam owner)
//	{
//		
//	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Not yet implemented, not necessary.
	 */
	@Override
	public float getArea()
	{	
		return 42;
	}
	

	@Override
	public boolean isPointInShape(IVector2 point)
	{
		// TODO: This should work without a call of AIConfig, so that we can test this in a JUnit class!
		if(AIConfig.getGeometry().getField().isPointInShape(point))
		{
			if(AIMath.distancePP(point, new Vector2(goalCenter.x+175, goalCenter.y)) <= distanceToPenaltyArea)
			{
				return true;
			}
		
			if(AIMath.distancePP(point, new Vector2(goalCenter.x-175, goalCenter.y)) <= distanceToPenaltyArea)
			{
				return true;
			}
			if(penaltyRectangle.isPointInShape(point))
			{
				return true;
			}
		}
		return false;
	}
	

	@Override //TODO Could intercept circles behind the goalLine, but not before
	public boolean isLineIntersectingShape(ILine line)
	{
		
		if(line.isVertical()){
			try
			{
				if( line.getXValue(0) == goalCenter.x+450)
				{ 
					return true;		//line equals PenaltyAreaFrontLine
				}
			} catch (MathException err)
			{
				//nothing to be done
			}
		}

		try
		{
			Vector2 intersection = AIMath.intersectionPoint(line, PenaltyAreaFrontLine);
			if( intersection.y>=-175 && intersection.y<=175)
			{
				return true;		
			}
		} catch (MathException err)
		{
			//nothing to be done
		}
		
		try
		{
			if(AIMath.isLineInterceptingCircle(new Vector2(goalCenter.x+175, 
					goalCenter.y), distanceToPenaltyArea, line.getSlope(), line.getYIntercept()))
			{
				return true;
			}
		} catch (MathException err)
		{
			//nothing to be done
		}
		
		try
		{
			if(AIMath.isLineInterceptingCircle(new Vector2(goalCenter.x-175, 
					goalCenter.y), distanceToPenaltyArea, line.getSlope(), line.getYIntercept()))
			{
				return true;
			}
		} catch (MathException err)
		{
			//nothing to be done
		}
		
		return false;
	}
	

	@Override
	public IVector2 nearestPointOutside(IVector2 point)
	{
		if(!isPointInShape(point))
		{
			return point;
		}
		
		if(penaltyRectangle.isPointInShape(point))
		{
			if(owner == ETeam.TIGERS)
			{
				return new Vector2(goalCenter.x+450, point.y());
			}
			if(owner == ETeam.OPPONENTS)
			{
				return new Vector2(goalCenter.x-450, point.y());

			}
		}
		
		if(AIMath.distancePP(point, new Vector2(goalCenter.x+175, goalCenter.y)) <= distanceToPenaltyArea)
		{
				float angle = new Vector2(point.x()-goalCenter.x+175, point.y()-goalCenter.y).getAngle();
				
				return new Vector2(angle).scaleTo(distanceToPenaltyArea);
		}else
		{
			float angle = new Vector2(point.x()-goalCenter.x-175, point.y()-goalCenter.y).getAngle();
			
			return new Vector2(angle).scaleTo(distanceToPenaltyArea);	
		}
	}






	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
