/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCollision implements ICollisionObject
{
	private final CircleCollision	circleCollision;
	private final LineCollision	lineCollision;
	private final IVector3			pos;
	private final IVector3			vel;
	private final double				center2DribblerDist;
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param center2DribblerDist
	 */
	public BotCollision(final IVector3 pos, final IVector3 vel, final double center2DribblerDist)
	{
		this.pos = pos;
		this.vel = vel;
		this.center2DribblerDist = center2DribblerDist;
		
		circleCollision = new CircleCollision(new Circle(pos.getXYVector(), Geometry.getBotRadius()
				+ Geometry.getBallRadius()), vel.getXYVector());
		
		ILine frontLine = getKickerFrontLine(pos, center2DribblerDist);
		lineCollision = new LineCollision(frontLine, vel.getXYVector(), new Vector2(pos.z()));
	}
	
	
	/**
	 * @param pos
	 * @param center2DribblerDist
	 * @return
	 */
	public static ILine getKickerFrontLine(final IVector3 pos, final double center2DribblerDist)
	{
		double theta = Math.acos((center2DribblerDist + Geometry.getBallRadius())
				/ (Geometry.getBotRadius() + Geometry.getBallRadius()));
		IVector2 leftBotEdge = pos.getXYVector()
				.addNew(new Vector2(pos.z() - theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
		IVector2 rightBotEdge = pos.getXYVector()
				.addNew(new Vector2(pos.z() + theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
		ILine frontLine = Line.newLine(leftBotEdge, rightBotEdge);
		return frontLine;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel.getXYVector();
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		Optional<ICollision> circleCol = circleCollision.getCollision(prePos, postPos);
		if (!circleCol.isPresent())
		{
			// ball is not within bot circle
			return Optional.empty();
		}
		
		IVector2 botPos2Collision = circleCol.get().getPos().subtractNew(pos);
		if (botPos2Collision.isZeroVector())
		{
			// should only be possible for a circle with zero radius
			return Optional.empty();
		}
		
		double theta = Math.acos((center2DribblerDist + Geometry.getBallRadius())
				/ (Geometry.getBotRadius() + Geometry.getBallRadius()));
		double angleDiff = Math.abs(AngleMath.getShortestRotation(botPos2Collision.getAngle(), pos.z()));
		if (angleDiff >= theta)
		{
			// ball is NOT in front of kicker -> collision is on circle
			return circleCol;
		}
		
		return lineCollision.getCollision(prePos, postPos);
	}
	
}
