/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.g3force.configurable.ConfigRegistration;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.ellipse.Ellipse;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class KickBallObstacleV2 implements IObstacle, IDrawableShape
{
	
	private final TrackedBall		ball;
	private final IVector2			openDir;
	private final List<IObstacle>	shapes;
	
	private Color						color					= Color.red;
	
	private double						freeFront			= 80;
	private double						freeSide				= 60;
	private double						freeBack				= 150;
	private double						secRadius			= 200;
	private double						minCircleRadius	= 40;
	
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", KickBallObstacleV2.class);
	}
	
	
	@SuppressWarnings("unused")
	private KickBallObstacleV2()
	{
		ball = null;
		openDir = null;
		shapes = null;
	}
	
	
	/**
	 * @param ball
	 * @param openDir
	 * @param bot
	 */
	public KickBallObstacleV2(final TrackedBall ball, final IVector2 openDir, final ITrackedBot bot)
	{
		this.ball = ball;
		this.openDir = openDir;
		
		shapes = getObstacle(0);
	}
	
	
	private List<IObstacle> getObstacle(final double t)
	{
		List<IObstacle> shapes = new ArrayList<>();
		
		IVector2 center = ball.getPosByTime(t);
		double ellXradius = (secRadius - freeFront) / 2;
		Ellipse frontEll = new Ellipse(center.addNew(openDir.scaleToNew(-(freeFront + ellXradius))), ellXradius,
				secRadius, openDir.getAngle() + AngleMath.PI);
		shapes.add(new EllipseObstacle(frontEll));
		
		double maxCircleRadius = (secRadius - freeSide) / 2;
		IVector2 circleCenterL = center.addNew(openDir.scaleToNew(-((freeFront + ellXradius) - maxCircleRadius)))
				.add(openDir.getNormalVector().scaleTo(secRadius - maxCircleRadius));
		IVector2 circleCenterS = center.addNew(openDir.scaleToNew(freeBack - minCircleRadius))
				.add(openDir.getNormalVector().scaleTo(secRadius - maxCircleRadius));
		
		double dist = GeoMath.distancePP(circleCenterL, circleCenterS);
		int numCircles = 10;
		for (int i = 0; i < numCircles; i++)
		{
			IVector2 c1 = GeoMath.stepAlongLine(circleCenterL, circleCenterS, (dist * i) / (numCircles - 1));
			IVector2 c2 = c1.addNew(openDir.getNormalVector().scaleTo(-(2 * (freeSide + maxCircleRadius))));
			double r = minCircleRadius + (((maxCircleRadius - minCircleRadius) * (numCircles - i)) / (numCircles - 1));
			shapes.add(new CircleObstacle(new Circle(c1, r)));
			shapes.add(new CircleObstacle(new Circle(c2, r)));
		}
		
		return shapes;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		// List<IObstacle> shapes = getObstacle(Math.min(0.0, t));
		for (IObstacle obs : shapes)
		{
			if (obs.isPointCollidingWithObstacle(point, t))
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		IVector2 ballPos = ball.getPosByTime(t);
		double angle = point.subtractNew(ballPos).getAngle();
		
		if ((Math.abs(AngleMath.getShortestRotation(angle, openDir.getAngle())) > AngleMath.PI_HALF))
		{
			if ((Math.abs(AngleMath.getShortestRotation(angle, openDir.getNormalVector().getAngle())) < AngleMath.PI_HALF))
			{
				return ballPos.addNew(openDir.getNormalVector().scaleToNew(secRadius + 100));
			}
			return ballPos.addNew(openDir.getNormalVector().scaleToNew(-(secRadius + 100)));
		}
		
		return ballPos.addNew(openDir.scaleToNew(secRadius + 100));
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
	}
	
	
	@Override
	public boolean isSensitiveToTouch()
	{
		return true;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// double t = 0;
		// for (double t = 0; t < ball.getTimeByVel(0); t += 0.2)
		{
			// List<IObstacle> shapes = getObstacle(t);
			for (IObstacle o : shapes)
			{
				o.setColor(color);
				o.paintShape(g, tool, invert);
			}
		}
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
