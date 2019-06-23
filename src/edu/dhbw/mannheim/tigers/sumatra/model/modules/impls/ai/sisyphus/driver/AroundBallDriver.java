/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableTrajectory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.Triangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static float								driveSpeed4BallSpeed		= 0.8f;
	
	@Configurable(comment = "Angle [rad] of the triangle")
	private static float								triangleAngle				= 0.3f;
	
	@Configurable(comment = "Dist [mm] that the bot should always keep to ball (plus radius)")
	private static float								securityDistance2Ball	= 100;
	
	
	private final DynamicPosition					receiver;
	private float										distBehindBall				= 50;
	
	@Configurable
	private static float								minFutureTime				= 0.7f;
	
	
	/**
	 * @param receiver
	 */
	public AroundBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	private float estimatedDistance2Destination(final TrackedTigerBot bot, final IVector2 ballPos)
	{
		// final IVector2 dest;
		// switch (state)
		// {
		// case TURN:
		// dest = ballPos.addNew(direction.scaleToNew(-400));
		// break;
		// case KICK:
		
		
		IVector2 lp = GeoMath.leadPointOnLine(bot.getPos(), ballPos, receiver);
		
		float angle = AngleMath.getShortestRotation(receiver.subtractNew(ballPos).getAngle(),
				bot.getPos().subtractNew(ballPos).getAngle());
		
		float distBallLp = GeoMath.distancePP(ballPos, lp);
		if (Math.abs(angle) > AngleMath.PI_HALF)
		{
			distBallLp *= -1;
		}
		
		return GeoMath.distancePP(lp, bot.getPos()) + distBallLp;
		// return GeoMath.distancePP(ballPos, bot.getPos()) - AIConfig.getGeometry().getBotRadius();
		// dest = ballPos.addNew(direction.scaleToNew(-getBot().getCenter2DribblerDist()));
		// default:
		// throw new IllegalStateException();
		// }
		// return GeoMath.distancePP(bot.getPos(), dest);
	}
	
	
	private IVector2 estimateFutureBallPos(final WorldFrame wFrame, final float estimatedDistance)
	{
		float driveSpeed = driveSpeed4BallSpeed;
		final float time2Ball = Math.max((estimatedDistance / (driveSpeed * 1000)), 0.1f);
		
		final IVector2 futureBallPos = wFrame.getBall().getPosByTime(time2Ball);
		return futureBallPos;
	}
	
	
	private IVector2 estimateFutureBallPosSpline(final WorldFrame wFrame, final TrackedTigerBot bot,
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
		float rotationShoot2Bot = AngleMath.difference(shootDir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(rotationShoot2Bot) < AngleMath.PI_HALF)
		{
			IVector2 supportPoint = ballPos.addNew(shootDir
					.turnNew((AngleMath.PI - 0.7f) * -Math.signum(rotationShoot2Bot)).scaleTo(300));
			path.add(supportPoint.multiplyNew(1e-3f));
		}
		
		path.add(ballPos.multiplyNew(1e-3f));
		
		SplinePair3D spline3d = stg.create(path, botVel, endVel, 0, 0, 0, 0);
		shapes.add(new DrawableTrajectory(spline3d));
		
		final IVector2 futureBallPos = wFrame.getBall().getPosByTime(spline3d.getTotalTime() - 1.0f);
		return futureBallPos;
	}
	
	
	private IVector2 adaptTriangleDir(final WorldFrame wFrame, IVector2 dir)
	{
		if (wFrame.getBall().getVel().getLength2() > 0.3f)
		{
			float angleDirBallVelDiff = AngleMath.difference(wFrame.getBall()
					.getVel().getAngle(), dir.getAngle() + AngleMath.PI);
			// ball not rolling towards target?
			if (Math.abs(angleDirBallVelDiff) < AngleMath.deg2rad(90))
			{
				float ballMaxVel = 1.0f;
				float relChange = Math.min(1, wFrame.getBall().getVel().getLength2() / ballMaxVel);
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
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		final List<IDrawableShape> shapes = new ArrayList<>();
		final IVector2 ballPos = wFrame.getBall().getPos();
		final IVector2 ballVel = wFrame.getBall().getVel();
		
		final IVector2 destination;
		final float orientation;
		
		// final IVector2 futureBallPos = estimateFutureBallPosIterative();
		boolean useSpline = true;
		final IVector2 futureBallPos;
		if (!useSpline)
		{
			futureBallPos = estimateFutureBallPosSpline(wFrame, bot, shapes);
		}
		else
		{
			futureBallPos = estimateFutureBallPos(wFrame, estimatedDistance2Destination(bot, ballPos));
		}
		final float secDist2Ball = securityDistance2Ball + AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getGeometry().getBallRadius();
		
		IVector2 dir = adaptTriangleDir(wFrame, receiver.subtractNew(futureBallPos));
		// decrease triangle angle if ball is moving [0.08..triangleAngle]
		float maxVel = 1.5f;
		float behindBallAngle = Math.max(triangleAngle / 2, (1 - Math.min(1, ballVel.getLength2() / maxVel))
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
				+ AIConfig.getGeometry().getBallRadius() + bot.getBot().getCenter2DribblerDist()));
		
		setDone(triangle.isPointInShape(bot.getPos()) && ((bot.getVel().getLength2() < 0.3f)
				|| (Math.abs(AngleMath.getShortestRotation(bot.getVel().getAngle(), receiver.subtractNew(futureBallPos)
				.getAngle())) < 0.1f)));
		
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
			
			List<IVector2> interSec = GeoMath.tangentialIntersections(secCircle, bot.getPos());
			IVector2 refPoint = futureBallPos.addNew(dir.scaleToNew(-secDist2Ball));
			IVector2 supportPoint = null;
			float distAroundSecCircle = 100;
			
			// find closer support point
			float dist = Float.MAX_VALUE;
			for (IVector2 p : interSec)
			{
				float pDist = GeoMath.distancePP(refPoint, p);
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
			float dist2SupportPoint = GeoMath.distancePP(supportPoint, bot.getPos());
			float dist2FinalSupportPoint = GeoMath.distancePP(finalSupportPoint, bot.getPos());
			if (dist2FinalSupportPoint < dist2SupportPoint)
			{
				destination = finalSupportPoint;
			} else
			{
				destination = supportPoint;
			}
		}
		
		
		shapes.add(new DrawablePoint(futureBallPos, Color.orange));
		if (ballVel.getLength2() > 0.1f)
		{
			shapes.add(new DrawableLine(new Line(ballPos, ballVel.scaleToNew(1000)), Color.blue));
		}
		shapes.add(new DrawableLine(new Line(ballPos, receiver.subtractNew(ballPos).scaleToNew(5000)), Color.cyan));
		shapes.add(new DrawableLine(new Line(bot.getPos(), dir.scaleToNew(1000)), Color.orange));
		shapes.add(new DrawableLine(new Line(bot.getPos(), receiver.subtractNew(ballPos).scaleToNew(1000)), Color.magenta));
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
				new Color(0f, 1f, 1f, 0.4f));
		shapes.add(dTriangle);
		shapes.add(dTriangle2);
		
		shapes.add(secCircle);
		
		setShapesDebug(shapes);
		setDestination(destination);
		setOrientation(orientation);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.AROUND_BALL;
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
