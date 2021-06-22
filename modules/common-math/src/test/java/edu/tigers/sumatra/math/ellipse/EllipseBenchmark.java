/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Tests the performance of the {@link Ellipse} class vs the {@link Circle} class
 */
public class EllipseBenchmark
{
	private static final int RUNS = 50000;
	private static final int NUM_SAMPLE_DATA = 500;
	private final List<IVector2> centers = new ArrayList<>();
	private final List<Double> radiX = new ArrayList<>();
	private final List<Double> radiY = new ArrayList<>();
	private final Random rnd = new Random(0);


	public static void main(String[] args)
	{
		var b = new EllipseBenchmark();
		b.init();
		b.testCircleCreation();
		b.testEllipseCreation();
	}


	private void init()
	{
		for (int i = 0; i < NUM_SAMPLE_DATA; i++)
		{
			centers.add(Vector2.fromXY(getInt() - 3000, getInt() - 3000));
			radiX.add((double) getInt());
			radiY.add((double) getInt());
		}
	}


	private int getInt()
	{
		return rnd.nextInt(6000) + 1;
	}


	private void testEllipseCreation()
	{
		for (int i = 0; i < RUNS; i++)
		{
			Ellipse.createEllipse(centers.get(i % NUM_SAMPLE_DATA), radiX.get(i % NUM_SAMPLE_DATA),
					radiY.get(i % NUM_SAMPLE_DATA));
		}
	}


	private void testCircleCreation()
	{
		for (int i = 0; i < RUNS; i++)
		{
			IVector2 center = centers.get(i % NUM_SAMPLE_DATA);
			double radius = radiX.get(i % NUM_SAMPLE_DATA);
			Circle.createCircle(center, radius);
		}
	}
}
