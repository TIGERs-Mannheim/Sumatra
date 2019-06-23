/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;


/**
 * This stores 2 double values
 */
public class Coord
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private double m_x;
	private double m_y;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Coord(final double a_x, final double a_y)
	{
		m_x = a_x;
		m_y = a_y;
	}
	
	public Coord (final Coord copy)
	{
		m_x = copy.x();
		m_y = copy.y();
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void set(final double a_x, final double a_y)
	{
		m_x = a_x;
		m_y = a_y;
	}
	
	public void setX(final double a_x)
	{
		m_x = a_x;
	}
	
	public void setY(final double a_y)
	{
		m_y = a_y;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public double getDistanceTo(final Coord b)
	{
		double x2 = Math.pow(Math.abs(b.x()-m_x),2);
		double y2 = Math.pow(Math.abs(b.y()-m_y),2);
		return Math.sqrt(x2+y2);
	}
	
	public Coord getVectorTo(final Coord b)
	{
		double x = b.m_x - this.m_x;
		double y = b.m_y - this.m_y;
		return new Coord(x,y);
	}
	
	public double getLength()
	{
		double x2 = Math.pow(Math.abs(m_x),2);
		double y2 = Math.pow(Math.abs(m_y),2);
		return Math.sqrt(x2+y2);
	}
	
	public double x()
	{
		return m_x;
	}
	
	public double y()
	{
		return m_y;
	}
	
	public String toString()
	{
		String ret;
		ret = "( "+m_x+", "+m_y+")";
		return ret;
	}
}
