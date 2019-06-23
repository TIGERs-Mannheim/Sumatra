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
 * One Ball in a Fly
 * 
 * @author Birgit
 * 
 */
public class FlyingBall
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private Coord m_bottomPos     = new Coord(Def.DUMMY, Def.DUMMY);
	private Coord m_flyingPos     = new Coord(Def.DUMMY, Def.DUMMY);
		
	private double m_flyingHeight = Def.DUMMY;
	private double m_distance     = Def.DUMMY;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public FlyingBall(
			final Coord a_ballPos)
	{
		m_bottomPos = new Coord(a_ballPos);
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setFlyPositionAndCalculateFlyingHeight(final Coord a_flyPosition, final int camId)
	{
		m_flyingPos = a_flyPosition;
		double _bf_x = Math.abs(m_flyingPos.x() - m_bottomPos.x());
		double _bf_y = Math.abs(m_flyingPos.y() - m_bottomPos.y()); 
		double _bf_ = Math.sqrt(_bf_x*_bf_x + _bf_y*_bf_y);
		
		double _cf_x = Def.DUMMY;
		double _cf_y = Def.DUMMY;
		//set the correct camSide
		if(camId == Def.CamIDOne)
		{
			_cf_x = Math.abs(m_flyingPos.x() - Def.CamOneX);
			_cf_y = Math.abs(m_flyingPos.y() - Def.CamOneY); 
		}
		else
		{
			_cf_x = Math.abs(m_flyingPos.x() - Def.CamNullX);
			_cf_y = Math.abs(m_flyingPos.y() - Def.CamNullY); 
		}

		double _cf_ = Math.sqrt(_cf_x*_cf_x + _cf_y*_cf_y);
		
		//System.out.println("#####################################################");
		//System.out.println("cf: "+_cf_);
		//System.out.println("bf: "+_bf_);
		
		m_flyingHeight = Def.CamHeight*_bf_/(_bf_+_cf_);
		
		//System.out.println(this.toString());
		//System.out.println("#####################################################");
	}


	public void calculateDistanceToStart(final Coord point)
	{
		if(Double.isInfinite(m_flyingPos.x()) || Double.isInfinite(m_flyingPos.y()))
		{
			throw new IllegalArgumentException("FlyingBall: calculateDistanceToPoint is not possible, before setting the flying Position.");
		}
		
		double x2 = Math.pow(Math.abs(point.x()-m_flyingPos.x()),2);
		double y2 = Math.pow(Math.abs(point.y()-m_flyingPos.y()),2);
		m_distance = Math.sqrt(x2+y2);
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public double getDistance()
	{
		return  m_distance;
	}

	public Coord getFlyingPosition()
	{
		return  m_flyingPos;
	}


	public double getFlyingPositionX()
	{
		return  m_flyingPos.x();
	}

	public double getFlyingPositionY()
	{
		return  m_flyingPos.y();
	}

	public Coord getBottomPosition()
	{
		return  m_bottomPos;
	}

	public double getBottomPositionX()
	{
		return  m_bottomPos.x();
	}

	public double getBottomPositionY()
	{
		return  m_bottomPos.y();
	}

	public double getFlyingHeight()
	{
		return m_flyingHeight;
	}

	public String toString()
	{
		String str = "###Flying Ball###\n";
		str += "bottomPosX:    "+m_bottomPos.x()+"\n";
		str += "bottomPosY:    "+m_bottomPos.y()+"\n";
		str += "flyingPosX:    "+m_flyingPos.x()+"\n";
		str += "flyingPosY:    "+m_flyingPos.y()+"\n";
		str += "flyingHeight:  "+m_flyingHeight+"\n";
		
		return str;
	}
}
