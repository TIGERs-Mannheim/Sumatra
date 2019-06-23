/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;


/**
 * Tests the SplineTrajectoryGenerator.
 * 
 * @author AndreR
 */
public class SplineTrajectoryGeneratorTest
{
	/**
	 */
	@Test
	@Ignore
	public void testCreation()
	{
		final List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(0, 0));
		path.add(new Vector2f(1, 1));
		path.add(new Vector2f(0, 3));
		
		Vector2 initVel = new Vector2(0, 0);
		Vector2 endVel = new Vector2(-1, -1);
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0);
		
		ITrajectory<IVector3> pair = gen.create(path, initVel, endVel, 0, 0, 0, 0);
		
		exportSpline(pair, "splineTraj");
	}
	
	
	/**
	 * This test what happens if points in the path are close to each other.
	 * The spline curvature has to be adapted in this case to avoid
	 * unwanted extra circles.
	 */
	@Test
	@Ignore
	public void testClosePoints()
	{
		// testing trajectory
		final List<IVector2> path = new ArrayList<IVector2>();
		path.add(new Vector2f(0, 0));
		path.add(new Vector2f(0.2f, 0.6));
		path.add(new Vector2f(0.3f, 0.8));
		path.add(new Vector2f(0.2f, 1.0));
		path.add(new Vector2f(0f, 1.7));
		
		Vector2 initVel = new Vector2(0, 0);
		Vector2 endVel = new Vector2(0, 0);
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0);
		
		ITrajectory<IVector3> pair = gen.create(path, initVel, endVel, 0, 0, 0, 0);
		
		exportSpline(pair, "splineTrajClose");
	}
	
	
	/**
	 * Test the append method
	 */
	@Test
	@Ignore
	public void testAppendSpline()
	{
		IVector2 p1 = new Vector2(0, 0);
		IVector2 p2 = new Vector2(2, 0);
		IVector2 p3 = new Vector2(2, 0.5);
		double angleP1 = 0;
		double angleP2 = 0;
		double angleP3 = -AngleMath.PI_HALF;
		
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(2.0f, 3.0);
		gen.setRotationTrajParams(6f, 1);
		gen.setReducePathScore(0);
		
		List<IVector2> nodes = new LinkedList<IVector2>();
		nodes.add(p1);
		nodes.add(p2);
		
		IVector2 inBetweenVel = AVector2.ZERO_VECTOR; // p2.subtractNew(p1).scaleTo(0.3f);
		ITrajectory<IVector3> pair = gen.create(nodes, AVector2.ZERO_VECTOR, inBetweenVel, angleP1, angleP2, 0, 0);
		
		List<IVector2> nodes2 = new LinkedList<IVector2>();
		nodes2.add(p2);
		nodes2.add(p3);
		ITrajectory<IVector3> pair2 = gen.create(nodes2, inBetweenVel, AVector2.ZERO_VECTOR, angleP2, angleP3, 0, 0);
		
		exportSpline(pair, "splineTrajAppendPair1");
		exportSpline(pair2, "splineTrajAppendPair2");
	}
	
	
	private void exportSpline(final ITrajectory<IVector3> pair, final String idAndName)
	{
		CSVExporter exporter = new CSVExporter("logs/" + idAndName, false);
		exporter.setHeader("t", "px", "py", "vx", "vy", "ax", "ay", "rot");
		
		for (double t = 0.0; t < pair.getTotalTime(); t += 0.01)
		{
			IVector2 p = pair.getPositionMM(t).getXYVector();
			IVector2 v = pair.getVelocity(t).getXYVector();
			IVector2 a = pair.getAcceleration(t).getXYVector();
			double r = pair.getPositionMM(t).z();
			
			exporter.addValues(t, p.x(), p.y(), v.x(), v.y(), a.x(), a.y(), r);
		}
		
		exporter.close();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
