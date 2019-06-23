/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.04.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter.CSVExporter;


/**
 * test for spline implementation
 * 
 * @author DanielW
 * 
 */
public class SplineTest
{
	

	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline#XYSpline(edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path)}
	 * .
	 */
	@Test
	public void testXYSpline()
	{
		List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(20, 20));
		path.add(new Vector2f(-50, 20));
		path.add(new Vector2f(-20, 40));
		path.add(new Vector2f(50, 30));
		path.add(new Vector2f(40, -40));
		
		XYSpline spline = new XYSpline(path, new Vector2f(0, 0));
		assertNotNull(spline);
	}
	

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline#evaluateFunction(float)} .
	 */
	@Test
	public void testEvaluateFunction()
	{
		List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(20, 20));
		path.add(new Vector2f(-50, 20));
		path.add(new Vector2f(-20, 40));
		path.add(new Vector2f(-50, 30));
		path.add(new Vector2f(40, -40));
		
		XYSpline spline = new XYSpline(path, new Vector2f(0, 0));
		
		CSVExporter.createInstance("splinetest", "spline", true);
		CSVExporter exporter = CSVExporter.getInstance("splinetest");
		exporter.setHeader("xvalue", "yvalue", "xdir", "ydir", "curvature");
		

		for (float i = 0.0f; i <= spline.getMaxTValue(); i += 0.1)
		{
			IVector2 point = spline.evaluateFunction(i);
			IVector2 dir = spline.getTangentialVector(i);
			exporter.addValues(point.x(), point.y(), dir.x(), dir.y(), spline.getCurvature(i));
		}
		
		exporter.close();
	}
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
