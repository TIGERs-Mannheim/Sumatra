/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Arc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class CatchBallObstacle implements IObstacle, IDrawableShape
{
	private final TrackedBall		ball;
	private final float				toBallDist;
	private final DynamicPosition	receiver;
	private final float				shootSpeed;
	
	@Configurable
	private static float				defRadius		= 200;
	@Configurable
	private static float				defArcAngle		= 1.2f;
	@Configurable
	private static float				maxPredTimeBot	= 1f;
	@Configurable
	private static float				minVel			= 0.5f;
	
	private float						radius			= defRadius;
	private float						arcAngle			= defArcAngle;
	
	
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
	public CatchBallObstacle(final TrackedBall ball, final float toBallDist,
			final DynamicPosition receiver, final float shootSpeed)
	{
		this.ball = ball;
		this.toBallDist = toBallDist;
		this.receiver = receiver;
		this.shootSpeed = shootSpeed;
	}
	
	
	private Arc createArc(final float t)
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
		
		float approxOrient = 0;
		if (!pos.equals(recv))
		{
			approxOrient = recv.subtractNew(pos).getAngle();
		}
		float targetAngle = AiMath.calcRedirectOrientation(pos, approxOrient, vBallAtDest, recv, shootSpeed);
		IVector2 dir = new Vector2(targetAngle);
		
		float startAngle = (targetAngle + AngleMath.PI) + (arcAngle / 2);
		Arc arc = new Arc(pos.addNew(dir.scaleToNew(-toBallDist / 2)), radius, startAngle, AngleMath.PI_TWO - arcAngle);
		return arc;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		return createArc(t).isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		return createArc(t).nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		for (float t = 0; t <= ball.getTimeByVel(0); t += 0.2f)
		{
			IVector2 posStill = ball.getPosByVel(t);
			
			for (int i = 0; i < 5; i++)
			{
				IVector2 mu = GeoMath.stepAlongLine(posStill, receiver, -radius * 3);
				IVector2 sample = mu
						.addNew(new Vector2(rnd.nextFloat() * AngleMath.PI_TWO).scaleTo((float) rnd.nextGaussian() * 2000));
				subPoints.add(sample);
			}
		}
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		Font font = new Font("", Font.PLAIN, 5);
		g.setFont(font);
		
		float tBallStop = ball.getTimeByVel(0);
		float tStep = Math.max(0.05f, tBallStop / 10);
		for (float t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 pos = ball.getPosByTime(t);
			IVector2 dir = receiver.getPosAt(Math.min(t, maxPredTimeBot)).subtractNew(pos);
			new DrawableLine(new Line(pos, dir)).paintShape(g, fieldPanel, invert);
			
			Arc arc = createArc(t);
			arc.paintShape(g, fieldPanel, invert);
			
			final IVector2 center = fieldPanel.transformToGuiCoordinates(pos, invert).addNew(new Vector2(0, 00));
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawString(String.format("%.1f", t), (center.x() + 20), (center.y() + 2));
		}
	}
	
	
	/**
	 * @return the radius
	 */
	public final float getRadius()
	{
		return radius;
	}
	
	
	/**
	 * @param radius the radius to set
	 */
	public final void setRadius(final float radius)
	{
		this.radius = radius;
	}
	
	
	/**
	 * @return the arcAngle
	 */
	public final float getArcAngle()
	{
		return arcAngle;
	}
	
	
	/**
	 * @param arcAngle the arcAngle to set
	 */
	public final void setArcAngle(final float arcAngle)
	{
		this.arcAngle = arcAngle;
	}
	
}
