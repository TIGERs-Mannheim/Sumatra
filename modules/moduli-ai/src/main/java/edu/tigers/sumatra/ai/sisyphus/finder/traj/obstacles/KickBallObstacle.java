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
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class KickBallObstacle implements IObstacle, IDrawableShape
{
	
	private final TrackedBall	ball;
	private final IVector2		openDir;
										
										
	private double					innerRadius	= 150;
	private double					outerRadius	= 250;
	private double					barRadius	= 300;
	private double					openAngle	= 0.5;
														
	private Color					color			= Color.red;
	private ITrackedBot			bot;
										
										
	static
	{
		ConfigRegistration.registerClass("sisyphus", KickBallObstacle.class);
	}
	
	
	@SuppressWarnings("unused")
	private KickBallObstacle()
	{
		ball = null;
		openDir = null;
	}
	
	
	/**
	 * @param ball
	 * @param openDir
	 * @param bot
	 */
	public KickBallObstacle(final TrackedBall ball, final IVector2 openDir, final ITrackedBot bot)
	{
		this.ball = ball;
		this.openDir = openDir;
		this.bot = bot;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		if (ball.getVel().getLength2() > 0.2)
		{
			double angleVelToBot = Math.abs(AngleMath.getShortestRotation(ball.getVel().getAngle(),
					bot.getPosByTime(t).subtractNew(ball.getPosByTime(t)).getAngle()));
			if (angleVelToBot < AngleMath.PI_HALF)
			{
				return false;
			}
		}
		
		IVector2 ballPos = ball.getPosByTime(t);
		double dist = GeoMath.distancePP(point, ballPos);
		if (dist < innerRadius)
		{
			return false;
		}
		
		double angle = point.subtractNew(ballPos).getAngle();
		if (Math.abs(AngleMath.getShortestRotation(angle, openDir.getAngle())) < openAngle)
		{
			return false;
		}
		
		double angleDiff = Math.abs(AngleMath.getShortestRotation(angle, openDir.getAngle()));
		if (angleDiff < AngleMath.PI_HALF)
		{
			double rel = 1 - (angleDiff / AngleMath.PI_HALF);
			double relDist = outerRadius + (rel * (barRadius - outerRadius));
			if (dist < relDist)
			{
				return true;
			}
		}
		
		if (dist > outerRadius)
		{
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		IVector2 ballPos = ball.getPosByTime(t);
		double dist = GeoMath.distancePP(point, ballPos);
		
		double angle = point.subtractNew(ballPos).getAngle();
		if (Math.abs(AngleMath.getShortestRotation(angle, openDir.getAngle())) < openAngle)
		{
			return point;
		}
		
		if (dist >= barRadius)
		{
			return point;
		}
		
		if ((Math.abs(AngleMath.getShortestRotation(angle, openDir.getAngle())) < AngleMath.PI_HALF))
		{
			return ballPos.addNew(openDir.scaleToNew(barRadius));
		}
		
		if (dist > outerRadius)
		{
			return point;
		}
		
		return GeoMath.stepAlongLine(ballPos, point, barRadius);
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
		Font font = new Font("", Font.PLAIN, 5);
		g.setFont(font);
		
		double tBallStop = ball.getTimeByVel(0);
		double tStep = Math.max(0.05f, tBallStop / 12.0);
		for (double t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 pos = ball.getPosByTime(t);
			
			new DrawableCircle(pos, innerRadius, color).paintShape(g, tool, invert);
			new DrawableCircle(pos, outerRadius, color).paintShape(g, tool, invert);
			
			new DrawableLine(new Line(pos, openDir.turnNew(openAngle).scaleTo(barRadius)), color, false)
					.paintShape(g,
							tool, invert);
							
			new DrawableLine(new Line(pos, openDir.turnNew(-(openAngle)).scaleTo(barRadius)), color, false)
					.paintShape(g, tool, invert);
					
			if (t > 0)
			{
				final IVector2 center = tool.transformToGuiCoordinates(pos, invert).addNew(new Vector2(0, 00));
				g.setColor(color);
				g.setStroke(new BasicStroke(1));
				g.drawString(String.format("%.1f", t), (float) (center.x() + 20), (float) (center.y() + 2));
			}
		}
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
	
	
	/**
	 * @return the innerRadius
	 */
	public double getInnerRadius()
	{
		return innerRadius;
	}
	
	
	/**
	 * @param innerRadius the innerRadius to set
	 */
	public void setInnerRadius(final double innerRadius)
	{
		this.innerRadius = innerRadius;
	}
	
	
	/**
	 * @return the outerRadius
	 */
	public double getOuterRadius()
	{
		return outerRadius;
	}
	
	
	/**
	 * @param outerRadius the outerRadius to set
	 */
	public void setOuterRadius(final double outerRadius)
	{
		this.outerRadius = outerRadius;
	}
	
	
	/**
	 * @return the barRadius
	 */
	public double getBarRadius()
	{
		return barRadius;
	}
	
	
	/**
	 * @param barRadius the barRadius to set
	 */
	public void setBarRadius(final double barRadius)
	{
		this.barRadius = barRadius;
	}
	
	
	/**
	 * @return the openAngle
	 */
	public double getOpenAngle()
	{
		return openAngle;
	}
	
	
	/**
	 * @param openAngle the openAngle to set
	 */
	public void setOpenAngle(final double openAngle)
	{
		this.openAngle = openAngle;
	}
	
}
