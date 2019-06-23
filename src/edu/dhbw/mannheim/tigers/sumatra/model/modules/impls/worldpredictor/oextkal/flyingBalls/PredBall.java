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


/**
 * One possible predicted ball, will be deleted later
 * 
 * @author Birgit
 * 
 */
public class PredBall
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private double	x	= Def.DUMMY;
	private double	y	= Def.DUMMY;
	private double	z	= Def.DUMMY;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PredBall()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param val
	 */
	public void setX(double val)
	{
		x = val;
	}
	
	
	/**
	 * @param val
	 */
	public void setY(double val)
	{
		y = val;
	}
	
	
	/**
	 * @param val
	 */
	public void setZ(double val)
	{
		z = val;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public double x()
	{
		return x;
	}
	
	
	/**
	 * @return
	 */
	public double y()
	{
		return y;
	}
	
	
	/**
	 * @return
	 */
	public double z()
	{
		return z;
	}
	
	
	@Override
	public String toString()
	{
		String ret = "";
		ret += "\nX: " + x;
		ret += "\nY: " + y;
		ret += "\nZ: " + z;
		return ret;
	}
}
