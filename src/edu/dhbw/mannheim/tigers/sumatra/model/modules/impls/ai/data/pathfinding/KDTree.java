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



/**
 * efficient way to store the nodes in. based on its structure, nearest neighbour search is very fast.
 * to get to know how exactly it works, please read at http://en.wikipedia.org/wiki/Kd-tree
 * 
 * @author König
 * 
 */
public class KDTree
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** root node...having this node, every other one can be found */
	public KDNode root;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KDTree(KDNode root)
	{
		this.root = root;
		
		//don't know if starting with x or y is better...this choice was at random
		root.dim = EKDDimension.X_PLANE;
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * returns the node that represents the rectangle, the new node is in.
	 * 
	 * less: left. equal or bigger: right
	 * 
	 * @param newKDNode
	 */
	public KDNode getKdPseudoNearest(KDNode newKDNode)
	{		
		KDNode currentNode = root;
		KDNode pseudoNearest = null;
		
		while(pseudoNearest == null)
		{
			if(currentNode.dim == EKDDimension.X_PLANE)
			{
				//x-plane
				if(newKDNode.x < currentNode.x)
				{
					//left subtree
					if(currentNode.leftChild != null)
					{
						//traverse tree a bit more
						currentNode = currentNode.leftChild;
					}
					else
					{
						pseudoNearest = currentNode;
					}
				}
				else
				{
					//right subtree
					if(currentNode.rightChild != null)
					{
						//traverse tree a bit more
						currentNode = currentNode.rightChild;
					}
					else
					{
						pseudoNearest = currentNode;
					}
				}
			}
			else //currentNode.dim == EKDDimension.X_PLANE <-- this is for sure as long as there are only two dimensions
			{
				//y-plane
				if(newKDNode.y < currentNode.y)
				{
					//left subtree
					if(currentNode.leftChild != null)
					{
						//traverse tree a bit more
						currentNode = currentNode.leftChild;
					}
					else
					{
						pseudoNearest = currentNode;
					}
				}
				else
				{
					//right subtree
					if(currentNode.rightChild != null)
					{
						//traverse tree a bit more
						currentNode = currentNode.rightChild;
					}
					else
					{
						pseudoNearest = currentNode;
					}
				}
			}		
		}
		return pseudoNearest;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
