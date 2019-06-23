/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableTrajectory2D;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.triangle.Triangle;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.SplineTrajectoryGenerator;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Drive securely and fast around the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AroundBallDriver extends PositionDriver implements IKickPathDriver
{
	
	
	@SuppressWarnings("unused")
	private static final Logger					log							= Logger.getLogger(AroundBallDriver.class
			.getName());
	
	private final SplineTrajectoryGenerator	stg							= new SplineTrajectoryGenerator();
	
	@Configurable(comment = "Vel [m/s] that is assumed for driving to ball when calculating future ball pos")
	private static double							driveSpeed4BallSpeed		= 0.8;
	
	@Configurable(comment = "Angle [rad] of the triangle")
	private static double							triangleAngle				= 0.3;
	
	@Configurable(comment = "Dist [mm] that the bot should always keep to ball (plus radius)")
	private static double							securityDistance2Ball	= 100;
	
	
	private final DynamicPosition					receiver;
	private final double								distBehindBall				= 50;
	
	@Configurable
	private static double							minFutureTime				= 0.7;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", AroundBallDriver.class);
	}
	
	
	/**
	 * @param receiver
	 */
	public AroundBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	private double estimatedDistance2Destination(final ITrackedBot bot, final IVector2 ballPos)
	{
		// final IVector2 dest;
		// switch (state)
		// {
		// case TURN:
		// dest = ballPos.addNew(direction.scaleToNew(-400));
		// break;
		// case KICK:
		
		
		IVector2 lp = GeoMath.leadPointOnLine(bot.getPos(), ballPos, receiver);
		
		double angle = AngleMath.getShortestRotation(receiver.subtractNew(ballPos).getAngle(),
				bot.getPos().subtractNew(ballPos).getAngle());
		
		double distBallLp = GeoMath.distancePP(ballPos, lp);
		if (Math.abs(angle) > AngleMath.PI_HALF)
		{
			distBallLp *= -1;
		}
		
		return GeoMath.distancePP(lp, bot.getPos()) + distBallLp;
		// return GeoMath.distancePP(ballPos, bot.getPos()) - Geometry.getBotRadius();
		// dest = ballPos.addNew(direction.scaleToNew(-getBot().getCenter2DribblerDist()));
		// default:
		// throw new IllegalStateException();
		// }
		// return GeoMath.distancePP(bot.getPos(), dest);
	}
	
	
	private IVector2 estimateFutureBallPos(final WorldFrame wFrame, final double estimatedDistance)
	{
		double driveSpeed = driveSpeed4BallSpeed;
		final double time2Ball = Math.max((estimatedDistance / (driveSpeed * 1000)), 0.1);
		
		final IVector2 futureBallPos = wFrame.getBall().getPosByTime(time2Ball);
		return futureBallPos;
	}
	
	
	private IVector2 estimateFutureBallPosSpline(final WorldFrame wFrame, final ITrackedBot bot,
			final List<IDrawableShape> shapes)
	{
		IVector2 ballPos = wFrame.getBall().getPosByTime(0.0f);
		IVector2 shootDir = receiver.subtractNew(ballPos);
		IVector2 botVel = new Vector2(0, 0); // bot.getVel();
		IVector2 botPos = bot.getPos();
		
		IVector2 endVel = shootDir.scaleToNew(0.3f);
		
		List<IVector2> path = new ArrayList<>(3);
		path.add(botPos.multiplyNew(1e-3f));
		
		IVector2 ball2Bot = botPos.subtractNew(ballPos);
		double rotationShoot2Bot = AngleMath.difference(shootDir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(rotationShoot2Bot) < AngleMath.PI_HALF)
		{
			IVector2 supportPoint = ballPos.addNew(shootDir
					.turnNew((AngleMath.PI - 0.7) * -Math.signum(rotationShoot2Bot)).scaleTo(300));
			path.add(supportPoint.multiplyNew(1e-3f));
		}
		
		path.add(ballPos.multiplyNew(1e-3f));
		
		ITrajectory<IVector2> spline3d = stg.create2d(path, botVel, endVel);
		shapes.add(new DrawableTrajectory2D(spline3d));
		
		final IVector2 futureBallPos = wFrame.getBall().getPosByTime(spline3d.getTotalTime() - 1.0);
		return futureBallPos;
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
	
	
	/**
	 * @param bot
	 * @param wFrame
	 */
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		final List<IDrawableShape> shapes = new ArrayList<>();
		final IVector2 ballPos = wFrame.getBall().getPos();
		final IVector2 ballVel = wFrame.getBall().getVel();
		
		final IVector2 destination;
		final double orientation;
		
		// final IVector2 futureBallPos = estimateFutureBallPosIterative();
		boolean useSpline = false;
		final IVector2 futureBallPos;
		if (useSpline)
		{
			futureBallPos = estimateFutureBallPosSpline(wFrame, bot, shapes);
		} else
		{
			futureBallPos = estimateFutureBallPos(wFrame, estimatedDistance2Destination(bot, ballPos));
		}
		final double secDist2Ball = securityDistance2Ball + Geometry.getBotRadius()
				+ Geometry.getBallRadius();
		
		IVector2 dir = adaptTriangleDir(wFrame, receiver.subtractNew(futureBallPos));
		// decrease triangle angle if ball is moving [0.08..triangleAngle]
		double maxVel = 1.5;
		double behindBallAngle = Math.max(triangleAngle / 2, (1 - Math.min(1, ballVel.getLength2() / maxVel))
				* triangleAngle);
		Triangle triangle = new Triangle(
				futureBallPos.addNew(dir.scaleToNew(100)),
				futureBallPos.addNew(dir.scaleToNew(-50000).turnNew(behindBallAngle)),
				futureBallPos.addNew(dir.scaleToNew(-50000).turnNew(-behindBallAngle)));
		
		DrawableCircle secCircle = new DrawableCircle(futureBallPos, secDist2Ball, Color.magenta);
		
		// TODO more intelligent orientation
		orientation = receiver.subtractNew(ballPos).getAngle();
		
		// setDone(triangle.isPointInShape(bot.getPos()));
		
		IVector2 finalDest = GeoMath.stepAlongLine(futureBallPos, receiver, -(distBehindBall
				+ Geometry.getBallRadius() + bot.getBot().getCenter2DribblerDist()));
		
		setDone(triangle.isPointInShape(bot.getPos()) && ((bot.getVel().getLength2() < 0.3)
				|| (Math.abs(AngleMath.getShortestRotation(bot.getVel().getAngle(), receiver.subtractNew(futureBallPos)
						.getAngle())) < 0.1)));
		
		if (secCircle.isPointInShape(bot.getPos()) && triangle.isPointInShape(bot.getPos()))
		{
			destination = finalDest;
		} else if (secCircle.isPointInShape(bot.getPos()))
		{
			// too near to ball, have to get away
			
			IVector2 nearestOutside = secCircle.nearestPointOutside(bot.getPos());
			IVector2 ball2Bot = bot.getPos().subtractNew(ballPos);
			// are we in front of the ball?
			if (Math.abs(AngleMath.getShortestRotation(ball2Bot.getAngle(), dir.getAngle())) < (AngleMath.PI_HALF))
			{
				// in front of ball! move sidewards to get out of secCircle, but also not away from ball
				IVector2 start = secCircle.center().addNew(dir.scaleToNew(secDist2Ball));
				if (start.equals(nearestOutside))
				{
					// unlikely case that bot is perfectly in front of the ball... :D
					nearestOutside = nearestOutside.addNew(dir.turnNew(AngleMath.PI_HALF).scaleTo(100));
				}
				destination = GeoMath.stepAlongLine(start, nearestOutside, 500);
			} else
			{
				// we are next to or almost behind ball, move backwards.
				destination = secCircle.center().addNew(dir.scaleToNew(-1000));
			}
		} else
		{
			// find a good way around the ball
			
			List<IVector2> interSec = secCircle.tangentialIntersections(bot.getPos());
			IVector2 refPoint = futureBallPos.addNew(dir.scaleToNew(-secDist2Ball));
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
					supportPoint = GeoMath.stepAlongLine(p, futureBallPos, -distAroundSecCircle);
				}
				shapes.add(new DrawablePoint(p, Color.magenta));
			}
			
			IVector2 finalSupportPoint = futureBallPos.addNew(dir.scaleToNew(-distAroundSecCircle - secDist2Ball));
			double dist2SupportPoint = GeoMath.distancePP(supportPoint, bot.getPos());
			double dist2FinalSupportPoint = GeoMath.distancePP(finalSupportPoint, bot.getPos());
			if (dist2FinalSupportPoint < dist2SupportPoint)
			{
				destination = finalSupportPoint;
			} else
			{
				destination = supportPoint;
			}
		}
		
		
		shapes.add(new DrawablePoint(futureBallPos, Color.orange));
		if (ballVel.getLength2() > 0.1)
		{
			shapes.add(new DrawableLine(new Line(ballPos, ballVel.scaleToNew(1000)), Color.blue));
		}
		shapes.add(new DrawableLine(new Line(ballPos, receiver.subtractNew(ballPos).scaleToNew(5000)), Color.cyan));
		shapes.add(new DrawableLine(new Line(bot.getPos(), dir.scaleToNew(1000)), Color.orange));
		shapes.add(
				new DrawableLine(new Line(bot.getPos(), receiver.subtractNew(ballPos).scaleToNew(1000)), Color.magenta));
		shapes.add(new DrawableLine(new Line(bot.getPos(), new Vector2(bot.getAngle()).scaleTo(5000)), Color.BLACK));
		
		DrawablePoint dpState = new DrawablePoint(bot.getPos().addNew(new Vector2(-150, 100)));
		dpState.setText("AroundBall");
		shapes.add(dpState);
		
		DrawableTriangle dTriangle = new DrawableTriangle(
				futureBallPos.addNew(dir.scaleToNew(50)),
				futureBallPos.addNew(dir.scaleToNew(-1000).turnNew(behindBallAngle)),
				futureBallPos.addNew(dir.scaleToNew(-1000).turnNew(-behindBallAngle)),
				new Color(0f, 1f, 1f, 0.2f));
		DrawableTriangle dTriangle2 = new DrawableTriangle(
				futureBallPos.addNew(dir.scaleToNew(50)),
				futureBallPos.addNew(dir.scaleToNew(-1000).turnNew(behindBallAngle)),
				futureBallPos.addNew(dir.scaleToNew(-1000).turnNew(-behindBallAngle)),
				new Color(0f, 1, 1, 0.4f));
		shapes.add(dTriangle);
		shapes.add(dTriangle2);
		
		shapes.add(secCircle);
		
		setShapes(EShapesLayer.KICK_SKILL, shapes);
		setDestination(destination);
		setOrientation(orientation);
		super.update(bot, aBot, wFrame);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.AROUND_BALL;
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
