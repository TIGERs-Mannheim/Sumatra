/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class TrajPathNode implements IDrawableShape
{
	private final BangBangTrajectory2D	trajXY;
	private final ITrajectory1D			trajW;
	private final float						endTime;
	private final int							id;
	private boolean							reset;
	
	private static final int				MAX_ID				= 255;
	
	@Configurable
	private static float						collisionStepSize	= 0.1f;
	
	@Configurable
	private static float						plottingStepSize	= 0.1f;
	
	@Configurable
	private static boolean					showTimes			= false;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private TrajPathNode()
	{
		trajXY = null;
		trajW = null;
		endTime = 0;
		id = 0;
		reset = false;
	}
	
	
	/**
	 * @param traj
	 * @param trajW
	 * @param endTime
	 * @param id
	 */
	public TrajPathNode(final BangBangTrajectory2D traj, final ITrajectory1D trajW, final float endTime, final int id)
	{
		assert id >= 0;
		trajXY = traj;
		this.trajW = trajW;
		this.endTime = endTime;
		this.id = id;
		if (id == 0)
		{
			reset = true;
		}
	}
	
	
	/**
	 * @param obstacles
	 * @param baseTime
	 * @return
	 */
	public float getEarliestCollision(final List<IObstacle> obstacles, final float baseTime)
	{
		for (float t = 0; t < Math.min(endTime, 10); t += collisionStepSize)
		{
			IVector2 pos = trajXY.getPosition(t);
			for (IObstacle obs : obstacles)
			{
				if (obs.isPointCollidingWithObstacle(pos, baseTime + t))
				{
					// DebugShapeHacker.addDebugShape(obs);
					// DebugShapeHacker.addDebugShape(new DrawablePoint(pos));
					return t;
				}
			}
		}
		return Float.MAX_VALUE;
	}
	
	
	/**
	 * @param obstacles
	 * @param baseTime
	 * @return
	 */
	public boolean hasCollision(final List<IObstacle> obstacles, final float baseTime)
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
	 * @return the traj
	 */
	public final ITrajectory1D getTrajW()
	{
		return trajW;
	}
	
	
	/**
	 * @return
	 */
	public final IVector2 getDestination()
	{
		return trajXY.getPosition(trajXY.getTotalTime());
	}
	
	
	/**
	 * @return
	 */
	public final float getTargetAngle()
	{
		return trajW.getPosition(trajW.getTotalTime());
	}
	
	
	/**
	 * @return the endTime
	 */
	public final float getEndTime()
	{
		return endTime;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		if (showTimes)
		{
			Font font = new Font("", Font.PLAIN, 5);
			g.setFont(font);
		}
		for (float t = 0; t < endTime; t += plottingStepSize)
		{
			IVector2 pos = trajXY.getPosition(t);
			IVector2 posTrans = fieldPanel.transformToGuiCoordinates(pos, invert);
			IVector2 vel = trajXY.getVelocity(t);
			
			float relVel = Math.min(1, vel.getLength2() / 2f);
			float colorGreen = 1;
			float colorRed = 1;
			if (relVel > 0)
			{
				colorRed = 1 - relVel;
			}
			if (relVel < 0)
			{
				colorGreen = 1 + relVel;
			}
			
			g.setColor(new Color(colorRed, colorGreen, 0));
			
			g.fillOval((int) posTrans.x() - 1, (int) posTrans.y() - 1, 2, 2);
			if (showTimes)
			{
				g.drawString(String.format("%.1f", t), posTrans.x() + 2, posTrans.y() + 0.8f);
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
		sb.append(node.getTraj().getPosition(0));
		sb.append(", ");
		sb.append(node.getTrajW().getPosition(0));
		sb.append(" -> ");
		sb.append(node.getTraj().getPosition(node.getEndTime()));
		sb.append(", ");
		sb.append(node.getTrajW().getPosition(node.getEndTime()));
		sb.append("; ");
		sb.append(node.getDestination());
		sb.append(",");
		sb.append(node.getTargetAngle());
		sb.append(" totalTime=");
		sb.append(node.getEndTime());
		sb.append(" reset=");
		sb.append(node.isReset());
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
	
	
	/**
	 * @return the reset
	 */
	public final boolean isReset()
	{
		return reset;
	}
	
	
	/**
	 * @param reset the reset to set
	 */
	public final void setReset(final boolean reset)
	{
		this.reset = reset;
	}
}
