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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
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
public class ERRTPlanner_WPC
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger	log									= Logger.getLogger(getClass());
	
	// 100mm is the distance the cam sees behind the field in istanbul
	private final float		FIELD_LENGTH						= AIConfig.getGeometry().getFieldLength() + 100;
	// 150mm is the distance the cam sees next to the field in istanbul
	private final float		FIELD_WIDTH							= AIConfig.getGeometry().getFieldWidth() + 150;
	private final float		BOT_RADIUS							= AIConfig.getGeometry().getBotRadius();
	private final float		BALL_RADIUS							= AIConfig.getGeometry().getBallRadius();
	private final Goal		GOAL_OUR								= AIConfig.getGeometry().getGoalOur();
	private final Goal		GOAL_THEIR							= AIConfig.getGeometry().getGoalTheir();
	private final float		CENTER_CIRCLE_RADIUS				= AIConfig.getGeometry().getCenterCircleRadius();
	private final float		GOALPOST_RADIUS					= 10f;
	
	/** distance between 2 nodes */
	private final float		STEP_SIZE							= AIConfig.getErrt().getStepSize();
	/** step size of the final path */
	private final float		FINAL_STEP_SIZE					= AIConfig.getErrt().getFinalStepSize();
	/** defines how much iterations will at most be created */
	private final float		MAX_ITERATIONS						= AIConfig.getErrt().getMaxIterations();
	/** distance, bots have to keep away from obstacles */
	private final float		SAFETY_DISTANCE					= AIConfig.getErrt().getSafetyDistance();
	/** distance, bots have to keep away from obstacles when checking old path */
	// private final float SAFETY_DISTANCE_OLD_PATH = AIConfig.getErrt().getSafetyDistanceOldPath();
	// /** distance, bots have to keep away from obstacles when in second round */
	private final float		SAFETY_DISTANCE_SEC_TRY			= AIConfig.getErrt().getSafetyDistance2Try();
	/** possibility to choose targetNode as next goal */
	private final float		P_DESTINATION						= AIConfig.getErrt().getpDestination();
	/** possibility to choose a waypoint as next goal */
	private final float		P_WAYPOINT							= AIConfig.getErrt().getpWaypoint();
	/** size of waypointcache */
	private final int			WPC_SIZE								= AIConfig.getErrt().getWPCSize();
	/** how much target can differ from target of last cycle, so that oldPath still is checked */
	// private final float TOLLERABLE_TARGET_SHIFT = AIConfig.getErrt().getTollerableTargetShift();
	// /** how much target can differ from target of last cycle to use WPC */
	private final float		TOLLERABLE_TARGET_SHIFT_WPC	= AIConfig.getErrt().getTollerableTargetShiftWPC();
	// /** tollerance for updating old path */
	// private final float POSITIONING_TOLLERANCE = AIConfig.getTolerances().getPositioning();
	
	private WorldFrame		wFrame;
	// private Path oldPath;
	// private Path newPath;
	
	/** bot, path is calculated for */
	private TrackedTigerBot	thisBot;
	private int					botId;
	/** goal of path */
	private Vector2f			goal;
	private Node				goalNode;
	/** list, all bots except thisBot are stored in */
	private List<Node>		botPosList							= new ArrayList<Node>(12);
	/** output list with path in it */
	private List<IVector2>	pathPointList;
	/** shall the ball be considered? */
	private boolean			considerBall;
	/** random generator for the second R in RRT (Rapidly-Exploring RANDOM Trees) */
	private Random				generator							= new Random();
	/** generated possibility for decision which point shall be chosen */
	private float				p;
	/** waypoint cache */
	private Node[]				waypoints							= new Node[WPC_SIZE];
	/** fill level of WPC */
	int							WPCFillLvl							= 0;
	/** ball */
	private Vector2f			ballPos;
	// /** restricted area */
	// private List<I2DShape> restrictedAreas = new ArrayList<I2DShape>();
	/** it's kickoff so the centercircle and the opponents half are prohibited */
	private boolean			prohibitOpponentsHalf			= false;
	private boolean			prohibitCenterCircle				= false;
//	private boolean			prohibitOpponentPenArea			= false; //!!! NOT PROHIBITED BY THE RULES !!!
	private boolean			prohibitTigersPenArea			= true;
	private boolean			isFreekick							= false;
	
	private IVector2			centerPoint							= new Vector2f(0, 0);
//	private IVector2			opponentsPenAreaCircleCenter	= new Vector2f(GOAL_THEIR.getGoalCenter().x + 150,
//																					GOAL_THEIR.getGoalCenter().y);
	private IVector2			tigersPenAreaCircleCenter		= new Vector2f(GOAL_OUR.getGoalCenter().x - 150,
																					GOAL_OUR.getGoalCenter().y);
	private int					penAreaCircleRadius				= 650;																// häcks
	// häcks
	private int					distanceAtFreekick				= 500;
	
	private int					escapeStepSize						= 500;
	
	// private int NO_SAFETY = 0;
	
	// sounds weird, but the next two ones can be combined as you like
	private boolean			IS_AGRESSIVE						= true;
	private boolean			IS_RAMBO_MODE						= true;
	
	private float				usedSafetyDistance;
	
	
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
	public Path doCalculation(WorldFrame wFrame, int botId, Path oldPath, IVector2 goalVec, boolean considerBall,
			boolean isGoalie, EGameSituation gameSit, boolean isSecondTry)
	{
		// store parameters in local vars
		this.wFrame = wFrame;
		// this.oldPath = oldPath;
		this.botId = botId;
		this.thisBot = wFrame.tigerBots.get(botId);
		this.goal = new Vector2f(goalVec);
		this.goalNode = new Node(goal);
		this.considerBall = considerBall;
		this.ballPos = new Vector2f(wFrame.ball.pos3());
		
		if (isSecondTry)
		{
			usedSafetyDistance = SAFETY_DISTANCE_SEC_TRY;
		} else
		{
			usedSafetyDistance = SAFETY_DISTANCE;
		}
		
		if (gameSit == EGameSituation.KICK_OFF)
		{
			// if bot or goal in opponents half i still use it although it's kickoff
			if (thisBot.pos.x <= 0 && goal.x <= 0)
			{
				prohibitOpponentsHalf = true;
			}
			// if bot or goal are in centercircle i still use it although it's kickoff
			if (AIMath.distancePP(thisBot, centerPoint) > CENTER_CIRCLE_RADIUS
					&& AIMath.distancePP(goal, centerPoint) > CENTER_CIRCLE_RADIUS)
			{
				prohibitCenterCircle = true;
			}
		} else if (gameSit == EGameSituation.SET_PIECE)
		{
			if (AIMath.distancePP(thisBot, ballPos) > distanceAtFreekick
					&& AIMath.distancePP(goal, ballPos) > distanceAtFreekick)
			{
				isFreekick = true;
			}
		}
		
//		if (AIMath.distancePP(thisBot, opponentsPenAreaCircleCenter) < penAreaCircleRadius
//				|| AIMath.distancePP(goal, opponentsPenAreaCircleCenter) < penAreaCircleRadius)
//		{
//			prohibitOpponentPenArea = false;
//		}
		
		if (isGoalie)
		{
			prohibitTigersPenArea = false;
		} else if (AIMath.distancePP(thisBot, tigersPenAreaCircleCenter) < penAreaCircleRadius
				|| AIMath.distancePP(goal, tigersPenAreaCircleCenter) < penAreaCircleRadius)
		{
			prohibitTigersPenArea = false;
		}
		
		// for(I2DShape rA : restrictedAreas)
		// {
		// //if rA would make pathfinding impossible, don't use it
		// if(!rA.isPointInShape(goal) && !rA.isPointInShape(thisBot.pos) )
		// {
		// this.restrictedAreas.add(rA);
		// }
		// }
		
		// all bots except thisBot in botPosList
		putBotsInList();
		
		// all checks, if there is a need for a new RRT-cycle, are done WITHOUT ball consideration
		// maybe this should be done, but i'm not quite sure about it
		// maybe there shouldn't be this checks, when ball has to be considered
		// i'll think about it, when the rest is done
		

		// is it possible to use oldPath?
		// is oldPath existent?
		/*
		 * if (oldPath != null && oldPath.path != null && oldPath.path.size() > 0)
		 * {
		 * oldPath = actualizeOldPath();
		 * // direct way from current pos to target free? then use it
		 * newPath = checkDirectWayBotPos2Target();
		 * if (newPath != null)
		 * {
		 * // might be oldPath, but that doesn't matter, because changed-flag is already set in that case
		 * return newPath;
		 * }
		 * IVector2 oldGoal = oldPath.getGoal();
		 * if (oldGoal.equals(goal))
		 * {
		 * // way still free?
		 * if (isOldPathStillOK())
		 * {
		 * // System.out.println("out: old");
		 * oldPath.changed = false;
		 * return oldPath;
		 * }
		 * }
		 * 
		 * // if target hasn't changed too much
		 * if (oldGoal.equals(goalNode, TOLLERABLE_TARGET_SHIFT))
		 * {
		 * if (isOldPathOnlySlightlyChanged())
		 * {
		 * oldPath.changed = true;
		 * 
		 * // okay, the new goal can be added to oldPath...can the previously target be replaced?
		 * if (oldPath.path.size() > 1)
		 * {
		 * if (isWayOK(goal, oldPath.path.get(oldPath.path.size() - 2), SAFETY_DISTANCE_OLD_PATH))
		 * {
		 * oldPath.path.remove(oldPath.path.size() - 1);
		 * }
		 * }
		 * // add new goal to oldPath
		 * oldPath.path.add(goal);
		 * 
		 * return oldPath;
		 * }
		 * }
		 * }
		 */

		if (!isGoalie && !IS_AGRESSIVE)
		{
			Node suckingBot = botThatIsSuckingAtItsPositionByHinderingMeToCalcAGoodPath();
			if (suckingBot != null)
			{
				return calcEscapeRoute(suckingBot);
			}
		}
		
		// clear WPC? yes if targetshift is more than value
		if (oldPath != null && oldPath.getGoal() != null)
		{
			if (!goal.equals(oldPath.getGoal(), TOLLERABLE_TARGET_SHIFT_WPC))
			{
				WPCFillLvl = 0;
			}
		}
		
		// if oldPath can be used without using RRT, this part will not be reached.
		// if here, then RRT will have to start working
		// System.out.println("rrt working");
		// we have to compute a new path
		ArrayList<Node> tree = growTree();
		
		final List<IVector2> pathPointList;
		
		if (tree != null)
		{
			// System.out.println("out: rrt");
			
			fillWPC();
			
			tree = makeTreeDoubleLinked(tree);
			
			tree = smoothTree(tree);
			reduceAmountOfPoints(tree);
			
			pathPointList = transformToPathPointList();
		} else if (IS_RAMBO_MODE)
		{
			// if no way was found, choose the direct line to goal
			// this way is as good as every other one and it's the shortest
			if (!isSecondTry)
			{
				return doCalculation(wFrame, thisBot.id, oldPath, goal, considerBall, isGoalie, gameSit, true);
			} else
			{
				pathPointList = new ArrayList<IVector2>(1);
				
				pathPointList.add(goal);
			}
		} else
		{
			pathPointList = new ArrayList<IVector2>(1);
			
			pathPointList.add(thisBot.pos.addNew(new Vector2(0.1f, 0.1f)));
		}
		// System.out.println(this.goal);
		isSecondTry = false;
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
				// if bot blocks goal, don't consider it
				if (!bot.pos.equals(goal, BOT_RADIUS + usedSafetyDistance))
				{
					botPosList.add(new Node(bot.pos));
				}
			}
		}
		// store opponents
		for (TrackedBot bot : wFrame.foeBots.values())
		{
			// if bot blocks goal, don't consider it
			if (!bot.pos.equals(goal, BOT_RADIUS + usedSafetyDistance))
			{
				botPosList.add(new Node(bot.pos));
			}
		}
	}
	

	// /**
	// * check if direct path is free. if it is: is it changed?
	// *
	// * @return either a path or, if direct way is not free, null
	// */
	// private Path checkDirectWayBotPos2Target()
	// {
	// // check if direct connection from current pos to target is free; 0 because it doesn't matter in that case
	// if (isWayOK(thisBot.pos, goal, 0))
	// {
	// pathPointList = new ArrayList<IVector2>(1);
	// pathPointList.add(goal);
	//
	// Path newPath = new Path(thisBot.id, pathPointList);
	//
	// // if oldPath has only one element, it's already been the direct connection
	// if (oldPath != null && oldPath.path.size() <= 2 && oldPath.getGoal().equals(newPath.getGoal()))
	// {
	// // set changed = false, so skills are not enqueued again
	//
	// newPath = oldPath; // reuse old path in that case (DanielW)
	// newPath.changed = false;
	// }
	//
	// return newPath;
	// } else
	// {
	// return null;
	// }
	// }
	

	/**
	 * checks direct connection between given point a (e.g. botPosition) and point b (e.g. target)
	 * 
	 * @param a start point
	 * @param b end point
	 * @return true if connection is FREE
	 */
	private boolean isWayOK(IVector2 a, IVector2 b, float safetyDistance)
	{
		if (isBotInWay(a, b, safetyDistance))
		{
			return false;
		}
		
		if (isGoalPostInWay(a, b, safetyDistance))
		{
			return false;
		}
		
		if (isProhibitedFieldAreaInWay(a, b))
		{
			return false;
		}
		
		if (considerBall)
		{
			if (isBallInWay(a, b, safetyDistance))
			{
				return false;
			}
		}
		
		// YOOUUUUU SHALL NOT PASS! (watching LoTR right now^^`)
		return true;
	}
	

	// /**
	// * checks result from last time
	// *
	// * @return
	// */
	// private boolean isOldPathStillOK()
	// {
	// int size = oldPath.path.size();
	//
	// // check way between current botpos and first pathpoint
	// if (!isWayOK(thisBot.pos, oldPath.path.get(0), SAFETY_DISTANCE_OLD_PATH))
	// {
	// return false;
	// }
	//
	// for (int i = 0; i < size - 1; ++i)
	// {
	// if (!isWayOK(oldPath.path.get(i), oldPath.path.get(i + 1), SAFETY_DISTANCE_OLD_PATH))
	// {
	// return false;
	// }
	// }
	//
	// // nothing hit yet? then path okay
	// return true;
	// }
	

	// /**
	// * old goal is != new goal, but very close. so this method checks, if new goal can be reached from old goal
	// *
	// * @return
	// */
	// private boolean isOldPathOnlySlightlyChanged()
	// {
	// // new goal is in short range of old goal (has been checked in calling method)
	//
	// // check if old is still okay?
	// if (!isOldPathStillOK())
	// {
	// return false;
	// }
	//
	// // check if connection between last point of last cycle to new target is free
	// int size = oldPath.path.size();
	// if (!isWayOK(oldPath.path.get(size - 1), goal, 0))
	// {
	// return false;
	// }
	// return true;
	// }
	

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
			
			// but first we have to check if same node is still nearest one...this is only neccessary by kd-trees
			// nearest = getNearest(tree, extended);
			if (isWayOK(nearest, extended, usedSafetyDistance))
			{
				nearest.addChild(extended);
				tree.add(extended);
				
				// check direct link between extended and goalNode
				if (isWayOK(extended, goalNode, usedSafetyDistance))
				{
					// why not just adding goalNode to nodeStorage? thats because then it's not possible to smooth the path
					// as good as with doing the following stuff
					
					// take little steps toward goal till being there
					// for loop to protect heapspace if something goes wrong
					tree = subdividePath(tree, extended, goalNode);
					
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
		// for loop to protect heap space if something goes wrong
		for (i = 0; (!start.equals(end, FINAL_STEP_SIZE + 1)) && i < 100; ++i)
		{
			ext = extendTree(end, start, FINAL_STEP_SIZE);
			tree.add(ext);
			start.addChild(ext);
			start.setSuccessor(ext);
			start = ext;
		}
		if (i == 100)
		{
			log.error("Method 'subdividePath' nearly has been in an endless-loop. That function seems to be not correct!. Please report to class-owner of ERRTPlanner");
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
		} else if (p <= P_DESTINATION + P_WAYPOINT && WPCFillLvl > 0)
		{
			return getNodeFromWPC();
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
		// for x value: (y is alike)
		// generator returns value between 0 and 1. Multiplication causes value between 0 and FIELDLENGTH (which is 6050)
		// subtraction causes a shift to the regular values from -3050 to +3050
		
		// if opponent half is prohibited, it is scaled to -3050 to 0
		float x, y;
		
		if (!prohibitOpponentsHalf)
		{
			x = (generator.nextFloat() * FIELD_LENGTH) - FIELD_LENGTH / 2;
		} else
		{
			x = generator.nextFloat() * -(FIELD_LENGTH / 2);
		}
		y = (generator.nextFloat() * FIELD_WIDTH) - FIELD_WIDTH / 2;
		
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
		// calculate with squares, because real distance is not needed and: if a^2 > b^2 then |a| > |b|
		float minSquareDistance = Float.MAX_VALUE; // longer than possible -> first tested node will be nearer
		
		float currentSquareDistance;
		
		// will be returned
		// initialized with current botPos
		Node nearestNode = new Node(thisBot.pos);
		
		for (Node currentNode : nodeStorage)
		{
			currentSquareDistance = AIMath.distancePPSqr(nextTarget, currentNode);
			
			// found a better one
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
		
		// // is target in the restricted area?
		// for(I2DShape rA : restrictedAreas)
		// {
		// // if point is inside restricted area: return nearest point outside
		// // if point is outside restricted area: return point
		// // rather if than new object...
		// if(rA.isPointInShape(extended))
		// {
		// extended = new Node(rA.nearestPointOutside(target));
		// }
		// }
		
		// below: not necessary, but...every line is a child of mine, you know?
		// if coords are less than zero or bigger than the values shown below, it's not on the field anymore ---
		// if (extended.x < -0.5f * FIELD_LENGTH)
		// {
		// extended.x = 0.5f * FIELD_LENGTH;
		// } else if (extended.x > 0.5f * FIELD_LENGTH)
		// {
		// extended.x = 0.5f * FIELD_LENGTH;
		// }
		// if (extended.y < -0.5f * FIELD_WIDTH)
		// {
		// extended.y = -0.5f * FIELD_WIDTH;
		// } else if (extended.y > 0.5f * FIELD_WIDTH)
		// {
		// extended.y = 0.5f * FIELD_WIDTH;
		// }
		
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
	

	// /**
	// * bot moves, so it has to be checked, if bot already reached a path point. </br>
	// * if so, removes it
	// *
	// * @return
	// */
	// private Path actualizeOldPath()
	// {
	// // should run till return statement is reached
	// while (true)
	// {
	// if (oldPath.path.size() > 1)
	// {
	// IVector2 ppA = oldPath.path.get(0);
	// IVector2 ppB = oldPath.path.get(1);
	//
	// float distanceX = ppB.x() - ppA.x();
	// float distanceY = ppB.y() - ppA.y();
	//
	// // should run till return statement is reached
	// float u = ((thisBot.pos.x - ppA.x()) * distanceX + (thisBot.pos.y - ppA.y()) * distanceY)
	// / (distanceX * distanceX + distanceY * distanceY);
	//
	// if (u < 0)
	// {
	// // bot is before ppA, i.e. path only has to be actualized, if distance is below POSITIONING_TOLLERANCE
	// if (thisBot.pos.equals(ppA, POSITIONING_TOLLERANCE * 2))
	// {
	// oldPath.path.remove(0);
	// oldPath.changed = true;
	// }
	// return oldPath;
	// } else
	// {
	// // bot has already gone a part of the path
	// // delete first Vector2 and check again
	// oldPath.path.remove(0);
	// oldPath.changed = true;
	// }
	// } else
	// {
	// return oldPath;
	// }
	// }
	// }
	//
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- temp methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * checks if the direct link between two points is free. returns true if a collision happens
	 */
	private synchronized boolean isBotInWay(IVector2 a, IVector2 b, float safetyDistance)
	{
		// --- CORRECT, BUT SLOW... ---
		
		// System.out.println("-------------");
		// System.out.println("start: "+xNodeA+" "+yNodeA);
		// System.out.println("dest : "+xNodeB+" "+yNodeB);
		// System.out.println("obst : "+botPos.firstElement().x+" "+botPos.firstElement().y);
		// System.out.println("<<<<<<<<<<<<<<<<<<<<");
		
		for (Node pos : botPosList)
		{
			if (isElementInWay(a, b, pos, BOT_RADIUS, safetyDistance))
			{
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * checks if ball would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isBallInWay(IVector2 nodeA, IVector2 nodeB, float safetyDistance)
	{
		if (isElementInWay(nodeA, nodeB, ballPos, BALL_RADIUS, safetyDistance))
		{
			return true;
		} else
		{
			return false;
		}
	}
	

	/**
	 * checks if goalpost would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isGoalPostInWay(IVector2 nodeA, IVector2 nodeB, float safetyDistance)
	{
		if (isElementInWay(nodeA, nodeB, GOAL_OUR.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, GOAL_OUR.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, GOAL_THEIR.getGoalPostLeft(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else if (isElementInWay(nodeA, nodeB, GOAL_THEIR.getGoalPostRight(), GOALPOST_RADIUS, safetyDistance))
		{
			return true;
		} else
		{
			return false;
		}
	}
	

	private boolean isProhibitedFieldAreaInWay(IVector2 nodeA, IVector2 nodeB)
	{
		// if there is more than one prohibited area, this check needs to be done for each element
		if (prohibitCenterCircle)
		{
			if (isElementInWay(nodeA, nodeB, centerPoint, CENTER_CIRCLE_RADIUS, 0))// no safety needed
			{
				return true;
			}
		}
		
//		// opponents penalty area 			!!! NOT PROHIBITED BY THE RULES !!!
//		if (prohibitOpponentPenArea)
//		{
//			if (isElementInWay(nodeA, nodeB, opponentsPenAreaCircleCenter, penAreaCircleRadius, 0))// no safety needed
//			{
//				return true;
//			}
//		}
		
		if (prohibitTigersPenArea)
		{
			if (isElementInWay(nodeA, nodeB, tigersPenAreaCircleCenter, penAreaCircleRadius, 0))// no safety needed
			{
				return true;
			}
		}
		
		if (isFreekick)
		{
			if (isElementInWay(nodeA, nodeB, ballPos, distanceAtFreekick, 0))// no safety needed
			{
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * checks if element would be hit by driving
	 * algorithm: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean isElementInWay(IVector2 nodeA, IVector2 nodeB, IVector2 elementPos, float elementRadius,
			float safetyDistance)
	{
		if (nodeA.equals(nodeB))
		{
			return false;
		}
		
		float distanceX = nodeB.x() - nodeA.x();
		float distanceY = nodeB.y() - nodeA.y();
		IVector2 nearest = null;
		
		float u = ((elementPos.x() - nodeA.x()) * distanceX + (elementPos.y() - nodeA.y()) * distanceY)
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
		
		if (nearest.equals(elementPos, (BOT_RADIUS + elementRadius + safetyDistance)))
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
		
		while (currentNode.parent != null)
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
		Node start = tree.get(0);// start with first (-->current bot pos);
		float eps = 0.01f;
		
		// begin with node after goalNode (direct connection to this node has already been checked in grow tree)
		end = end.parent;
		
		// if null, then node before startnode is reached
		while (end.parent != null)
		{
			while (!start.equals(end, eps))
			{
				// check line between current node and compareNode
				if (isWayOK(start, end, usedSafetyDistance))
				{
					start.addChild(end);
					start.setSuccessor(end);
					
					// this may be a very long line. so i subdivide it.
					subdividePath(tree, start, end);
					// make it break
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
			if (isWayOK(currentNode, currentNode.parent.parent, usedSafetyDistance))
			{
				currentNode.parent = currentNode.parent.parent;
				currentNode.parent.setSuccessor(currentNode); // this has been your grandpa some milli-seconds ago
			}
			// if not, move forward (in this case backward ;-)
			else
			{
				currentNode = currentNode.parent;
			}
		}
		
		// now the path between finalDestination and start has much less points *slap on my back*
	}
	

	/**
	 * gets one random node from waypointcache
	 * shouldn't be called if WPC_fill_lvl == 0
	 * @return
	 */
	private Node getNodeFromWPC()
	{
		// generates a value [0,1[. multiplys with fill lvl of waypointcache
		// --> index between 0 and fill lvl of WPC
		// returns that element
		return waypoints[(int) (generator.nextFloat() * WPCFillLvl)];
	}
	

	/**
	 * fills waypointcache with all the nodes found on the path
	 * 
	 */
	private void fillWPC()
	{
		Node currentNode = goalNode;
		
		// if null, then startnode is reached
		while (currentNode.parent != null)
		{
			currentNode = currentNode.parent;
			
			insertNodeToWPC(currentNode);
		}
	}
	

	/**
	 * puts a node into the waypointcache
	 */
	private void insertNodeToWPC(Node node)
	{
		// WPC is not yet full. put at the end
		if (WPCFillLvl < WPC_SIZE)
		{
			waypoints[WPCFillLvl] = new Node(node);
			WPCFillLvl++;
			return;
		}
		// WPC is full. replace random node
		else
		{
			waypoints[(int) (generator.nextFloat() * WPC_SIZE)] = new Node(node);
			return;
		}
	}
	

	private Path calcEscapeRoute(Node obstacle)
	{
		// calc node away from obstacle
		IVector2 escapePoint = AIMath.stepAlongLine(obstacle, thisBot.pos, escapeStepSize);
		
		pathPointList = new ArrayList<IVector2>(1);
		pathPointList.add(escapePoint);
		
		Path newPath = new Path(thisBot.id, pathPointList);
		
		return newPath;
	}
	

	private Node botThatIsSuckingAtItsPositionByHinderingMeToCalcAGoodPath()
	{
		Node nearestNode = null;
		float nearest = Float.MAX_VALUE;
		float current;
		
		for (Node botPos : botPosList)
		{
			current = AIMath.distancePP(thisBot.pos, botPos);
			{
				if (current < nearest)
				{
					nearest = current;
					nearestNode = new Node(botPos);
				}
			}
		}
		if ((AIMath.distancePP(thisBot, nearestNode) > ((2 * BOT_RADIUS) + usedSafetyDistance))
				&& isElementInWay(thisBot.pos, goal, nearestNode, BOT_RADIUS, 0))
		{
			nearestNode = null;
		}
		return nearestNode;
	}
}
