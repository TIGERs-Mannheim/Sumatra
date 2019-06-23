package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle;

/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - field raster generator
 * Date: 11.08.2010
 * Authors:
 * Oliver Steinbrecher
 * *********************************************************
 */

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * 
 * This class represents a rectangle. It is used to describe a part-rectangle of the field.
 * The reference point is used for rectangle positioning on the field. This extends
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle} with a explicit
 * identifier.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
@Entity
public class AIRectangle extends Rectanglef implements Comparable<AIRectangle>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long				serialVersionUID				= -17346148584353658L;
	
	/** */
	public static final int					MIN_AIRECTANGLE_SCORE		= 0;
	/** */
	public static final int					MAX_AIRECTANGLE_SCORE		= 100;
	/** */
	public static final int					DEFAULT_AIRECTANGLE_SCORE	= 0;
	
	
	/** stores the id of a rectangle */
	private int									rectangleID;
	
	private transient List<AIRectangle>	neighbours;
	
	private float								value								= DEFAULT_AIRECTANGLE_SCORE;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param rectangleID
	 * @param referencePoint
	 * @param rectangleLength
	 * @param rectangleWidth
	 */
	public AIRectangle(int rectangleID, IVector2 referencePoint, int rectangleLength, int rectangleWidth)
	{
		super(referencePoint, rectangleLength, rectangleWidth);
		
		this.rectangleID = rectangleID;
	}
	
	
	/**
	 * Creates a copy of the AIRectangle.
	 * WARNING: neighbours not supported yet!
	 * 
	 * @param orig
	 */
	public AIRectangle(AIRectangle orig)
	{
		super(new Vector2f(orig.topLeft()), orig.xExtend(), orig.yExtend());
		
		rectangleID = orig.getRectangleID();
		value = orig.getValue();
		// neighbours not supported
		neighbours = new LinkedList<AIRectangle>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public int getRectangleID()
	{
		return rectangleID;
	}
	
	
	/**
	 * @return
	 */
	public float getValue()
	{
		return value;
	}
	
	
	/**
	 * @param value
	 */
	public void setValue(float value)
	{
		
		this.value = value;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Returns true if rectangle is at margin.
	 * Does not differ between goal margin and length margin!
	 * 
	 * @return true when this rectangle is at margin
	 */
	public boolean isAtMargin()
	{
		final int row = ((rectangleID - 1) / (AIConfig.getFieldRaster().getNumberOfColumns() * AIConfig.getFieldRaster()
				.getAnalysingFactor())) + 1;
		
		if ((row == 1)
				|| (row == (AIConfig.getFieldRaster().getNumberOfRows() * AIConfig.getFieldRaster().getAnalysingFactor())))
		{
			return true;
		}
		
		final int rowMod = rectangleID
				% (AIConfig.getFieldRaster().getNumberOfColumns() * AIConfig.getFieldRaster().getAnalysingFactor());
		
		if ((rowMod == 0) || (rowMod == 1))
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param neighbours
	 */
	public void setNeighbours(List<AIRectangle> neighbours)
	{
		this.neighbours = neighbours;
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
	 * @return
	 */
	public List<AIRectangle> getNeighbours()
	{
		return neighbours;
	}
	
	
	/**
	 * Index starts at top left and increases clockwise. If rectangle is at margin, all outside "neighbour" rectangles
	 * are null.
	 * 
	 * <pre>
	 * 0|1|2
	 * 7|x|3
	 * 6|5|4
	 * </pre>
	 * 
	 * @param index
	 * @return
	 */
	public AIRectangle getNeighbour(int index)
	{
		return neighbours.get(index);
	}
	
	
	/**
	 * Returns the sum of neighbours values.
	 * 
	 * @return
	 */
	public float getTotalNeighboursValue()
	{
		float sum = value;
		for (int i = 0; i < neighbours.size(); i++)
		{
			if (neighbours.get(i) != null)
			{
				sum += neighbours.get(i).getValue();
			}
		}
		return sum;
	}
	
	
	/**
	 * Returns the neighbour with the highest scoring value.
	 * 
	 * @return {@link AIRectangle} with highest score.
	 */
	public AIRectangle getHighestNeighbour()
	{
		int i = 0;
		while (neighbours.get(i) == null)
		{
			i++;
		}
		
		AIRectangle highestNeighbour = neighbours.get(i);
		
		for (; i < 8; i++)
		{
			final AIRectangle rect = neighbours.get(i);
			if (rect != null)
			{
				if (highestNeighbour.compareTo(rect) == 1)
				{
					highestNeighbour = rect;
				}
			}
		}
		return highestNeighbour;
	}
	
	
	// --------------------------------------------------------------------------
	// --- standard methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		
		builder.append("Rectangle ID" + rectangleID + "(");
		builder.append(+xExtend());
		builder.append("/");
		builder.append(+yExtend());
		builder.append(")");
		builder.append("ref" + topLeft().toString());
		builder.append("value: " + value);
		return builder.toString();
	}
	
	
	@Override
	public int hashCode()
	{
		return super.hashCode() + Float.valueOf(getValue()).hashCode() + Integer.valueOf(rectangleID).hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		// super.equals checks for null and this !
		if (!super.equals(obj))
		{
			return false;
		}
		
		final AIRectangle rectangle = (AIRectangle) obj;
		if (rectangleID == rectangle.rectangleID)
		{
			if (value == rectangle.getValue())
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public int compareTo(AIRectangle o)
	{
		if (o.getValue() >= value)
		{
			return 1;
		}
		
		return -1;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param rectangleID the rectangleID to set
	 */
	protected void setRectangleID(int rectangleID)
	{
		this.rectangleID = rectangleID;
	}
}
