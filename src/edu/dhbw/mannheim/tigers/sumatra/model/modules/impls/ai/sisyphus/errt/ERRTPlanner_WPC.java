/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Christian
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.IPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.ParameterDebugger;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.simple.SimpleTree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.waypoints.Waypoints;


/**
 * This is part of Sisyphus. It calculates a path to target. It's not dynamic but calculates with static obstacles.
 * 
 * This version has a waypoint cache
 * 
 * For every bot a single ERRTPlanner_WPC is needed
 * 
 * @author DirkK klostermannnn@googlemail.com
 */
public class ERRTPlanner_WPC implements IPathFinder
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log			= Logger.getLogger(ERRTPlanner_WPC.class.getName());
	
	/** bot, path is calculated for */
	private TrackedTigerBot				thisBot;
	
	/** the goal of the calculated path */
	private Node							goalNode;
	
	/** waypoint cache */
	private Waypoints						waypoints;
	
	/** Information about the Field, calculates whether a way is free */
	private FieldInformation			fieldInfo;
	
	private final ParameterDebugger	paramDebug	= new ParameterDebugger();
	
	private TuneableParameter			adjustableParams;
	
	private ITree							ramboTree	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ERRTPlanner_WPC()
	{
		adjustableParams = new TuneableParameter();
		waypoints = new Waypoints(adjustableParams);
	}
	
	
	@Override
	public Path calcPath(PathFinderInput pathFinderInput)
	{
		// TODO: DirkK delete this if you want to use the pp test play
		adjustableParams = new TuneableParameter();
		
		List<Path> pathes = new ArrayList<Path>();
		List<IVector2> intermediates = new ArrayList<IVector2>(pathFinderInput.getMoveCon().getIntermediateStops());
		intermediates.add(new Vector2f(pathFinderInput.getFieldInfo().getPreprocessedTarget()));
		for (int i = 0; i < intermediates.size(); i++)
		{
			IVector2 intermediate = intermediates.get(i);
			IVector2 start;
			if (i == 0)
			{
				start = pathFinderInput.getFieldInfo().getPreprocessedStart();
			} else
			{
				start = pathes.get(i - 1).getPath().get(pathes.get(i - 1).getPath().size() - 1);
			}
			boolean isPathToTarget = false;
			if (i == (intermediates.size() - 1))
			{
				isPathToTarget = true;
			}
			pathFinderInput.getFieldInfo().addIgnoredPoitn(intermediate);
			pathes.add(doCalculation(pathFinderInput, start, intermediate, !isPathToTarget, false));
		}
		return merge(pathes);
	}
	
	
	private Path merge(List<Path> pathes)
	{
		int lastVector = 0;
		Path pathToUse = pathes.get(pathes.size() - 1);
		pathes.remove(pathes.size() - 1);
		for (Path intermediatePath : pathes)
		{
			// intermediatePath.getPath().remove(intermediatePath.getPath().get(intermediatePath.getPath().size() - 1));
			pathToUse.getPath().addAll(lastVector, intermediatePath.getPath());
			lastVector = intermediatePath.size();
		}
		return pathToUse;
	}
	
	
	/**
	 * starts calculation in ERRT
	 * @param pathFinderInput
	 * @param start
	 * @param target
	 * @param isIntermediate
	 * @param isSecondTry
	 * @return
	 */
	public Path doCalculation(PathFinderInput pathFinderInput, IVector2 start, IVector2 target, boolean isIntermediate,
			boolean isSecondTry)
	{
		// if (!pathFinderInput.getFieldInfo().isWayOK(start, start.addNew(new Vector2(0.01, 0.01))))
		// {
		// log.warn("Cannot calculate a path, start point blocked");
		// }
		// if (!pathFinderInput.getFieldInfo().isWayOK(target, target.addNew(new Vector2(0.01, 0.01))))
		// {
		// log.warn("Cannot calculate a path, target blocked");
		// }
		{
			if (!isSecondTry)
			{
				paramDebug.calculationStarted();
			} else
			{
				paramDebug.secondTry();
			}
		}
		
		// store parameters in local vars
		BotID botId = pathFinderInput.getBotId();
		thisBot = pathFinderInput.getwFrame().tigerBotsVisible.get(botId);
		goalNode = new Node(target);
		
		// all Informations about the field
		fieldInfo = pathFinderInput.getFieldInfo();
		
		// there are always three steps to find a way
		// 1. search for a way with a normal safety distance
		// 2. if no way was found, search again with a smaller safety distance
		// 3. if no way was found, take the direct path (RAMBO)
		if (isSecondTry)
		{
			fieldInfo.setUsedSafetyDistance(adjustableParams.getSecondSafetyDistance());
		} else
		{
			fieldInfo.setUsedSafetyDistance(adjustableParams.getSafetyDistance());
		}
		fieldInfo.setSafetyDistanceBall(adjustableParams.getSafetyDistanceBall());
		
		// clear WPC? yes if the goal has changed
		if (!waypoints.equalsGoal(goalNode) && !isIntermediate)
		{
			waypoints.clear(goalNode);
		}
		
		boolean pathChanged = true;
		Path oldPath = pathFinderInput.getExistingPathes().get(botId);
		if ((oldPath != null) && !oldPath.getPath().isEmpty() && checkOldWay(oldPath))
		{
			pathChanged = false;
		}
		
		// let the tree grow until the goal is reached
		final ITree tree = growTree(start);
		List<IVector2> pathPointList = null;
		
		boolean rambo = false;
		
		if (tree != null)
		{
			// smoothes the tree
			smoothTree(tree, pathFinderInput);
			
			// transform it to a list
			pathPointList = transformToPathPointList();
			
			// fill the waypoint cache with this tree
			waypoints.fillWPC(pathPointList, goalNode);
			
			
		} else
		{
			if (!isSecondTry)
			{
				// boooo :(
				// no way has been found...lets try a shorter safety distance this time
				return doCalculation(pathFinderInput, start, target, isIntermediate, true);
			}
			// if no way was found for the second time, choose the direct line to goal
			// this way is as good as every other one and it's the shortest
			pathPointList = new ArrayList<IVector2>(2);
			paramDebug.ramboChosen();
			rambo = true;
			pathPointList.add(start);
			pathPointList.add(target);
		}
		
		paramDebug.calculationFinished();
		
		Path newPath = new Path(botId, pathPointList, pathFinderInput.getTarget(), pathFinderInput.getDstOrient());
		
		// if the start was adjusted because of the ball, add a direct way to the ball
		if (fieldInfo.isStartAdjustedBecauseOfBall())
		{
			newPath.getPath().add(0, thisBot.getPos());
		}
		// if the target was adjusted because of the ball, add a direct way to the ball
		if (fieldInfo.isTargetAdjustedBecauseOfBall()
				&& pathFinderInput.getMoveCon().getVelAtDestination().equals(Vector2.ZERO_VECTOR, 0.01f))
		{
			newPath.getPath().add(pathFinderInput.getTarget());
		}
		
		// if the bot is currently illegally in the penalty area, kick him out directly
		if (fieldInfo.isBotIllegallyInPenaltyArea() && (thisBot.getPos().subtractNew(target).getLength2() > 500))
		{
			// add a node outside the penalty area, if the bot should leave this area asap
			newPath.getPath().add(0,
					new Node(fieldInfo.getNearestNodeOutsidePenArea().addNew(thisBot.getVel().scaleToNew(500))));
		}
		
		
		newPath.setChanged(pathChanged);
		newPath.setRambo(rambo);
		if (rambo)
		{
			newPath.setTree(ramboTree);
		}
		return newPath;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private boolean checkOldWay(Path path)
	{
		for (int i = 0; i < (path.getPath().size() - 1); i++)
		{
			IVector2 pathPoint = path.getPath().get(i);
			IVector2 nextPoint = path.getPath().get(i + 1);
			if (!fieldInfo.isWayOK(pathPoint, nextPoint))
			{
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * starts the RRT
	 * 
	 * @return nodeStorage, null if no success
	 */
	private ITree growTree(IVector2 start)
	{
		// indicates if the pathplanner has reached his final destination
		boolean isTargetReached = false;
		
		// kdTree with currentBotPos as root
		final ITree tree = new SimpleTree(new Node(start), goalNode);
		
		
		for (int i = 0; (i < adjustableParams.getMaxIterations()) && !isTargetReached; i++)
		{
			// decide, if the next target is goal, a waypoint or a random node. returns the winner ---
			final Node directionNode = chooseTarget(thisBot.getId());
			
			// find nearest node to target. this one is called 'nearest'.
			final Node nearest = tree.getNearest(directionNode, false);
			
			// find point that is between 'target' and 'nearest' and distance to parent node is 'STEP_SETTING'
			final Node extended = new Node(GeoMath.stepAlongLine(nearest, directionNode, adjustableParams.getStepSize()));
			
			// if nothing is hit while moving there, the node is ok, and we can add it to 'nodeStorage'
			// but first we have to check if same node is still nearest one...this is only necessary by kd-trees
			if (fieldInfo.isWayOK(nearest, extended))
			{
				tree.add(nearest, extended, false);
				// check direct link between extended and goalNode
				if (fieldInfo.isWayOK(extended, goalNode))
				{
					// why not just adding goalNode to nodeStorage? thats because then it's not possible to smooth the path
					// as good as with doing the following stuff
					// take little steps toward goal till being there
					addSubdividePath(tree, extended, goalNode, false);
					isTargetReached = true;
				}
			}
		}
		// has target been reached or has MAY_ITERATIONS been reached?
		if (!isTargetReached)
		{
			ramboTree = tree;
			// so many iterations and still no success
			return null;
		}
		
		return tree;
	}
	
	
	/**
	 * chooses between goal, waypoints and a random-point
	 * 
	 * @return target of next iteration
	 */
	private Node chooseTarget(BotID botID)
	{
		// generate new float-value between 0 and 1
		Random generator = new Random(System.nanoTime());
		float p = generator.nextFloat();
		
		
		if (p <= adjustableParams.getpGoal())
		{
			// goal
			return goalNode;
			
		} else if ((p <= (adjustableParams.getpGoal() + adjustableParams.getpWaypoint())) && !waypoints.isEmpty())
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
		return new Node(AIConfig.getGeometry().getFieldWBorders().getRandomPointInShape());
	}
	
	
	/**
	 * transforms the smoothedNodeStorage into a List {@literal <Vector2>}. </br>
	 * 
	 * smoothedNodeStorage contains every node, generated by RRT.
	 * Also, they can only be read from back to front, because of the linked list
	 * 
	 * @return result
	 */
	private List<IVector2> transformToPathPointList()
	{
		// last element, added to nodeStorage is goal (or another node, damn close to it)
		Node currentNode = goalNode;
		
		final List<IVector2> result = new ArrayList<IVector2>();
		while (currentNode.getParent() != null)
		{
			result.add(0, new Vector2(currentNode));
			currentNode = currentNode.getParent();
		}
		return result;
	}
	
	
	/**
	 * smoothes the path from current botPos to goal
	 * doesn't affect all the other Nodes in nodeStorage
	 * 
	 * at the moment: input will be changed
	 * @param tree
	 * @param pfi
	 */
	public void smoothTree(ITree tree, PathFinderInput pfi)
	{
		// set the successors on the path to have a double linked list
		tree.makeDoubleLinkedList();
		
		Node end = goalNode;
		Node start = tree.getRoot();
		
		final float eps = 0.01f;
		// begin with node after goalNode (direct connection to this node has already been checked in grow tree)
		end = end.getParent();
		// if null, then node before startnode is reached
		while ((end != null) && (end.getParent() != null))
		{
			while ((start != null) && !start.equals(end, eps))
			{
				// check line between current node and compareNode
				if (fieldInfo.isWayOK(start, end))
				{
					// remove all nodes between start and end
					tree.removeBetween(start, end, true);
					
					// this may be a very long line. so i subdivide it.
					addSubdividePath(tree, start, end, true);
					
					// make it break
					start = end;
					
					if (Float.isNaN(start.x()) || Float.isNaN(start.x()))
					{
						log.fatal("Böse smoothTree NaNs");
					}
				}
				// if not, move forward (in this case backward ;-)
				else
				{
					start = start.getSuccessor();
				}
			}
			end = end.getParent();
			// start next time with first again (-->current bot pos)
			start = tree.getRoot();
		}
		
		// reduce the points on long straight parts (easier spline afterwards)
		reduceAmountOfPoints(tree);
	}
	
	
	/**
	 * reduce the amount of points on a straight line of the path
	 * improves the spline
	 * 
	 * @param tree
	 */
	public void reduceAmountOfPoints(ITree tree)
	{
		Node currentNode = goalNode;
		
		// if null, then node before startnode is reached
		while ((currentNode != null) && (currentNode.getParent() != null)
				&& (currentNode.getParent().getParent() != null))
		{
			// check line between current node and his grandfather. if free, let grandpa adopt you
			if (fieldInfo.isWayOK(currentNode, currentNode.getParent().getParent()))
			{
				tree.removeBetween(currentNode.getParent().getParent(), currentNode, true);
			}
			// if not, move forward (in this case backward ;-)
			else
			{
				currentNode = currentNode.getParent();
			}
		}
		
		// now the path between finalDestination and start has much less points *slap on my back*
	}
	
	
	/**
	 * add a long straight path to the tree
	 * but subdivide it in small pieces
	 * 
	 * @param tree
	 * @param start the Node where the long path should start
	 * @param end the goal of the new path (will be created, too)
	 * @param isSuccessor determines if the successor variable should be set, too
	 */
	public void addSubdividePath(ITree tree, Node start, Node end, boolean isSuccessor)
	{
		// precaclulation to get the amount of intermediate points
		final Node subtractTemp = new Node(new Vector2f(start.x, start.y));
		final float dist = subtractTemp.subtract(end).getLength2();
		
		// amount of intermediate points needed
		final float iterations = dist / adjustableParams.getStepSize();
		
		Node currentNode = start;
		
		// for loop to protect heap space if something goes wrong
		for (int i = 0; (i < (Math.floor(iterations) - 1)); i++)
		{
			final Node ext = new Node(GeoMath.stepAlongLine(currentNode, end, adjustableParams.getStepSize()));
			tree.add(currentNode, ext, isSuccessor);
			currentNode = ext;
		}
		// add a link to the end node
		tree.add(currentNode, end, isSuccessor);
	}
	
	
	// -------------------- ONLY FOR DEBUG ---------------------
	/**
	 * @return the parameters used at the moment
	 */
	public TuneableParameter getAdjustableParams()
	{
		return adjustableParams;
	}
	
	
	/**
	 * print ghe gathered information stored in the debugger (calculation times, driving times,...)
	 */
	public void printDebuggerLogs()
	{
		log.warn(paramDebug.toString());
	}
	
	
	/**
	 * a path is complete and the goal is reached, for the next path other new parameters are taken
	 * @param adjustableParams
	 * @param wFrame
	 */
	public void goalReached(TuneableParameter adjustableParams, WorldFrame wFrame)
	{
		this.adjustableParams = adjustableParams;
		waypoints = new Waypoints(adjustableParams);
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
	
	
}
