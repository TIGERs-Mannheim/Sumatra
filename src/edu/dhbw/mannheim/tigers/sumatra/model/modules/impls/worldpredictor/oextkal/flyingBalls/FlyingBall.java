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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def.Cam;


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
	private Coord	mBottomPos		= new Coord(Def.DUMMY, Def.DUMMY);
	private Coord	mFlyingPos		= new Coord(Def.DUMMY, Def.DUMMY);
	
	private double	mFlyingHeight	= Def.DUMMY;
	private double	mDistance		= Def.DUMMY;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aBallPos
	 */
	public FlyingBall(final Coord aBallPos)
	{
		mBottomPos = new Coord(aBallPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aFlyPosition
	 * @param camId
	 */
	public void setFlyPositionAndCalculateFlyingHeight(final Coord aFlyPosition, final int camId)
	{
		mFlyingPos = aFlyPosition;
		final double bfX = Math.abs(mFlyingPos.x() - mBottomPos.x());
		final double bfY = Math.abs(mFlyingPos.y() - mBottomPos.y());
		final double bf = Math.sqrt((bfX * bfX) + (bfY * bfY));
		
		Cam cam = Def.cams.get(camId);
		final double cfX = Math.abs(mFlyingPos.x() - cam.x);
		final double cfY = Math.abs(mFlyingPos.y() - cam.y);
		
		final double cf = Math.sqrt((cfX * cfX) + (cfY * cfY));
		
		mFlyingHeight = (Def.camHeight * bf) / (bf + cf);
	}
	
	
	/**
	 * @param point
	 */
	public void calculateDistanceToStart(final Coord point)
	{
		if (Double.isInfinite(mFlyingPos.x()) || Double.isInfinite(mFlyingPos.y()))
		{
			throw new IllegalArgumentException(
					"FlyingBall: calculateDistanceToPoint is not possible, before setting the flying Position.");
		}
		
		final double x2 = Math.pow(Math.abs(point.x() - mFlyingPos.x()), 2);
		final double y2 = Math.pow(Math.abs(point.y() - mFlyingPos.y()), 2);
		mDistance = Math.sqrt(x2 + y2);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public double getDistance()
	{
		return mDistance;
	}
	
	
	/**
	 * @return
	 */
	public Coord getFlyingPosition()
	{
		return mFlyingPos;
	}
	
	
	/**
	 * @return
	 */
	public double getFlyingPositionX()
	{
		return mFlyingPos.x();
	}
	
	
	/**
	 * @return
	 */
	public double getFlyingPositionY()
	{
		return mFlyingPos.y();
	}
	
	
	/**
	 * @return
	 */
	public Coord getBottomPosition()
	{
		return mBottomPos;
	}
	
	
	/**
	 * @return
	 */
	public double getBottomPositionX()
	{
		return mBottomPos.x();
	}
	
	
	/**
	 * @return
	 */
	public double getBottomPositionY()
	{
		return mBottomPos.y();
	}
	
	
	/**
	 * @return
	 */
	public double getFlyingHeight()
	{
		return mFlyingHeight;
	}
	
	
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append("###Flying Ball###\n");
		str.append("bottomPosX:    " + mBottomPos.x() + "\n");
		str.append("bottomPosY:    " + mBottomPos.y() + "\n");
		str.append("flyingPosX:    " + mFlyingPos.x() + "\n");
		str.append("flyingPosY:    " + mFlyingPos.y() + "\n");
		str.append("flyingHeight:  " + mFlyingHeight + "\n");
		
		return str.toString();
	}
}
