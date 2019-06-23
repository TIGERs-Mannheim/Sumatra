/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.flyingBalls;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * One Ball in a Fly
 * 
 * @author Birgit
 */
public class FlyingBall
{
	private final IVector2	bottomPos;
	private IVector2			flyingPos		= new Vector2(0, 0);
	
	private double				flyingHeight	= 0;
	private double				distance			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param ballPos
	 */
	public FlyingBall(final IVector2 ballPos)
	{
		bottomPos = new Vector2(ballPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param flyPosition
	 * @param camId
	 */
	public void setFlyPositionAndCalculateFlyingHeight(final IVector2 flyPosition, final int camId)
	{
		flyingPos = flyPosition;
		final double bfX = Math.abs(flyingPos.x() - bottomPos.x());
		final double bfY = Math.abs(flyingPos.y() - bottomPos.y());
		final double bf = Math.sqrt((bfX * bfX) + (bfY * bfY));
		
		float prinX = AIConfig.getGeometry().getCameraPrincipalPointX()[camId];
		float prinY = AIConfig.getGeometry().getCameraPrincipalPointY()[camId];
		final double cfX = Math.abs(flyingPos.x() - prinX);
		final double cfY = Math.abs(flyingPos.y() - prinY);
		
		final double cf = Math.sqrt((cfX * cfX) + (cfY * cfY));
		
		float camHeight = AIConfig.getGeometry().getCameraHeights()[camId];
		flyingHeight = (camHeight * bf) / (bf + cf);
	}
	
	
	/**
	 * @param point
	 */
	public void calculateDistanceToStart(final IVector2 point)
	{
		if (Double.isInfinite(flyingPos.x()) || Double.isInfinite(flyingPos.y()))
		{
			throw new IllegalArgumentException(
					"FlyingBall: calculateDistanceToPoint is not possible, before setting the flying Position.");
		}
		
		final double x2 = Math.pow(Math.abs(point.x() - flyingPos.x()), 2);
		final double y2 = Math.pow(Math.abs(point.y() - flyingPos.y()), 2);
		distance = Math.sqrt(x2 + y2);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public double getDistance()
	{
		return distance;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getFlyingPosition()
	{
		return flyingPos;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getBottomPosition()
	{
		return bottomPos;
	}
	
	
	/**
	 * @return
	 */
	public double getFlyingHeight()
	{
		return flyingHeight;
	}
	
	
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append("###Flying Ball###\n");
		str.append("bottomPosX:    " + bottomPos.x() + "\n");
		str.append("bottomPosY:    " + bottomPos.y() + "\n");
		str.append("flyingPosX:    " + flyingPos.x() + "\n");
		str.append("flyingPosY:    " + flyingPos.y() + "\n");
		str.append("flyingHeight:  " + flyingHeight + "\n");
		
		return str.toString();
	}
}
