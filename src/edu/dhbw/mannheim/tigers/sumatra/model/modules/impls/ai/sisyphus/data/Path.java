/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s):
 * Christian K�nig
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree;


/**
 * Dataobject, path and some additional information such as velocity at some point is stored in
 * 
 * @author Christian K�nig
 */
@Entity
public class Path
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	/** path has changed since last iteration? */
	private boolean			changed				= true;
	/** */
	private BotID				botID;
	
	/** Contains all points that define the path, except the starting-point! */
	private List<IVector2>	path;
	
	private SplinePair3D		hermiteSpline;
	
	private boolean			old					= false;
	
	private long				timestamp;
	
	private PathGuiFeatures	pathGuiFeatures	= new PathGuiFeatures();
	
	private boolean			rambo					= false;
	
	private IVector2			target;
	private float				destOrient;
	
	private Collision			firstCollision		= null;
	
	private ITree				tree					= null;
	
	
	/**
	 * @param botID
	 * @param target
	 * @param destOrient
	 */
	public Path(BotID botID, IVector2 target, float destOrient)
	{
		this.botID = botID;
		this.target = target;
		this.destOrient = destOrient;
		path = new ArrayList<IVector2>();
		timestamp = System.nanoTime();
	}
	
	
	/**
	 * 
	 * @param botID
	 * @param nodes
	 * @param target
	 * @param destOrient
	 */
	public Path(BotID botID, List<IVector2> nodes, IVector2 target, float destOrient)
	{
		this.botID = botID;
		this.target = target;
		this.destOrient = destOrient;
		path = nodes;
		timestamp = System.nanoTime();
	}
	
	
	/**
	 * @return A light copy of this {@link Path}
	 */
	public Path copyLight()
	{
		final List<IVector2> newNodes = new ArrayList<IVector2>();
		newNodes.addAll(getPath());
		
		final Path result = new Path(getBotID(), newNodes, target, destOrient);
		result.changed = changed;
		result.pathGuiFeatures = pathGuiFeatures;
		result.rambo = rambo;
		result.firstCollision = firstCollision;
		result.tree = tree;
		result.setHermiteSpline(hermiteSpline);
		
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
		return getPath().add(node);
	}
	
	
	/**
	 * @return Returns the <u>last point in {@link #path}</u>; and <code>null</code> if the path is empty.
	 * @author Gero
	 */
	public IVector2 getGoal()
	{
		if (getPath().size() > 0)
		{
			return getPath().get(getPath().size() - 1);
		}
		// oh-oh
		return null;
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getStart()
	{
		if (getPath().size() > 0)
		{
			return getPath().get(0);
		}
		// oh-oh
		return null;
	}
	
	
	/**
	 * @return
	 */
	public List<IVector2> getPath()
	{
		return path;
	}
	
	
	/**
	 * this size (e.g. the number of pathpoints) of the path
	 * 
	 * @return the size
	 * @author DanielW
	 */
	public int size()
	{
		return getPath().size();
	}
	
	
	/**
	 * calculates the distance to the very end of the path
	 * 
	 * @param currentPosition
	 * @return the distance [mm]
	 * @author DanielW
	 */
	private float getLength(IVector2 currentPosition)
	{
		return getLength(currentPosition, getPath().size() - 1);
	}
	
	
	/**
	 * calculates the distance to the pathpoint referenced by index.
	 * 
	 * @param currentPosition
	 * @param index index; 0 is the very next pathpoint
	 * @return the distance [mm]
	 * @author DanielW
	 */
	private float getLength(IVector2 currentPosition, int index)
	{
		// safety check
		if ((getPath().size() < 1) || (index > (getPath().size() - 1)) || (index < 0))
		{
			// there is no point; or index out of range (0..path.size-1)
			return 0;
		}
		
		float length = 0;
		length = getPath().get(0).subtractNew(currentPosition).getLength2();
		for (int i = 1; i <= index; i++)
		{
			length += getPath().get(i).subtractNew(getPath().get(i - 1)).getLength2();
		}
		
		
		return length;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Path (old: ");
		sb.append(old);
		sb.append(", changed: ");
		sb.append(changed);
		sb.append(", timestamp: ");
		sb.append(timestamp);
		sb.append("): \n");
		for (IVector2 pathPoint : getPath())
		{
			sb.append(pathPoint.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	/**
	 * @param currentPosition
	 * @param tolerance
	 * @return
	 */
	public boolean isGoalReached(IVector2 currentPosition, float tolerance)
	{
		return getLength(currentPosition) < tolerance;
	}
	
	
	/**
	 * @return the spline
	 */
	public ISpline getSpline()
	{
		if (hermiteSpline == null)
		{
			return null;
			
		}
		return hermiteSpline.getPositionTrajectory();
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
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the rambo
	 */
	public final boolean isRambo()
	{
		return rambo;
	}
	
	
	/**
	 * @return the old
	 */
	public boolean isOld()
	{
		return old;
	}
	
	
	/**
	 * @param rambo the rambo to set
	 */
	public final void setRambo(boolean rambo)
	{
		this.rambo = rambo;
	}
	
	
	/**
	 * @param old the old to set
	 */
	public void setOld(boolean old)
	{
		this.old = old;
	}
	
	
	/**
	 * @return the changed
	 */
	public boolean isChanged()
	{
		return changed;
	}
	
	
	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}
	
	
	/**
	 * @return the hermiteSpline
	 */
	public SplinePair3D getHermiteSpline()
	{
		return hermiteSpline;
	}
	
	
	/**
	 * @param hermiteSpline the hermiteSpline to set
	 */
	public void setHermiteSpline(SplinePair3D hermiteSpline)
	{
		this.hermiteSpline = hermiteSpline;
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @return the botID
	 */
	public BotID getBotID()
	{
		return botID;
	}
	
	
	/**
	 * @return the destOrient
	 */
	public final float getDestOrient()
	{
		return destOrient;
	}
	
	
	/**
	 * @return the firstCollisionAt
	 */
	public Collision getFirstCollisionAt()
	{
		return firstCollision;
	}
	
	
	/**
	 * @param firstCollisionAt the firstCollisionAt to set
	 */
	public void setFirstCollisionAt(Collision firstCollisionAt)
	{
		firstCollision = firstCollisionAt;
	}
	
	
	/**
	 * @return the tree
	 */
	public ITree getTree()
	{
		return tree;
	}
	
	
	/**
	 * @param tree the tree to set
	 */
	public void setTree(ITree tree)
	{
		this.tree = tree;
	}
	
	
}
