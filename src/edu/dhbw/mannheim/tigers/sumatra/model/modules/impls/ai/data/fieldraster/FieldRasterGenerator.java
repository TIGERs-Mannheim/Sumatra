/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRaster;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AVisibleCon;


/**
 * This class is used to generate the positioning and analyzing field raster.
 * Please load configuration first before using functions of this.
 * The raster numbering starts top left form left to right.
 * 
 * AI Agent will start and load field raster generator.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class FieldRasterGenerator
{
	protected final Log						log				= LogFactory.getLog(this.getClass().getName());
	
	private final int							RANDOM_TRIES	= 20;
	
	private final Geometry					geometry;
	private final FieldRaster				fieldRasterConfig;
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	
	private static FieldRasterGenerator	instance;
	
	
	private FieldRasterGenerator()
	{
		geometry = AIConfig.getGeometry();
		fieldRasterConfig = AIConfig.getFieldRaster();
	}
	

	public static synchronized FieldRasterGenerator getInstance()
	{
		if (null == instance)
		{
			instance = new FieldRasterGenerator();
		}
		return instance;
	}
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float getColumnSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return geometry.getFieldLength() / fieldRasterConfig.getNumberOfColumns();
	}
	

	public float getRowSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return geometry.getFieldWidth() / fieldRasterConfig.getNumberOfRows();
	}
	

	public float getColumnAnalysingSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return geometry.getFieldLength() / fieldRasterConfig.getNumberOfColumns()
				* fieldRasterConfig.getAnalysingFactor();
	}
	

	public float getRowAnalysingSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return geometry.getFieldWidth() / (fieldRasterConfig.getNumberOfRows() * fieldRasterConfig.getAnalysingFactor());
	}
	

	// --------------------------------------------------------------------------
	// --- private-method(s) ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Verifies a field number and checks if its within the field.
	 */
	private boolean verifyField(int fieldNumber)
	{
		if (fieldNumber > 0
				&& fieldNumber <= fieldRasterConfig.getNumberOfColumns() * fieldRasterConfig.getNumberOfRows())
		{
			return true;
		} else
		{
			return false;
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This function is used to return a sub-rectangle of the field for positioning.
	 * 
	 * @param fieldNumber the number of the field which should be returned.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	public AIRectangle getPositioningRectangle(int fieldNumber)
	{
		if (verifyField(fieldNumber))
		{
			// starting position upper left of the field (-3025 / 2025).
			float x = -(geometry.getFieldLength() / 2);
			float y = (geometry.getFieldWidth() / 2);
			
			for (int i = 0; i < fieldRasterConfig.getNumberOfRows(); i++)
			{
				if (fieldNumber <= fieldRasterConfig.getNumberOfColumns() * (i + 1))
				{
					x += ((fieldNumber - i * fieldRasterConfig.getNumberOfColumns()) - 1) * getColumnSize();
					y -= i * getRowSize();
					
					return new AIRectangle(fieldNumber, new Vector2(x, y), (int) getColumnSize(), (int) getRowSize());
				}
			}
		} else
		{
			throw new IllegalArgumentException("Field raster index out of bounds. (fieldNumber : " + fieldNumber + ")");
		}
		return null;
	}
	

	/**
	 * This function is used to return a sub-rectangle of the field for analyzing.
	 * 
	 * @param fieldNumber the number of the field which should be returned.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	public AIRectangle getAnalysingRectangle(int fieldNumber)
	{
		if (verifyField(fieldNumber))
		{
			int analysingRows = fieldRasterConfig.getNumberOfRows() * fieldRasterConfig.getAnalysingFactor();
			int analysingColumns = fieldRasterConfig.getNumberOfColumns() * fieldRasterConfig.getAnalysingFactor();
			
			int analysingRowSize = ((int) getRowSize()) / fieldRasterConfig.getAnalysingFactor();
			int analysingRowColumnSize = ((int) getColumnSize()) / fieldRasterConfig.getAnalysingFactor();
			
			// --- starting position upper left of field ---
			float x = -(geometry.getFieldLength() / 2);
			float y = (geometry.getFieldWidth() / 2);
			
			for (int i = 0; i < analysingRows; i++)
			{
				if (fieldNumber <= analysingColumns * (i + 1))
				{
					x += ((fieldNumber - i * analysingColumns) - 1) * analysingRowColumnSize;
					y -= i * analysingRowSize;
					
					return new AIRectangle(fieldNumber, new Vector2(x, y), analysingRowColumnSize, analysingRowSize);
				}
			}
		} else
		{
			throw new IllegalArgumentException("Field raster index out of bounds.");
		}
		return null;
	}
	

	/**
	 * 
	 * Estimates which position sub-rectangle of the field contains
	 * the specified position.
	 * 
	 * @param position
	 * @return rectangle
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public AIRectangle getPositionRectFromPosition(Vector2 position)
	{
		if (geometry.getField().isPointInShape(position))
		{
			// estimate x-axis range
			float xMin = -(geometry.getFieldLength() / 2);
			float xMax = -(geometry.getFieldLength() / 2) + getColumnSize();
			int columnNumber = -1;
			
			for (int i = 1; i <= fieldRasterConfig.getNumberOfColumns(); i++)
			{
				if (position.x() >= xMin && position.x() <= xMax)
				{
					columnNumber = i;
					break;
				}
				
				xMin += getColumnSize();
				xMax += getColumnSize();
			}
			
			// estimate y-axis range
			float yMin = (geometry.getFieldWidth() / 2) - getRowSize();
			float yMax = (geometry.getFieldWidth() / 2);
			int rowNumber = -1;
			
			for (int i = 1; i <= fieldRasterConfig.getNumberOfRows(); i++)
			{
				if (position.y() >= yMin && position.y() <= yMax)
				{
					rowNumber = i;
					break;
				}
				
				yMin -= getRowSize();
				yMax -= getRowSize();
			}
			
			int fieldnumber = ((rowNumber - 1) * fieldRasterConfig.getNumberOfColumns()) + columnNumber;
			
			return getPositioningRectangle(fieldnumber);
			
		} else
		{
			throw new IllegalArgumentException("Position is out of field bounds.");
		}
		
	}
	

	/**
	 * 
	 * Estimates which analysing sub-rectangle of the field contains
	 * the specified position.
	 * 
	 * @param position
	 * @return rectangle
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public AIRectangle getAnalysingRectFromPosition(Vector2 position)
	{
		if (geometry.getField().isPointInShape(position))
		{
			// estimate x-axis range
			float xMin = -(geometry.getFieldLength() / 2);
			float xMax = -(geometry.getFieldLength() / 2) + getColumnAnalysingSize();
			int columnNumber = -1;
			
			for (int i = 1; i <= fieldRasterConfig.getNumberOfColumns() * fieldRasterConfig.getAnalysingFactor(); i++)
			{
				if (position.x() >= xMin && position.x() <= xMax)
				{
					columnNumber = i;
					break;
				}
				
				xMin += getColumnAnalysingSize();
				xMax += getColumnAnalysingSize();
			}
			
			// estimate y-axis range
			float yMin = (geometry.getFieldWidth() / 2) - getRowAnalysingSize();
			float yMax = (geometry.getFieldWidth() / 2);
			int rowNumber = -1;
			
			for (int i = 1; i <= fieldRasterConfig.getNumberOfRows() * fieldRasterConfig.getAnalysingFactor(); i++)
			{
				if (position.y() >= yMin && position.y() <= yMax)
				{
					rowNumber = i;
					break;
				}
				
				yMin -= getRowAnalysingSize();
				yMax -= getRowAnalysingSize();
			}
			
			int fieldnumber = ((rowNumber - 1) * fieldRasterConfig.getNumberOfColumns() * fieldRasterConfig
					.getAnalysingFactor()) + columnNumber;
			
			return getPositioningRectangle(fieldnumber);
		} else
		{
			throw new IllegalArgumentException("Position is out of field bounds.");
		}
	}
	

	/**
	 * 
	 * This functions returns a random point within a sub-rectangle of the field.
	 * 
	 * @param fieldNumber the number of the sub-rectangle of the field.
	 * @return point within sub-rectangle of the field..
	 */
	public Vector2 getRandomPointInPosRec(int fieldNumber)
	{
		AIRectangle actualRect = getPositioningRectangle(fieldNumber);
		
		return actualRect.getRandomPointInShape();
	}
	

	// --------------------------------------------------------------------------
	// --- Condition Handling ---------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * 
	 * This algorithm creates RANDOM_TRIES (20) random points within the specified rectangle and always checks if the
	 * target is visible from the actual point. The point with the min. distance from the actual
	 * bot position will be returned.
	 * 
	 * @param fieldNumber
	 * @param worldFrame
	 * @param botId
	 * @param {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(int fieldNumber, WorldFrame worldFrame, int botId, AVisibleCon condition)
	{
		return getRandomConditionPoint(getPositioningRectangle(fieldNumber), worldFrame, botId, condition);
	}
	

	/**
	 * 
	 * This algorithm creates RANDOM_TRIES (20) random points within the specified rectangle and always checks if the
	 * target is visible from the actual point. The point with the min. distance from the actual
	 * bot position will be returned.
	 * 
	 * @param rectangle
	 * @param worldFrame
	 * @param botId
	 * @param {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(AIRectangle rectangle, WorldFrame worldFrame, int botId, AVisibleCon condition)
	{
		Vector2 point = rectangle.getRandomPointInShape();
		Vector2 botPos = new Vector2(worldFrame.tigerBots.get(botId).pos);
		
		float distanceMin = AIMath.distancePP(botPos, point);
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			Vector2 newPoint = rectangle.getRandomPointInShape();
			
			if (condition.checkCondition(worldFrame))
			{
				float distance = AIMath.distancePP(botPos, newPoint);
				
				if (distance < distanceMin)
				{
					point = newPoint;
					distanceMin = distance;
				}
			}
		}
		
		return point;
	}
	

	/**
	 * 
	 * This algorithm creates RANDOM_TRIES (20) random points within the specified rectangle and always checks if the
	 * target is visible from the actual point. The point with the min. distance from the actual
	 * bot position will be returned. This function handles two {@link AVisibleCon}.
	 * 
	 * @param rectangle
	 * @param worldframe
	 * @param botId
	 * @param {@link AVisibleCon}
	 * @param {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(AIRectangle rectangle, WorldFrame worldFrame, int botId,
			AVisibleCon condition1, AVisibleCon condition2)
	{
		Vector2 point = rectangle.getRandomPointInShape();
		Vector2 botPos = new Vector2(worldFrame.tigerBots.get(botId).pos);
		
		float distanceMin = AIMath.distancePP(botPos, point);
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			Vector2 newPoint = rectangle.getRandomPointInShape();
			
			if (condition1.checkCondition(worldFrame) && condition2.checkCondition(worldFrame))
			{
				float distance = AIMath.distancePP(botPos, newPoint);
				
				if (distance < distanceMin)
				{
					point = newPoint;
					distanceMin = distance;
				}
			}
		}
		

		return point;
	}
	

	/**
	 * 
	 * This algorithm creates RANDOM_TRIES (20) random points within the specified rectangle and always checks if the
	 * target is visible from the actual point. The point with the min. distance from the actual
	 * bot position will be returned. This function handles a list of {@link AVisibleCon}.
	 * 
	 * @param rectangle
	 * @param worldframe
	 * @param botId
	 * @param list of {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 * 
	 */
	public Vector2 getRandomPointWithConditions(AIRectangle rectangle, WorldFrame worldFrame, int botId,
			ArrayList<AVisibleCon> conditions)
	{
		
		Vector2 safetyPoint = rectangle.getRandomPointInShape();
		Vector2 bestPoint = new Vector2(AIConfig.INIT_VECTOR);
		
		Vector2 botPos = new Vector2(worldFrame.tigerBots.get(botId).pos);
		
		int minDistance = -1;
		int numberOfValidCons;
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			// getting new Points
			Vector2 newPoint = rectangle.getRandomPointInShape();
			numberOfValidCons = 0;
			
			// testing all Cons
			for (AVisibleCon con : conditions)
			{
				if (con.checkCondition(worldFrame))
				{
					numberOfValidCons++;
				} else
				{
					break;
				}
			}
			
			// all cons valid?
			if (conditions.size() == numberOfValidCons)
			{
				// distance to current Position smaller?
				if (AIMath.distancePP(botPos, newPoint) < minDistance)
				{
					bestPoint = newPoint; // remember best point
				}
			}
		}
		
		// return best point or safetyPoint
		if (minDistance == -1)
		{
			bestPoint = safetyPoint;
		}
		
		return bestPoint;
	}
	
}
