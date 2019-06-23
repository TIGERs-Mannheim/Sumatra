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

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PointCloud;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ESide;


/**
 * 
 * @author GuntherB, FlorianS
 * 
 */
public class OffensePointsReceiver extends AOffensePoints
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static int	myMemorySize			= 3;
	private static int	myTriesPerCircle		= 6;
	
	private Goal			goal						= AIConfig.getGeometry().getGoalTheir();
	private final float	goalSize					= goal.getSize();
	
	private final float	fieldLength				= AIConfig.getGeometry().getFieldLength();
	private final float	fieldWidth				= AIConfig.getGeometry().getFieldWidth();
	private final float	penaltyAreaLength		= 500;
	
	private final float	RECT_MIN_LENGTH		= 1000;
	private final float	LENGTH_BEHIND_BALL	= 500;
	
	/** enum to classify different ball position x-values */
	private enum EBallPositionX
	{
		FRONT,
		BACK,
		CENTER
	};
	
	/** enum to classify different ball position y-values */
	private enum EBallPositionY
	{
		LEFT,
		RIGHT,
		CENTER
	};
	
	private EBallPositionX	ballX;
	private EBallPositionY	ballY;
	private ESide				side;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 */
	public OffensePointsReceiver(ESide side)
	{
		super(myMemorySize, myTriesPerCircle);
		this.side = side;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected PointCloud generateNewCloud(WorldFrame worldFrame)
	{
		Vector2f ballPos = worldFrame.ball.pos;
		
		Vector2 leftReferencePoint = new Vector2(AIConfig.INIT_VECTOR);
		float leftRectWidth = 0;
		
		Vector2 rightReferencePoint = new Vector2(AIConfig.INIT_VECTOR);
		float rightRectWidth = 0;
		
		float rectLength = 0;
		
		// classify ball position x-value
		if (ballPos.x - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL > fieldLength / 2 - penaltyAreaLength - RECT_MIN_LENGTH
				- LENGTH_BEHIND_BALL)
		{
			ballX = EBallPositionX.FRONT;
		} else if (ballPos.x - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL < -fieldLength / 2 + penaltyAreaLength)
		{
			ballX = EBallPositionX.BACK;
		} else
		{
			ballX = EBallPositionX.CENTER;
		}
		
		// classify ball position y-value
		if (ballPos.y > goalSize / 2)
		{
			ballY = EBallPositionY.LEFT;
		} else if (ballPos.y < -goalSize / 2)
		{
			ballY = EBallPositionY.RIGHT;
		} else
		{
			ballY = EBallPositionY.CENTER;
		}
		
		// get non side specific rectangle values
		if (ballX == EBallPositionX.FRONT)
		{
			leftReferencePoint.x = fieldLength / 2 - penaltyAreaLength - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL;
			rightReferencePoint.x = fieldLength / 2 - penaltyAreaLength - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL;
		} else if (ballX == EBallPositionX.BACK)
		{
			leftReferencePoint.x = -fieldLength / 2 + penaltyAreaLength;
			rightReferencePoint.x = -fieldLength / 2 + penaltyAreaLength;
		} else if (ballX == EBallPositionX.CENTER)
		{
			leftReferencePoint.x = ballPos.x - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL;
			rightReferencePoint.x = ballPos.x - RECT_MIN_LENGTH - LENGTH_BEHIND_BALL;
		}
		rectLength = RECT_MIN_LENGTH;
		
		// get side specific rectangle values
		if (side == ESide.LEFT)
		{
			if (ballY == EBallPositionY.LEFT)
			{
				leftReferencePoint.y = goalSize / 2;
				leftRectWidth = goalSize;
			} else if (ballY == EBallPositionY.RIGHT)
			{
				leftReferencePoint.y = fieldWidth / 2;
				leftRectWidth = (fieldWidth - goal.getSize()) / 2;
			} else if (ballY == EBallPositionY.CENTER)
			{
				leftReferencePoint.y = fieldWidth / 2;
				leftRectWidth = (fieldWidth - goal.getSize()) / 2;
			}
		} else if (side == ESide.RIGHT)
		{
			if (ballY == EBallPositionY.LEFT)
			{
				rightReferencePoint.y = -goalSize / 2;
				rightRectWidth = (fieldWidth - goal.getSize()) / 2;
			} else if (ballY == EBallPositionY.RIGHT)
			{
				rightReferencePoint.y = goalSize / 2;
				rightRectWidth = goalSize;
			} else if (ballY == EBallPositionY.CENTER)
			{
				rightReferencePoint.y = -goalSize / 2;
				rightRectWidth = (fieldWidth - goal.getSize()) / 2;
			}
		}
		
		IVector2 point = AIConfig.INIT_VECTOR;
		
		// create rectangle and get random point from it
		if (side == ESide.LEFT)
		{
			Rectangle rect = new Rectangle(leftReferencePoint, rectLength, leftRectWidth);
			point = rect.getRandomPointInShape();
		} else if (side == ESide.RIGHT)
		{
			Rectangle rect = new Rectangle(rightReferencePoint, rectLength, rightRectWidth);
			point = rect.getRandomPointInShape();
		}
		
		return new PointCloud(new ValuePoint(point));
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
	

	public static float evaluatePoint(Vector2 point, WorldFrame wf)
	{
		
		// here, all different steps, that evaluate a cloud, should be called from the supertype,
		// and be multiplied with the value please put new evaluateMethods into the supertype
		PointCloud cloud = new PointCloud(new ValuePoint(point.x, point.y));
		
		float newValue = 100.0f;
		
		// add value for minimum distance
		newValue *= evaluateMinimumDistances(cloud, wf);
		newValue *= evaluateBallVisibility(cloud, wf);
		newValue *= evaluateGoalVisibility(cloud, wf);
		// newValue *= evaluateAngle(cloud, wf);
		newValue *= evaluatePositionValidity(cloud, wf);
		newValue *= evaluateEvolution(cloud);
		

		// TODOcheck whether point is still valid (in rectangle / not on the same rectangle as ball)
		
		return newValue;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
