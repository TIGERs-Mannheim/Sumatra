/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;


/**
 *
 */
public class OffensiveActionTreeTest
{
	
	@Test
	public void testTreeGeneration()
	{
		OffensiveActionTree tree = new OffensiveActionTree();
		tree.addNode(EOffensiveActionMove.CLEARING_KICK);
		tree.addNode(EOffensiveActionMove.CLEARING_KICK, EOffensiveActionMove.GOAL_KICK);
		tree.addNode(EOffensiveActionMove.CLEARING_KICK, EOffensiveActionMove.LOW_CHANCE_GOAL_KICK);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.GOAL_KICK);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.STANDARD_PASS);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.CLEARING_KICK);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.CLEARING_KICK,
				EOffensiveActionMove.CLEARING_KICK);
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.CLEARING_KICK,
				EOffensiveActionMove.GOAL_KICK);
		
		Assert.assertEquals(EOffensiveActionMove.GOAL_KICK,
				tree.getAction(EOffensiveActionMove.CLEARING_KICK, EOffensiveActionMove.GOAL_KICK).getType());
		
		Assert.assertEquals(EOffensiveActionMove.CLEARING_KICK,
				tree.getAction(EOffensiveActionMove.CLEARING_KICK, EOffensiveActionMove.GOAL_KICK).getParent().getType());
		
		Assert.assertEquals(EOffensiveActionMove.CLEARING_KICK,
				tree.getAction(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, EOffensiveActionMove.CLEARING_KICK,
						EOffensiveActionMove.CLEARING_KICK).getType());
		
		List<EOffensiveActionMove> path = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		
		path.add(EOffensiveActionMove.FORCED_PASS);
		scores.add(1.0);
		
		tree.updateTree(path, scores);
	}
	
	
	@Test
	public void testParsing1()
	{
		OffensiveActionTree tree = new OffensiveActionTree();
		
		List<EOffensiveActionMove> path = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.GOAL_KICK);
		scores.add(1.0);
		scores.add(1.2);
		tree.updateTree(path, scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.GOAL_KICK);
		scores.add(1.0);
		scores.add(1.1);
		scores.add(1.2);
		tree.updateTree(path, scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.GOAL_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(0.8);
		tree.updateTree(path, scores);
		
		Map<EOffensiveActionMove, Double> adjustedValues = tree
				.getAdjustedScoresForCurrentPath();
		
		EOffensiveActionMove maxMove = calcMaxMove(adjustedValues);
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.STANDARD_PASS, maxMove);
		
		adjustedValues = tree
				.getAdjustedScoresForCurrentPath(EOffensiveActionMove.STANDARD_PASS);
		
		maxMove = calcMaxMove(adjustedValues);
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.STANDARD_PASS, maxMove);
	}
	
	
	private EOffensiveActionMove calcMaxMove(final Map<EOffensiveActionMove, Double> adjustedValues)
	{
		Double max = Double.MIN_VALUE;
		EOffensiveActionMove maxMove = null;
		for (Map.Entry<EOffensiveActionMove, Double> entry : adjustedValues.entrySet())
		{
			if (entry.getValue() > max)
			{
				max = entry.getValue();
				maxMove = entry.getKey();
			}
		}
		return maxMove;
	}
	
	
	@Test
	public void testParsing2()
	{
		OffensiveActionTree tree = new OffensiveActionTree();
		
		List<EOffensiveActionMove> path = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.GOAL_KICK);
		scores.add(1.0);
		scores.add(1.2);
		tree.updateTree(path, scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.GOAL_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(0.7);
		tree.updateTree(path, scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.CLEARING_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(1.5);
		tree.updateTree(path, scores);
		
		Map<EOffensiveActionMove, Double> adjustedValues = tree
				.getAdjustedScoresForCurrentPath();
		
		EOffensiveActionMove maxMove = calcMaxMove(adjustedValues);
		
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.CLEARING_KICK, maxMove);
	}
	
}
