/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.simple;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embeddable;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;


/**
 * basic tree for the ERRT algorithm, every node can have an unlimited amount of children
 * 
 * @author dirk
 * 
 */
@Embeddable
public class SimpleTree implements ITree
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(SimpleTree.class.getName());
	
	
	private Node						root;
	private Node						goalNode;
	
	// lists all nodes
	// not regarded for smoothing
	private Set<Node>					listOfAll;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param root
	 * @param goal
	 */
	public SimpleTree(IVector2 root, Node goal)
	{
		goalNode = goal;
		this.root = new Node(root);
		listOfAll = new HashSet<Node>();
		listOfAll.add(this.root);
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
	public Node getNearest(Node target, boolean allowRoot)
	{
		// calculate with squares, because real distance is not needed and: if a^2 > b^2 then |a| > |b|
		// longer than possible -> first tested node will be nearer
		float minSquareDistance = Float.MAX_VALUE;
		
		float currentSquareDistance;
		
		Node nearestNode = root;
		
		for (final Node currentNode : listOfAll)
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
	 * add a node to the tree
	 * 
	 * @param father the father/parent of the new node
	 * @param newNode the node which should be added to the tree
	 * @param isSuccessor determines if the successor variable should be set, too
	 */
	@Override
	public void add(Node father, Node newNode, boolean isSuccessor)
	{
		// add it to the list of all nodes
		if (!listOfAll.contains(newNode))
		{
			listOfAll.add(newNode);
		}
		father.addChild(newNode);
		newNode.setParent(father);
		
		// if the successors are already set they have to be updated, too
		if (isSuccessor)
		{
			father.setSuccessor(newNode);
		}
	}
	
	
	/**
	 * 
	 * add a node to the tree
	 * 
	 * @param father the father/parent of the new node
	 * @param newNode the node which should be added to the tree
	 */
	public void add(Node father, Node newNode)
	{
		add(father, newNode, false);
	}
	
	
	@Override
	public void removeBetween(Node startNode, Node endNode, boolean isSuccessor)
	{
		startNode.addChild(endNode);
		endNode.setParent(startNode);
		if (isSuccessor)
		{
			startNode.setSuccessor(endNode);
		}
	}
	
	
	/**
	 * set the successor variable for the path
	 */
	@Override
	public void makeDoubleLinkedList()
	{
		Node currentNode = goalNode;
		while (currentNode.getParent() != null)
		{
			final Node parent = currentNode.getParent();
			parent.setSuccessor(currentNode);
			currentNode = parent;
		}
	}
	
	
	/**
	 * print the path stored in the tree
	 */
	public void printPath()
	{
		log.warn("Path:");
		Node currentNode = root;
		while (currentNode.getSuccessor() != null)
		{
			log.warn(currentNode.toString());
			currentNode = currentNode.getSuccessor();
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
}
