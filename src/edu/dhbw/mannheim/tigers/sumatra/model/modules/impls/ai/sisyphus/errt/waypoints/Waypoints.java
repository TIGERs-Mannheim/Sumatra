/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.waypoints;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;


/**
 * contains the waypoints for one bot and the methods, which can be performed on this waypoints
 * 
 * @author dirk
 * 
 */
public class Waypoints
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** size of waypointcache */
	private final int					wpcSize;
	// /** how much target can differ from target of last cycle to use WPC */
	private final float				tollerableTargetShift;
	/** waypoint cache */
	private final List<IVector2>	waypoints	= new ArrayList<IVector2>();
	/** the path which were extracted from the waypoints during the last frame */
	private Node						goal;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param tuneParams
	 */
	public Waypoints(TuneableParameter tuneParams)
	{
		goal = null;
		
		wpcSize = tuneParams.getWpcSize();
		tollerableTargetShift = tuneParams.getTollerableTargetShift();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newGoal
	 * @return
	 */
	public boolean equalsGoal(Node newGoal)
	{
		if (goal != null)
		{
			return goal.equals(newGoal, tollerableTargetShift);
		}
		return false;
	}
	
	
	/**
	 * @param newGoal
	 */
	public void clear(Node newGoal)
	{
		waypoints.clear();
		goal = newGoal;
	}
	
	
	/**
	 * fills the Waypoint cache, if there are more waypointNodes as allowed (WPC_SIZE), random nodes will be deleted
	 * 
	 * @param waypointNodes
	 * @param goal
	 */
	public void fillWPC(List<IVector2> waypointNodes, Node goal)
	{
		waypoints.addAll(waypointNodes);
		final int waypointsSize = waypoints.size();
		for (int i = wpcSize; i < waypointsSize; i++)
		{
			int rmWaypoint = (int) Math.round(Math.random() * waypoints.size());
			if (rmWaypoint >= 1)
			{
				rmWaypoint -= 1;
			}
			waypoints.remove(rmWaypoint);
		}
		this.goal = goal;
	}
	
	
	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return (waypoints.isEmpty());
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getArbitraryNode()
	{
		if (waypoints.size() > 0)
		{
			return waypoints.get((int) Math.round(Math.random() * (waypoints.size() - 1)));
		}
		return null;
	}
	
	
	/**
	 * @return
	 */
	public int size()
	{
		return waypoints.size();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
