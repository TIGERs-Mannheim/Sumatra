/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s):
 * Christian König
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;


/**
 * Dataobject, path and some additional information such as velocity at some point is stored in
 * 
 * @author Christian König
 */
public class KDPath
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	/** path has changed since last iteration? */
	public boolean						changed	= true;
	public final int					botID;
	
	/** Contains all {@link PathPoint} that defines the path, except the starting-point! */
	public final List<IVector2>	path;
	
	/** <strong>Warning:</strong> For internal use only! */
	public final List<KDNode>		wayPointCache;
	
	
	public KDPath(int botID)
	{
		this.botID = botID;
		path = new ArrayList<IVector2>();
		wayPointCache = new ArrayList<KDNode>();
	}
	

	public KDPath(int botID, List<IVector2> nodes)
	{
		this.botID = botID;
		this.path = nodes;
		wayPointCache = new ArrayList<KDNode>();
	}
	

	public KDPath(int botID, List<IVector2> nodes, List<KDNode> wayPointCache)
	{
		this.botID = botID;
		this.path = nodes;
		this.wayPointCache = wayPointCache;
	}
	

	/**
	 * Meant for external use. <strong>Does NOT copy {@link #wayPointCache} or {@link PathPoint}s in {@link #path}
	 * !!!</strong> (As this is for internal use
	 * only)
	 * @return A light copy of this {@link Path}
	 */
	public Path copyLight()
	{
		List<IVector2> newNodes = new ArrayList<IVector2>();
		for (IVector2 v : this.path)
		{
			newNodes.add(v);
		}
		
		final Path result = new Path(this.botID, newNodes, null);// DO NOT USE WAYPOINTCACHE! =)
		result.changed = this.changed;
		
		return result;
	}
	
	
	/**
	 * adds param 'node' to Path.path
	 * 
	 * @param node PathPoint to be added
	 * @return added?
	 */
	public boolean add(Vector2 node)
	{
		return path.add(node);
	}
	

	/**
	 * @return Returns the <u>last {@link PathPoint} in {@link #path}</u>; and <code>null</code> if the path is empty.
	 * @author Gero
	 */
	public IVector2 getGoal()
	{
		if (path.size() > 0)
		{
			return path.get(path.size() - 1);
		} else
		{
			// oh-oh
			return null;
		}
	}
}
