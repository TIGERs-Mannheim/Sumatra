/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trees;

import com.sleepycat.persist.model.Persistent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class stores the offensive moves that have been executed in the past. The moves are weighted and can be analysed
 * for better decision makings later.
 */
@Persistent
public class OffensiveActionTree
{
	private OffensiveActionTreeNode head = new OffensiveActionTreeNode(null, null);
	
	
	public OffensiveActionTree()
	{
		// nothing
	}
	
	
	public OffensiveActionTreeNode getAction(String... path)
	{
		OffensiveActionTreeNode action = head;
		for (final String aPath : path)
		{
			if (!action.getChildren().containsKey(aPath))
			{
				return null;
			}
			action = action.getChildren().get(aPath);
		}
		return action;
	}
	
	
	/**
	 * @param path current Path
	 * @return returns value scores depending on learned data
	 */
	public Map<String, Double> getAdjustedScoresForCurrentPath(String... path)
	{
		OffensiveActionTreeNode action = getAction(path);
		Map<String, Double> actionWeightScores = new HashMap<>();
		if (action == null)
		{
			return actionWeightScores;
		}
		for (String key : action.getChildren().keySet())
		{
			actionWeightScores.put(key,
					traverseMaxScores(action.getChildren().get(key), action.getChildren().get(key).getWeight()));
		}
		return actionWeightScores;
	}
	
	
	private double traverseMaxScores(OffensiveActionTreeNode node, double weight)
	{
		Map<String, Double> actioWeightScores = new HashMap<>();
		for (OffensiveActionTreeNode child : node.getChildren().values())
		{
			actioWeightScores.put(child.getType(),
					traverseMaxScores(child, weight * child.getWeight()));
		}
		if (actioWeightScores.isEmpty())
		{
			return weight;
		}
		
		double maxScore = Double.MIN_VALUE;
		for (double score : actioWeightScores.values())
		{
			if (score > maxScore)
			{
				maxScore = score;
			}
		}
		return maxScore;
	}
	
	
	/**
	 * @param path paths
	 * @param scores scores
	 */
	public void updateTree(List<String> path, List<Double> scores)
	{
		if (scores.size() != path.size())
		{
			throw new IllegalArgumentException("Size of path not equal to size of score");
		}
		OffensiveActionTreeNode currentNode = head;
		int i = 0;
		for (String move : path)
		{
			if (i != 0)
			{
				// dif is positive if new score is better
				double dif = scores.get(i) - scores.get(i - 1); // or always initial score ?
				currentNode.updateWeight(dif);
			}
			i++;
			if (!currentNode.getChildren().containsKey(move))
			{
				currentNode.addChild(move);
			}
			currentNode = currentNode.getChildren().get(move);
		}
	}
	
	
	public void addNode(String... path)
	{
		OffensiveActionTreeNode action = head;
		for (int i = 0; i < path.length - 1; i++)
		{
			action = action.getChildren().get(path[i]);
		}
		action.addChild(path[path.length - 1]);
	}
	
	
	public OffensiveActionTreeNode getHead()
	{
		return head;
	}
	
	
	public void setHead(final OffensiveActionTreeNode head)
	{
		this.head = head;
	}
}
