/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.performance.ellipse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Tests the performance of the {@link Ellipse} class vs the {@link Circle} class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class EllipsePerfTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	RUNS					= 50000;
	private static final int	NUM_SAMPLE_DATA	= 500;
	private List<IVector2>		centers				= new ArrayList<IVector2>();
	private List<Float>			radiX					= new ArrayList<Float>();
	private List<Float>			radiY					= new ArrayList<Float>();
	private Random					rnd					= new Random(System.nanoTime());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	@Before
	public void init()
	{
		for (int i = 0; i < NUM_SAMPLE_DATA; i++)
		{
			centers.add(new Vector2(getInt() - 3000, getInt() - 3000));
			radiX.add((float) getInt());
			radiY.add((float) getInt());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private int getInt()
	{
		return rnd.nextInt(6000) + 1;
	}
	
	
	/**
	 */
	@Test
	public void testEllipseCreation()
	{
		for (int i = 0; i < RUNS; i++)
		{
			new Ellipse(centers.get(i % NUM_SAMPLE_DATA), radiX.get(i % NUM_SAMPLE_DATA), radiY.get(i % NUM_SAMPLE_DATA));
		}
	}
	
	
	/**
	 */
	@Test
	public void testCircleCreation()
	{
		for (int i = 0; i < RUNS; i++)
		{
			IVector2 center = centers.get(i % NUM_SAMPLE_DATA);
			float radius = radiX.get(i % NUM_SAMPLE_DATA);
			new Circle(center, radius);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
