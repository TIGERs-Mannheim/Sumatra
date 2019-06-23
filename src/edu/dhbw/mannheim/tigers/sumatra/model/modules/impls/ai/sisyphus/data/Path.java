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

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

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
@Persistent(version = 1)
public class Path
{
	// ------------------------------------------------------------------------
	// --- variables ----------------------------------------------------------
	// ------------------------------------------------------------------------
	
	@PrimaryKey
	private int					id;
	
	/** path has changed since last iteration? */
	private boolean			changed			= true;
	/** */
	@NotNull
	private BotID				botID;
	
	/** Contains all points that define the path, except the starting-point! */
	@NotNull
	@AssertValid
	private List<IVector2>	path;
	
	@NotNull
	@AssertValid
	private SplinePair3D		hermiteSpline;
	
	private boolean			old				= false;
	
	private boolean			rambo				= false;
	
	@NotNull
	private IVector2			destination;
	private float				destOrient;
	
	private Collision			firstCollision	= null;
	
	private ITree				tree				= null;
	
	
	@SuppressWarnings("unused")
	private Path()
	{
	}
	
	
	/**
	 * @param botID
	 * @param target
	 * @param destOrient
	 */
	public Path(final BotID botID, final IVector2 target, final float destOrient)
	{
		this.botID = botID;
		destination = target;
		this.destOrient = destOrient;
		path = new ArrayList<IVector2>();
	}
	
	
	/**
	 * @param botID
	 * @param nodes
	 * @param target
	 * @param destOrient
	 */
	public Path(final BotID botID, final List<IVector2> nodes, final IVector2 target, final float destOrient)
	{
		this.botID = botID;
		destination = target;
		this.destOrient = destOrient;
		path = nodes;
	}
	
	
	/**
	 * @return A light copy of this {@link Path}
	 */
	public Path copyLight()
	{
		final List<IVector2> newNodes = new ArrayList<IVector2>();
		newNodes.addAll(getPath());
		
		final Path result = new Path(getBotID(), newNodes, destination, destOrient);
		result.changed = changed;
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
	public boolean add(final Vector2 node)
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
	private float getLength(final IVector2 currentPosition)
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
	private float getLength(final IVector2 currentPosition, final int index)
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
	public boolean isGoalReached(final IVector2 currentPosition, final float tolerance)
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
	 * @return the rambo
	 */
	public final boolean isRambo()
	{
		return rambo;
	}
	
	
	/**
	 * @param rambo the rambo to set
	 */
	public final void setRambo(final boolean rambo)
	{
		this.rambo = rambo;
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
	public void setHermiteSpline(final SplinePair3D hermiteSpline)
	{
		this.hermiteSpline = hermiteSpline;
	}
	
	
	/**
	 * @return the target [mm]
	 */
	public IVector2 getDestination()
	{
		return destination;
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
	public void setFirstCollisionAt(final Collision firstCollisionAt)
	{
		firstCollision = firstCollisionAt;
	}
	
	
	/**
	 * Pathplanning tree (useful for rambo cases)
	 * 
	 * @return the tree
	 */
	public ITree getTree()
	{
		return tree;
	}
	
	
	/**
	 * @param tree the tree to set
	 */
	public void setTree(final ITree tree)
	{
		this.tree = tree;
	}
	
	
}
