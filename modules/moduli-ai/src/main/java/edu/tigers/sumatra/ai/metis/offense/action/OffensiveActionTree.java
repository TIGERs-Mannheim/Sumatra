/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;


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
	
	
	public OffensiveActionTreeNode getAction(EOffensiveActionMove... path)
	{
		OffensiveActionTreeNode action = head;
		for (final EOffensiveActionMove aPath : path)
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
	public Map<EOffensiveActionMove, Double> getAdjustedScoresForCurrentPath(EOffensiveActionMove... path)
	{
		OffensiveActionTreeNode action = getAction(path);
		Map<EOffensiveActionMove, Double> actionWeightScores = new EnumMap<>(EOffensiveActionMove.class);
		for (EOffensiveActionMove key : EOffensiveActionMove.values())
		{
			if (action != null && action.getChildren().containsKey(key))
			{
				actionWeightScores.put(key,
						traverseMaxScores(action.getChildren().get(key), action.getChildren().get(key).getWeight()));
			} else
			{
				actionWeightScores.put(key, 1.0);
			}
		}
		return actionWeightScores;
	}
	
	
	private double traverseMaxScores(OffensiveActionTreeNode node, double weight)
	{
		Map<EOffensiveActionMove, Double> actioWeightScores = new EnumMap<>(EOffensiveActionMove.class);
		for (EOffensiveActionMove key : EOffensiveActionMove.values())
		{
			if (node.getChildren().containsKey(key))
			{
				// step to child
				actioWeightScores.put(key,
						traverseMaxScores(node.getChildren().get(key), weight * node.getChildren().get(key).getWeight()));
			} else
			{
				actioWeightScores.put(key, weight);
			}
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
	public void updateTree(List<EOffensiveActionMove> path, List<Double> scores)
	{
		if (scores.size() != path.size())
		{
			throw new IllegalArgumentException("Size of path not equal to size of score");
		}
		OffensiveActionTreeNode currentNode = head;
		int i = 0;
		for (EOffensiveActionMove move : path)
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
	
	
	public void addNode(EOffensiveActionMove... path)
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
}
