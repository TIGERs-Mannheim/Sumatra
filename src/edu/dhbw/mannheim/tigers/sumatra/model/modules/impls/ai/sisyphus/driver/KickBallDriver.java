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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.Triangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static float				triangleAngleOuter		= 0.7f;
	
	@Configurable
	private static float				secDist2Ball				= 200;
	
	@Configurable
	private static float				futureBallLookahead		= 0.2f;
	
	@Configurable
	private static float				posTolerance				= 50;
	
	@Configurable
	private static float				ballVelTolerance			= 0.5f;
	
	private List<IDrawableShape>	shapes						= new ArrayList<>();
	private final DynamicPosition	receiver;
	
	
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
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
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
				if (Math.abs(AngleMath.getShortestRotation(destDir.getAngle(), dir.getAngle())) > (AngleMath.PI_HALF + AngleMath.PI_QUART))
				{
					destDir = destDir.turnNew(AngleMath.PI);
				}
				// GeoMath.stepAlongLine(botDestForHit, bot.getPos(), -distBehindBallHitTarget);
				IVector2 dest = botDestForHit.subtractNew(destDir.scaleToNew(distBehindBallHitTarget));
				
				SplineGenerator sg = new SplineGenerator();
				List<IVector2> nodes = new ArrayList<>(2);
				nodes.add(dest);
				SplinePair3D spline = sg.createSpline(bot, nodes, direction.getAngle(), 2);
				IVector3 splineVel = spline.getVelocityByTime(0.1f);
				destination = bot.getPos().addNew(splineVel.getXYVector().scaleTo(100));
				
				// float dist = Math.min(300, GeoMath.distancePP(dest, bot.getPos()));
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
		
		
		if (ballVel.getLength2() > 0.1f)
		{
			shapes.add(new DrawableLine(new Line(ballPos, ballVel.scaleToNew(1000)), Color.blue));
		}
		shapes.add(new DrawableLine(new Line(ballPos, receiver.subtractNew(ballPos).scaleToNew(5000)), Color.cyan));
		shapes.add(new DrawableLine(new Line(bot.getPos(), dir.scaleToNew(1000)), Color.orange));
		shapes.add(new DrawableLine(new Line(bot.getPos(), receiver.subtractNew(ballPos).scaleToNew(1000)), Color.magenta));
		shapes.add(new DrawableLine(new Line(bot.getPos(), new Vector2(bot.getAngle()).scaleTo(5000)), Color.BLACK));
		
		DrawablePoint dpState = new DrawablePoint(bot.getPos().addNew(new Vector2(-150, 100)));
		dpState.setText("KickBall");
		shapes.add(dpState);
		
		DrawableTriangle dTriangleOuter = new DrawableTriangle(
				ballPos.addNew(dir.scaleToNew(50)),
				ballPos.addNew(dir.scaleToNew(-1000).turnNew(triangleAngleOuter)),
				ballPos.addNew(dir.scaleToNew(-1000).turnNew(-triangleAngleOuter)),
				new Color(0f, 1f, 1f, 0.2f));
		dTriangleOuter.setFill(true);
		shapes.add(dTriangleOuter);
		this.shapes = shapes;
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePathDebug(bot, shapes);
		shapes.addAll(this.shapes);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_BALL;
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
		return true;
	}
}
