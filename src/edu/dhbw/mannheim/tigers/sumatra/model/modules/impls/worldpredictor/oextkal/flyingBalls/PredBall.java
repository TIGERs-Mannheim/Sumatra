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
	private double m_X = Def.DUMMY;
	private double m_Y = Def.DUMMY;
	private double m_Z = Def.DUMMY;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PredBall(){}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setX(double val)
	{
		m_X = val;
	}
	
	public void setY(double val)
	{
		m_Y = val;
	}
	
	public void setZ(double val)
	{
		m_Z = val;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------

	public double x()
	{
		return m_X;
	}
	
	public double y()
	{
		return m_Y;
	}
	
	public double z()
	{
		return m_Z;
	}
	
	public String toString()
	{
		String ret = "";
		ret += "\nX: "+m_X;
		ret += "\nY: "+m_Y;
		ret += "\nZ: "+m_Z;
		return ret;
	}
}
