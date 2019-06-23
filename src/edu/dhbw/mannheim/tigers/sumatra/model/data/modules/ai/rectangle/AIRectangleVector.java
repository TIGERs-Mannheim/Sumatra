/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator;


/**
 * This is a vectors which stores all analysing {@link AIRectangle}s.
 * 
 * @author PaulB
 * 
 */
@Entity
public class AIRectangleVector
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<AIRectangle>		rectangles;
	private FieldRasterGenerator	fieldGenerator;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Creates an vector which contains all analyzing {@link AIRectangle}s
	 * created by the {@link FieldRasterGenerator}.
	 * @param fieldGenerator
	 * 
	 */
	public AIRectangleVector(FieldRasterGenerator fieldGenerator)
	{
		final List<AIRectangle> rects = new ArrayList<AIRectangle>();
		this.fieldGenerator = fieldGenerator;
		for (int i = 0; i < fieldGenerator.getNumberOfAnalysingFields(); i++)
		{
			rects.add(fieldGenerator.getAnalysisFieldRectangle(i));
		}
		
		rectangles = Collections.unmodifiableList(rects);
	}
	
	
	/**
	 * Create a copy of AIRectangleVector
	 * @param orig
	 */
	public AIRectangleVector(AIRectangleVector orig)
	{
		final List<AIRectangle> rects = new Vector<AIRectangle>();
		for (int i = 0; i < orig.getFieldGenerator().getNumberOfAnalysingFields(); i++)
		{
			// copy
			rects.add(new AIRectangle(orig.get(i)));
		}
		fieldGenerator = orig.getFieldGenerator();
		rectangles = Collections.unmodifiableList(rects);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Need for copy the object
	 * 
	 * @return
	 */
	public FieldRasterGenerator getFieldGenerator()
	{
		return fieldGenerator;
	}
	
	
	/**
	 * Returns the {@link AIRectangle} by index.
	 * 
	 * @param index [0-size()]
	 * @throws IllegalArgumentException when index is out of bounds
	 * @return {@link AIRectangle}
	 */
	public AIRectangle get(int index)
	{
		if ((index >= 0) && (index < rectangles.size()))
		{
			return rectangles.get(index);
		}
		throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Index out of Bounds.");
	}
	
	
	/**
	 * @return size of Vector
	 */
	public int size()
	{
		return rectangles.size();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the rectangles
	 */
	public List<AIRectangle> getRectangles()
	{
		return rectangles;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param rectangles the rectangles to set
	 */
	protected void setRectangles(List<AIRectangle> rectangles)
	{
		this.rectangles = rectangles;
	}
}
