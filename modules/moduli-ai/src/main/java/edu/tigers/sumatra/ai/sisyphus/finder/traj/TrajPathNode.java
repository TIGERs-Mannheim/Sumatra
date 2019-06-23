/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class TrajPathNode implements IDrawableShape
{
	private final BangBangTrajectory2D	trajXY;
	private final double						endTime;
	private final int							id;
													
	private static final int				MAX_ID				= 255;
																			
	@Configurable
	private static double					collisionStepSize	= 0.1;
																			
	@Configurable
	private static double					plottingStepSize	= 0.1;
																			
	@Configurable
	private static boolean					showTimes			= false;
																			
	@Configurable
	private static boolean					showVel				= true;
																			
																			
	static
	{
		ConfigRegistration.registerClass("sisyphus", TrajPathNode.class);
	}
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private TrajPathNode()
	{
		trajXY = null;
		endTime = 0;
		id = 0;
	}
	
	
	/**
	 * @param traj
	 * @param endTime
	 * @param id
	 */
	public TrajPathNode(final BangBangTrajectory2D traj, final double endTime, final int id)
	{
		assert id >= 0;
		trajXY = traj;
		this.endTime = endTime;
		this.id = id;
	}
	
	
	/**
	 * @param obstacles
	 * @param baseTime
	 * @return
	 */
	public double getEarliestCollision(final List<IObstacle> obstacles, final double baseTime)
	{
		for (double t = 0; t < Math.min(endTime, 10); t += collisionStepSize)
		{
			IVector2 pos = trajXY.getPositionMM(t);
			for (IObstacle obs : obstacles)
			{
				if (obs.isPointCollidingWithObstacle(pos, baseTime + t))
				{
					return t;
				}
			}
		}
		return Double.MAX_VALUE;
	}
	
	
	/**
	 * @param obstacles
	 * @param baseTime
	 * @return
	 */
	public boolean hasCollision(final List<IObstacle> obstacles, final double baseTime)
	{
		return getEarliestCollision(obstacles, baseTime) < endTime;
	}
	
	
	/**
	 * @return the traj
	 */
	public final BangBangTrajectory2D getTraj()
	{
		return trajXY;
	}
	
	
	/**
	 * @return
	 */
	public final IVector2 getDestination()
	{
		return trajXY.getPositionMM(trajXY.getTotalTime());
	}
	
	
	/**
	 * @return the endTime
	 */
	public final double getEndTime()
	{
		return endTime;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (showTimes)
		{
			Font font = new Font("", Font.PLAIN, 5);
			g.setFont(font);
		}
		for (double t = 0; t < endTime; t += plottingStepSize)
		{
			IVector2 pos = trajXY.getPositionMM(t);
			IVector2 posTrans = tool.transformToGuiCoordinates(pos, invert);
			IVector2 vel = trajXY.getVelocity(t);
			
			double relVel = Math.min(1, vel.getLength2() / 2.0);
			double colorGreen = 1;
			double colorRed = 1;
			if (relVel > 0)
			{
				colorRed = 1 - relVel;
			}
			if (relVel < 0)
			{
				colorGreen = 1 + relVel;
			}
			
			g.setColor(new Color((float) colorRed, (float) colorGreen, 0));
			
			g.fillOval((int) posTrans.x() - 1, (int) posTrans.y() - 1, 2, 2);
			if (showTimes)
			{
				g.drawString(String.format("%.1f", t), (float) posTrans.x() + 2, (float) posTrans.y() + 0.8f);
			}
			if (showVel)
			{
				g.drawString(String.format("%.1f", vel.getLength2()), (float) posTrans.x() - 15,
						(float) posTrans.y() + 0.8f);
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		TrajPathNode node = this;
		sb.append(node.getId());
		sb.append(": ");
		sb.append(node.getTraj().getPositionMM(0));
		sb.append(" -> ");
		sb.append(node.getTraj().getPositionMM(node.getEndTime()));
		sb.append("; ");
		sb.append(node.getDestination());
		sb.append(" totalTime=");
		sb.append(node.getEndTime());
		return sb.toString();
	}
	
	
	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}
	
	
	/**
	 * @return
	 */
	public final int getNextId()
	{
		return (id + 1) % MAX_ID;
	}
	
	
	/**
	 * @return
	 */
	public final int getPreviousId()
	{
		return ((id - 1) + MAX_ID) % MAX_ID;
	}
}
