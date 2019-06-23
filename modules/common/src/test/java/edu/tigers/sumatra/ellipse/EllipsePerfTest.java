/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ellipse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.ellipse.Ellipse;


/**
 * Tests the performance of the {@link Ellipse} class vs the {@link Circle} class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Ignore
public class EllipsePerfTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int		RUNS					= 50000;
	private static final int		NUM_SAMPLE_DATA	= 500;
	private final List<IVector2>	centers				= new ArrayList<IVector2>();
	private final List<Double>		radiX					= new ArrayList<Double>();
	private final List<Double>		radiY					= new ArrayList<Double>();
	private final Random				rnd					= new Random(System.nanoTime());
																	
																	
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
			radiX.add((double) getInt());
			radiY.add((double) getInt());
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
			double radius = radiX.get(i % NUM_SAMPLE_DATA);
			new Circle(center, radius);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
