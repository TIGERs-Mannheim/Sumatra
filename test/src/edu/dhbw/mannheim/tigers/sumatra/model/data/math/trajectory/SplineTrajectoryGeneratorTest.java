/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * Tests the SplineTrajectoryGenerator.
 * 
 * @author AndreR
 */
public class SplineTrajectoryGeneratorTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Before
	public void init()
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	@Test
	public void testCreation()
	{
		final List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(0, 0));
		path.add(new Vector2f(1, 1));
		path.add(new Vector2f(0, 3));
		
		Vector2 initVel = new Vector2(0, 0);
		Vector2 endVel = new Vector2(-1, -1);
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0f);
		
		SplinePair3D pair = gen.create(path, initVel, endVel, 0, 0, 0, 0);
		
		exportSpline(pair, "splineTraj");
	}
	
	
	/**
	 * This test what happens if points in the path are close to each other.
	 * The spline curvature has to be adapted in this case to avoid
	 * unwanted extra circles.
	 */
	@Test
	public void testClosePoints()
	{
		// testing trajectory
		final List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(0, 0));
		path.add(new Vector2f(0.2f, 0.6f));
		path.add(new Vector2f(0.3f, 0.8f));
		path.add(new Vector2f(0.2f, 1.0f));
		path.add(new Vector2f(0f, 1.7f));
		
		Vector2 initVel = new Vector2(0, 0);
		Vector2 endVel = new Vector2(0, 0);
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0f);
		
		SplinePair3D pair = gen.create(path, initVel, endVel, 0, 0, 0, 0);
		
		exportSpline(pair, "splineTrajClose");
	}
	
	
	/**
	 * Test the append method
	 */
	@Test
	public void testAppendSpline()
	{
		IVector2 p1 = new Vector2(0, 0);
		IVector2 p2 = new Vector2(2, 0);
		IVector2 p3 = new Vector2(2, 0.5f);
		float angleP1 = 0;
		float angleP2 = 0;
		float angleP3 = -AngleMath.PI_HALF;
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0f);
		gen.setRotationTrajParams(6f, 1f);
		gen.setReducePathScore(0);
		
		List<IVector2> nodes = new LinkedList<IVector2>();
		nodes.add(p1);
		nodes.add(p2);
		
		IVector2 inBetweenVel = AVector2.ZERO_VECTOR; // p2.subtractNew(p1).scaleTo(0.3f);
		SplinePair3D pair = gen.create(nodes, AVector2.ZERO_VECTOR, inBetweenVel, angleP1, angleP2, 0, 0f);
		
		List<IVector2> nodes2 = new LinkedList<IVector2>();
		nodes2.add(p2);
		nodes2.add(p3);
		SplinePair3D pair2 = gen.create(nodes2, inBetweenVel, AVector2.ZERO_VECTOR, angleP2, angleP3, 0, 0f);
		
		exportSpline(pair, "splineTrajAppendPair1");
		exportSpline(pair2, "splineTrajAppendPair2");
		
		pair.append(pair2);
		
		exportSpline(pair, "splineTrajAppend");
	}
	
	
	private void exportSpline(final SplinePair3D pair, final String idAndName)
	{
		CSVExporter exporter = new CSVExporter("logs/" + idAndName, false);
		exporter.setHeader("t", "px", "py", "vx", "vy", "ax", "ay", "rot");
		
		ITrajectory2D spline = pair.getPositionTrajectory();
		for (float t = 0.0f; t < spline.getTotalTime(); t += 0.01f)
		{
			Vector2 p = spline.getPosition(t);
			Vector2 v = spline.getVelocity(t);
			Vector2 a = spline.getAcceleration(t);
			float r = pair.getRotationTrajectory().getPosition(t);
			
			exporter.addValues(t, p.x, p.y, v.x, v.y, a.x, a.y, r);
		}
		
		exporter.close();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
