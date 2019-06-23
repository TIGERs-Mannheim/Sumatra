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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


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
	private double a = Def.DUMMY;
	private double b = Def.DUMMY;
	private double c = Def.DUMMY;
	private double d = Def.DUMMY;
	private double e = Def.DUMMY;
	private double alpha = Def.DUMMY;
	
	private Matrix m_A;
	private Matrix m_v_b;
	
	//private Jama.Matrix m_A;
	//private Jama.Matrix m_v_b;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RegParab(final Coord[] data)
	{
		//if not enough elements are given, throw an exception
		int n = data.length;
	
		if(n < 4)
		{
			throw new IllegalArgumentException("A RegParab-Calculation is only with at least 4 elements possible");
		}
		double m_a[][] = new double[3][3];
		double v_b[] = {0,0,0};
		
		
		//fill Matrix
		double Zxi1   = 0;
		double Zxi2   = 0;
		double Zxi3   = 0;
		double Zxi4   = 0;
		double Zxiyi  = 0;
		double Zxi2yi = 0;
		double Zyi    = 0;
		
		double x = Def.DUMMY;
		double y = Def.DUMMY;
		
		for(int i = 0; i < n; i++)
		{
			x = data[i].x();
			y = data[i].y();
			
			Zxi1   += x;
			Zxi2   += x*x;
			Zxi3   += x*x*x;
			Zxi4   += x*x*x*x;
			Zxiyi  += x*y;
			Zxi2yi += x*x*y;
			Zyi    += y;
		}

		m_a[0][0] = Zxi4;
		m_a[0][1] = Zxi3;
		m_a[0][2] = Zxi2;
		m_a[1][0] = Zxi3;
		m_a[1][1] = Zxi2;
		m_a[1][2] = Zxi1;
		m_a[2][0] = Zxi2;
		m_a[2][1] = Zxi1;
		m_a[2][2] = ((double) n);
		     
		v_b[0] = Zxi2yi;
		v_b[1] = Zxiyi;
		v_b[2] = Zyi;
		
		//eigene matrix
		m_A = new Matrix(m_a);
		m_v_b = new Matrix(v_b, 3,1, false);
		
		//jama
		//m_A = new Jama.Matrix(m_a);
		//m_v_b = new Jama.Matrix(v_b,3);
			
		generateParabelFromMatrix();		
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public RegParab appendData(final Coord data)
	{
		double x = data.x();
		double y = data.y();
		
		m_A.set(0,0, m_A.get(0,0)+x*x*x*x);
		m_A.set(0,1, m_A.get(0,1)+x*x*x);
		m_A.set(0,2, m_A.get(0,2)+x*x);
		m_A.set(1,0, m_A.get(1,0)+x*x*x);
		m_A.set(1,1, m_A.get(1,1)+x*x);
		m_A.set(1,2, m_A.get(1,2)+x);
		m_A.set(2,0, m_A.get(2,0)+x*x);
		m_A.set(2,1, m_A.get(2,1)+x);
		m_A.set(2,2, m_A.get(2,2)+1);
		     
		m_v_b.set(0,0, m_v_b.get(0,0)+x*x*y);
		m_v_b.set(1,0, m_v_b.get(1,0)+x*y);
		m_v_b.set(2,0, m_v_b.get(2,0)+y);
		
		return this;
	}
	
	private RegParab generateParabelFromMatrix()
	{
		//eigene Matrix
		Matrix v_x = m_A.solve_Cholesky(m_v_b);
		
		//jama
		//Jama.Matrix v_x = m_A.lu().solve(m_v_b);
		
		a = v_x.get(0, 0);
		b = v_x.get(1, 0);
		c = v_x.get(2, 0);

		d = b/(2*a);
		e = c-a*d*d;
		alpha = Math.atan(b);
		
		return this;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public double f(double a_x)
	{
		return a*a_x*a_x+b*a_x+c;
	}
	
	public double getA()
	{
		return a;
	}
	
	public double getB()
	{
		return b;
	}
	
	public double getC()
	{
		return c;
	}
	
	public double getD()
	{
		return d;
	}
	
	public double getE()
	{
		return e;
	}
	
	public double getAlpha()
	{
		return alpha;
	}
	
	public String toString()
	{
		String ret;
		ret =  "\na: "+a;
		ret += "\nb: "+b;
		ret += "\nc: "+c;
		ret += "\nd: "+d;
		ret += "\ne: "+e;
		ret += "\nAlpha: "+alpha;
		return ret;
	}
}
