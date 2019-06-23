/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;


/**
 * Calculates a parable out of measurement
 * 
 * @author Birgit
 * 
 */
public class RegParab
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private double			a		= Def.DUMMY;
	private double			b		= Def.DUMMY;
	private double			c		= Def.DUMMY;
	private double			d		= Def.DUMMY;
	private double			e		= Def.DUMMY;
	private double			alpha	= Def.DUMMY;
	
	private final Matrix	mA;
	private final Matrix	mvb;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	/**
	 * @param data
	 */
	public RegParab(final Coord[] data)
	{
		// if not enough elements are given, throw an exception
		final int n = data.length;
		
		if (n < 4)
		{
			throw new IllegalArgumentException("A RegParab-Calculation is only with at least 4 elements possible");
		}
		final double ma[][] = new double[3][3];
		final double vb[] = { 0, 0, 0 };
		
		
		// fill Matrix
		double zxi1 = 0;
		double zxi2 = 0;
		double zxi3 = 0;
		double zxi4 = 0;
		double zxiyi = 0;
		double zxi2yi = 0;
		double zyi = 0;
		
		for (int i = 0; i < n; i++)
		{
			final double x = data[i].x();
			final double y = data[i].y();
			
			zxi1 += x;
			zxi2 += x * x;
			zxi3 += x * x * x;
			zxi4 += x * x * x * x;
			zxiyi += x * y;
			zxi2yi += x * x * y;
			zyi += y;
		}
		
		ma[0][0] = zxi4;
		ma[0][1] = zxi3;
		ma[0][2] = zxi2;
		ma[1][0] = zxi3;
		ma[1][1] = zxi2;
		ma[1][2] = zxi1;
		ma[2][0] = zxi2;
		ma[2][1] = zxi1;
		ma[2][2] = (n);
		
		vb[0] = zxi2yi;
		vb[1] = zxiyi;
		vb[2] = zyi;
		
		// eigene matrix
		mA = new Matrix(ma);
		mvb = new Matrix(vb, 3, 1, false);
		
		generateParabelFromMatrix();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param data
	 * @return
	 */
	public RegParab appendData(final Coord data)
	{
		final double x = data.x();
		final double y = data.y();
		
		mA.set(0, 0, mA.get(0, 0) + (x * x * x * x));
		mA.set(0, 1, mA.get(0, 1) + (x * x * x));
		mA.set(0, 2, mA.get(0, 2) + (x * x));
		mA.set(1, 0, mA.get(1, 0) + (x * x * x));
		mA.set(1, 1, mA.get(1, 1) + (x * x));
		mA.set(1, 2, mA.get(1, 2) + x);
		mA.set(2, 0, mA.get(2, 0) + (x * x));
		mA.set(2, 1, mA.get(2, 1) + x);
		mA.set(2, 2, mA.get(2, 2) + 1);
		
		mvb.set(0, 0, mvb.get(0, 0) + (x * x * y));
		mvb.set(1, 0, mvb.get(1, 0) + (x * y));
		mvb.set(2, 0, mvb.get(2, 0) + y);
		
		return this;
	}
	
	
	private RegParab generateParabelFromMatrix()
	{
		// eigene Matrix
		final Matrix vx = mA.solveCholesky(mvb);
		
		a = vx.get(0, 0);
		b = vx.get(1, 0);
		c = vx.get(2, 0);
		
		d = b / (2 * a);
		e = c - (a * d * d);
		alpha = Math.atan(b);
		
		return this;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param ax
	 * @return
	 */
	public double f(double ax)
	{
		return (a * ax * ax) + (b * ax) + c;
	}
	
	
	/**
	 * @return
	 */
	public double getA()
	{
		return a;
	}
	
	
	/**
	 * @return
	 */
	public double getB()
	{
		return b;
	}
	
	
	/**
	 * @return
	 */
	public double getC()
	{
		return c;
	}
	
	
	/**
	 * @return
	 */
	public double getD()
	{
		return d;
	}
	
	
	/**
	 * @return
	 */
	public double getE()
	{
		return e;
	}
	
	
	/**
	 * @return
	 */
	public double getAlpha()
	{
		return alpha;
	}
	
	
	@Override
	public String toString()
	{
		return "RegParab [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", alpha=" + alpha + ", mA=" + mA
				+ ", mvb=" + mvb + "]";
	}
}
