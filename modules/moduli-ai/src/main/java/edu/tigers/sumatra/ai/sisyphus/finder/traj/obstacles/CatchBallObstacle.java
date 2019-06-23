/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
@SuppressWarnings("unused")
public class CatchBallObstacle implements IObstacle, IDrawableShape
{
	
	private final TrackedBall		ball;
	private final double				toBallDist;
	private final DynamicPosition	receiver;
	private final double				shootSpeed;
											
	@Configurable
	private static double			defRadius		= 200;
	@Configurable
	private static double			defArcAngle		= 1.2;
	@Configurable
	private static double			maxPredTimeBot	= 1;
	@Configurable
	private static double			minVel			= 0.5;
																
	private double						radius			= defRadius;
	private double						arcAngle			= defArcAngle;
																
																
	static
	{
		ConfigRegistration.registerClass("sisyphus", CatchBallObstacle.class);
	}
	
	
	@SuppressWarnings("unused")
	private CatchBallObstacle()
	{
		ball = null;
		toBallDist = 0;
		receiver = null;
		shootSpeed = 0;
	}
	
	
	/**
	 * @param ball
	 * @param toBallDist
	 * @param receiver
	 * @param shootSpeed
	 */
	public CatchBallObstacle(final TrackedBall ball, final double toBallDist,
			final DynamicPosition receiver, final double shootSpeed)
	{
		this.ball = ball;
		this.toBallDist = toBallDist;
		this.receiver = receiver;
		this.shootSpeed = shootSpeed;
	}
	
	
	private DrawableArc createArc(final double t)
	{
		IVector2 recv = receiver.getPosAt(Math.min(t, maxPredTimeBot));
		IVector2 pos;
		IVector2 vBallAtDest;
		if (ball.getVel().getLength2() >= minVel)
		{
			pos = ball.getPosByTime(t);
			vBallAtDest = ball.getVel().scaleToNew(ball.getVelByPos(pos));
		} else
		{
			pos = ball.getPos();
			vBallAtDest = AVector2.ZERO_VECTOR;
		}
		
		double approxOrient = 0;
		if (!pos.equals(recv))
		{
			approxOrient = recv.subtractNew(pos).getAngle();
		}
		// double targetAngle = RedirectParamCalc.forBot(bot).calcRedirectOrientation(pos, approxOrient, vBallAtDest,
		// recv, shootSpeed);
		double targetAngle = approxOrient;
		IVector2 dir = new Vector2(targetAngle);
		
		double startAngle = (targetAngle + AngleMath.PI) + (arcAngle / 2.0);
		Arc arc = new Arc(pos.addNew(dir.scaleToNew(-toBallDist)), radius, startAngle, AngleMath.PI_TWO - arcAngle);
		return new DrawableArc(arc, Color.red);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return createArc(t).isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return createArc(t).nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		for (double t = 0; t <= ball.getTimeByVel(0); t += 0.2)
		{
			IVector2 posStill = ball.getPosByVel(t);
			
			for (int i = 0; i < 5; i++)
			{
				IVector2 mu = GeoMath.stepAlongLine(posStill, receiver, -radius * 3);
				IVector2 sample = mu
						.addNew(new Vector2(rnd.nextDouble() * AngleMath.PI_TWO).scaleTo(rnd.nextGaussian() * 2000));
				subPoints.add(sample);
			}
		}
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		Font font = new Font("", Font.PLAIN, 5);
		g.setFont(font);
		
		double tBallStop = ball.getTimeByVel(0);
		double tStep = Math.max(0.05f, tBallStop / 10.0);
		for (double t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 pos = ball.getPosByTime(t);
			IVector2 dir = receiver.getPosAt(Math.min(t, maxPredTimeBot)).subtractNew(pos);
			new DrawableLine(new Line(pos, dir)).paintShape(g, tool, invert);
			
			DrawableArc arc = createArc(t);
			arc.paintShape(g, tool, invert);
			
			final IVector2 center = tool.transformToGuiCoordinates(pos, invert).addNew(new Vector2(0, 00));
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawString(String.format("%.1f", t), (float) (center.x() + 20), (float) (center.y() + 2));
		}
	}
	
	
	/**
	 * @return the radius
	 */
	public final double getRadius()
	{
		return radius;
	}
	
	
	/**
	 * @param radius the radius to set
	 */
	public final void setRadius(final double radius)
	{
		this.radius = radius;
	}
	
	
	/**
	 * @return the arcAngle
	 */
	public final double getArcAngle()
	{
		return arcAngle;
	}
	
	
	/**
	 * @param arcAngle the arcAngle to set
	 */
	public final void setArcAngle(final double arcAngle)
	{
		this.arcAngle = arcAngle;
	}
	
}
