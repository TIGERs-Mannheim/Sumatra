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
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 4)
public class TrajPath implements ITrajectory2D, IDrawableShape
{
	@SuppressWarnings("unused")
	private static final Logger		log			= Logger.getLogger(TrajPath.class.getName());
	
	private final List<TrajPathNode>	nodes;
	private final float					totalTime;
	private final long					tStart;
	private final long					timestamp;
	
	private float							currentTime	= 0;
	
	private static final Random		RND			= new Random();
	private final float					rndId;
	
	@SuppressWarnings("unused")
	@Deprecated
	private boolean						connectingPath;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private TrajPath()
	{
		nodes = Collections.emptyList();
		totalTime = 0;
		tStart = 0;
		rndId = RND.nextFloat();
		timestamp = System.currentTimeMillis();
	}
	
	
	/**
	 * @param nodes
	 * @param currentTime
	 * @param tStart
	 */
	public TrajPath(final List<TrajPathNode> nodes, final float currentTime, final long tStart)
	{
		assert !nodes.isEmpty();
		this.nodes = nodes;
		float t = 0;
		for (TrajPathNode node : nodes)
		{
			t += node.getEndTime();
		}
		totalTime = t;
		this.tStart = tStart;
		setCurrentTime(currentTime);
		rndId = RND.nextFloat();
		timestamp = System.currentTimeMillis();
	}
	
	
	/**
	 * Create a place-holder path that just stands still
	 * 
	 * @param staticPos
	 * @param orientation
	 */
	public TrajPath(final IVector2 staticPos, final float orientation)
	{
		nodes = new ArrayList<>(1);
		BangBangTrajectory2D traj = TrajectoryGenerator.generatePositionTrajectoryStub(staticPos);
		ITrajectory1D trajW = TrajectoryGenerator.generateRotationTrajectoryStub(orientation, traj);
		TrajPathNode node = new TrajPathNode(traj, trajW, 0, 0);
		nodes.add(node);
		totalTime = 0;
		tStart = System.nanoTime();
		setCurrentTime(0);
		rndId = RND.nextFloat();
		timestamp = System.currentTimeMillis();
	}
	
	
	/**
	 * Shallow copy. Keeps reference to nodes!
	 * 
	 * @param path
	 */
	public TrajPath(final TrajPath path)
	{
		nodes = path.nodes;
		totalTime = path.totalTime;
		currentTime = path.currentTime;
		tStart = path.tStart;
		rndId = path.rndId;
		timestamp = path.timestamp;
	}
	
	
	/**
	 * @param obstacles
	 * @return
	 */
	public float getEarliestCollision(final List<IObstacle> obstacles)
	{
		float t = 0;
		for (TrajPathNode traj : nodes)
		{
			float tCollision = traj.getEarliestCollision(obstacles, t);
			if (tCollision < traj.getEndTime())
			{
				return t + tCollision;
			}
			t += traj.getEndTime();
		}
		return Float.MAX_VALUE;
	}
	
	
	/**
	 * @param tEnd
	 * @return
	 */
	public List<TrajPathNode> getPrePath(final float tEnd)
	{
		List<TrajPathNode> subNodes = new ArrayList<>();
		float t = 0;
		for (TrajPathNode traj : nodes)
		{
			if ((tEnd - t) <= (traj.getEndTime() + 1e-5))
			{
				break;
			}
			subNodes.add(traj);
			t += traj.getEndTime();
		}
		return subNodes;
	}
	
	
	/**
	 * @param tIn
	 * @return
	 */
	public TrajPathNode getNodeAt(final float tIn)
	{
		float tEnd = tIn;
		float t = 0;
		for (TrajPathNode node : nodes)
		{
			if ((tEnd - t) <= (node.getEndTime()))
			{
				return node;
			}
			t += node.getEndTime();
		}
		return nodes.get(nodes.size() - 1);
	}
	
	
	/**
	 * @param tIn
	 * @return
	 */
	public int getNodeIdxAt(final float tIn)
	{
		float tEnd = tIn;
		float t = 0;
		for (int i = 0; i < nodes.size(); i++)
		{
			TrajPathNode node = nodes.get(i);
			if ((tEnd - t) <= (node.getEndTime()))
			{
				return i;
			}
			t += node.getEndTime();
		}
		return nodes.size() - 1;
	}
	
	
	/**
	 * @param tIn
	 * @return
	 */
	public float getBaseTimeAt(final float tIn)
	{
		float tEnd = Math.min(tIn, totalTime);
		float t = 0;
		TrajPathNode node = null;
		for (int i = 0; i < nodes.size(); i++)
		{
			node = nodes.get(i);
			if ((tEnd - t) <= (node.getEndTime() + 1e-5))
			{
				return t;
			}
			t += node.getEndTime();
		}
		if (node != null)
		{
			return t - node.getEndTime();
		}
		return t;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getFinalDestination()
	{
		TrajPathNode node = nodes.get(nodes.size() - 1);
		return node.getDestination();
	}
	
	
	/**
	 * @return
	 */
	public float getFinalOrientation()
	{
		TrajPathNode node = nodes.get(nodes.size() - 1);
		return node.getTargetAngle();
	}
	
	
	/**
	 * @param obstacles
	 * @return
	 */
	public boolean hasCollision(final List<IObstacle> obstacles)
	{
		return getEarliestCollision(obstacles) < totalTime;
	}
	
	
	/**
	 * @return the totalTime
	 */
	@Override
	public final float getTotalTime()
	{
		return totalTime;
	}
	
	
	/**
	 * @return
	 */
	public final float getCurrentTime()
	{
		return currentTime;
	}
	
	
	/**
	 * @return
	 */
	public final float getVeryCurrentTime()
	{
		return Math.min(totalTime, (System.nanoTime() - tStart) / 1e9f);
	}
	
	
	/**
	 * 
	 */
	public final void updateCurrentTime()
	{
		setCurrentTime((System.nanoTime() - tStart) / 1e9f);
	}
	
	
	/**
	 * @param currentTime
	 */
	public final void setCurrentTime(final float currentTime)
	{
		// this.currentTime = Math.max(0, Math.min(totalTime, currentTime));
		// this.currentTime = Math.min(totalTime, currentTime);
		this.currentTime = currentTime;
	}
	
	
	/**
	 * @return
	 */
	public long getStartTime()
	{
		return tStart;
	}
	
	
	/**
	 * @return
	 */
	public final float getRemainingTime()
	{
		return Math.max(0, totalTime - getCurrentTime());
	}
	
	
	@Override
	public Vector2 getPosition(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTraj().getPosition(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTraj().getPosition(lastNode.getEndTime());
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTraj().getVelocity(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTraj().getVelocity(lastNode.getEndTime());
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTraj().getAcceleration(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTraj().getAcceleration(lastNode.getEndTime());
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public float getOrientation(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTrajW().getPosition(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTrajW().getPosition(lastNode.getEndTime());
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public float getaVel(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTrajW().getVelocity(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTrajW().getVelocity(lastNode.getEndTime());
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public float getaAcc(final float t)
	{
		float tt = 0;
		for (TrajPathNode node : nodes)
		{
			float tLocal = t - tt;
			if (tLocal <= node.getEndTime())
			{
				return node.getTrajW().getAcceleration(tLocal);
			}
			tt += node.getEndTime();
		}
		TrajPathNode lastNode = nodes.get(nodes.size() - 1);
		return lastNode.getTrajW().getAcceleration(lastNode.getEndTime());
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		new DrawableBot(getPosition(getCurrentTime()), getOrientation(getCurrentTime()), Color.cyan).paintShape(g,
				fieldPanel, invert);
		for (TrajPathNode node : nodes)
		{
			node.paintShape(g, fieldPanel, invert);
			new DrawableBot(node.getTraj().getPosition(0), node.getTrajW().getPosition(0),
					Color.magenta, 0.3f).paintShape(g, fieldPanel, invert);
			new DrawableBot(node.getTraj().getPosition(node.getEndTime()), node.getTrajW().getPosition(node.getEndTime()),
					Color.red, 0.3f).paintShape(g, fieldPanel, invert);
		}
		new DrawableBot(getFinalDestination(), getFinalOrientation(), Color.red).paintShape(g, fieldPanel, invert);
	}
	
	
	/**
	 * @return the nodes
	 */
	public final List<TrajPathNode> getNodes()
	{
		return Collections.unmodifiableList(nodes);
	}
	
	
	@Override
	public String toString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		
		StringBuilder sb = new StringBuilder();
		sb.append("TrajPath [numNodes=");
		sb.append(nodes.size());
		sb.append(", totalTime=");
		sb.append(totalTime);
		sb.append(", curTime=");
		sb.append(currentTime);
		sb.append(", timestamp=");
		sb.append(sdf.format(new Date(timestamp)));
		sb.append("] Nodes:");
		for (TrajPathNode node : nodes)
		{
			sb.append("\n");
			sb.append(node.toString());
		}
		return sb.toString();
	}
	
	
	/**
	 * @return the rndId
	 */
	public final float getRndId()
	{
		return rndId;
	}
}
