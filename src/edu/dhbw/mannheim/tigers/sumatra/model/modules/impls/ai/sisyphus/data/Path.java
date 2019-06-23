/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Base class for any path
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 4)
public class Path implements IPath
{
	private static final Random	RND								= new Random(SumatraClock.nanoTime());
	@PrimaryKey
	private int							id									= RND.nextInt(Integer.MAX_VALUE);
	private static final Logger	log								= Logger.getLogger(Path.class.getName());
	private final List<IVector2>	pathPoints;
	private List<IVector2>			unsmoothedPathPoints			= new ArrayList<>(0);
	
	private transient IVector2		startPos;
	
	
	private final float				targetOrientation;
	private int							currentDestinationNodeIdx	= 0;
	
	private boolean					rambo								= false;
	private transient ITree			ramboTree						= null;
	private static final Path		DEFAULT							= new Path();
	
	
	@SuppressWarnings("unused")
	private Path()
	{
		this(Arrays.asList(new IVector2[] { AVector2.ZERO_VECTOR }), 0);
	}
	
	
	/**
	 * @return
	 */
	public static Path getDefault()
	{
		return DEFAULT;
	}
	
	
	/**
	 * @param pathPoints
	 * @param lookAtTarget
	 */
	public Path(final List<IVector2> pathPoints, final IVector2 lookAtTarget)
	{
		this(pathPoints, getOrientation(pathPoints, lookAtTarget));
	}
	
	
	/**
	 * @param pathPoints
	 * @param targetOrientation
	 */
	public Path(final List<IVector2> pathPoints, final float targetOrientation)
	{
		this.pathPoints = new ArrayList<IVector2>(pathPoints);
		this.targetOrientation = targetOrientation;
		if (pathPoints.isEmpty())
		{
			throw new IllegalArgumentException("A path must have at least one node!");
		}
	}
	
	
	/**
	 * Light copy of APath
	 * 
	 * @param orig
	 */
	public Path(final IPath orig)
	{
		this(orig.getPathPoints(), orig.getTargetOrientation());
		id = (int) getUniqueId();
		rambo = orig.isRambo();
		currentDestinationNodeIdx = orig.getCurrentDestinationNodeIdx();
		startPos = orig.getStartPos();
		unsmoothedPathPoints = orig.getUnsmoothedPathPoints();
	}
	
	
	protected static float getOrientation(final List<IVector2> pathPoints, final IVector2 lookAtTarget)
	{
		IVector2 dir = lookAtTarget.subtractNew(pathPoints.get(pathPoints.size() - 1));
		if (dir.isZeroVector())
		{
			log.warn("lookAtTarget and destination are equal. Can not calculate final orientation. Set to 0"
					+ lookAtTarget + " " + pathPoints.get(pathPoints.size() - 1));
			return 0;
		}
		return dir.getAngle();
	}
	
	
	@Override
	public long getUniqueId()
	{
		return id;
	}
	
	
	@Override
	public IVector2 getStart()
	{
		return pathPoints.get(0);
	}
	
	
	@Override
	public IVector2 getEnd()
	{
		return pathPoints.get(pathPoints.size() - 1);
	}
	
	
	@Override
	public float getTargetOrientation()
	{
		return targetOrientation;
	}
	
	
	@Override
	public List<IVector2> getPathPoints()
	{
		return Collections.unmodifiableList(pathPoints);
	}
	
	
	@Override
	public boolean isRambo()
	{
		return rambo;
	}
	
	
	/**
	 * @param rambo the rambo to set
	 */
	public void setRambo(final boolean rambo)
	{
		this.rambo = rambo;
	}
	
	
	/**
	 * @return the currentDestinationNodeIdx
	 */
	@Override
	public int getCurrentDestinationNodeIdx()
	{
		return currentDestinationNodeIdx;
	}
	
	
	/**
	 * @param currentDestinationNodeIdx the currentDestinationNodeIdx to set
	 */
	@Override
	public void setCurrentDestinationNodeIdx(final int currentDestinationNodeIdx)
	{
		this.currentDestinationNodeIdx = currentDestinationNodeIdx;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public IVector2 getCurrentDestination()
	{
		return pathPoints.get(currentDestinationNodeIdx);
	}
	
	
	/**
	 * @return the startPos
	 */
	@Override
	public IVector2 getStartPos()
	{
		return startPos;
	}
	
	
	/**
	 * @param startPos the startPos to set
	 */
	@Override
	public void setStartPos(final IVector2 startPos)
	{
		this.startPos = startPos;
	}
	
	
	/**
	 * Add a starting node to the pathPoints
	 * 
	 * @param node
	 */
	public void addStartNode(final IVector2 node)
	{
		pathPoints.add(0, node);
	}
	
	
	/**
	 * @return the unsmoothedPathPoints
	 */
	@Override
	public List<IVector2> getUnsmoothedPathPoints()
	{
		return unsmoothedPathPoints;
	}
	
	
	/**
	 * @param unsmoothedPathPoints the unsmoothedPathPoints to set
	 */
	public void setUnsmoothedPathPoints(final List<IVector2> unsmoothedPathPoints)
	{
		this.unsmoothedPathPoints = unsmoothedPathPoints;
	}
	
	
	/**
	 * @return the tree
	 */
	@Override
	public final ITree getTree()
	{
		return ramboTree;
	}
	
	
	/**
	 * @param tree the tree to set
	 */
	public final void setTree(final ITree tree)
	{
		ramboTree = tree;
	}
	
}
