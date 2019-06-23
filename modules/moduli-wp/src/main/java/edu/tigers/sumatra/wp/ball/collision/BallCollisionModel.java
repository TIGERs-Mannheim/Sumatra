/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.dynamics.IState;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionModel implements IBallCollisionModel
{
	
	
	/**
	 * @param state
	 * @param dt
	 * @param context
	 * @return
	 */
	@Override
	public ICollisionState processCollision(final IState state, final double dt,
			final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler();
		
		addCollisionObjects(ch, context);
		
		return ch.process(state, dt);
	}
	
	
	private void addCollisionObjects(final CollisionHandler ch, final MotionContext context)
	{
		ILine goalLine1 = Line.fromPoints(
				Geometry.getGoalOur().getLeftPost()
						.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() + Geometry.getBallRadius(), 0)),
				Geometry.getGoalOur().getRightPost()
						.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() + Geometry.getBallRadius(), 0)));
		ch.addObject(new SingleSidedLineCollision(goalLine1, AVector3.ZERO_VECTOR, AVector2.X_AXIS));
		ILine goalLine2 = Line.fromPoints(
				Geometry.getGoalTheir().getLeftPost()
						.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth() - Geometry.getBallRadius(), 0)),
				Geometry.getGoalTheir().getRightPost()
						.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth() - Geometry.getBallRadius(), 0)));
		ch.addObject(new SingleSidedLineCollision(goalLine2, AVector3.ZERO_VECTOR, AVector2.X_AXIS.multiplyNew(-1)));
		
		context.getBots().values().stream()
				.map(info -> new BotCollision(
						info.getPos(),
						info.getVel(),
						info.getCenter2DribblerDist(),
						info.getDribbleRpm() > 0,
						info.getKickSpeed(),
						info.isChip()))
				.forEach(ch::addObject);
	}
}
