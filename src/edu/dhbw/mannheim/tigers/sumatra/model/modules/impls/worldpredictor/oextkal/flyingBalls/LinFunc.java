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

/**
 * linear function with form y = a*x+n
 * 
 * @author Birgit
 * 
 */
public class LinFunc
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private double m_m = Def.DUMMY;
	private double m_n = Def.DUMMY;
	private boolean orientationNormal;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public LinFunc(
			final Coord a_pointA,
			final Coord a_pointB)
	{
		m_m = ((a_pointB.y()-a_pointA.y())/(a_pointB.x()-a_pointA.x()));
		m_n = a_pointA.y() - m_m* a_pointA.x();
		
		if(a_pointB.x() - a_pointA.x() > 0)
		{
			orientationNormal = true;
		}
		else
		{
			orientationNormal = false;
		}
	}
	
	public LinFunc(
			final Coord a_point,
			final Coord a_viewDir,
			boolean takeThisVektorAsView)
	{
		m_m = (a_viewDir.y() / a_viewDir.x());
		m_n = a_point.y() - m_m*a_point.x();
		
		orientationNormal = true;
	}
	
	public LinFunc(
			final Coord a_point,
			final double a_viewAngle)
	{
		m_m = Math.tan(a_viewAngle);
		m_n = a_point.y() - m_m*a_point.x();
		
		if(Math.abs(a_viewAngle) < Math.PI/2.0)
		{
			orientationNormal = true;
		}
		else
		{
			orientationNormal = false;
		}
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public Coord goDistanceFromPoint(final Coord a_point, final double distance)
	{
		Coord res = new Coord(Def.DUMMY, Def.DUMMY);
		
		double deltaX = Math.cos(Math.atan(m_m))*distance;
		
		double newX;
		if(orientationNormal)
		{
			newX = a_point.x()+deltaX;
		}
		else
		{
			newX = a_point.x()-deltaX;
		}
		res.set(newX, f(newX));
				
		return res;
	}
	
	public double f(final double x)
	{
		return m_m*x+m_n;
	}
	
	/*
	 * returns the cutpoint between the functions
	 * if
	 */
	public static Coord getCutCoords(
			final LinFunc a, 
			final LinFunc b)
	{
		Coord ret = new Coord(Def.DUMMY, Def.DUMMY);
		
		//m_a*x + n_a = m_b*x + n_b;
		double x = (b.m_n -a.m_n)/(a.m_m-b.m_m);
		double y = a.f(x);
		
		//if the cut not or cut in every point, because they are the same, return default
		if(x == Def.DUMMY || x == -Def.DUMMY ||
		   y == Def.DUMMY || y == -Def.DUMMY)
		{
			throw new IllegalArgumentException("LinFunc: The Linear Functions have no, or infinity much cutpoints!");
		}
		else //everything is normal
		{
			ret.set(x,y);
		}
		
		return ret;
	}

	/*
	 * returns an angle between the given vector and the function, which is between 0 and PI
	 */
	public double getAngleToVector(Coord vec_b) 
	{
		Coord vec_a = new Coord(1.0, m_m);
		
		if(!orientationNormal)
		{
			vec_a.set(-vec_a.x(), - vec_a.y());
		}
		
		//System.out.println("vec1"+vec_a.toString());
		//System.out.println("vec2"+vec_b.toString());
		double up = vec_a.x()*vec_b.x() + vec_a.y()*vec_b.y();
		double down =	Math.sqrt(vec_a.x()*vec_a.x()+vec_a.y()*vec_a.y())*
							Math.sqrt(vec_b.x()*vec_b.x()+vec_b.y()*vec_b.y());
		
		double angle = Math.acos(up/down);
		//System.out.println("Winkel hier: "+angle);
		return angle;
	}
	
	public String toString() 
	{		
		return "f(x) = "+m_m+"*x + "+m_n+" );  "+orientationNormal;
		
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
