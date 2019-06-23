/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt.tree.simple;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.sisyphus.errt.tree.ITree;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.Node;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * basic tree for the ERRT algorithm, every node can have an unlimited amount of children
 * 
 * @author dirk
 */
@Persistent(version = 1)
public class SimpleTree implements ITree
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log				= Logger.getLogger(SimpleTree.class.getName());
																
	private Node						root;
	private Node						goalNode;
											
	// lists all nodes
	// not regarded for smoothing
	private Set<Node>					listOfAll;
	private FieldHash<Node>			nodeHash;
											
	// Set of all Leaf nodes (calculated during adding a node)
	private Set<Node>					setOfLeafs;
											
	private int							nodesAll			= 0;
	private int							regardedNodes	= 0;
																
																
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private SimpleTree()
	{
	}
	
	
	/**
	 * @param root
	 * @param goal
	 */
	public SimpleTree(final IVector2 root, final Node goal)
	{
		goalNode = goal;
		this.root = new Node(root);
		listOfAll = new HashSet<Node>();
		listOfAll.add(this.root);
		setOfLeafs = new HashSet<Node>();
		
		nodeHash = new FieldHash<Node>(1000);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * get nearest node in existing nodeStorage to nextNode
	 * 
	 * @param target
	 * @return
	 */
	@Override
	public Node getNearest(final Node target, final boolean allowRoot)
	{
		// calculate with squares, because real distance is not needed and: if a^2 > b^2 then |a| > |b|
		// longer than possible -> first tested node will be nearer
		double minSquareDistance = Double.MAX_VALUE;
		
		double currentSquareDistance;
		
		Node nearestNode = root;
		
		Collection<Node> nodesToSearch = listOfAll;
		
		Collection<Node> nearByNodes = nodeHash.find(target.x(), target.y());
		nodesAll += nodesToSearch.size();
		
		// take only nearby nodes if there are some, improves performance by 10-15%
		if (!nearByNodes.isEmpty())
		{
			nodesToSearch = nearByNodes;
		}
		regardedNodes += nodesToSearch.size();
		
		for (final Node currentNode : nodesToSearch)
		{
			currentSquareDistance = GeoMath.distancePPSqr(target, currentNode);
			
			// found a better one
			
			if ((currentSquareDistance < minSquareDistance) || (!allowRoot && nearestNode.equals(root)))
			{
				if (!currentNode.equals(root) || allowRoot)
				{
					nearestNode = currentNode;
					minSquareDistance = currentSquareDistance;
				}
			}
		}
		
		return nearestNode;
	}
	
	
	/**
	 * TODO dirk, add comment!
	 * 
	 * @return
	 */
	public String regardedPercentage()
	{
		return regardedNodes + " / " + nodesAll + " = " + (((double) regardedNodes) / ((double) nodesAll));
	}
	
	
	/**
	 * add a node to the tree
	 * 
	 * @param father the father/parent of the new node
	 * @param newNode the node which should be added to the tree
	 */
	@Override
	public void add(final Node father, final Node newNode)
	{
		// add it to the list of all nodes
		if (!listOfAll.contains(newNode))
		{
			nodeHash.add(newNode.x(), newNode.y(), newNode);
			listOfAll.add(newNode);
		}
		if (setOfLeafs.contains(father))
		{
			setOfLeafs.remove(father);
		}
		if (newNode.getChildren().size() <= 0)
		{
			setOfLeafs.add(newNode);
		}
		father.addChild(newNode);
		newNode.setParent(father);
	}
	
	
	/**
	 * print the path stored in the tree
	 */
	public void printPath()
	{
		log.warn("Path (reverse):");
		Node currentNode = goalNode;
		while (currentNode.getParent() != null)
		{
			log.warn(currentNode.toString());
			currentNode = currentNode.getParent();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public Node getRoot()
	{
		if (root == null)
		{
			root = new Node(new Vector2());
		}
		return root;
	}
	
	
	@Override
	public Set<Node> getAllNodes()
	{
		return listOfAll;
	}
	
	
	@Override
	public Set<Node> getAllLeafs()
	{
		return listOfAll;
	}
	
	
}
