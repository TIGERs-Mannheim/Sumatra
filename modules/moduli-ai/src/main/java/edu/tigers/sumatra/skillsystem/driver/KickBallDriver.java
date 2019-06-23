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
import edu.tigers.sumatra.ai.sisyphus.spline.SplineGenerator;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.triangle.Triangle;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Kick the ball. This driver assumes that the bot is already behind the ball!
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallDriver extends PositionDriver implements IKickPathDriver
{
	
	
	@SuppressWarnings("unused")
	private static final Logger	log							= Logger.getLogger(KickBallDriver.class.getName());
	
	@Configurable(comment = "Dist [mm] that the destination is set behind ball hit point")
	private static int				distBehindBallHitTarget	= 50;
	
	@Configurable(comment = "Angle [rad] of the triangle")
	private static double			triangleAngleOuter		= 0.7;
	
	@Configurable
	private static double			secDist2Ball				= 200;
	
	@Configurable
	private static double			futureBallLookahead		= 0.2;
	
	@Configurable
	private static double			posTolerance				= 50;
	
	@Configurable
	private static double			ballVelTolerance			= 0.5;
	
	private final DynamicPosition	receiver;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", KickBallDriver.class);
	}
	
	
	/**
	 * @param receiver
	 */
	public KickBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
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
		
		IVector2 dir = receiver.subtractNew(ballPos);
		Triangle triangleOuter = new Triangle(
				ballPos.addNew(dir.scaleToNew(50)),
				ballPos.addNew(dir.scaleToNew(-50000).turnNew(triangleAngleOuter)),
				ballPos.addNew(dir.scaleToNew(-50000).turnNew(-triangleAngleOuter)));
		
		if (!triangleOuter.isPointInShape(bot.getPos()))
		{
			setDone(true);
		}
		
		final IVector2 destination;
		IVector2 futureBallPos = wFrame.getBall().getPosByTime(futureBallLookahead);
		IVector2 direction = receiver.subtractNew(ballPos);
		if (GeoMath.distancePP(bot.getPos(), futureBallPos) > 200)
		{
			destination = futureBallPos.addNew(
					dir.scaleToNew(-(bot.getBot().getCenter2DribblerDist() - distBehindBallHitTarget)));
		} else
		{
			IVector2 lp = GeoMath.leadPointOnLine(bot.getPos(), futureBallPos, receiver);
			// TODO
			if ((GeoMath.distancePP(lp, bot.getPos()) < posTolerance) || (bot.getVel().getLength2() < ballVelTolerance))
			{
				IVector2 botDestForHit = futureBallPos.addNew(direction
						.scaleToNew(-bot.getBot().getCenter2DribblerDist()));
				
				IVector2 destDir = botDestForHit.subtractNew(bot.getPos()).normalize();
				if (Math.abs(AngleMath.getShortestRotation(destDir.getAngle(), dir.getAngle())) > (AngleMath.PI_HALF
						+ AngleMath.PI_QUART))
				{
					destDir = destDir.turnNew(AngleMath.PI);
				}
				// GeoMath.stepAlongLine(botDestForHit, bot.getPos(), -distBehindBallHitTarget);
				IVector2 dest = botDestForHit.subtractNew(destDir.scaleToNew(distBehindBallHitTarget));
				
				SplineGenerator sg = new SplineGenerator();
				List<IVector2> nodes = new ArrayList<>(2);
				nodes.add(dest);
				ITrajectory<IVector3> spline = sg.createSpline(bot, nodes, direction.getAngle(), 2);
				IVector3 splineVel = spline.getVelocity(0.1f);
				destination = bot.getPos().addNew(splineVel.getXYVector().scaleToNew(100));
				
				// double dist = Math.min(300, GeoMath.distancePP(dest, bot.getPos()));
				// dist = 150;
				// destination = GeoMath.stepAlongLine(bot.getPos(), dest,
				// dist);
			} else
			{
				IVector2 dest = lp.addNew(direction.scaleToNew(100));
				if (GeoMath.distancePP(dest, ballPos) < secDist2Ball)
				{
					destination = GeoMath.stepAlongLine(ballPos, dest, secDist2Ball);
				} else
				{
					destination = dest;
				}
			}
		}
		// if (GeoMath.distancePP(bot.getPos(), futureBallPos) <= getBot().getCenter2DribblerDist())
		// {
		// // future ball is inside bot. Normal calculation will not work properly,
		// // so just send the bot with its dribbler onto the ball.
		// destination = futureBallPos.addNew(direction.scaleToNew(-getBot().getCenter2DribblerDist()));
		// } else
		// {
		// IVector2 botDestForHit = futureBallPos.addNew(direction
		// .scaleToNew(-getBot().getCenter2DribblerDist()));
		// destination = GeoMath.stepAlongLine(botDestForHit, bot.getPos(), -distBehindBallHitTarget);
		// }
		setDestination(destination);
		setOrientation(direction.getAngle());
		
		
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
		dpState.setText("KickBall");
		shapes.add(dpState);
		
		DrawableTriangle dTriangleOuter = new DrawableTriangle(
				ballPos.addNew(dir.scaleToNew(50)),
				ballPos.addNew(dir.scaleToNew(-1000).turnNew(triangleAngleOuter)),
				ballPos.addNew(dir.scaleToNew(-1000).turnNew(-triangleAngleOuter)),
				new Color(0f, 1, 1, 0.2f));
		dTriangleOuter.setFill(true);
		shapes.add(dTriangleOuter);
		setShapes(EShapesLayer.KICK_SKILL, shapes);
		
		super.update(bot, aBot, wFrame);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_BALL;
	}
	
	
	@Override
	public boolean isEnableDribbler()
	{
		return false;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return true;
	}
}
