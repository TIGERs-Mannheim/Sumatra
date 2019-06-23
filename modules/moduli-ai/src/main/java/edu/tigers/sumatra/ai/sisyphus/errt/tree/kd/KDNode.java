/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 21, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt.tree.kd;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.sisyphus.errt.tree.Node;


/**
 * TODO DanielAl add comment!
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent
public class KDNode implements Comparable<Node>
{
	/** The DImensions this KDTree and KDNode will use */
	private final static int	dim			= 2;
	/** */
	private KDNode					leftChild	= null;
	/** */
	private KDNode					rightChild	= null;
	/** */
	private int						depth;
	private Node					val;
	
	
	/**
	 * needed by BerkleyDB
	 */
	@SuppressWarnings("unused")
	private KDNode()
	{
		
	}
	
	
	/**
	 * @param val
	 */
	public KDNode(final Node val)
	{
		this.val = val;
	}
	
	
	/**
	 * Adds a KDNode to this KDNode by determining the right dimension to use and on which side (left or right) it
	 * belongs. Also if this KDNode already has a Child at the desired position the new child will be added to the
	 * existing child instead of this KDNode.
	 * 
	 * @param child
	 */
	public void addChild(final KDNode child)
	{
		// switch over depth%dim, because this will switch the dimensions to use for KDTree insertion
		// case 0 is X-Axis; case 1 is Y-Axis
		switch (depth % dim)
		{
			case 0:
				if (child.val.x() < val.x())
				{
					addLeft(child);
				} else
				{
					addRight(child);
				}
				break;
			case 1:
				if (child.val.y() < val.y())
				{
					addLeft(child);
				} else
				{
					addRight(child);
				}
				break;
			default:
		}
	}
	
	
	protected void addRight(final KDNode child)
	{
		if (rightChild == null)
		{
			rightChild = child;
			child.val.setParent(val);
			child.setDepth(getDepth() + 1);
		} else
		{
			// do not override already existing nodes. going one step down
			rightChild.addChild(child);
		}
	}
	
	
	protected void addLeft(final KDNode child)
	{
		if (leftChild == null)
		{
			leftChild = child;
			child.val.setParent(val);
			child.setDepth(getDepth() + 1);
		} else
		{
			// do not override already existing nodes. going one step down
			leftChild.addChild(child);
		}
	}
	
	
	protected KDNode getNearest(final Node target)
	{
		KDNode subtree;
		int comparison = 0;
		if ((depth % 2) == 0)
		{
			comparison = Double.valueOf(getVal().x()).compareTo(Double.valueOf(target.x()));
		} else
		{
			comparison = Double.valueOf(getVal().y()).compareTo(Double.valueOf(target.y()));
		}
		if ((comparison > 0) && (getLeft() != null))
		{
			subtree = getLeft().getNearest(target);
		} else if ((comparison < 0) && (getRight() != null))
		{
			subtree = getRight().getNearest(target);
		}
		else
		{
			subtree = this;
		}
		
		return subtree;
		
	}
	
	
	/**
	 * @param depth
	 */
	protected void setDepth(final int depth)
	{
		this.depth = depth;
	}
	
	
	/**
	 * @return
	 */
	public int getDepth()
	{
		return depth;
	}
	
	
	/**
	 * @return
	 */
	public Node getVal()
	{
		return val;
	}
	
	
	/**
	 * Replaces a child with another one
	 * 
	 * @param current
	 * @param newChild
	 */
	public void replaceChild(final KDNode current, final KDNode newChild)
	{
		if (leftChild == current)
		{
			leftChild = newChild;
		} else
		{
			rightChild = newChild;
		}
		newChild.setDepth(getDepth() + 1);
		newChild.rebuild();
	}
	
	
	/**
	 * applies new depths for all subtrees
	 * TODO DanielAl if old depth%2 = 0 and newdepth%2 = 1 , there is a problem, because it isn't a proper kd tree
	 * anymore
	 */
	private void rebuild()
	{
		if (leftChild != null)
		{
			leftChild.setDepth(getDepth() + 1);
			leftChild.rebuild();
		}
		if (rightChild != null)
		{
			rightChild.setDepth(getDepth() + 1);
			rightChild.rebuild();
		}
	}
	
	
	/**
	 * @param a
	 * @return
	 */
	@Override
	public int compareTo(final Node a)
	{
		// switch over depth%dim, because this will switch the dimensions to use for KDTree insertion
		// case 0 is X-Axis; case 1 is Y-Axis
		switch (depth % dim)
		{
			case 0:
				if (a.x() < getVal().x())
				{
					return -1;
				}
				return 1;
			case 1:
				if (a.y() < getVal().y())
				{
					return -1;
				}
				return 1;
			default:
		}
		return 0;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + depth;
		result = (prime * result) + ((leftChild == null) ? 0 : leftChild.hashCode());
		result = (prime * result) + ((rightChild == null) ? 0 : rightChild.hashCode());
		result = (prime * result) + ((val == null) ? 0 : val.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object a)
	{
		if ((this == a)
				|| ((a != null)
						&& (a instanceof KDNode)
						&& (((KDNode) a).getDepth() == getDepth())
						&& ((KDNode) a).getVal().equals(getVal())
						&& ((KDNode) a).getLeft().equals(getLeft())
						&& ((KDNode) a).getRight().equals(getRight())))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @return
	 */
	public KDNode getRight()
	{
		return rightChild;
	}
	
	
	/**
	 * @return
	 */
	public KDNode getLeft()
	{
		return leftChild;
	}
}
