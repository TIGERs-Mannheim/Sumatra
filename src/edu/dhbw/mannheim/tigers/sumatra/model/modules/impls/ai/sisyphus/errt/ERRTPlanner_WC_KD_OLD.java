/**
 * please let this in sumatra for a while. in case i have the time to implement this again, this file will be a great help
 * 
 * @author Christian König
 */


///*
// * *********************************************************
// * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
// * Project: TIGERS - Sumatra
// * Date: 06.11.2010
// * Author(s): Christian
// * 
// * *********************************************************
// */
//package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
//import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
//import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
//import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
//import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector3f;
//import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.KDNode;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.KDTree;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Node;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
//import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.dss.DynamicSafetySearch;
//
//
///**
// * This is part of Sisyphus. It calculates a path to target. It's not dynamic but calculates with static obstacles. The
// * dynamic part is done by {@link DynamicSafetySearch}
// * 
// * @author ChristianK
// * 
// */
//public class ERRTPlanner_WC_KD_OLD
//{
//	// --------------------------------------------------------------------------
//	// --- variables and constants ----------------------------------------------
//	// --------------------------------------------------------------------------
//	
//	private final float	FIELD_LENGTH						= AIConfig.getGeometry().getFieldLength(); 
//	private final float	FIELD_WIDTH							= AIConfig.getGeometry().getFieldWidth(); 
//	private final float	BOT_RADIUS							= AIConfig.getGeometry().getBotRadius(); 
//	private final float	BALL_RADIUS							= AIConfig.getGeometry().getBallRadius();
//
//	/** distance between 2 nodes */
//	private final float	STEP_SIZE							= AIConfig.getErrt().getStepSize(); 
//	/** how near a node has to be to the target to be sufficient; must be larger than stepSetting */
//	private final float	TARGET_SQ_DISCREPANCY_DEFAULT	= AIConfig.getErrt().getTargetSqDiscrepancy();
//	/** defines how much iterations will at most be created */
//	private final float	MAX_ITERATIONS						= AIConfig.getErrt().getMaxIterations();
//	/** possibility to choose targetNode as next goal */
//	private final float	P_DESTINATION						= AIConfig.getErrt().getpDestination();
//	/** possibility to choose a waypoint as next goal */
//	private final float	P_WAYPOINT							= AIConfig.getErrt().getpWaypoint();
//	/** how much target can differ from target of last cycle, so that oldPath still is checked */
//	private final float	TOLLERABLE_TARGET_SHIFT			= AIConfig.getErrt().getTollerableTargetShift();
//	private final float  POSITIONING_TOLLERANCE			= AIConfig.getTolerances().getPositioning();
//
//	private WorldFrame			wFrame;
//	private Path					oldPath;
//	private Path					newPath;
//	private int						botId;
//	/** bot, path is calculated for */
//	private TrackedTigerBot		thisBot;
//	/** goal of path */
//	private Vector2				goal;
//	/** goal of path, stored in 'Node' */
//	private KDNode					goalKdNode;
//	/** list, all bots except thisBot are stored in */
//	private List<Node>			botPosList							= new ArrayList<Node>(12);
//	/** output list with path in it */
//	private List<Vector2>		pathPointList;
//	/** shall the ball be considered? */
//	private boolean				considerBall;
//	/** random generator for the second R in RRT (Rapidly-Exploring RANDOM Trees) */
//	private Random					generator							= new Random();
//	/** generated possibility for decision which point shall be chosen */
//	private float					p;
//	private float					tollerableTargetSquareDiscrepany;
//	private Vector3f				ballPos;
//	/** storage for waypoints */
//	private List<KDNode>			wayPointCache						= new ArrayList<KDNode>();
//	
//
//	// --------------------------------------------------------------------------
//	// --- constructors ---------------------------------------------------------
//	// --------------------------------------------------------------------------
//	
//	/**
//	 * starts calculation in ERRT
//	 * @param botId
//	 * @param wFrame
//	 * 
//	 * @param oldPath
//	 * @param considerBall
//	 * @return
//	 */
//	public Path doCalculation(WorldFrame wFrame, int botId, Path oldPath, IVector2 goalVec, boolean considerBall)
//	{
//		// store parameters in local vars
//		this.wFrame = wFrame;
//		this.oldPath = oldPath;
//		this.botId = botId;
//		this.thisBot = wFrame.tigerBots.get(botId);
//		this.goal = new Vector2(goalVec);
//		this.goalKdNode = new KDNode(goal);
//		this.considerBall = considerBall;
//		this.ballPos = wFrame.ball.pos3();
//		if (oldPath != null)
//		{
//			this.wayPointCache = oldPath.wayPointCache;
//		}
//		// System.out.println("###############");
//		// System.out.println(this.goal);
//		// if ball has to be considered, the target discrepany should be way bigger (to be exact, a bot radius)
//		// if(this.considerBall)
//		// {
//		// tollerableTargetDiscrepany = TOLLERABLE_TARGET_SHIFT;
//		// } else
//		// {
//		tollerableTargetSquareDiscrepany = TARGET_SQ_DISCREPANCY_DEFAULT;
//		// }
//		
//		// all bots except thisBot in botPosList
//		putBotsInList();
//		
//		// all checks, if there is a need for a new RRT-cycle, are done WITHOUT ball consideration
//		// maybe this should be done, but i'm not quite sure about it
//		// maybe there shouldn't be this checks, when ball has to be considered
//		// i'll think about it, when the rest is done
//		
//		// direct way from current pos to target free? then use it
//		newPath = checkDirectWayBotPos2Target();
//		if (newPath != null)
//		{
//			// might be oldPath, but that doesn't matter, because changed-flag is already set in that case
//			// System.out.println("out: direct");
//			return newPath;
//		}
//		
//		// is it possible to use oldPath?
//		// is oldPath existent
//		if (oldPath != null && oldPath.path != null && oldPath.path.size() > 0)
//		{
//			oldPath = actualizeOldPath();
//			
//			Vector2 oldGoal = oldPath.getGoal();
//			if (oldGoal.equals(goal))
//			{
//				// way still free?
//				if (isOldPathStillOK())
//				{
//					// System.out.println("out: old");
//					oldPath.changed = false;
//					return oldPath;
//				}
//			}
//			
//			// if target hasn't changed too much
//			if (oldGoal.subtractNew(goal).getLength2() <= TOLLERABLE_TARGET_SHIFT)
//			{
//				if (isOldPathOnlySlightlyChanged())
//				{
//					oldPath.changed = true;
//					oldPath.path.add(goal);
//					
//					return oldPath;
//				}
//			}
//		}
//		
//		// if oldPath can be used without using RRT, this part will not be reached.
//		// if here, then RRT will have to start working
//		
//		// we have to compute a new path
//		KDTree kdTree = growTree();
//		
//		final List<Vector2> pathPointList;
//		
//		if (kdTree != null)
//		{
//			// System.out.println("out: rrt");
//			
//			wayPointCache = fillWayPointCache();
//			
//			smoothTree();
//			
//			pathPointList = transformToPathPointList();
//		} else
//		{
//			// if no way was found, choose the direct line to goal
//			// this way is as good as every other one and it's the shortest
//			// collisions will be prevented by DSS
//			pathPointList = new ArrayList<Vector2>(1);
//			
//			pathPointList.add(new Vector2(goal));
//			// System.out.println("out: not at all");
//		}
//		// System.out.println(this.goal);
//		return new Path(botId, pathPointList, wayPointCache);
//	}
//	
//
//	// --------------------------------------------------------------------------
//	// --- methods --------------------------------------------------------------
//	// --------------------------------------------------------------------------
//	
//	/**
//	 * puts all bots (except bot, this path is for) in list, so i can iterate over them more easily
//	 * 
//	 */
//	private void putBotsInList()
//	{
//		// clear botPosList from last run-cycle
//		botPosList.clear();
//		
//		// store tigers
//		for (TrackedTigerBot bot : wFrame.tigerBots.values())
//		{
//			// the bot itself is no obstacle
//			if (bot.id != botId)
//			{
//				botPosList.add(new Node(bot.pos));
//			}
//		}
//		// store opponents
//		for (TrackedBot bot : wFrame.foeBots.values())
//		{
//			botPosList.add(new Node(bot.pos));
//		}
//	}
//	
//
//	/**
//	 * check if direct path is free. if it is: is it changed?
//	 * 
//	 * @return either a path or, if direct way is not free, null
//	 */
//	private Path checkDirectWayBotPos2Target()
//	{
//		// check if direct connection from current pos to target is free
//		if (isWayOK(thisBot.pos, goal))
//		{
//			pathPointList = new ArrayList<Vector2>(1);
//			pathPointList.add(goal);
//			
//			Path newPath = new Path(thisBot.id, pathPointList);
//			
//			// if oldPath has only one element, it's already been the direct connection
//			if (oldPath != null && oldPath.path.size() == 1)
//			{
//				// set changed = false, so skills are not enqueued again
//				newPath.changed = false;
//			}
//			
//			return newPath;
//		} else
//		{
//			return null;
//		}
//	}
//	
//
//	/**
//	 * checks direct connection between given point a (e.g. botPosition) and point b (e.g. target)
//	 * 
//	 * @param a start point
//	 * @param b end point
//	 * @return true if connection is FREE
//	 */
//	private boolean isWayOK(IVector2 a, IVector2 b)
//	{
//		if (!isBotInWay(a, b))
//		{
//			// ball is obstacle?
//			if (considerBall)
//			{
//				return !isBallInWay(a, b);
//			} else
//			{
//				return true;
//			}
//		}
//		
//		// YOOUUUUU SHALL NOT PASS! (watching LoTR right now^^`)
//		return false;
//	}
//	
//
//	/**
//	 * checks result from last time
//	 * 
//	 * @return
//	 */
//	private boolean isOldPathStillOK()
//	{
//		int size = oldPath.path.size();
//		
//		// check way between current botpos and first pathpoint
//		if (!isWayOK(thisBot.pos, oldPath.path.get(0)))
//		{
//			return false;
//		}
//		
//		for (int i = 0; i < size - 1; ++i)
//		{
//			if (!isWayOK(oldPath.path.get(i), oldPath.path.get(i + 1)))
//			{
//				return false;
//			}
//		}
//		
//		// nothing hit yet? then path okay
//		return true;
//	}
//	
//
//	/**
//	 * old goal is != new goal, but very close. so this method checks, if new goal can be reached from old goal
//	 * 
//	 * @return
//	 */
//	private boolean isOldPathOnlySlightlyChanged()
//	{
//		// new goal is in short range of old goal (has been checked in calling method)
//		
//		// check if old is still okay?
//		if (!isOldPathStillOK())
//		{
//			return false;
//		}
//		
//		// check if connection between last point of last cycle to new target is free
//		int size = oldPath.path.size();
//		if (!isWayOK(oldPath.path.get(size - 1), goal))
//		{
//			return false;
//		}
//		return true;
//	}
//	
//
//	/**
//	 * starts the RRT
//	 * 
//	 * @return nodeStorage, null if no success
//	 */
//	private KDTree growTree()
//	{
//		// indicates if the pathplanner has reached his final destination
//		boolean isTargetReached = false;
//		
//		// kdTree with currentBotPos as root
//		KDTree kdTree = new KDTree(new KDNode(thisBot.pos));
//		
//
//		KDNode nearest;
//		KDNode target;
//		int iterations;
//		for (iterations = 0; iterations < MAX_ITERATIONS && !isTargetReached; ++iterations)
//		{
//			// decide, if the next target is goal, a waypoint or a random node. returns the winner ---
//			target = chooseTarget();
//			
//			// find nearest node to target. this one is called 'nearest'.
//			nearest = kdTree.getKdPseudoNearest(target);
//			
//			// find point that is between 'target' and 'nearest' and distance to parent node is 'STEP_SETTING'
//			KDNode extended = extendTree(target, nearest);
//			// System.out.println("t "+target+" n "+nearest+" e "+extended+" ok: "+isWayOK(nearest, extended) );
//			// if nothing is hit while moving there, the node is ok, and we can add it to 'nodeStorage'
//			
//			// but first we have to check if same node is still nearest one...
//			nearest = kdTree.getKdPseudoNearest(extended);
//			if (isWayOK(nearest, extended))
//			{
//				nearest.addChild(extended);
//				
//				// check direct link between extended and goalNode
//				if (isWayOK(extended, goalKdNode))
//				{
//					// why not just adding goalNode to nodeStorage? thats because then it's not possible to smooth the path
//					// as good as with doing the following stuff
//					
//					KDNode ext;
//					
//					// take little steps toward goal till being there
//					// for loop to protect heapspace if something goes wrong
//					for (int i = 0; (!(kdNodeSquareDistance(goalKdNode, extended) < tollerableTargetSquareDiscrepany))
//							&& i < 100; ++i)
//					{
//						ext = extendTree(goalKdNode, extended);
//						extended.addChild(ext);
//						
//						extended = ext;
//					}
//					
//					extended.addChild(goalKdNode);
//					
//					isTargetReached = true;
//				}
//			}
//		}
//		// System.out.println("it "+iterations);
//		// has target been reached or has MAY_ITERATIONS been reached?
//		if (!isTargetReached)
//		{
//			// so many iterations and still no success
//			// System.out.println(":(");
//			return null;
//		}
//		
//		return kdTree;
//	}
//	
//
//	/**
//	 * chooses between goal, waypoints and a random-point
//	 * 
//	 * @return target of next iteration
//	 */
//	private KDNode chooseTarget()
//	{
//		// generate new float-value between 0 and 1
//		p = generator.nextFloat();
//		
//		if (p <= P_DESTINATION)
//		{
//			// target is chosen
//			return goalKdNode;
//		} else if (p <= P_DESTINATION + P_WAYPOINT && !wayPointCache.isEmpty())
//		{
//			return getKDNodeFromWayPointCache();
//		} else
//		{
//			// random point
//			return createRandomNode();
//		}
//	}
//	
//
//	/**
//	 * creates a node, randomly placed on entire field
//	 * 
//	 * @return
//	 */
//	private KDNode createRandomNode()
//	{
//		// generator returns value between 0 and 1. Multiplication causes value between 0 and FIELDLENGTH (which is 6050)
//		// subtraction causes a shift to the regular values from -3050 to +3050
//		float x = (generator.nextFloat() * FIELD_LENGTH) - FIELD_LENGTH / 2;
//		
//		// see x
//		float y = (generator.nextFloat() * FIELD_WIDTH) - FIELD_WIDTH / 2;
//		
//		return (new KDNode(x, y));
//	}
//	
//
//	// /**
//	// * get nearest node in existing nodeStorage to nextNode
//	// *
//	// * @param nodeStorage
//	// * @param nextTarget
//	// * @return
//	// */
//	// private Node getNearest(List<Node> nodeStorage, Node nextTarget)
//	// {
//	// //calculate with squares, because real distance is not needed and: if a^2 > b^2 then |a| > |b|
//	// float minSquareDistance = Float.MAX_VALUE; // longer than possible -> first tested node will be nearer
//	//
//	// float currentSquareDistance;
//	//
//	// //will be returned
//	// //initialized with current botPos
//	// Node nearestNode = new Node(thisBot.pos);
//	//
//	// for(Node currentNode : nodeStorage)
//	// {
//	// currentSquareDistance = kdNodeSquareDistance(nextTarget, currentNode);
//	//
//	// //found a better one
//	// if (currentSquareDistance < minSquareDistance)
//	// {
//	// nearestNode = currentNode;
//	// minSquareDistance = currentSquareDistance;
//	// }
//	// }
//	//
//	// return nearestNode;
//	// }
//	
//	/**
//	 * returns distance^2
//	 * 
//	 * @param a
//	 * @param b
//	 * @return
//	 */
//	private float kdNodeSquareDistance(KDNode a, KDNode b)
//	{
//		// vector2 from a to b
//		Vector2 ab = a.subtractNew(b);
//		
//		return ab.x * ab.x + ab.y * ab.y;
//	}
//	
//
//	/**
//	 * finds point that is between nearest node and target node and distance to nearest node is STEP_SIZE
//	 * 
//	 * @param target
//	 * @param nearest
//	 * @return
//	 */
//	private KDNode extendTree(KDNode target, KDNode nearest)
//	{
//		KDNode extended = new KDNode(AIMath.stepAlongLine(nearest, target, STEP_SIZE));
//		
//		// if coords are less than zero or bigger than the values shown below, it's not on the field anymore ---
//		if (extended.x < -0.5f * FIELD_LENGTH)
//		{
//			extended.x = 0.5f * FIELD_LENGTH;
//		} else if (extended.x > 0.5f * FIELD_LENGTH)
//		{
//			extended.x = 0.5f * FIELD_LENGTH;
//		}
//		if (extended.y < -0.5f * FIELD_WIDTH)
//		{
//			extended.y = -0.5f * FIELD_WIDTH;
//		} else if (extended.y > 0.5f * FIELD_WIDTH)
//		{
//			extended.y = 0.5f * FIELD_WIDTH;
//		}
//		
//		return extended;
//	}
//	
//
//	/**
//	 * smoothes the path from current botPos to goal
//	 * doesn't affect all the other Nodes in nodeStorage
//	 * 
//	 * at the moment: input will be changed
//	 * 
//	 * @param nodeStorage
//	 * @return
//	 */
//	private void smoothTree()
//	{
//		KDNode currentNode = goalKdNode;
//		
//		// if null, then node before startnode is reached
//		while (currentNode.parent.parent != null)
//		{
//			// check line between current node and his grandfather. if free, let grandpa adopt you
//			if (isWayOK(currentNode, currentNode.parent.parent))
//			{
//				currentNode.parent = currentNode.parent.parent;
//			}
//			// if not, move forward (in this case backward ;-)
//			else
//			{
//				currentNode = currentNode.parent;
//			}
//		}
//		
//		// now the path between finalDestination and start is much better *slap on my back*
//	}
//	
//
//	/**
//	 * transforms the smoothedNodeStorage into a List {@literal <Vector2>}. </br>
//	 * 
//	 * smoothedNodeStorage contains every node, generated by RRT.
//	 * Also, they can only be read from back to front, because of the linked list
//	 * 
//	 * @param smoothedNodeStorage
//	 * @return result
//	 */
//	private List<Vector2> transformToPathPointList()
//	{
//		// last element, added to nodeStorage is goal (or another node, damn close to it)
//		KDNode currentNode = goalKdNode;
//		
//		List<Vector2> result = new ArrayList<Vector2>();
//		// result.add(0, new Vector2(currentNode , 0 , endAngle));
//		while (currentNode.parent != null)
//		{
//			result.add(0, new Vector2(currentNode));
//			currentNode = currentNode.parent;
//		}
//		
//		return result;
//	}
//	
//
//	/**
//	 * bot moves, so it has to be checked, if bot already reached a path point. </br>
//	 * if so, removes it
//	 * 
//	 * @return
//	 */
//	private Path actualizeOldPath()
//	{
//		// should run till return statement is reached
//		while (true)
//		{
//			if (oldPath.path.size() > 1)
//			{
//				Vector2 ppA = oldPath.path.get(0);
//				Vector2 ppB = oldPath.path.get(1);
//				
//				float distanceX = ppB.x - ppA.x;
//				float distanceY = ppB.y - ppA.y;
//				
//				// should run till return statement is reached
//				float u = ((thisBot.pos.x - ppA.x) * distanceX + (thisBot.pos.y - ppA.y) * distanceY)
//						/ (distanceX * distanceX + distanceY * distanceY);
//				
//				if (u < 0)
//				{
//					// bot is before ppA, i.e. path only has to be actualized, if distance is below POSITIONING_TOLLERANCE
//					if(thisBot.pos.equals(ppA, POSITIONING_TOLLERANCE))
//					{
//						oldPath.path.remove(0);
//						oldPath.changed = true;
//					}
//					return oldPath;
//				} else
//				{
//					// bot has already gone a part of the path
//					// delete first Vector2 and check again
//					oldPath.path.remove(0);
//					oldPath.changed = true;
//				}
//			} else
//			{
//				return oldPath;
//			}
//		}
//	}
//	
//
//	// --------------------------------------------------------------------------
//	// --- getter/setter --------------------------------------------------------
//	// --------------------------------------------------------------------------
//	
//
//	// --------------------------------------------------------------------------
//	// --- temp methods ---------------------------------------------------------
//	// --------------------------------------------------------------------------
//	
//	/**
//	 * checks if the direct link between two points is free. returns true if a collision happens
//	 */
//	private synchronized boolean isBotInWay(IVector2 a, IVector2 b)
//	{
//		// --- CORRECT, BUT SLOW... ---
//		
//		// System.out.println("-------------");
//		// System.out.println("start: "+xNodeA+" "+yNodeA);
//		// System.out.println("dest : "+xNodeB+" "+yNodeB);
//		// System.out.println("obst : "+botPos.firstElement().x+" "+botPos.firstElement().y);
//		// System.out.println("<<<<<<<<<<<<<<<<<<<<");
//		
//		for (Node pos : botPosList)
//		{
//			float distanceX = b.x() - a.x();
//			float distanceY = b.y() - a.y();
//			IVector2 nearest = null;
//			
//			float u = ((pos.x - a.x()) * distanceX + (pos.y - a.y()) * distanceY)
//					/ (distanceX * distanceX + distanceY * distanceY);
//			
//			if (u < 0)
//			{
//				nearest = a;
//			} else if (u > 1)
//			{
//				nearest = b;
//			} else
//			{
//				// nearest point on line is between nodeA and nodeB
//				nearest = new Node(a.x() + (int) (u * distanceX), a.y() + (int) (u * distanceY));
//			}
//			
//			if (AIMath.distancePP(nearest, pos) < (2 * BOT_RADIUS))
//			{
//				// System.out.println("hit1");
//				return true;
//			}
//		}
//		
//		return false;
//	}
//	
//
//	/**
//	 * checks if bot would be hit by driving
//	 * algoritm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
//	 * 
//	 * @param nodeA
//	 * @param nodeB
//	 * @return
//	 */
//	private boolean isBallInWay(IVector2 nodeA, IVector2 nodeB)
//	{
//		float distanceX = nodeB.x() - nodeA.x();
//		float distanceY = nodeB.y() - nodeA.y();
//		IVector2 nearest = null;
//		
//		float u = ((ballPos.x - nodeA.x()) * distanceX + (ballPos.y - nodeA.y()) * distanceY)
//				/ (distanceX * distanceX + distanceY * distanceY);
//		
//		if (u < 0)
//		{
//			nearest = nodeA;
//		} else if (u > 1)
//		{
//			nearest = nodeB;
//		} else
//		{
//			// nearest point on line is between nodeA and nodeB
//			nearest = new Node(nodeA.x() + (int) (u * distanceX), nodeA.y() + (int) (u * distanceY));
//		}
//		
//		if (AIMath.distancePP(nearest, ballPos) < BOT_RADIUS + BALL_RADIUS)
//		{
//			return true;
//		}
//		return false;
//	}
//	
//
//	/**
//	 * fills in the waypoint cache
//	 * @param nodeStorage
//	 * 
//	 * @return
//	 */
//	private List<KDNode> fillWayPointCache()
//	{
//		// last element, added to nodeStorage is goal.
//		KDNode currentNode = goalKdNode;
//		
//		// if null, then node before startnode is reached
//		while (currentNode.parent != null)
//		{
//			wayPointCache.add(currentNode);
//			
//			currentNode = currentNode.parent;
//		}
//		
//		// now every node is in waypointcache
//		return wayPointCache;
//	}
//	
//
//	/**
//	 * returns random node from waypointcache. only called in RRT when is not empty. you should leave it that way
//	 * 
//	 * @return
//	 */
//	private KDNode getKDNodeFromWayPointCache()
//	{
//		p = generator.nextFloat();
//		// random element within WPC
//		int i = (int) Math.round((p * (wayPointCache.size() - 1)) + 0.5);
//		
//		return wayPointCache.get(i);
//	}
//}
