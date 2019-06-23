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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;


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
	private double		mM	= Def.DUMMY;
	private double		mN	= Def.DUMMY;
	private boolean	orientationNormal;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aPointA
	 * @param aPointB
	 */
	public LinFunc(final Coord aPointA, final Coord aPointB)
	{
		mM = ((aPointB.y() - aPointA.y()) / (aPointB.x() - aPointA.x()));
		mN = aPointA.y() - (mM * aPointA.x());
		
		if ((aPointB.x() - aPointA.x()) > 0)
		{
			orientationNormal = true;
		} else
		{
			orientationNormal = false;
		}
	}
	
	
	/**
	 * @param aPoint
	 * @param aViewDir
	 * @param takeThisVektorAsView
	 */
	public LinFunc(final Coord aPoint, final Coord aViewDir, boolean takeThisVektorAsView)
	{
		mM = (aViewDir.y() / aViewDir.x());
		mN = aPoint.y() - (mM * aPoint.x());
		
		orientationNormal = true;
	}
	
	
	/**
	 * @param aPoint
	 * @param aViewAngle
	 */
	public LinFunc(final Coord aPoint, final double aViewAngle)
	{
		mM = Math.tan(aViewAngle);
		mN = aPoint.y() - (mM * aPoint.x());
		
		if (Math.abs(aViewAngle) < (Math.PI / 2.0))
		{
			orientationNormal = true;
		} else
		{
			orientationNormal = false;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aPoint
	 * @param distance
	 * @return
	 */
	public Coord goDistanceFromPoint(final Coord aPoint, final double distance)
	{
		final Coord res = new Coord(Def.DUMMY, Def.DUMMY);
		
		final double deltaX = Math.cos(Math.atan(mM)) * distance;
		
		double newX;
		if (orientationNormal)
		{
			newX = aPoint.x() + deltaX;
		} else
		{
			newX = aPoint.x() - deltaX;
		}
		res.set(newX, f(newX));
		
		return res;
	}
	
	
	/**
	 * @param x
	 * @return
	 */
	public double f(final double x)
	{
		return (mM * x) + mN;
	}
	
	
	/**
	 * returns the cutpoint between the functions
	 * if
	 * @param a
	 * @param b
	 * @return
	 */
	public static Coord getCutCoords(final LinFunc a, final LinFunc b)
	{
		final Coord ret = new Coord(Def.DUMMY, Def.DUMMY);
		
		/** m_a*x + n_a = m_b*x + n_b */
		final double x = (b.mN - a.mN) / (a.mM - b.mM);
		final double y = a.f(x);
		
		// if the cut not or cut in every point, because they are the same, return default
		if (new Float(x).isInfinite() || new Float(y).isInfinite())
		{
			throw new IllegalArgumentException("LinFunc: The Linear Functions have no, or infinite much cutpoints!");
		}
		ret.set(x, y);
		
		return ret;
	}
	
	
	/**
	 * returns an angle between the given vector and the function, which is between 0 and PI
	 * @param vecB
	 * @return
	 */
	public double getAngleToVector(Coord vecB)
	{
		final Coord vecA = new Coord(1.0, mM);
		
		if (!orientationNormal)
		{
			vecA.set(-vecA.x(), -vecA.y());
		}
		
		final double up = (vecA.x() * vecB.x()) + (vecA.y() * vecB.y());
		final double down = Math.sqrt((vecA.x() * vecA.x()) + (vecA.y() * vecA.y()))
				* Math.sqrt((vecB.x() * vecB.x()) + (vecB.y() * vecB.y()));
		
		return Math.acos(up / down);
	}
	
	
	@Override
	public String toString()
	{
		return "f(x) = " + mM + "*x + " + mN + " );  " + orientationNormal;
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
