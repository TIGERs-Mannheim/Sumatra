/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s):
 * GuntherB
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.PointCloud;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * 
 * @author GuntherB, FlorianS
 * 
 */
public class OffensePointsCarrier extends AOffensePoints
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static int			myMemorySize		= 3;
	private static int			myTriesPerCircle	= 10;
	
	private final Goal			goal					= AIConfig.getGeometry().getGoalTheir();
	private final float			goalSize				= goal.getSize();
	
	private final float			fieldLength			= AIConfig.getGeometry().getFieldLength();
	private final float			fieldWidth			= AIConfig.getGeometry().getFieldWidth();
	private static final float	penaltyAreaLength	= 500;
	
	/** enum to classify different ball position y-values */
	private enum EBallPositionY
	{
		LEFT,
		RIGHT,
		CENTER
	}
	
	private EBallPositionY	ballY;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public OffensePointsCarrier()
	{
		super(myMemorySize, myTriesPerCircle);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected PointCloud generateNewCloud(WorldFrame worldFrame)
	{
		final IVector2 ballPos = worldFrame.ball.getPos();
		
		final Vector2 referencePoint = new Vector2(GeoMath.INIT_VECTOR);
		float rectLength = 0;
		float rectWidth = 0;
		
		// get attack mode
		if (ballPos.y() > (goalSize / 2))
		{
			ballY = EBallPositionY.LEFT;
		} else if (ballPos.y() < (-goalSize / 2))
		{
			ballY = EBallPositionY.RIGHT;
		} else
		{
			ballY = EBallPositionY.CENTER;
		}
		
		// get rectangle's reference point x-value
		referencePoint.x = 0;
		
		// get rectangle's reference point y-value
		if (ballY == EBallPositionY.LEFT)
		{
			referencePoint.y = fieldWidth / 2;
		} else if (ballY == EBallPositionY.RIGHT)
		{
			referencePoint.y = -goalSize / 2;
		} else if (ballY == EBallPositionY.CENTER)
		{
			referencePoint.y = goalSize / 2;
		}
		
		// get rectangle's length
		rectLength = (fieldLength / 2) - penaltyAreaLength;
		
		// get rectangle's width
		if (ballY == EBallPositionY.LEFT)
		{
			rectWidth = (fieldWidth - goalSize) / 2;
		} else if (ballY == EBallPositionY.RIGHT)
		{
			rectWidth = (fieldWidth - goalSize) / 2;
		} else if (ballY == EBallPositionY.CENTER)
		{
			rectWidth = goalSize;
		}
		
		// create rectangle
		final Rectangle rect = new Rectangle(referencePoint, rectLength, rectWidth);
		
		// get random point from rectangle
		final Vector2 point = rect.getRandomPointInShape();
		
		return new PointCloud(new ValuePoint(point.x, point.y));
	}
	
	
	@Override
	protected float evaluateCloud(PointCloud cloud, WorldFrame wf)
	{
		// here, all different steps, that evaluate a cloud, should be called from the supertype,
		// and be multiplied with the value please put new evaluateMethods into the supertype
		
		float newValue = 100.0f;
		
		// add value for minimum distance
		newValue *= evaluateMinimumDistances(cloud, wf);
		newValue *= evaluateBallVisibility(cloud, wf);
		newValue *= evaluateGoalVisibility(cloud, wf);
		// newValue *= evaluateAngle(cloud, wf);
		newValue *= evaluatePositionValidity(cloud, wf);
		newValue *= evaluateEvolution(cloud);
		
		return newValue;
	}
	
	
	/**
	 * @param point
	 * @param wf
	 * @return
	 */
	public static float evaluatePoint(Vector2 point, WorldFrame wf)
	{
		
		// here, all different steps, that evaluate a cloud, should be called from the supertype,
		// and be multiplied with the value please put new evaluateMethods into the supertype
		final PointCloud cloud = new PointCloud(new ValuePoint(point.x, point.y));
		
		float newValue = 100.0f;
		
		// add value for minimum distance
		newValue *= evaluateMinimumDistances(cloud, wf);
		newValue *= evaluateBallVisibility(cloud, wf);
		newValue *= evaluateGoalVisibility(cloud, wf);
		// newValue *= evaluateAngle(cloud, wf);
		newValue *= evaluatePositionValidity(cloud, wf);
		newValue *= evaluateEvolution(cloud);
		
		return newValue;
	}
	
	
	@Override
	protected void setInfoInTacticalField(AIInfoFrame curFrame, List<ValuePoint> points)
	{
		curFrame.tacticalInfo.setOffCarrierPoints(points);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// create default list
		final Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
		final List<ValuePoint> defaultList = new ArrayList<ValuePoint>();
		defaultList.add(new ValuePoint(fieldCenter.x(), fieldCenter.y()));
		setInfoInTacticalField(curFrame, defaultList);
	}
	
	
}
