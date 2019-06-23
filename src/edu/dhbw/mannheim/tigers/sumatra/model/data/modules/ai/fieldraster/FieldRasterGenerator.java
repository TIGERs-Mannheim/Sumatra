/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.AVisibleCon;


/**
 * This class is used to generate the positioning and analyzing field raster.
 * Please load configuration first before using functions of this.
 * The raster numbering starts top left form left to right.
 * 
 * AI Agent will start and load field raster generator.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public final class FieldRasterGenerator
{
	private static final int				RANDOM_TRIES	= 20;
	
	private final FieldRasterConfig		fieldRasterConfig;
	
	/** Analyze rectangle buffer for better access performance */
	private Map<Integer, AIRectangle>	analyzeRectangleBuffer;
	/** Positioning rectangle buffer for better access performance */
	private Map<Integer, AIRectangle>	posRectangleBuffer;
	
	private static final int				ID_SHIFT			= 1;
	private int									numberOfColumns;
	private int									numberOfRows;
	private int									analysingFactor;
	private int									numberOfPositioningFields;
	private int									numberOfAnnalysingFields;
	
	
	/**
	 * To decide with field size should by loaded
	 * 
	 */
	public enum EGeneratorTyp
	{
		/** Use the smallFieldConfig */
		PLAYFINDER,
		/** Use the bigFieldConfig */
		MAIN;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param typ
	 */
	public FieldRasterGenerator(EGeneratorTyp typ)
	{
		fieldRasterConfig = AIConfig.getFieldRaster();
		analyzeRectangleBuffer = new HashMap<Integer, AIRectangle>();
		posRectangleBuffer = new HashMap<Integer, AIRectangle>();
		switch (typ)
		{
			case MAIN:
				numberOfColumns = fieldRasterConfig.getNumberOfColumns();
				numberOfRows = fieldRasterConfig.getNumberOfRows();
				analysingFactor = fieldRasterConfig.getAnalysingFactor();
				numberOfAnnalysingFields = fieldRasterConfig.getNumberOfAnalysingFields();
				numberOfPositioningFields = fieldRasterConfig.getNumberOfPositioningFields();
				break;
			case PLAYFINDER:
				numberOfColumns = fieldRasterConfig.getPlayfinderNumberOfColumns();
				numberOfRows = fieldRasterConfig.getPlayfinderNumberOfRows();
				analysingFactor = fieldRasterConfig.getPlayfinderAnalysingFactor();
				numberOfAnnalysingFields = fieldRasterConfig.getPlayfinderNumberOfAnalysingFields();
				numberOfPositioningFields = fieldRasterConfig.getPlayfinderNumberOfPositioningFields();
				break;
		}
		
		initialiseAnalysisFieldsBuffer();
		initialisePosFieldBuffer();
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public float getColumnSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return AIConfig.getGeometry().getFieldLength() / numberOfColumns;
	}
	
	
	/**
	 * @return
	 */
	public float getRowSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return AIConfig.getGeometry().getFieldWidth() / numberOfRows;
	}
	
	
	/**
	 * @return
	 */
	public float getColumnAnalysingSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return AIConfig.getGeometry().getFieldLength() / (numberOfColumns * analysingFactor);
	}
	
	
	/**
	 * @return
	 */
	public float getRowAnalysingSize()
	{
		// --- use of float necessary because of rounding errors. ---
		return AIConfig.getGeometry().getFieldWidth() / (numberOfRows * analysingFactor);
	}
	
	
	/**
	 * This function is used to return a sub-rectangle of the field for analyzing.
	 * This method loads the rectangle from the rectangle buffer
	 * (see {@link FieldRasterGenerator#posRectangleBuffer})
	 * 
	 * @param fieldNumber the number of the field which should be returned.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	public AIRectangle getAnalysisFieldRectangle(int fieldNumber)
	{
		if (verifyAnalysisField(fieldNumber))
		{
			return analyzeRectangleBuffer.get(fieldNumber);
		}
		throw new IllegalArgumentException("Field raster index out of bounds. (fieldNumber : " + fieldNumber + ")");
	}
	
	
	/**
	 * This function is used to return a sub-rectangle of the field for positioning.
	 * This method loads the rectangle from the rectangle buffer
	 * (see {@link FieldRasterGenerator#analyzeRectangleBuffer}).
	 * 
	 * @param fieldNumber the number of the field which should be returned.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	public AIRectangle getPosFieldRectangle(int fieldNumber)
	{
		if (verifyField(fieldNumber))
		{
			return posRectangleBuffer.get(fieldNumber);
		}
		throw new IllegalArgumentException("Field raster index out of bounds. (fieldNumber : " + fieldNumber + ")");
	}
	
	
	// --------------------------------------------------------------------------
	// --- private-method(s) ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Verifies a field number and checks if its within the field.
	 */
	private boolean verifyField(int fieldNumber)
	{
		if ((fieldNumber >= 0) && (fieldNumber < numberOfPositioningFields))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Verifies a field number and checks if its within the field.
	 */
	private boolean verifyAnalysisField(int fieldNumber)
	{
		if ((fieldNumber >= 0) && (fieldNumber < numberOfAnnalysingFields))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Initializes {@link FieldRasterGenerator#analyzeRectangleBuffer}.
	 */
	private void initialiseAnalysisFieldsBuffer()
	{
		final int numberOfFields = numberOfAnnalysingFields - 1;
		if (verifyAnalysisField(numberOfFields))
		{
			for (int i = 0; i <= numberOfFields; i++)
			{
				analyzeRectangleBuffer.put(i, calcAnalysingRectangle(i));
			}
			
			for (int i = 1; i < analyzeRectangleBuffer.size(); i++)
			{
				setNeighbours(analyzeRectangleBuffer.get(i));
			}
			
			
		} else
		{
			throw new IllegalArgumentException("Field raster index out of bounds. (fieldNumber : " + numberOfFields + ")");
		}
	}
	
	
	/**
	 * Initializes {@link FieldRasterGenerator#posRectangleBuffer}.
	 */
	private void initialisePosFieldBuffer()
	{
		final int numberOfFields = numberOfPositioningFields - 1;
		if (verifyAnalysisField(numberOfFields))
		{
			for (int i = 0; i <= numberOfFields; i++)
			{
				posRectangleBuffer.put(i, calcPositioningRectangle((i)));
			}
		} else
		{
			throw new IllegalArgumentException("Field raster index out of bounds. (fieldNumber : " + numberOfFields + ")");
		}
		
	}
	
	
	/**
	 * If rectangle is at margin all outside "neighbour" rectangles are null.
	 * 
	 * <pre>
	 * 0|1|2
	 * 7|x|3
	 * 6|5|4
	 * </pre>
	 * 
	 * @param rect
	 */
	private void setNeighbours(AIRectangle rect)
	{
		final List<AIRectangle> rectList = new ArrayList<AIRectangle>();
		
		final int columns = numberOfColumns * analysingFactor;
		final int rows = numberOfRows * analysingFactor;
		
		final int[][] rectArray = new int[rows][columns];
		
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				rectArray[i][j] = (i * columns) + j;
			}
		}
		
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				if (rectArray[i][j] == rect.getRectangleID())
				{
					if (((i + 1) < rows) && ((j - 1) >= 0))
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i + 1][j - 1]));
					} else
					{
						rectList.add(null);
					}
					
					if ((j - 1) >= 0)
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i][j - 1]));
					} else
					{
						rectList.add(null);
					}
					
					if (((i - 1) >= 0) && ((j - 1) >= 0))
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i - 1][j - 1]));
					} else
					{
						rectList.add(null);
					}
					
					if ((i - 1) >= 0)
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i - 1][j]));
					} else
					{
						rectList.add(null);
					}
					
					if (((i - 1) >= 0) && ((j + 1) < columns))
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i - 1][j + 1]));
					} else
					{
						rectList.add(null);
					}
					
					if ((j + 1) < columns)
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i][j + 1]));
					} else
					{
						rectList.add(null);
					}
					
					if (((i + 1) < rows) && ((j + 1) < columns))
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i + 1][j + 1]));
					} else
					{
						rectList.add(null);
					}
					
					if ((i + 1) < rows)
					{
						rectList.add(analyzeRectangleBuffer.get(rectArray[i + 1][j]));
					} else
					{
						rectList.add(null);
					}
				}
			}
		}
		
		rect.setNeighbours(rectList);
	}
	
	
	// --------------------------------------------------------------------------
	// --- public-method(s) -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This function calculates a sub-rectangle of the field for positioning.
	 * 
	 * @param fieldNumber the number of the field which should be calculated.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	private AIRectangle calcPositioningRectangle(int fieldNumber)
	{
		if (verifyField(fieldNumber))
		{
			final int calcNumber = fieldNumber + ID_SHIFT;
			// starting position upper left of the field (-3025 / 2025).
			float x = -(AIConfig.getGeometry().getFieldLength() / 2);
			float y = (AIConfig.getGeometry().getFieldWidth() / 2);
			
			for (int i = 0; i < numberOfRows; i++)
			{
				if (calcNumber <= (numberOfColumns * (i + 1)))
				{
					x += ((calcNumber - (i * numberOfColumns)) - 1) * getColumnSize();
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
	 * This function calculates a sub-rectangle of the field for analyzing.
	 * 
	 * @param fieldNumber the number of the field which should be calculated.
	 * @return the rectangle which is requested.
	 * @throws IllegalArgumentException when index is out of bounds.
	 */
	private AIRectangle calcAnalysingRectangle(int fieldNumber)
	{
		if (verifyAnalysisField(fieldNumber))
		{
			final int calcNumber = fieldNumber + ID_SHIFT;
			final int analysingRows = numberOfRows * analysingFactor;
			final int analysingColumns = numberOfColumns * analysingFactor;
			
			final int analysingRowSize = ((int) getRowSize()) / analysingFactor;
			final int analysingRowColumnSize = ((int) getColumnSize()) / analysingFactor;
			
			// --- starting position upper left of field ---
			float x = -(AIConfig.getGeometry().getFieldLength() / 2);
			float y = (AIConfig.getGeometry().getFieldWidth() / 2);
			
			for (int i = 0; i < analysingRows; i++)
			{
				if (calcNumber <= (analysingColumns * (i + 1)))
				{
					x += ((calcNumber - (i * analysingColumns)) - 1) * analysingRowColumnSize;
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
	 * Estimates the position rectangle id which contains the specified position.
	 * 
	 * @param position
	 * @return id
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public int getPositionIDFromPosition(IVector2 position)
	{
		if (AIConfig.getGeometry().getField().isPointInShape(position))
		{
			// estimate x-axis range
			float xMin = -(AIConfig.getGeometry().getFieldLength() / 2);
			float xMax = -(AIConfig.getGeometry().getFieldLength() / 2) + getColumnSize();
			int columnNumber = -1;
			
			for (int i = 1; i <= numberOfColumns; i++)
			{
				if ((position.x() >= xMin) && (position.x() <= xMax))
				{
					columnNumber = i;
					break;
				}
				
				xMin += getColumnSize();
				xMax += getColumnSize();
			}
			
			// estimate y-axis range
			float yMin = (AIConfig.getGeometry().getFieldWidth() / 2) - getRowSize();
			float yMax = (AIConfig.getGeometry().getFieldWidth() / 2);
			int rowNumber = -1;
			
			for (int i = 1; i <= numberOfRows; i++)
			{
				if ((position.y() >= yMin) && (position.y() <= yMax))
				{
					rowNumber = i;
					break;
				}
				
				yMin -= getRowSize();
				yMax -= getRowSize();
			}
			
			final int fieldnumber = ((rowNumber - 1) * numberOfColumns) + columnNumber;
			
			return fieldnumber - ID_SHIFT;
			
		}
		throw new IllegalArgumentException("Position is out of field bounds.");
	}
	
	
	/**
	 * Estimates the positioning rectangle which contains the specified position.
	 * 
	 * @param position
	 * @return rectangle
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public AIRectangle getPositionRectFromPosition(IVector2 position)
	{
		return calcPositioningRectangle(getPositionIDFromPosition(position));
	}
	
	
	/**
	 * Estimates the analysing rectangle id which contains the specified position.
	 * 
	 * @param position
	 * @return id
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public int getAnalysingIDFromPosition(IVector2 position)
	{
		if (AIConfig.getGeometry().getField().isPointInShape(position))
		{
			// estimate x-axis range
			float xMin = -(AIConfig.getGeometry().getFieldLength() / 2);
			float xMax = -(AIConfig.getGeometry().getFieldLength() / 2) + getColumnAnalysingSize();
			int columnNumber = -1;
			
			for (int i = 1; i <= (numberOfColumns * analysingFactor); i++)
			{
				if ((position.x() >= xMin) && (position.x() <= xMax))
				{
					columnNumber = i;
					break;
				}
				
				xMin += getColumnAnalysingSize();
				xMax += getColumnAnalysingSize();
			}
			
			// estimate y-axis range
			float yMin = (AIConfig.getGeometry().getFieldWidth() / 2) - getRowAnalysingSize();
			float yMax = (AIConfig.getGeometry().getFieldWidth() / 2);
			int rowNumber = -1;
			
			for (int i = 1; i <= (numberOfRows * analysingFactor); i++)
			{
				if ((position.y() >= yMin) && (position.y() <= yMax))
				{
					rowNumber = i;
					break;
				}
				
				yMin -= getRowAnalysingSize();
				yMax -= getRowAnalysingSize();
			}
			
			final int fieldnumber = ((rowNumber - 1) * numberOfColumns * analysingFactor) + columnNumber;
			
			return fieldnumber - ID_SHIFT;
		}
		throw new IllegalArgumentException("Position is out of field bounds.");
	}
	
	
	/**
	 * Estimates the analyzing rectangle which contains the specified position.
	 * 
	 * @param position
	 * @return rectangle
	 * @throws IllegalArgumentException when position is out of field bounds.
	 */
	public AIRectangle getAnalysingRectFromPosition(IVector2 position)
	{
		return calcPositioningRectangle(getAnalysingIDFromPosition(position));
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
		final AIRectangle actualRect = calcPositioningRectangle(fieldNumber);
		
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
	 * @param condition {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(int fieldNumber, WorldFrame worldFrame, BotID botId, AVisibleCon condition)
	{
		return getRandomConditionPoint(calcPositioningRectangle(fieldNumber), worldFrame, botId, condition);
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
	 * @param condition {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(AIRectangle rectangle, WorldFrame worldFrame, BotID botId,
			AVisibleCon condition)
	{
		Vector2 point = rectangle.getRandomPointInShape();
		final Vector2 botPos = new Vector2(worldFrame.getTiger(botId).getPos());
		
		float distanceMin = GeoMath.distancePP(botPos, point);
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			final Vector2 newPoint = rectangle.getRandomPointInShape();
			
			if (condition.checkCondition(worldFrame, botId) == EConditionState.FULFILLED)
			{
				final float distance = GeoMath.distancePP(botPos, newPoint);
				
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
	 * @param worldFrame
	 * @param botId
	 * @param condition1 {@link AVisibleCon}
	 * @param condition2 {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 */
	public Vector2 getRandomConditionPoint(AIRectangle rectangle, WorldFrame worldFrame, BotID botId,
			AVisibleCon condition1, AVisibleCon condition2)
	{
		Vector2 point = rectangle.getRandomPointInShape();
		final Vector2 botPos = new Vector2(worldFrame.getTiger(botId).getPos());
		
		float distanceMin = GeoMath.distancePP(botPos, point);
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			final Vector2 newPoint = rectangle.getRandomPointInShape();
			
			if ((condition1.checkCondition(worldFrame, botId) == EConditionState.FULFILLED)
					&& (condition2.checkCondition(worldFrame, botId) == EConditionState.FULFILLED))
			{
				final float distance = GeoMath.distancePP(botPos, newPoint);
				
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
	 * @param worldFrame
	 * @param botId
	 * @param conditions list of {@link AVisibleCon}
	 * 
	 * @return the nearest of 20 random points where the target is visible
	 * 
	 */
	public Vector2 getRandomPointWithConditions(AIRectangle rectangle, WorldFrame worldFrame, BotID botId,
			List<AVisibleCon> conditions)
	{
		
		final Vector2 safetyPoint = rectangle.getRandomPointInShape();
		Vector2 bestPoint = new Vector2(GeoMath.INIT_VECTOR);
		
		final Vector2 botPos = new Vector2(worldFrame.getTiger(botId).getPos());
		
		float minDistance = -1f;
		int numberOfValidCons;
		
		for (int i = 0; i < RANDOM_TRIES; i++)
		{
			// getting new Points
			final Vector2 newPoint = rectangle.getRandomPointInShape();
			numberOfValidCons = 0;
			
			// testing all Cons
			for (final AVisibleCon con : conditions)
			{
				if (con.checkCondition(worldFrame, botId) == EConditionState.FULFILLED)
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
				final float distance = GeoMath.distancePP(botPos, newPoint);
				if (distance < minDistance)
				{
					// remember best point
					minDistance = distance;
					bestPoint = newPoint;
				}
			}
		}
		
		// return best point final or safetyPoint
		if (minDistance == -1)
		{
			bestPoint = safetyPoint;
		}
		
		return bestPoint;
	}
	
	
	/**
	 * @param analyzeRectangleBuffer the analyzeRectangleBuffer to set
	 */
	public void setAnalyzeRectangleBuffer(Map<Integer, AIRectangle> analyzeRectangleBuffer)
	{
		this.analyzeRectangleBuffer = analyzeRectangleBuffer;
	}
	
	
	/**
	 * @param posRectangleBuffer the posRectangleBuffer to set
	 */
	public void setPosRectangleBuffer(Map<Integer, AIRectangle> posRectangleBuffer)
	{
		this.posRectangleBuffer = posRectangleBuffer;
	}
	
	
	/**
	 * TODO Philipp, add comment!
	 * 
	 * @return
	 */
	public int getNumberOfRows()
	{
		return numberOfRows;
	}
	
	
	/**
	 * TODO Philipp, add comment!
	 * 
	 * @return
	 */
	public int getAnalysingFactor()
	{
		return analysingFactor;
	}
	
	
	/**
	 * TODO Philipp, add comment!
	 * 
	 * @return
	 */
	public int getNumberOfColumns()
	{
		return numberOfColumns;
	}
	
	
	/**
	 * @return the numberOfAnnalysingFields
	 */
	public int getNumberOfAnalysingFields()
	{
		return numberOfAnnalysingFields;
	}
	
}
