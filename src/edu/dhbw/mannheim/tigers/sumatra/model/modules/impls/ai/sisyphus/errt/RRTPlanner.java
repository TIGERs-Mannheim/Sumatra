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

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Node;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.dss.DynamicSafetySearch;


/**
 * This is part of Sisyphus. It calculates a path to target. It's not dynamic but calculates with static obstacles. The
 * dynamic part is done by {@link DynamicSafetySearch}
 * 
 * 
 * This version is the BASIS-RRT. NO EXTENSIONS
 * 
 * 
 * @author ChristianK
 * 
 */
public class RRTPlanner
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger						log					= Logger.getLogger(getClass());
		
	private final float	FIELD_LENGTH						= AIConfig.getGeometry().getFieldLength(); 
	private final float	FIELD_WIDTH							= AIConfig.getGeometry().getFieldWidth(); 
	private final float	BOT_RADIUS							= AIConfig.getGeometry().getBotRadius(); 
	private final float	BALL_RADIUS							= AIConfig.getGeometry().getBallRadius();
	
	/** distance between 2 nodes */
	private final float	STEP_SIZE							= AIConfig.getErrt().getStepSize(); 
	/** step size of the final path */
	private final float 	FINAL_STEP_SIZE					= AIConfig.getErrt().getFinalStepSize();	
	/** defines how much iterations will at most be created */
	private final float	MAX_ITERATIONS						= AIConfig.getErrt().getMaxIterations();
	/** distance, bots have to keep away from obstacles */
	private final float	SAFETY_DISTANCE					= AIConfig.getErrt().getSafetyDistance();
	/** possibility to choose targetNode as next goal */
	private final float	P_DESTINATION						= AIConfig.getErrt().getpDestination();
	//	/** possibility to choose a waypoint as next goal */
	//	private final float	P_WAYPOINT							= AIConfig.getErrt().getpWaypoint();
	/** how much target can differ from target of last cycle, so that oldPath still is checked */
	private final float	TOLLERABLE_TARGET_SHIFT			= AIConfig.getErrt().getTollerableTargetShift();
	/** tollerance for updating old path */
	private final float  POSITIONING_TOLLERANCE			= AIConfig.getTolerances().getPositioning();
	
	private WorldFrame			wFrame;
	private Path					oldPath;
	private Path					newPath;
	
	/** bot, path is calculated for */
	private TrackedTigerBot		thisBot;
	private int						botId;
	/** goal of path */
	private Vector2f				goal;
	private Node					goalNode;
	/** list, all bots except thisBot are stored in */
	private List<Node>			botPosList							= new ArrayList<Node>(12);
	/** output list with path in it */
	private List<IVector2>		pathPointList;
	/** shall the ball be considered? */
	private boolean				considerBall;
	/** random generator for the second R in RRT (Rapidly-Exploring RANDOM Trees) */
	private Random					generator							= new Random();
	/** generated possibility for decision which point shall be chosen */
	private float					p;
	
	private Vector2f				ballPos;
	
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * starts calculation in ERRT
	 * @param botId
	 * @param wFrame
	 * 
	 * @param oldPath
	 * @param considerBall
	 * @return
	 */
	public Path doCalculation(WorldFrame wFrame, int botId, Path oldPath, IVector2 goalVec, boolean considerBall)
	{
		// store parameters in local vars
		this.wFrame = wFrame;
		this.oldPath = oldPath;
		this.botId = botId;
		this.thisBot = wFrame.tigerBots.get(botId);
		this.goal = new Vector2f(goalVec);
		this.goalNode = new Node(goal);
		this.considerBall = considerBall;
		this.ballPos = new Vector2f(wFrame.ball.pos3());
		
		// all bots except thisBot in botPosList
		putBotsInList();
		
		// all checks, if there is a need for a new RRT-cycle, are done WITHOUT ball consideration
		// maybe this should be done, but i'm not quite sure about it
		// maybe there shouldn't be this checks, when ball has to be considered
		// i'll think about it, when the rest is done
		
		// direct way from current pos to target free? then use it
		newPath = checkDirectWayBotPos2Target();
		if (newPath != null)
		{
			// might be oldPath, but that doesn't matter, because changed-flag is already set in that case
			return newPath;
		}
		
		// is it possible to use oldPath?
		// is oldPath existent?
		if (oldPath != null && oldPath.path != null && oldPath.path.size() > 0)
		{
			oldPath = actualizeOldPath();
			
			Vector2f oldGoal = new Vector2f(oldPath.getGoal());
			if (oldGoal.equals(goal))
			{
				// way still free?
				if (isOldPathStillOK())
				{
					// System.out.println("out: old");
					oldPath.changed = false;
					return oldPath;
				}
			}
			
			// if target hasn't changed too much
			if (oldGoal.equals(goalNode, TOLLERABLE_TARGET_SHIFT))
			{
				if (isOldPathOnlySlightlyChanged())
				{
					oldPath.changed = true;
					oldPath.path.add(goal);
					
					return oldPath;
				}
			}
		}
		
		// if oldPath can be used without using RRT, this part will not be reached.
		// if here, then RRT will have to start working
		
		// we have to compute a new path
		ArrayList<Node> tree = growTree();
		
		final List<IVector2> pathPointList;
		
		if (tree != null)
		{
			// System.out.println("out: rrt");
			
			//			wayPointCache = fillWayPointCache();
			tree = makeTreeDoubleLinked(tree);
			
			tree = smoothTree(tree);
			reduceAmountOfPoints(tree);
			
			pathPointList = transformToPathPointList();
		} else
		{
			// if no way was found, choose the direct line to goal
			// this way is as good as every other one and it's the shortest
			// collisions will be prevented by DSS
			pathPointList = new ArrayList<IVector2>(1);
			
			pathPointList.add(new Vector2(goal));
			// System.out.println("out: not at all");
		}
		// System.out.println(this.goal);
		return new Path(botId, pathPointList);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * puts all bots (except bot, this path is for) in list, so i can iterate over them more easily
	 * 
	 */
	private void putBotsInList()
	{
		// clear botPosList from last run-cycle
		botPosList.clear();
		
		// store tigers
		for (TrackedTigerBot bot : wFrame.tigerBots.values())
		{
			// the bot itself is no obstacle
			if (bot.id != botId)
			{
				//if bot blocks goal, don't consider it
				if(!bot.pos.equals(goal, BOT_RADIUS))
				{
					botPosList.add(new Node(bot.pos));
				}
			}
		}
		// store opponents
		for (TrackedBot bot : wFrame.foeBots.values())
		{
			//if bot blocks goal, don't consider it
			if(!bot.pos.equals(goal, BOT_RADIUS))
			{
				botPosList.add(new Node(bot.pos));
			}
		}
	}
	
	
	/**
	 * check if direct path is free. if it is: is it changed?
	 * 
	 * @return either a path or, if direct way is not free, null
	 */
	private Path checkDirectWayBotPos2Target()
	{
		// check if direct connection from current pos to target is free
		if (isWayOK(thisBot.pos, goal))
		{
			pathPointList = new ArrayList<IVector2>(1);
			pathPointList.add(goal);
			
			Path newPath = new Path(thisBot.id, pathPointList);
			
			// if oldPath has only one element, it's already been the direct connection
			if (oldPath != null && oldPath.path.size() == 1)
			{
				// set changed = false, so skills are not enqueued again
				newPath.changed = false;
			}
			
			return newPath;
		} else
		{
			return null;
		}
	}
	
	
	/**
	 * checks direct connection between given point a (e.g. botPosition) and point b (e.g. target)
	 * 
	 * @param a start point
	 * @param b end point
	 * @return true if connection is FREE
	 */
	private boolean isWayOK(IVector2 a, IVector2 b)
	{
		if (!isBotInWay(a, b))
		{
			// ball is obstacle?
			if (considerBall)
			{
				return !isBallInWay(a, b);
			} else
			{
				return true;
			}
		}
		
		// YOOUUUUU SHALL NOT PASS! (watching LoTR right now^^`)
		return false;
	}
	
	
	/**
	 * checks result from last time
	 * 
	 * @return
	 */
	private boolean isOldPathStillOK()
	{
		int size = oldPath.path.size();
		
		// check way between current botpos and first pathpoint
		if (!isWayOK(thisBot.pos, oldPath.path.get(0)))
		{
			return false;
		}
		
		for (int i = 0; i < size - 1; ++i)
		{
			if (!isWayOK(oldPath.path.get(i), oldPath.path.get(i + 1)))
			{
				return false;
			}
		}
		
		// nothing hit yet? then path okay
		return true;
	}
	
	
	/**
	 * old goal is != new goal, but very close. so this method checks, if new goal can be reached from old goal
	 * 
	 * @return
	 */
	private boolean isOldPathOnlySlightlyChanged()
	{
		// new goal is in short range of old goal (has been checked in calling method)
		
		// check if old is still okay?
		if (!isOldPathStillOK())
		{
			return false;
		}
		
		// check if connection between last point of last cycle to new target is free
		int size = oldPath.path.size();
		if (!isWayOK(oldPath.path.get(size - 1), goal))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * starts the RRT
	 * 
	 * @return nodeStorage, null if no success
	 */
	private ArrayList<Node> growTree()
	{
		// indicates if the pathplanner has reached his final destination
		boolean isTargetReached = false;
		
		// kdTree with currentBotPos as root
		ArrayList<Node> tree = new ArrayList<Node>();
		tree.add(new Node(thisBot.pos));
		
		
		Node nearest;
		Node target;
		int iterations;
		for (iterations = 0; iterations < MAX_ITERATIONS && !isTargetReached; ++iterations)
		{
			// decide, if the next target is goal, a waypoint or a random node. returns the winner ---
			target = chooseTarget();
			
			// find nearest node to target. this one is called 'nearest'.
			nearest = getNearest(tree, target);
			
			// find point that is between 'target' and 'nearest' and distance to parent node is 'STEP_SETTING'
			Node extended = extendTree(target, nearest, STEP_SIZE);
			// System.out.println("t "+target+" n "+nearest+" e "+extended+" ok: "+isWayOK(nearest, extended) );
			// if nothing is hit while moving there, the node is ok, and we can add it to 'nodeStorage'
			
			//			// but first we have to check if same node is still nearest one...this is only neccessary by kd-trees
			//			nearest = getNearest(tree, extended); 
			if (isWayOK(nearest, extended))
			{
				nearest.addChild(extended);
				tree.add(extended);
				
				// check direct link between extended and goalNode
				if (isWayOK(extended, goalNode))
				{
					// why not just adding goalNode to nodeStorage? thats because then it's not possible to smooth the path
					// as good as with doing the following stuff
					
					// take little steps toward goal till being there
					// for loop to protect heapspace if something goes wrong
					tree = subdividePath(tree,extended, goalNode);
					
					isTargetReached = true;
				}
			}
		}
		// System.out.println("it "+iterations);
		// has target been reached or has MAY_ITERATIONS been reached?
		if (!isTargetReached)
		{
			// so many iterations and still no success
			// System.out.println(":(");
			return null;
		}
		
		return tree;
	}
	
	
	/**
	 * take little steps toward goal till being there
	 */
	private ArrayList<Node> subdividePath(ArrayList<Node> tree, Node start, Node end)
	{
		Node ext;
		int i;
		//for loop to protect heap space if something goes wrong
		for (i = 0; (!start.equals(end, FINAL_STEP_SIZE+1))
		&& i < 100; ++i)
		{
			ext = extendTree(end, start, FINAL_STEP_SIZE);
			tree.add(ext);
			start.addChild(ext);
			start.setSuccessor(ext);
			start = ext;
		}
		if(i == 100)
		{
			log.error("Method 'subdividePath' nearly has been in an endless-loop. That function seems to be not correct!. Please report to class-owner of RRTPlanner");
		}
		start.addChild(end);
		
		return tree; 
	}
	
	
	/**
	 * chooses between goal, waypoints and a random-point
	 * 
	 * @return target of next iteration
	 */
	private Node chooseTarget()
	{
		// generate new float-value between 0 and 1
		p = generator.nextFloat();
		
		if (p <= P_DESTINATION)
		{
			// target is chosen
			return goalNode;
		} 
		//			else if (p <= P_DESTINATION + P_WAYPOINT && !wayPointCache.isEmpty())
		//		{
		//			return getKDNodeFromWayPointCache();
		//		} 
		else
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
		// generator returns value between 0 and 1. Multiplication causes value between 0 and FIELDLENGTH (which is 6050)
		// subtraction causes a shift to the regular values from -3050 to +3050
		float x = (generator.nextFloat() * FIELD_LENGTH) - FIELD_LENGTH / 2;
		
		// see x
		float y = (generator.nextFloat() * FIELD_WIDTH) - FIELD_WIDTH / 2;
		
		return (new Node(x, y));
	}
	
	
	/**
	 * get nearest node in existing nodeStorage to nextNode
	 *
	 * @param nodeStorage
	 * @param nextTarget
	 * @return
	 */
	private Node getNearest(List<Node> nodeStorage, Node nextTarget)
	{
		//calculate with squares, because real distance is not needed and: if a^2 > b^2 then |a| > |b|
		float minSquareDistance = Float.MAX_VALUE; // longer than possible -> first tested node will be nearer
		
		float currentSquareDistance;
		
		//will be returned
		//initialized with current botPos
		Node nearestNode = new Node(thisBot.pos);
		
		for(Node currentNode : nodeStorage)
		{
			currentSquareDistance = AIMath.distancePPSqr(nextTarget, currentNode);
			
			//found a better one
			if (currentSquareDistance < minSquareDistance)
			{
				nearestNode = currentNode;
				minSquareDistance = currentSquareDistance;
			}
		}
		
		return nearestNode;
	}
	
	
	/**
	 * finds point that is between nearest node and target node and distance to nearest node is STEP_SIZE
	 * 
	 * @param target
	 * @param nearest
	 * @return
	 */
	private Node extendTree(Node target, Node nearest, float step)
	{
		Node extended = new Node(AIMath.stepAlongLine(nearest, target, step));
		
		// if coords are less than zero or bigger than the values shown below, it's not on the field anymore ---
		if (extended.x < -0.5f * FIELD_LENGTH)
		{
			extended.x = 0.5f * FIELD_LENGTH;
		} else if (extended.x > 0.5f * FIELD_LENGTH)
		{
			extended.x = 0.5f * FIELD_LENGTH;
		}
		if (extended.y < -0.5f * FIELD_WIDTH)
		{
			extended.y = -0.5f * FIELD_WIDTH;
		} else if (extended.y > 0.5f * FIELD_WIDTH)
		{
			extended.y = 0.5f * FIELD_WIDTH;
		}
		
		return extended;
	}
	
	/**
	 * transforms the smoothedNodeStorage into a List {@literal <Vector2>}. </br>
	 * 
	 * smoothedNodeStorage contains every node, generated by RRT.
	 * Also, they can only be read from back to front, because of the linked list
	 * 
	 * @param smoothedNodeStorage
	 * @return result
	 */
	private List<IVector2> transformToPathPointList()
	{
		// last element, added to nodeStorage is goal (or another node, damn close to it)
		Node currentNode = goalNode;
		
		List<IVector2> result = new ArrayList<IVector2>();
		// result.add(0, new Vector2(currentNode , 0 , endAngle));
		while (currentNode.parent != null)
		{
			result.add(0, new Vector2(currentNode));
			currentNode = currentNode.parent;
		}
		
		return result;
	}
	
	
	/**
	 * bot moves, so it has to be checked, if bot already reached a path point. </br>
	 * if so, removes it
	 * 
	 * @return
	 */
	private Path actualizeOldPath()
	{
		// should run till return statement is reached
		while (true)
		{
			if (oldPath.path.size() > 1)
			{
				IVector2 ppA = oldPath.path.get(0);
				IVector2 ppB = oldPath.path.get(1);
				
				float distanceX = ppB.x() - ppA.x();
				float distanceY = ppB.y() - ppA.y();
				
				// should run till return statement is reached
				float u = ((thisBot.pos.x - ppA.x()) * distanceX + (thisBot.pos.y - ppA.y()) * distanceY)
				/ (distanceX * distanceX + distanceY * distanceY);
				
				if (u < 0)
				{
					// bot is before ppA, i.e. path only has to be actualized, if distance is below POSITIONING_TOLLERANCE
					if(thisBot.pos.equals(ppA, POSITIONING_TOLLERANCE))
					{
						oldPath.path.remove(0);
						oldPath.changed = true;
					}
					return oldPath;
				} else
				{
					// bot has already gone a part of the path
					// delete first Vector2 and check again
					oldPath.path.remove(0);
					oldPath.changed = true;
				}
			} else
			{
				return oldPath;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- temp methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * checks if the direct link between two points is free. returns true if a collision happens
	 */
	private synchronized boolean isBotInWay(IVector2 a, IVector2 b)
	{
		// --- CORRECT, BUT SLOW... ---
		
		// System.out.println("-------------");
		// System.out.println("start: "+xNodeA+" "+yNodeA);
		// System.out.println("dest : "+xNodeB+" "+yNodeB);
		// System.out.println("obst : "+botPos.firstElement().x+" "+botPos.firstElement().y);
		// System.out.println("<<<<<<<<<<<<<<<<<<<<");
		
		for (Node pos : botPosList)
		{
			float distanceX = b.x() - a.x();
			float distanceY = b.y() - a.y();
			IVector2 nearest = null;
			
			float u = ((pos.x - a.x()) * distanceX + (pos.y - a.y()) * distanceY)
			/ (distanceX * distanceX + distanceY * distanceY);
			
			if (u < 0)
			{
				nearest = a;
			} else if (u > 1)
			{
				nearest = b;
			} else
			{
				// nearest point on line is between nodeA and nodeB
				nearest = new Node(a.x() + (int) (u * distanceX), a.y() + (int) (u * distanceY));
			}
			
			if (nearest.equals(pos , 2 * BOT_RADIUS + SAFETY_DISTANCE))
			{
				// System.out.println("hit1");
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * checks if bot would be hit by driving
	 * algoritm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isBallInWay(IVector2 nodeA, IVector2 nodeB)
	{
		float distanceX = nodeB.x() - nodeA.x();
		float distanceY = nodeB.y() - nodeA.y();
		IVector2 nearest = null;
		
		float u = ((ballPos.x - nodeA.x()) * distanceX + (ballPos.y - nodeA.y()) * distanceY)
		/ (distanceX * distanceX + distanceY * distanceY);
		
		if (u < 0)
		{
			nearest = nodeA;
		} else if (u > 1)
		{
			nearest = nodeB;
		} else
		{
			// nearest point on line is between nodeA and nodeB
			nearest = new Node(nodeA.x() + (int) (u * distanceX), nodeA.y() + (int) (u * distanceY));
		}
		
		if (nearest.equals(ballPos , (BOT_RADIUS + BALL_RADIUS + SAFETY_DISTANCE)) )
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * creates double linked list, so that it can be traversed both directions
	 * 
	 * @param tree
	 * @return
	 */
	private ArrayList<Node> makeTreeDoubleLinked(ArrayList<Node> tree)
	{
		Node currentNode = goalNode;
		
		while(currentNode.parent != null)
		{
			currentNode.parent.setSuccessor(currentNode);
			currentNode = currentNode.parent;
		}
		
		return tree;
	}
	
	/**
	 * smoothes the path from current botPos to goal
	 * doesn't affect all the other Nodes in nodeStorage
	 * 
	 * at the moment: input will be changed
	 * @param tree 
	 * 
	 * @param nodeStorage
	 * @return
	 */
	private ArrayList<Node> smoothTree(ArrayList<Node> tree)
	{
		Node end = goalNode;
		Node start = tree.get(0);// start with first  (-->current bot pos);
		float eps = 0.01f;
		
		// begin with node after goalNode (direct connection to this node has already been checked in grow tree) 
		end = end.parent;
		
		// if null, then node before startnode is reached
		while(end.parent != null)
		{
			while (!start.equals(end,eps))
			{
				// check line between current node and compareNode
				if (isWayOK(start, end))
				{
					start.addChild(end);
					start.setSuccessor(end);
					
					//this may be a very long line. so i subdivide it.
					subdividePath(tree, start, end);
					//make it break
					start = end;
				}
				// if not, move forward (in this case backward ;-)
				else
				{
					start = start.getSuccessor();
				}
			}
			end = end.parent;
			start = tree.get(0);// start next time with first again (-->current bot pos);
		}
		// now the path between goal and start is much better *slap on my back*
		
		return tree;
	}
	
	private void reduceAmountOfPoints(ArrayList<Node> tree)
	{
		
		Node currentNode = goalNode;
		
		// if null, then node before startnode is reached
		while (currentNode.parent.parent != null)
		{
			// check line between current node and his grandfather. if free, let grandpa adopt you
			if (isWayOK(currentNode, currentNode.parent.parent))
			{
				currentNode.parent = currentNode.parent.parent;
				currentNode.parent.setSuccessor(currentNode); //this has been your grandpa some milli-seconds ago
			}
			// if not, move forward (in this case backward ;-)
			else
			{
				currentNode = currentNode.parent;
			}
		}
		
		// now the path between finalDestination and start has much less points *slap on my back*
	}
}