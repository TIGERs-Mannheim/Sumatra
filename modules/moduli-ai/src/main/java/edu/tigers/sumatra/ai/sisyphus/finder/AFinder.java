/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 10, 2015
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.errt.TuneableParameter;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Abstract PathFinder class which provides helper methods such as smooth path.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class AFinder implements IPathFinder
{
	private TuneableParameter adjustableParams;
	
	
	/**
	  * 
	  */
	public AFinder()
	{
		adjustableParams = new TuneableParameter();
	}
	
	
	@Override
	public IPath calcPath(final PathFinderInput pathFinderInput)
	{
		List<IPath> pathes = new ArrayList<>();
		List<IVector2> intermediates = new ArrayList<IVector2>(0);
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
				start = pathes.get(i - 1).getPathPoints().get(pathes.get(i - 1).getPathPoints().size() - 1);
			}
			boolean isPathToTarget = false;
			if (i == (intermediates.size() - 1))
			{
				isPathToTarget = true;
			}
			// pathFinderInput.getFieldInfo().addIgnoredPoitn(intermediate);
			pathes.add(doCalculation(pathFinderInput, start, intermediate, !isPathToTarget, false));
			// log.warn("Path: " + pathes.toString());
		}
		IPath finalPath = merge(pathes);
		// log.warn("Path: " + finalPath.toString());
		return finalPath;
	}
	
	
	private IPath merge(final List<IPath> pathes)
	{
		int lastVector = 0;
		IPath pathToUse = pathes.get(pathes.size() - 1);
		pathes.remove(pathes.size() - 1);
		for (IPath intermediatePath : pathes)
		{
			// intermediatePath.getPath().remove(intermediatePath.getPath().get(intermediatePath.getPath().size() - 1));
			pathToUse.getPathPoints().addAll(lastVector, intermediatePath.getPathPoints());
			lastVector = intermediatePath.getPathPoints().size();
		}
		return pathToUse;
	}
	
	
	abstract protected IPath doCalculation(final PathFinderInput pathFinderInput, final IVector2 start,
			final IVector2 target, final boolean isIntermediate, final boolean isSecondTry);
			
			
	/**
	 * Adds points to the PathPointList, when <br>
	 * 1. Start was adjusted because of the ball <br>
	 * 2. Target was adjusted because of the ball <br>
	 * 3. The bot is illegally in the penalty Area
	 * 
	 * @param pathPointList
	 * @param pathFinderInput
	 * @param target
	 */
	protected void addPathPointsFromAdjustments(final List<IVector2> pathPointList,
			final PathFinderInput pathFinderInput, final IVector2 target)
	{
		FieldInformation fieldInfo = pathFinderInput.getFieldInfo();
		ITrackedBot thisBot = fieldInfo.getwFrame().getBot(pathFinderInput.getBotId());
		
		// if the start was adjusted because of the ball, add a direct way to the ball
		if (fieldInfo.isStartAdjustedBecauseOfBall())
		{
			pathPointList.add(0, thisBot.getPos());
		}
		// if the target was adjusted because of the ball, add a direct way to the ball
		if (fieldInfo.isTargetAdjustedBecauseOfBall())
		{
			pathPointList.add(pathFinderInput.getDestination());
		}
		
		// if the bot is currently illegally in the penalty area, kick him out directly
		if (fieldInfo.isBotIllegallyInPenaltyArea() && (thisBot.getPos().subtractNew(target).getLength2() > 500))
		{
			// add a node outside the penalty area, if the bot should leave this area asap
			pathPointList.add(0, fieldInfo.getNearestNodeOutsidePenArea().addNew(thisBot.getVel().scaleToNew(500)));
		}
	}
	
	
	protected void smoothPath(final List<IVector2> nodeList, final PathFinderInput pfi)
	{
		if (nodeList.size() <= 0)
		{
			// Path contains no value
			return;
		}
		// starts from the beginning and find the furthest node to which a direct connection exist and then directly
		// connect the path to this point
		smoothPathStep1(nodeList, pfi, 0, nodeList.size() - 1);
		
		// start from the beginning and create a line from each path element, check whether the line crosses a later
		// element of the path and then directly connect the path to this point
		smoothPathStep2(nodeList, pfi);
	}
	
	
	private void smoothPathStep1(final List<IVector2> nodeList, final PathFinderInput pfi, final int start,
			final int end)
	{
		nodeList.add(0, pfi.getFieldInfo().getwFrame().getBot(pfi.getBotId()).getPos());
		for (int i = 0; i < nodeList.size(); i++)
		{
			for (int j = nodeList.size() - 1; j > (i + 1); j--)
			{
				if (pfi.getFieldInfo().isWayOK(nodeList.get(i), nodeList.get(j),
						adjustableParams.getReduceSafetyForPathSmoothing()))
				{
					List<IVector2> itemsToRemove = new ArrayList<IVector2>(nodeList.subList(i + 1, j));
					nodeList.removeAll(itemsToRemove);
					smoothPathStep1(nodeList, pfi, 0, nodeList.size() - 1);
					return;
				}
			}
		}
		nodeList.remove(0);
		if (nodeList.isEmpty())
		{
			nodeList.add(pfi.getDestination());
		}
	}
	
	
	private void smoothPathStep2(final List<IVector2> nodeList, final PathFinderInput pfi)
	{
		for (int i = 0; i < (nodeList.size() - 1); i++)
		{
			IVector2 current = nodeList.get(i);
			IVector2 next = nodeList.get(i + 1);
			if (current.equals(next, 0.01))
			{
				continue;
			}
			Line startToFirst = new Line(current, next.subtractNew(current));
			
			// the next path element does not need to be checked, so skip it
			for (int j = i + 2; j < (nodeList.size() - 1); j++)
			{
				IVector2 node1 = nodeList.get(j);
				IVector2 node2 = nodeList.get(j + 1);
				Line laterPartInPath = new Line(node1, node2.subtractNew(node1));
				try
				{
					IVector2 intersection = GeoMath.intersectionPoint(startToFirst, laterPartInPath);
					Rectangle laterPart = new Rectangle(node1, node2);
					if (laterPart.isPointInShape(intersection))
					{
						// delete all points between start and node2
						for (int k = (i + 1); k < (j + 1); k++)
						{
							// the index of i is not an error, please regard that the node gets shorter by every removal, so
							// the position where we remove keeps the same
							nodeList.remove((i + 1));
						}
						
						// instead, insert the intersection point but first check if the intersection point does not exist
						// already (this could happen because quite often the path is extended in the direction of the goal
						// node, so this intersection could be the goal node!h
						if (!nodeList.get(i + 1).equals(intersection, 0.01))
						{
							nodeList.add((i + 1), intersection);
						}
					}
					
				} catch (MathException err)
				{
					// do nothing, parallel line, hence no match
				}
			}
		}
	}
	
	
	/**
	 * reduce the amount of points on a straight line of the path
	 * improves the spline
	 * 
	 * @param nodeList
	 * @param fieldInfo
	 */
	protected void reduceAmountOfPoints(final List<IVector2> nodeList, final FieldInformation fieldInfo)
	{
		int currentIndex = nodeList.size() - 1;
		if (currentIndex <= 1)
		{
			return;
		}
		
		while ((currentIndex >= 2))
		{
			IVector2 current = nodeList.get(currentIndex);
			IVector2 currentGrandFather = nodeList.get(currentIndex - 2);
			// check line between current point and two points in list before it. if free remove point in between
			if (fieldInfo.isWayOK(current, currentGrandFather, adjustableParams.getReduceSafetyForPathSmoothing()))
			{
				nodeList.remove(currentIndex - 1);
			}
			// this either the old node but its index is one less, because the list size is one less. or it is the previous
			// node.
			currentIndex--;
			
		}
		// now the path between finalDestination and start has much less points *slap on my back*
	}
	
	
	/**
	 * add a long straight path to the tree
	 * but subdivide it in small pieces
	 * 
	 * @param nodeList
	 * @param startIndex the index of the starting node in the list. the StartIndex + 1 in the list is the end of the
	 *           line. between those both points new points are added
	 */
	protected void addSubdividePath(final List<IVector2> nodeList, final int startIndex)
	{
		IVector2 start = nodeList.get(startIndex);
		IVector2 end = nodeList.get(startIndex + 1);
		// precaclulation to get the amount of intermediate points
		final double dist = start.subtractNew(end).getLength2();
		
		// amount of intermediate points needed
		final int iterations = (int) Math.floor(Math.min(dist, Geometry.getFieldWidth()
				+ Geometry.getFieldLength())
				/ adjustableParams.getStepSize());
				
		IVector2 currentNode = start;
		
		// for loop to protect heap space if something goes wrong
		for (int i = 0; i < (iterations - 1); i++)
		{
			final IVector2 ext = GeoMath.stepAlongLine(currentNode, end, adjustableParams.getStepSize());
			nodeList.add(startIndex + i + 1, ext);
			currentNode = ext;
		}
	}
	
	
	/**
	 * @return the adjustableParams
	 */
	@Override
	public TuneableParameter getAdjustableParams()
	{
		return adjustableParams;
	}
	
	
	/**
	 * @param adjustableParams the adjustableParams to set
	 */
	public void setAdjustableParams(final TuneableParameter adjustableParams)
	{
		this.adjustableParams = adjustableParams;
	}
	
	
}
