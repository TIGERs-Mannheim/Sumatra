/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Test bang bang trajectories for orientation
 * 
 * @author AndreR
 */
public class BangBangTrajectoryOrientTest
{
	private static final int		NUMBER_OF_TESTS	= 10000;
	private static final double	POS_LIMIT			= 10.0;
	private static final double	ROT_LIMIT			= AngleMath.PI_TWO;
	private static final double	POS_TOLERANCE		= 0.02;
	private static final double	VEL_TOLERANCE		= 1e-3f;
	private final Random				rng					= new Random();
																	
																	
	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}
	
	
	private IVector2 getRandomVector(final double minmax)
	{
		return new Vector2(getRandomDouble(minmax), getRandomDouble(minmax));
	}
	
	
	private void checkTimeOrder(final BangBangTrajectoryOrient traj)
	{
		Assert.assertTrue("tEnd=" + traj.getPart(0).tEnd, traj.getPart(0).tEnd >= 0.0);
		
		for (int i = 0; i < traj.getNumParts(); i++)
		{
			Assert.assertTrue("tEnd=" + traj.getPart(i).tEnd, Double.isFinite(traj.getPart(i).tEnd));
		}
		for (int i = 1; i < traj.getNumParts(); i++)
		{
			Assert.assertTrue("tEnd=" + traj.getPart(i).tEnd + ", tEnd-1=" + traj.getPart(i - 1).tEnd,
					traj.getPart(i).tEnd >= (traj.getPart(i - 1).tEnd - 0.001));
		}
	}
	
	
	private double[] getDoubles(final String str)
	{
		String[] strArr = str.split(" ");
		double[] doubles = new double[strArr.length];
		for (int i = 0; i < strArr.length; i++)
		{
			doubles[i] = Double.valueOf(strArr[i]);
		}
		return doubles;
	}
	
	
	private void testFromData(final String inputW, final String inputX, final String inputY)
	{
		double[] inW = getDoubles(inputW);
		double[] inX = getDoubles(inputX);
		double[] inY = getDoubles(inputY);
		
		BangBangTrajectory1D x = new BangBangTrajectory1D(inX[0], inX[1], inX[2], inX[3], inX[4], inX[5]);
		BangBangTrajectory1D y = new BangBangTrajectory1D(inY[0], inY[1], inY[2], inY[3], inY[4], inY[5]);
		BangBangTrajectory2D traj = new BangBangTrajectory2D(x, y);
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(inW[0], inW[1], inW[2], inW[3], inW[4],
				inW[5], traj, inW[6]);
				
		checkTimeOrder(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(inW[1], orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	private void testFromDataCircleAware(final String inputW, final String inputX, final String inputY)
	{
		double[] inW = getDoubles(inputW);
		double[] inX = getDoubles(inputX);
		double[] inY = getDoubles(inputY);
		
		BangBangTrajectory1D x = new BangBangTrajectory1D(inX[0], inX[1], inX[2], inX[3], inX[4], inX[5]);
		BangBangTrajectory1D y = new BangBangTrajectory1D(inY[0], inY[1], inY[2], inY[3], inY[4], inY[5]);
		BangBangTrajectory2D traj = new BangBangTrajectory2D(x, y);
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.circleAware(inW[0], inW[1], inW[2], inW[3], inW[4],
				inW[5], traj, inW[6]);
				
		checkTimeOrder(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(inW[1], orient.getPositionMM(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple()
	{
		IVector2 initPos = new Vector2(0, 0);
		IVector2 finalPos = new Vector2(1, 1);
		IVector2 initVel = new Vector2(0, 0);
		
		double initOrient = 0;
		double finalOrient = 20;
		double orientVel = 0;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 6.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 60.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple2()
	{
		IVector2 initPos = new Vector2(5, 0.87);
		IVector2 finalPos = new Vector2(-3.1, -0.2);
		IVector2 initVel = new Vector2(-0.03, -0.9);
		
		double initOrient = 0;
		double finalOrient = -30;
		double orientVel = -13;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple3()
	{
		IVector2 initPos = new Vector2(0.6, 2.9);
		IVector2 finalPos = new Vector2(0.7, -4.4);
		IVector2 initVel = new Vector2(0.5, 0.1);
		
		double initOrient = -13;
		double finalOrient = 26;
		double orientVel = 17;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple4()
	{
		IVector2 initPos = new Vector2(3.4, -3.2);
		IVector2 finalPos = new Vector2(4.4, -3.4);
		IVector2 initVel = new Vector2(-0.26802313, 0.81642056);
		
		double initOrient = 0;
		double finalOrient = 11.4;
		double orientVel = 9.503346;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple5()
	{
		IVector2 initPos = new Vector2(4.997572, -2.672954);
		IVector2 finalPos = new Vector2(-0.10543108, 1.9471979);
		IVector2 initVel = new Vector2(-0.63004065, -0.42672586);
		
		double initOrient = -27.106173f;
		double finalOrient = -23.349785f;
		double orientVel = 7.7604184;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple6()
	{
		IVector2 initPos = new Vector2(1.9, 4.6);
		IVector2 finalPos = new Vector2(-1, -4.8);
		IVector2 initVel = new Vector2(-0.7, 0.0);
		
		double initOrient = 0;
		double finalOrient = 1.2;
		double orientVel = 0;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple7()
	{
		IVector2 initPos = new Vector2(-4.5, -3);
		IVector2 finalPos = new Vector2(1.45, -3.8);
		IVector2 initVel = new Vector2(-0.30331457, -0.9621781);
		
		double initOrient = 0;
		double finalOrient = -26;
		double orientVel = -14.867189f;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple8()
	{
		IVector2 initPos = new Vector2(4.0, 0.29);
		IVector2 finalPos = new Vector2(-2.27, 1.3);
		IVector2 initVel = new Vector2(0.2229002, 0.119098425);
		
		double initOrient = 0;
		double finalOrient = 30;
		double orientVel = -2.0723794f;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple9()
	{
		IVector2 initPos = new Vector2(-0.4, 3.7);
		IVector2 finalPos = new Vector2(-4.9, 0.9);
		IVector2 initVel = new Vector2(0.82775927, -0.83017707);
		
		double initOrient = 0;
		double finalOrient = -12;
		double orientVel = -16.863201f;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple10()
	{
		IVector2 initPos = new Vector2(-2.7, 4.8);
		IVector2 finalPos = new Vector2(-2.5, 2.9);
		IVector2 initVel = new Vector2(-0.5953531, 0.04419589);
		
		double initOrient = 0;
		double finalOrient = -49;
		double orientVel = 4.714671;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple11()
	{
		IVector2 initPos = new Vector2(1.9, -3.7);
		IVector2 finalPos = new Vector2(3.5, -1.7);
		IVector2 initVel = new Vector2(0.45050716, -0.38066304);
		
		double initOrient = 0;
		double finalOrient = -3.6830845f;
		double orientVel = 2.3651054;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple12()
	{
		IVector2 initPos = new Vector2(-2.498, -1.2);
		IVector2 finalPos = new Vector2(-1.0, -1.2);
		IVector2 initVel = new Vector2(-0.004, 0.0);
		
		double initOrient = 0.0;
		double finalOrient = -0.015f;
		double orientVel = -0.07f;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 1.5, 1.5, 2.425987);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.direct(initOrient, finalOrient, orientVel, 30.0,
				60.0f, 30.0, traj, 2.0);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionDirect(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple13()
	{
		IVector2 initPos = new Vector2(-3.5, 0);
		IVector2 finalPos = new Vector2(-2.526, -1.323);
		IVector2 initVel = new Vector2(0.0, 0.0);
		
		double initOrient = 0.0;
		double finalOrient = 0.0;
		double orientVel = 0.0;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 2.5, 3.0, 2.1);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		// BangBangTrajectoryOrient orient = new BangBangTrajectoryOrient();
		// orient.createDirect(initOrient, finalOrient, orientVel, 10.0, 10.0, 10, traj, 2.1);
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.circleAware(initOrient, finalOrient, orientVel, 10.0,
				10.0f, 10.0, traj, 2.1);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		Assert.assertEquals(0.0f, orient.getVelocity(50), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionMM(orient.getTotalTime()), POS_TOLERANCE);
		Assert.assertEquals(finalOrient, orient.getPositionMM(50), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple14()
	{
		IVector2 initPos = new Vector2(0.4, 0.0);
		IVector2 finalPos = new Vector2(3.0, 0.0);
		IVector2 initVel = new Vector2(2.5, 0.0);
		
		double initOrient = 0.0;
		double finalOrient = 0.1;
		double orientVel = 0.1;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 2.0, 4.0, 2.47);
		
		// System.out.println(traj.getX());
		// System.out.println(traj.getY());
		
		BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.circleAware(initOrient, finalOrient, orientVel, 20.0,
				40.0f, 32.0, traj, 2.471369267);
				
		// System.out.println(orient);
		
		Assert.assertTrue(orient.getTotalTime() >= 0.0);
		
		// check final velocity == 0
		Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
		
		// check final position reached
		Assert.assertEquals(finalOrient, orient.getPositionMM(orient.getTotalTime()), POS_TOLERANCE);
	}
	
	
	/** */
	@Test
	public void testSimple15()
	{
		String inputW = "-0.61449492 -0.65300000 -0.05183829 30.00000000 30.00000000 25.00000000 2.00000000";
		String inputX = "1.60445690 1.44400012 -0.74664062 1.73714149 1.73714149 1.73714149";
		String inputY = "2.24218607 2.14800000 -0.43208939 0.99113041 0.99113041 0.99113041";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple16()
	{
		String inputW = "-1.40129042 -1.43700004 -0.06790126 30.00000000 30.00000000 25.00000000 2.00000000";
		String inputX = "4.01452494 4.06900024 0.46371013 1.97361875 1.97361875 1.97361875";
		String inputY = "-0.32291088 -0.31400001 0.07596169 0.32377279 0.32377279 0.32377279";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple17()
	{
		String inputW = "-1.43736553 -1.45200002 -0.67056268 30.00000000 30.00000000 25.00000000 2.00000000";
		String inputX = "4.04235697 4.10200024 0.32429737 0.59815985 0.59815985 0.59815985";
		String inputY = "-0.31835285 -0.74300003 0.05309106 1.90845609 1.90845609 1.90845609";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple18()
	{
		String inputW = "-24.322626113891600 -12.930775642395020 16.682678222656250 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "2.458647489547730 0.762066245079041 0.004544973373413 1.624501228332520 1.624501228332520 1.624501228332520";
		String inputY = "-2.150188684463501 -0.453182458877564 0.658808588981628 1.166617155075073 1.166617155075073 1.166617155075073";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple19()
	{
		String inputW = "27.539030075073242 -23.542854309082030 -16.585338592529297 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "0.159877538681030 3.747386932373047 0.623457312583923 1.264037489891052 1.264037489891052 1.264037489891052";
		String inputY = "-2.950394153594971 0.867860913276672 -0.007958173751831 1.549906253814697 1.549906253814697 1.549906253814697";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple20()
	{
		String inputW = "-3.867223262786865 -11.141796112060547 -13.045529365539550 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "0.907624363899231 0.669398307800293 0.820478558540344 1.043950557708740 1.043950557708740 1.043950557708740";
		String inputY = "-3.049795627593994 -0.258983969688416 0.876403093338013 1.705921292304993 1.705921292304993 1.705921292304993";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple21()
	{
		String inputW = "-16.325551986694336 -12.329846382141113 12.642318725585938 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "-0.398519039154053 3.196129798889160 0.789525866508484 1.534277796745300 1.534277796745300 1.534277796745300";
		String inputY = "0.700269341468811 3.010783195495606 -0.201447725296021 1.282962083816528 1.282962083816528 1.282962083816528";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple22()
	{
		String inputW = "8.698417663574219 -1.479485034942627 -14.497102737426758 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "0.554214715957642 4.504506587982178 0.330708622932434 1.737141489982605 1.737141489982605 1.737141489982605";
		String inputY = "-1.161908507347107 1.386625766754150 0.720958352088928 0.991130411624908 0.991130411624908 0.991130411624908";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple23()
	{
		String inputW = "11.312846183776855 -8.722629547119140 -16.420465469360350 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "4.326229095458984 -2.361482381820679 -0.609168529510498 1.486015915870667 1.486015915870667 1.486015915870667";
		String inputY = "-0.953315496444702 4.506130695343018 -0.118884682655334 1.338565111160278 1.338565111160278 1.338565111160278";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple24()
	{
		String inputW = "-13.599625587463379 12.103475570678711 17.111761093139650 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "3.970020294189453 -2.287299156188965 -0.678503274917603 1.494201302528381 1.494201302528381 1.494201302528381";
		String inputY = "1.271594166755676 -3.091067075729370 0.596162676811218 1.329421877861023 1.329421877861023 1.329421877861023";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple25()
	{
		String inputW = "-14.874776840209961 28.151578903198242 9.619854927062988 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "-2.576470851898193 -0.927060842514038 0.775293350219727 1.602752327919006 1.602752327919006 1.602752327919006";
		String inputY = "0.833050608634949 2.143689393997192 0.768347978591919 1.196321368217468 1.196321368217468 1.196321368217468";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple26()
	{
		String inputW = "5.387138366699219 -20.951017379760742 -11.608447074890137 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "-2.274324893951416 2.975687980651856 0.647039532661438 1.542121052742004 1.542121052742004 1.542121052742004";
		String inputY = "3.056082248687744 -1.404648423194885 -0.764784932136536 1.273523688316345 1.273523688316345 1.273523688316345";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple27()
	{
		String inputW = "-13.964767456054688 8.120719909667969 8.186260223388672 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "-1.825899481773377 1.682648658752441 0.875383615493774 1.064806222915649 1.064806222915649 1.064806222915649";
		String inputY = "-3.211776733398438 2.242960929870606 0.932605862617493 1.692981839179993 1.692981839179993 1.692981839179993";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple28()
	{
		String inputW = "-3.867223262786865 -11.141796112060547 -13.045529365539550 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "0.907624363899231 0.669398307800293 0.820478558540344 1.043950557708740 1.043950557708740 1.043950557708740";
		String inputY = "-3.049795627593994 -0.258983969688416 0.876403093338013 1.705921292304993 1.705921292304993 1.705921292304993";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple29()
	{
		String inputW = "-3.867223262786865 -11.141796112060547 -13.045529365539550 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "0.907624363899231 0.669398307800293 0.820478558540344 1.043950557708740 1.043950557708740 1.043950557708740";
		String inputY = "-3.049795627593994 -0.258983969688416 0.876403093338013 1.705921292304993 1.705921292304993 1.705921292304993";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple30()
	{
		String inputW = "-21.779121398925780 -15.167713165283203 11.893805503845215 20.000000000000000 20.000000000000000 25.000000000000000 2.000000000000000";
		String inputX = "3.020526885986328 0.102418661117554 -0.598468542098999 1.666340351104736 1.666340351104736 1.666340351104736";
		String inputY = "1.845453977584839 -0.245459079742432 -0.689102411270142 1.106033444404602 1.106033444404602 1.106033444404602";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple31()
	{
		String inputW = "-28.73267745971679700000 5.12589454650878900000 11.45207691192627000000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "2.69644927978515620000 0.99735260009765620000 -0.79558289051055910000 0.44418746232986450000 0.44418746232986450000 0.44418746232986450000";
		String inputY = "-3.40444684028625500000 1.58029198646545400000 -0.73582184314727780000 1.95005059242248540000 1.95005059242248540000 1.95005059242248540000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple32()
	{
		String inputW = "21.34025955200195300000 2.05683231353759770000 -17.21437454223632800000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "-1.87930703163146970000 -0.54036676883697510000 0.62267649173736570000 0.99113070964813230000 0.99113070964813230000 0.99113070964813230000";
		String inputY = "-0.00694811344146728500 -1.60078346729278560000 0.01266455650329589800 1.73714125156402590000 1.73714125156402590000 1.73714125156402590000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple33()
	{
		String inputW = "-16.8 -25.6 -10.25 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.76 2.6 0.921071171760559 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13 0.13 -0.153997182846069 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple34()
	{
		String inputW = "0.44488191604614260000 9.02601242065429700000 14.29263591766357400000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple35()
	{
		String inputW = "-22.11858558654785000000 -13.89505481719970700000 13.03060722351074200000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple36()
	{
		String inputW = "16.60051345825195300000 24.46213150024414000000 6.72909450531005900000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple37()
	{
		String inputW = "-24.53177261352539000000 -17.18886947631836000000 16.35877037048340000000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple38()
	{
		String inputW = "-9.95363426208496100000 -1.23937726020812990000 2.93634462356567400000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple39()
	{
		String inputW = "-7.61591053009033200000 -0.24627327919006348000 15.11818695068359400000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.75785493850708000000 2.59882569313049300000 0.92107117176055910000 1.52637684345245360000 1.52637684345245360000 1.52637684345245360000";
		String inputY = "3.13022089004516600000 0.13498067855834960000 -0.15399718284606934000 1.29235208034515380000 1.29235208034515380000 1.29235208034515380000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple40()
	{
		String inputW = "23.43049049377441400000 8.76996898651123000000 -16.92032432556152300000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.78184795379638700000 0.73598802089691160000 0.64924025535583500000 1.97159504890441900000 1.97159504890441900000 1.97159504890441900000";
		String inputY = "2.83686161041259770000 1.65492296218872070000 -0.69914245605468750000 0.33587655425071716000 0.33587655425071716000 0.33587655425071716000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple41()
	{
		String inputW = "-21.52712631225586000000 -22.84661293029785000000 -12.39106941223144500000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "4.78184795379638700000 0.73598802089691160000 0.64924025535583500000 1.97159504890441900000 1.97159504890441900000 1.97159504890441900000";
		String inputY = "2.83686161041259770000 1.65492296218872070000 -0.69914245605468750000 0.33587655425071716000 0.33587655425071716000 0.33587655425071716000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple42()
	{
		String inputW = "-17.75543403625488300000 -29.12480545043945300000 -10.05533695220947300000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "-2.39925980567932130000 3.94902276992797850000 -0.45032179355621340000 1.45271825790405270000 1.45271825790405270000 1.45271825790405270000";
		String inputY = "2.42465066909790040000 -4.19908523559570300000 -0.13117492198944092000 1.37463080883026120000 1.37463080883026120000 1.37463080883026120000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple43()
	{
		String inputW = "-6.19616031646728500000 -10.04843807220459000000 -12.41334342956543000000 20.00000000000000000000 20.00000000000000000000 25.00000000000000000000 2.00000000000000000000";
		String inputX = "2.85207462310791000000 0.81727325916290280000 0.11241996288299560000 1.99412012100219730000 1.99412012100219730000 1.99412012100219730000";
		String inputY = "0.31062901020050050000 0.09617626667022705000 -0.05719780921936035000 0.15324771404266357000 0.15324771404266357000 0.15324771404266357000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple44()
	{
		String inputW = "-0.8782987 -0.8782987 0.0 30.00000000000000000000 30.00000000000000000000 10.00000000000000000000 0.50000000000000000000";
		String inputX = "-2.080491542816162 -0.214149340987206 -0.424870878458023 1.994120121002197 1.994120121002197 0.498530030250549";
		String inputY = "-0.944235563278198 -0.791181445121765 0.263599574565887 0.153247714042664 0.153247714042664 0.038311928510666";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple45()
	{
		String inputW = "-0.757018268108368 -0.917448878288269 0.000000476837158 10.000000000000000 10.000000000000000 10.000000000000000 2.299999952316284";
		String inputX = "1.264032483100891 1.213525891304016 -0.190707549452782 0.360045790672302 0.360045790672302 0.414052665233612";
		String inputY = "-0.603196978569031 -0.881677448749542 -1.046767950057983 1.967324852943420 1.967324852943420 2.262423515319824";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple46()
	{
		String inputW = "-2.58115005493164060000 -1.14640140533447270000 -6.69611024856567400000 1.45173442363739010000 63.63888931274414000000 6.97178840637207000000 0.62385690212249760000";
		String inputX = "-1.99810266494750980000 -4.09091043472290000000 0.05394053459167480500 3.98997187614440900000 4.55086183547973600000 0.29913553595542910000";
		String inputY = "1.77936077117919920000 -2.06216287612915040000 0.23887181282043457000 7.30224418640136700000 8.32875633239746100000 0.54746264219284060000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple47()
	{
		String inputW = "1.36244773864746100000 0.91239929199218750000 16.41625976562500000000 14.59047126770019500000 12.28413772583007800000 5.21727371215820300000 0.51835131645202640000";
		String inputX = "0.33224701881408690000 3.53922009468078600000 -0.86381804943084720000 7.06796836853027300000 3.85892152786254900000 0.51682740449905400000";
		String inputY = "3.46150398254394530000 3.48820447921752930000 -0.34874773025512695000 0.54317188262939450000 0.29655730724334717000 0.03971807658672333000";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple48()
	{
		String inputW = "2.58895683288574200000 2.56172752380371100000 2.58365821838378900000 26.09255027770996000000 64.14102172851562000000 25.14172744750976600000 0.48158499598503113000";
		String inputX = "2.44090032577514650000 0.94820499420166020000 -0.55187332630157470000 0.24413113296031952000 0.93136173486709600000 0.07212430983781815000";
		String inputY = "4.16695356369018550000 -4.87281227111816400000 -0.49799263477325440000 1.61171603202819820000 6.14870595932006800000 0.47615355253219604000";
		
		testFromDataCircleAware(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple49()
	{
		String inputW = "2.217789411544800 0.628863990306854 5.290616989135742 79.986717224121100 27.073167800903320 7.241873741149902 0.109354831278324";
		String inputX = "1.234100461006165 -3.940692424774170 0.411371707916260 4.483094215393066 2.670888662338257 0.062141861766577";
		String inputY = "4.782915115356445 -2.716825008392334 0.701661229133606 6.491606235504150 3.867497920989990 0.089982599020004";
		
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple50()
	{
		String inputW = "-0.039731547236443 -0.039731547236443 0.000000000000000 10.000000000000000 10.000000000000000 10.000000000000000 2.000000000000000";
		String inputX = "-1.625439405441284 -0.711000025272369 -1.146594285964966 1.146594405174255 1.146594405174255 1.146594405174255";
		String inputY = "1.273529887199402 2.576000213623047 -1.638694882392883 1.638695001602173 1.638695001602173 1.638695001602173";
		
		// testFromDataCircleAware(inputW, inputX, inputY);
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple51()
	{
		String inputW = "-2.64599037170410160000 -2.81323766708374020000 -2.19567561149597170000 68.98682403564453000000 21.97458839416504000000 32.21256256103515600000 0.40858820080757140000";
		String inputX = "2.42509722709655760000 -3.98076891899108900000 -0.10960888862609863000 8.28174018859863300000 5.98423910140991200000 0.35110056400299070000";
		String inputY = "0.49720346927642820000 4.37321758270263700000 0.86854016780853270000 4.92941141128540000000 3.56190538406372070000 0.20898012816905975000";
		
		// testFromDataCircleAware(inputW, inputX, inputY);
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple52()
	{
		String inputW = "0.50303786993026730000 -0.91799050569534300000 -15.70657157897949200000 46.40136337280273400000 28.05644607543945300000 16.01968574523925800000 1.00782883167266850000";
		String inputX = "-4.92514085769653300000 3.50714445114135740000 -0.59413361549377440000 4.69116210937500000000 2.95955038070678700000 0.99646222591400150000";
		String inputY = "0.98437964916229250000 0.09578704833984375000 0.49290990829467773000 0.71058386564254760000 0.44829162955284120000 0.15093700587749480000";
		
		// testFromDataCircleAware(inputW, inputX, inputY);
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple53()
	{
		String inputW = "2.73487162590026860000 2.71760702133178700000 -15.96270275115966800000 7.22929000854492200000 1.20669531822204600000 11.96997547149658200000 0.11717189848423004000";
		String inputX = "-1.71554446220397950000 -4.19477844238281250000 0.30825626850128174000 1.28476798534393300000 4.49679851531982400000 0.05364285409450531000";
		String inputY = "0.59174001216888430000 -4.27607965469360350000 -0.40903556346893310000 2.49494910240173340000 8.73253631591796900000 0.10417148470878601000";
		
		// testFromDataCircleAware(inputW, inputX, inputY);
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	public void testSimple54()
	{
		String inputW = "2.80794811248779300000 -1.80657029151916500000 -1.16423106193542480000 67.21514129638672000000 68.21104431152344000000 26.38052558898925800000 0.42552191019058230000";
		String inputX = "-2.07184195518493650000 -2.96081018447875980000 -0.04915440082550049000 0.78744041919708250000 1.07911336421966550000 0.05597320199012756300";
		String inputY = "4.16906881332397500000 -2.61294841766357400000 0.58080518245697020000 5.93429756164550800000 8.13239860534668000000 0.42182448506355286000";
		
		// testFromDataCircleAware(inputW, inputX, inputY);
		testFromData(inputW, inputX, inputY);
	}
	
	
	/** */
	@Test
	@Ignore
	public void testMonteCarloCircleAware()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0f);
			
			double maxAccXy = (rng.nextDouble() * 10) + 0.1;
			double maxBrkXy = (rng.nextDouble() * 10) + 0.1;
			double maxVelXy = (rng.nextDouble() * 3.3) + 0.1;
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, maxAccXy, maxBrkXy, maxVelXy);
			
			for (int j = 0; j < NUMBER_OF_TESTS; j++)
			{
				double initOrient = AngleMath.normalizeAngle(getRandomDouble(ROT_LIMIT));
				double finalOrient = AngleMath.normalizeAngle(getRandomDouble(ROT_LIMIT));
				double orientVel = getRandomDouble(35);
				
				double maxAcc = (rng.nextDouble() * 99) + 1;
				double maxBrk = (rng.nextDouble() * 99) + 1;
				double maxVel = (rng.nextDouble() * 35) + 1;
				
				BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.circleAware(initOrient, finalOrient, orientVel,
						maxAcc, maxBrk, maxVel, traj, maxVelXy);
						
				try
				{
					checkTimeOrder(orient);
					
					Assert.assertTrue("totalTime=" + orient.getTotalTime(), orient.getTotalTime() >= 0.0);
					
					// check final velocity == 0
					Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
					
					// check final position reached
					double realOrient = orient.getPositionMM(orient.getTotalTime());
					Assert.assertEquals(0.0f, AngleMath.normalizeAngle(realOrient - finalOrient), POS_TOLERANCE);
				} catch (AssertionError err)
				{
					System.err.println(err.getMessage());
					err.printStackTrace();
					System.out.println(orient);
					System.out.println();
					System.out.println("String inputW = \"" + orient.getInitParams() + "\";");
					System.out.println("String inputX = \"" + traj.getX().getInitParams() + "\";");
					System.out.println("String inputY = \"" + traj.getY().getInitParams() + "\";");
					System.out.println();
				}
			}
		}
	}
	
	
	/** */
	private void doMonteCarloCircleAware(final int n)
	{
		for (int i = 0; i < n; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0f);
			
			double maxAccXy = (rng.nextDouble() * 10) + 0.1;
			double maxBrkXy = (rng.nextDouble() * 10) + 0.1;
			double maxVelXy = (rng.nextDouble() * 3.3) + 0.1;
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, maxAccXy, maxBrkXy, maxVelXy);
			
			for (int j = 0; j < n; j++)
			{
				double initOrient = AngleMath.normalizeAngle(getRandomDouble(ROT_LIMIT));
				double finalOrient = AngleMath.normalizeAngle(getRandomDouble(ROT_LIMIT));
				double orientVel = getRandomDouble(35);
				
				double maxAcc = (rng.nextDouble() * 99) + 1;
				double maxBrk = (rng.nextDouble() * 99) + 1;
				double maxVel = (rng.nextDouble() * 35) + 1;
				
				BangBangTrajectoryOrient orient = BangBangTrajectoryOrient.circleAware(initOrient, finalOrient, orientVel,
						maxAcc, maxBrk, maxVel, traj, maxVelXy);
				try
				{
					
					checkTimeOrder(orient);
					
					Assert.assertTrue("totalTime=" + orient.getTotalTime(), orient.getTotalTime() >= 0.0);
					
					// check final velocity == 0
					Assert.assertEquals(0.0f, orient.getVelocity(orient.getTotalTime()), VEL_TOLERANCE);
					
					// check final position reached
					double realOrient = orient.getPositionMM(orient.getTotalTime());
					Assert.assertEquals(0.0f, AngleMath.normalizeAngle(realOrient - finalOrient), POS_TOLERANCE);
				} catch (AssertionError err)
				{
					System.err.println(err.getMessage());
					err.printStackTrace();
					System.out.println(orient);
					System.out.println();
					System.out.println("String inputW = \"" + orient.getInitParams() + "\";");
					System.out.println("String inputX = \"" + traj.getX().getInitParams() + "\";");
					System.out.println("String inputY = \"" + traj.getY().getInitParams() + "\";");
					System.out.println();
				}
			}
		}
	}
	
	
	/**
	 * 
	 */
	@Test
	@Ignore
	public void testMonteCarloParallel()
	{
		int n = 10;
		ExecutorService service = Executors.newFixedThreadPool(n);
		for (int i = 0; i < n; i++)
		{
			service.execute(() -> doMonteCarloCircleAware(100000));
		}
		service.shutdown();
		try
		{
			service.awaitTermination(2, TimeUnit.DAYS);
		} catch (InterruptedException err)
		{
			err.printStackTrace();
		}
	}
}
