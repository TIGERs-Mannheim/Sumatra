/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.RectangleObstacle;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CatchBallDriver extends PositionDriver implements IKickPathDriver
{
	private static double			secDist	= 100;
	
	private final DynamicPosition	receiver;
	
	
	/**
	 * @param receiver
	 */
	public CatchBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		super.update(bot, aBot, wFrame);
		
		generateTraj(bot, wFrame);
	}
	
	
	private void generateTraj(final ITrackedBot bot, final WorldFrame wFrame)
	{
		Vector2 dest;
		double tEnd = wFrame.getBall().getTimeByVel(0);
		double t = 0;
		do
		{
			IVector2 ballPos = wFrame.getBall().getPosByTime(t);
			dest = ballPos.addNew(wFrame.getBall().getVel().scaleToNew(secDist));
			dest.add(dest.subtractNew(receiver).scaleTo(bot.getCenter2DribblerDist()));
			BangBangTrajectory2D traj = new TrajectoryGenerator().generatePositionTrajectory(bot, dest);
			
			RectangleObstacle obs = genObstacle(wFrame.getBall().getPos(), dest);
			
			if (traj.getTotalTime() > (t - 0.1))
			{
				t += 0.1;
			} else if (!hasCollision(traj, obs))
			{
				setDestination(dest);
				setOrientation(receiver.subtractNew(dest).getAngle());
				return;
			}
			
		} while (t < tEnd);
		
		// find a good way around the ball
		
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 dir = adaptTriangleDir(wFrame, receiver.subtractNew(ballPos));
		DrawableCircle secCircle = new DrawableCircle(ballPos, secDist, Color.magenta);
		List<IVector2> interSec = secCircle.tangentialIntersections(bot.getPos());
		IVector2 refPoint = ballPos.addNew(dir.scaleToNew(-secDist));
		IVector2 supportPoint = null;
		double distAroundSecCircle = 100;
		
		// find closer support point
		double dist = Double.MAX_VALUE;
		for (IVector2 p : interSec)
		{
			double pDist = GeoMath.distancePP(refPoint, p);
			if (pDist < dist)
			{
				dist = pDist;
				// set the point outside of secCircle to avoid that we get into the secCircle, which may slow down
				// movement.
				supportPoint = GeoMath.stepAlongLine(p, ballPos, -distAroundSecCircle);
			}
			// shapes.add(new DrawablePoint(p, Color.magenta));
		}
		
		IVector2 finalSupportPoint = ballPos.addNew(dir.scaleToNew(-distAroundSecCircle - secDist));
		double dist2SupportPoint = GeoMath.distancePP(supportPoint, bot.getPos());
		double dist2FinalSupportPoint = GeoMath.distancePP(finalSupportPoint, bot.getPos());
		if (dist2FinalSupportPoint < dist2SupportPoint)
		{
			setDestination(finalSupportPoint);
		} else
		{
			setDestination(supportPoint);
		}
		setOrientation(receiver.subtractNew(getDestination()).getAngle());
	}
	
	
	private IVector2 adaptTriangleDir(final WorldFrame wFrame, IVector2 dir)
	{
		if (wFrame.getBall().getVel().getLength2() > 0.3)
		{
			double angleDirBallVelDiff = AngleMath.difference(wFrame.getBall()
					.getVel().getAngle(), dir.getAngle() + AngleMath.PI);
			// ball not rolling towards target?
			if (Math.abs(angleDirBallVelDiff) < AngleMath.deg2rad(90))
			{
				double ballMaxVel = 1.0;
				double relChange = Math.min(1, wFrame.getBall().getVel().getLength2() / ballMaxVel);
				dir = dir.turnNew(relChange * angleDirBallVelDiff);
			}
		}
		return dir;
	}
	
	
	private RectangleObstacle genObstacle(final IVector2 center1, final IVector2 center2)
	{
		IVector2 dir = center1.subtractNew(center2);
		IVector2 orthDir = dir.getNormalVector().scaleTo(secDist + Geometry.getBotRadius());
		IVector2 p1 = center1.addNew(orthDir);
		IVector2 p2 = center2.subtractNew(orthDir);
		return new RectangleObstacle(new Rectangle(p1, p2));
	}
	
	
	private boolean hasCollision(final ITrajectory<IVector2> traj, final IObstacle obstacle)
	{
		for (double t = 0; t < traj.getTotalTime(); t += 0.1)
		{
			IVector2 pos = traj.getPositionMM(t);
			if (obstacle.isPointCollidingWithObstacle(pos, t))
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.CATCH_BALL;
	}
	
	
	@Override
	public boolean isEnableDribbler()
	{
		return false;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
