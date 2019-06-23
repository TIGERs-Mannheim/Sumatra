/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Christian, DirkK, DanielAl
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.analyze.ParameterDebugger;
import edu.tigers.sumatra.ai.sisyphus.data.Path;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.ITree;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.Node;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.kd.KDTree;
import edu.tigers.sumatra.ai.sisyphus.errt.tree.simple.SimpleTree;
import edu.tigers.sumatra.ai.sisyphus.finder.AFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.FieldInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This is part of Sisyphus. It calculates a path to target. It's not dynamic but calculates with static obstacles.
 * This version has a waypoint cache
 * For every bot a single ERRTFinder is needed
 * 
 * @author DirkK klostermannnn@googlemail.com
 * @author Daniel Andres <andreslopez.daniel@gmail.com> (a bit ...)
 */
public class ERRTFinder extends AFinder
{
	private static final Logger		log			= Logger.getLogger(ERRTFinder.class.getName());
																
	/** bot, path is calculated for */
	private ITrackedBot					thisBot;
												
	/** the goal of the calculated path */
	private Node							goalNode;
												
	/** waypoint cache */
	private Waypoints						waypoints;
												
												
	private final ParameterDebugger	paramDebug	= new ParameterDebugger();
																
																
	private final Random					generator	= new Random();
																
																
	/**
	 */
	public ERRTFinder()
	{
		waypoints = new Waypoints();
	}
	
	
	/**
	 * starts calculation in ERRT
	 * 
	 * @param pathFinderInput
	 * @param start
	 * @param target
	 * @param isIntermediate
	 * @param isSecondTry
	 * @return
	 */
	@Override
	protected IPath doCalculation(final PathFinderInput pathFinderInput, final IVector2 start, final IVector2 target,
			final boolean isIntermediate, final boolean isSecondTry)
	{
		if (!isSecondTry)
		{
			paramDebug.calculationStarted();
		} else
		{
			paramDebug.secondTry();
		}
		
		FieldInformation fieldInfo = pathFinderInput.getFieldInfo();
		
		goalNode = new Node(target);
		
		thisBot = fieldInfo.getwFrame().getBot(pathFinderInput.getBotId());
		
		// all Informations about the field
		fieldInfo.setSecondTry(isSecondTry);
		
		// there are always three steps to find a way
		// 1. search for a way with a normal safety distance
		// 2. if no way was found, search again with a smaller safety distance
		// 3. if no way was found, take the direct path (RAMBO)
		
		// clear WPC? yes if the goal has changed
		if (!waypoints.equalsGoal(goalNode) && !isIntermediate)
		{
			waypoints.clear(goalNode);
		}
		
		// let the tree grow until the goal is reached
		ITree tree = growTree(start, fieldInfo);
		
		boolean rambo = false;
		if (tree == null)
		{
			// tree = ramboTree;
			rambo = true;
		}
		
		// transform it to a list of Nodes
		List<Node> nodeList = transformToNodeList();
		
		// transform it to a list of PathPoints
		List<IVector2> pathPointList = transformToPathPointList(nodeList);
		
		List<IVector2> unsmoothedPath = new ArrayList<IVector2>(pathPointList);
		
		// smoothes the tree
		smoothPath(pathPointList, pathFinderInput);
		
		// fill the waypoint cache with this tree
		waypoints.fillWPC(pathPointList, goalNode);
		
		if (rambo)
		{
			if (!isSecondTry)
			{
				// boooo :(
				// no way has been found...lets try a shorter safety distance this time
				rambo = false;
				return doCalculation(pathFinderInput, start, target, isIntermediate, true);
			}
			// if no way was found for the second time, choose the direct line to goal
			// this way is as good as every other one and it's the shortest
			paramDebug.ramboChosen();
		}
		
		paramDebug.calculationFinished();
		
		addPathPointsFromAdjustments(pathPointList, pathFinderInput, target);
		
		Path newPath = new Path(pathPointList, pathFinderInput.getDstOrient());
		newPath.setUnsmoothedPathPoints(unsmoothedPath);
		
		return newPath;
	}
	
	
	/**
	 * starts the RRT
	 * 
	 * @return nodeStorage, null if no success
	 */
	private ITree growTree(final IVector2 start, final FieldInformation fieldInfo)
	{
		// indicates if the pathplanner has reached his final destination
		boolean isTargetReached = false;
		
		// Tree with currentBotPos as root
		final ITree tree = new SimpleTree(new Node(start), goalNode);
		
		// int iterations = 0;
		for (int i = 0; (i < getAdjustableParams().getMaxIterations()) && !isTargetReached; i++)
		{
			// iterations = i;
			// decide, if the next target is goal, a waypoint or a random node. returns the winner ---
			final Node directionNode = chooseTarget(thisBot.getBotId());
			
			// find nearest node to target. this one is called 'nearest'.
			final Node nearest = getNearest(tree, directionNode);
			
			// find point that is between 'target' and 'nearest' and distance to parent node is 'STEP_SETTING'
			final Node extended = new Node(GeoMath.stepAlongLine(nearest, directionNode, getAdjustableParams()
					.getStepSize()));
					
			// if nothing is hit while moving there, the node is ok, and we can add it to 'nodeStorage'
			// but first we have to check if same node is still nearest one...this is only necessary by kd-trees
			if (fieldInfo.isWayOK(nearest, extended))
			{
				tree.add(nearest, extended);
				// check direct link between extended and goalNode
				if (fieldInfo.isWayOK(extended, goalNode))
				{
					// why not just adding goalNode to nodeStorage? thats because then it's not possible to smooth the path
					// as good as with doing the following stuff
					// take little steps toward goal till being there
					addSubdividePathToTree(tree, extended, goalNode);
					isTargetReached = true;
				}
			}
		}
		
		// if (iterations > 300)
		// {
		// log.warn(((SimpleTree) tree).regardedPercentage() + " iterations: " + iterations);
		// }
		
		// has target been reached or has MAY_ITERATIONS been reached?
		if (!isTargetReached)
		{
			// connect the node nearest to the goal with goal direclty -> RAMBO
			addSubdividePathToTree(tree, getNearest(tree, goalNode), goalNode);
			// so many iterations and still no success
			return null;
		}
		return tree;
	}
	
	
	private Node getNearest(final ITree tree, final Node target)
	{
		final Node nearest;
		// KD Trees have in this way a huge performance problem, as they are each time build completeley new. But there is
		// no simple way to extend a KDTree in a balanced way.
		if (getAdjustableParams().isUseKDTree())
		{
			KDTree kdTree = new KDTree();
			// Use leafs of the SimpleTree to hold the Building of the KDTree simple. But this means, that it can be not
			// the nearest. But the approximation is OK.
			// Alternative you can use tree.getAllNodes()
			kdTree.addAll(tree.getAllLeafs());
			nearest = kdTree.getNearest(target, false);
		} else
		{
			nearest = tree.getNearest(target, false);
		}
		return nearest;
	}
	
	
	/**
	 * chooses between goal, waypoints and a random-point
	 * 
	 * @return target of next iteration
	 */
	private Node chooseTarget(final BotID botID)
	{
		// generate new double-value between 0 and 1
		double p = generator.nextDouble();
		
		if (p <= getAdjustableParams().getpGoal())
		{
			// goal
			return goalNode;
			
		} else if ((p <= (getAdjustableParams().getpGoal() + getAdjustableParams().getpWaypoint()))
				&& !waypoints.isEmpty())
		{
			// waypoint
			final IVector2 temp = waypoints.getArbitraryNode();
			// if anything went wrong
			if (temp == null)
			{
				return createRandomNode();
			}
			return new Node(temp);
			
		} else
		{
			// random point
			return createRandomNode();
		}
	}
	
	
	/**
	 * creates a node, randomly placed on entire field
	 * 
	 * @return
	 */
	private Node createRandomNode()
	{
		return new Node(Geometry.getFieldWBorders().getRandomPointInShape(generator));
	}
	
	
	/**
	 * Transforms a List {@literal <Node>} to a list of {@literal <IVector2>}
	 * 
	 * @return result
	 */
	private List<IVector2> transformToPathPointList(final List<Node> nodeList)
	{
		final List<IVector2> result = new ArrayList<IVector2>(nodeList.size());
		for (Node node : nodeList)
		{
			result.add(new Vector2(node));
		}
		return result;
	}
	
	
	/**
	 * transforms a Node Storage Tree which contains a doubleLinkedList into a List {@literal <Node>}. </br>
	 * smoothedNodeStorage contains every node, generated by RRT.
	 * Also, they can only be read from back to front, because of the linked list
	 * 
	 * @return result
	 */
	private List<Node> transformToNodeList()
	{
		// last element, added to nodeStorage is goal (or another node, damn close to it)
		Node currentNode = goalNode;
		
		final List<Node> result = new ArrayList<Node>();
		while (currentNode.getParent() != null)
		{
			result.add(0, currentNode);
			currentNode = currentNode.getParent();
		}
		return result;
	}
	
	
	/**
	 * add a long straight path to the tree
	 * but subdivide it in small pieces
	 * 
	 * @param tree
	 * @param start the Node where the long path should start
	 * @param end the goal of the new path (will be created, too)
	 */
	private void addSubdividePathToTree(final ITree tree, final Node start, final Node end)
	{
		// remove the end Node from the Children list, because we want to add nodes in between
		start.getChildren().remove(end);
		
		// Create helper List with IVector2 to use AFinder#addSubdivdePath() and then add start node and end node to the
		// list
		List<IVector2> subPath = new ArrayList<IVector2>();
		subPath.add(start);
		subPath.add(end);
		
		// call AFinder#addSubdividePath() with the helper list and the position of the start node
		addSubdividePath(subPath, 0);
		
		Node prevNode = (Node) subPath.get(0);
		
		// iterate from second IVector2 to the end and create each a new Node of it and add it to the children of the
		// predecessor.
		for (int i = 1; i < (subPath.size() - 1); i++)
		{
			Node node = new Node(subPath.get(i));
			tree.add(prevNode, node);
			prevNode = node;
		}
		// add a link to the end node
		tree.add(prevNode, end);
	}
	
	
	// -------------------- ONLY FOR DEBUG ---------------------
	
	/**
	 * print the gathered information stored in the debugger (calculation times, driving times,...)
	 */
	public void printDebuggerLogs()
	{
		log.warn(paramDebug.toString());
	}
	
	
	/**
	 * a path is complete and the goal is reached, for the next path other new parameters are taken
	 * 
	 * @param adjustableParams
	 * @param wFrame
	 */
	public void goalReached(final TuneableParameter adjustableParams, final WorldFrame wFrame)
	{
		setAdjustableParams(adjustableParams);
		waypoints = new Waypoints();
		paramDebug.goalReached();
		paramDebug.changeParameterConfigToTest(adjustableParams);
	}
	
	
	/**
	 * a path is complete and the goal is reached
	 */
	public void goalReached()
	{
		paramDebug.goalReached();
	}
	
	
	/**
	 * @return the paramDebug
	 */
	public ParameterDebugger getParamDebug()
	{
		return paramDebug;
	}
	
	
}
