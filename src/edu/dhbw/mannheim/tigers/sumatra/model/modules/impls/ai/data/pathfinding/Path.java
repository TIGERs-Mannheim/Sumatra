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
import edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline;


/**
 * Dataobject, path and some additional information such as velocity at some point is stored in
 * 
 * @author Christian König
 */
public class Path
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	/** path has changed since last iteration? */
	public boolean						changed				= true;
	public final int					botID;
	
	/** Contains all {@link PathPoint} that defines the path, except the starting-point! */
	public final List<IVector2>	path;
	
	/** <strong>Warning:</strong> For internal use only! */
	public final List<Node>			wayPointCache;
	
	private XYSpline					spline				= null;
	
	private PathGuiFeatures			pathGuiFeatures	= new PathGuiFeatures();
	
	
	public Path(int botID)
	{
		this.botID = botID;
		path = new ArrayList<IVector2>();
		wayPointCache = new ArrayList<Node>();
	}
	

	public Path(int botID, List<IVector2> nodes)
	{
		this.botID = botID;
		this.path = nodes;
		wayPointCache = new ArrayList<Node>();
	}
	

	public Path(int botID, List<IVector2> nodes, List<Node> wayPointCache)
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
		result.spline = this.spline;
		result.changed = this.changed;
		result.pathGuiFeatures = this.pathGuiFeatures;
		
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
	

	/**
	 * this size (e.g. the number of pathpoints) of the path
	 * 
	 * @return the size
	 * @author DanielW
	 */
	public int size()
	{
		return path.size();
	}
	

	/**
	 * calculates the distance to the very end of the path
	 * 
	 * @param currentPosition
	 * @return the distance [mm]
	 * @author DanielW
	 */
	public float getLength(IVector2 currentPosition)
	{
		return getLength(currentPosition, path.size() - 1);
	}
	

	/**
	 * calculates the distance to the pathpoint referenced by index.
	 * 
	 * @param currentPosition
	 * @param index index; 0 is the very next pathpoint
	 * @return the distance [mm]
	 * @author DanielW
	 */
	public float getLength(IVector2 currentPosition, int index)
	{
		// safety check
		if (path.size() < 1 || index > path.size() - 1 || index < 0)
		{
			return 0; // there is no point; or index out of range (0..path.size-1)
		}
		
		float length = 0;
		length = path.get(0).subtractNew(currentPosition).getLength2();
		for (int i = 1; i <= index; i++)
		{
			length += path.get(i).subtractNew(path.get(i - 1)).getLength2();
		}
		

		return length;
		

	}
	

	/**
	 * calculates the angle at a paths corner;
	 * 
	 * @param currentPosition bot position
	 * @param index of corner: 0 is the corner at the very next pathpoint;
	 * @return the cornerangle [rad] or 0 if there is only one pathpoint or the index is out of range (0..path.size-2)
	 * @author DanielW
	 */
	public float getCornerAngle(IVector2 currentPosition, int index)
	{
		// safety check
		if (path.size() < 2 || index > path.size() - 2 || index < 0)
		{
			return 0; // there is only the targetpoint in the path. no corner there; or wrong corner reference
		}
		
		float angle = 0;
		IVector2 pA, pB, pCorner;
		if (index == 0)
		{
			pA = currentPosition;
		} else
		{
			pA = path.get(index - 1);
		}
		pCorner = path.get(index);
		pB = path.get(index + 1);
		
		IVector2 pCorner_pA = pA.subtractNew(pCorner);
		IVector2 pCorner_pB = pB.subtractNew(pCorner);
		
		angle = Math.abs(pCorner_pA.getAngle() - pCorner_pB.getAngle());
		
		return angle;
	}
	

	/**
	 * @param spline the spline to set
	 */
	public void setSpline(XYSpline spline)
	{
		this.spline = spline;
	}
	

	/**
	 * @return the spline
	 */
	public XYSpline getSpline()
	{
		return spline;
	}
	

	/**
	 * @return the pathGuiFeatures
	 */
	public PathGuiFeatures getPathGuiFeatures()
	{
		return pathGuiFeatures;
	}
	

	/**
	 * @param pathGuiFeatures the pathGuiFeatures to set
	 */
	public void setPathGuiFeatures(PathGuiFeatures pathGuiFeatures)
	{
		this.pathGuiFeatures = pathGuiFeatures;
	}
	

}
