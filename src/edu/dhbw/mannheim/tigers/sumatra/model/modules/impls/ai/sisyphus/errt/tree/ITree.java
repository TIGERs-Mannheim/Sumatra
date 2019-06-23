/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree;

import java.util.Set;


/**
 * every Tree used by ERRT should implement this interface
 * 
 * @author DirkK
 */
public interface ITree
{
	/**
	 * get the nearest node to a certain target node on the field
	 * 
	 * @param target
	 * @param allowRoot
	 * @return
	 */
	Node getNearest(Node target, boolean allowRoot);
	
	
	/**
	 * add a node to the tree
	 * 
	 * @param father the father/parent of the new node
	 * @param newNode the node which should be added to the tree
	 */
	void add(Node father, Node newNode);
	
	
	/**
	 * get the root of the path
	 * 
	 * @return the node where the bot is
	 */
	Node getRoot();
	
	
	/**
	 * Get a set of all used Nodes in this tree
	 * 
	 * @return
	 */
	Set<Node> getAllNodes();
	
	
	/**
	 * Get a set of all Leaf Nodes in this tree
	 * 
	 * @return
	 */
	Set<Node> getAllLeafs();
	
	
}
