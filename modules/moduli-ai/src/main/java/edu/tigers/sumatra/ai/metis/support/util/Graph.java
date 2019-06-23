/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Straightforward undirected graph implementation.
 * Nodes are generic type N.
 *
 * @author Paul Chew
 *         Created November, December 2007. For use in Delaunay/Voronoi code.
 */
public class Graph<N>
{
	
	private Map<N, Set<N>> theNeighbors = // Node -> adjacent nodes
			new HashMap<>();
	private Set<N> theNodeSet = // Set view of all nodes
			Collections.unmodifiableSet(theNeighbors.keySet());
	
	
	/**
	 * Add a node. If node is already in graph then no change.
	 * 
	 * @param node the node to add
	 */
	public void add(N node)
	{
		if (theNeighbors.containsKey(node))
			return;
		theNeighbors.put(node, new ArraySet<>());
	}
	
	
	/**
	 * Add a link. If the link is already in graph then no change.
	 * 
	 * @param nodeA one end of the link
	 * @param nodeB the other end of the link
	 * @throws NullPointerException if either endpoint is not in graph
	 */
	public void add(N nodeA, N nodeB)
	{
		theNeighbors.get(nodeA).add(nodeB);
		theNeighbors.get(nodeB).add(nodeA);
	}
	
	
	/**
	 * Remove node and any links that use node. If node not in graph, nothing
	 * happens.
	 * 
	 * @param node the node to remove.
	 */
	public void remove(N node)
	{
		if (!theNeighbors.containsKey(node))
			return;
		for (N neighbor : theNeighbors.get(node))
			theNeighbors.get(neighbor).remove(node); // Remove "to" links
		theNeighbors.get(node).clear(); // Remove "from" links
		theNeighbors.remove(node); // Remove the node
	}
	
	
	/**
	 * Remove the specified link. If link not in graph, nothing happens.
	 * 
	 * @param nodeA one end of the link
	 * @param nodeB the other end of the link
	 * @throws NullPointerException if either endpoint is not in graph
	 */
	public void remove(N nodeA, N nodeB)
	{
		theNeighbors.get(nodeA).remove(nodeB);
		theNeighbors.get(nodeB).remove(nodeA);
	}
	
	
	/**
	 * Report all the neighbors of node.
	 * 
	 * @param node the node
	 * @return the neighbors of node
	 * @throws NullPointerException if node does not appear in graph
	 */
	public Set<N> neighbors(N node)
	{
		return Collections.unmodifiableSet(theNeighbors.get(node));
	}
	
	
	/**
	 * Returns an unmodifiable Set view of the nodes contained in this graph.
	 * The set is backed by the graph, so changes to the graph are reflected in
	 * the set.
	 * 
	 * @return a Set view of the graph's node set
	 */
	public Set<N> nodeSet()
	{
		return theNodeSet;
	}
	
}
