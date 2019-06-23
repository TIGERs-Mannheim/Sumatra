/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;


/**
 * This stores 2 double values
 */
public class Coord
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private double	mX;
	private double	mY;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aX
	 * @param aY
	 */
	public Coord(final double aX, final double aY)
	{
		mX = aX;
		mY = aY;
	}
	
	
	/**
	 * @param copy
	 */
	public Coord(final Coord copy)
	{
		mX = copy.x();
		mY = copy.y();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aX
	 * @param aY
	 */
	public void set(final double aX, final double aY)
	{
		mX = aX;
		mY = aY;
	}
	
	
	/**
	 * @param aX
	 */
	public void setX(final double aX)
	{
		mX = aX;
	}
	
	
	/**
	 * @param aY
	 */
	public void setY(final double aY)
	{
		mY = aY;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param b
	 * @return
	 */
	public double getDistanceTo(final Coord b)
	{
		final double x2 = Math.pow(Math.abs(b.x() - mX), 2);
		final double y2 = Math.pow(Math.abs(b.y() - mY), 2);
		return Math.sqrt(x2 + y2);
	}
	
	
	/**
	 * @param b
	 * @return
	 */
	public Coord getVectorTo(final Coord b)
	{
		final double x = b.mX - mX;
		final double y = b.mY - mY;
		return new Coord(x, y);
	}
	
	
	/**
	 * @return
	 */
	public double getLength()
	{
		final double x2 = Math.pow(Math.abs(mX), 2);
		final double y2 = Math.pow(Math.abs(mY), 2);
		return Math.sqrt(x2 + y2);
	}
	
	
	/**
	 * @return
	 */
	public double x()
	{
		return mX;
	}
	
	
	/**
	 * @return
	 */
	public double y()
	{
		return mY;
	}
	
	
	@Override
	public String toString()
	{
		String ret;
		ret = "( " + mX + ", " + mY + ")";
		return ret;
	}
}
