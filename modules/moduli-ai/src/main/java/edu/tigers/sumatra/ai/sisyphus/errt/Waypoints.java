/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.errt.tree.Node;
import edu.tigers.sumatra.math.IVector2;


/**
 * contains the waypoints for one bot and the methods, which can be performed on this waypoints
 * 
 * @author dirk
 */
public class Waypoints
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// WPC
	@Configurable(comment = "size of waypointcache", defValue = "100")
	private static int				wpcSize						= 100;
	@Configurable(comment = "how much target can differ from target of last cycle to use WPC [mm]", defValue = "100")
	private static double			tollerableTargetShift	= 100;
																			
	/** waypoint cache */
	private final List<IVector2>	waypoints					= new ArrayList<IVector2>();
	/** the path which were extracted from the waypoints during the last frame */
	private Node						goal;
											
											
	static
	{
		ConfigRegistration.registerClass("sisyphus", Waypoints.class);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Waypoints()
	{
		goal = null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newGoal
	 * @return
	 */
	public boolean equalsGoal(final Node newGoal)
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
	public void clear(final Node newGoal)
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
	public void fillWPC(final List<IVector2> waypointNodes, final Node goal)
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
