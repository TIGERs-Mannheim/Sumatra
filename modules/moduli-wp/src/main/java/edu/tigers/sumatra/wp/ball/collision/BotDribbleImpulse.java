/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.ball.IState;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotDribbleImpulse implements IImpulseObject
{
	private final ILine		kickerLine;
	@SuppressWarnings("unused")
	private final IVector2	kickerCenter;
	private final IVector2	normal;
	
	
	/**
	 * @param pos
	 * @param center2DribblerDist
	 */
	public BotDribbleImpulse(final IVector3 pos, final double center2DribblerDist)
	{
		ILine kickerFrontLine = BotCollision.getKickerFrontLine(pos, center2DribblerDist);
		double dribblerWidth = 50;
		double offset = (kickerFrontLine.directionVector().getLength() - dribblerWidth) / 2;
		kickerLine = new Line(
				kickerFrontLine.supportVector().addNew(kickerFrontLine.directionVector().scaleToNew(offset)),
				kickerFrontLine.directionVector().scaleToNew(dribblerWidth));
		
		normal = new Vector2(pos.z());
		kickerCenter = pos.getXYVector()
				.addNew(normal.scaleToNew(center2DribblerDist + Geometry.getBallRadius()));
	}
	
	
	@Override
	public IVector3 getImpulse(final IVector3 pos)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public IVector3 getTorqueAcc(final IState state)
	{
		IVector3 pos = state.getPos();
		double minDist2KickerLine = 10;
		if ((pos.z() < 170) && kickerLine.isPointOnLine(pos.getXYVector(), minDist2KickerLine))
		{
			return new Vector3(normal.scaleToNew(-7), 0);
		}
		return AVector3.ZERO_VECTOR;
	}
}
