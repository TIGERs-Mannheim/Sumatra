/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.12.2010
 * Author(s): König
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;


/**
 * element structure of kd-tree element
 * 
 * @author König
 * 
 */
public class KDNode extends Vector2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= 16823286759873876L;
	public KDNode					parent;
	public KDNode					leftChild;
	public KDNode					rightChild;
	public EKDDimension			dim;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * perhaps it will do what every constructor is doing... -.-
	 * 
	 * @param pos
	 */
	public KDNode(IVector2 pos)
	{
		super(pos);
	}
	

	/**
	 * perhaps it will do what every constructor is doing... -.-
	 * 
	 * @param pos
	 */
	public KDNode(float x, float y)
	{
		super(x, y);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * adds child to correct position in KD-tree
	 * 
	 * @param child
	 */
	public void addChild(KDNode child)
	{
		if (this.dim == EKDDimension.X_PLANE)
		{
			// x-plane
			if (child.x < this.x)
			{
				addLeft(child);
			} else
			{
				addRight(child);
			}
		} else if (this.dim == EKDDimension.Y_PLANE)
		{
			// y-plane
			if (child.y < this.y)
			{
				addLeft(child);
			} else
			{
				addRight(child);
			}
		} else
		{
			LOG.debug("wtf? no dim?");
		}
		
		// Switch dimensions
		if (this.dim == EKDDimension.X_PLANE)
		{
			child.dim = EKDDimension.Y_PLANE;
		} else
		{
			child.dim = EKDDimension.X_PLANE;
		}
		
	}
	
	private void addRight(KDNode child)
	{
		if (this.rightChild == null)
		{
			// "And I called him my pa, and he called me his son"
			this.rightChild = child;
			child.parent = this;
		} else
		{
			LOG.error("tried to add although there is already a kdnode");
		}
	}
	

	private void addLeft(KDNode child)
	{
		if (this.leftChild == null)
		{
			this.leftChild = child;
			child.parent = this;
		} else
		{
			LOG.error("tried to add although there is already a kdnode");
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
