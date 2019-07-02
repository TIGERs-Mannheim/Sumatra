/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.trees.OffensiveActionTreeMap;
import edu.tigers.sumatra.trees.OffensiveActionTreeNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;


/**
 *
 */
public class OffensiveActionTreeTest
{
	
	@Test
	public void testTreeGeneration()
	{
		OffensiveActionTree tree = new OffensiveActionTree();
		generateTree(tree);
		
		Assert.assertEquals(EOffensiveActionMove.GOAL_KICK.name(),
				tree.getAction(EOffensiveActionMove.CLEARING_KICK.name(), EOffensiveActionMove.GOAL_KICK.name()).getType());
		
		Assert.assertEquals(EOffensiveActionMove.CLEARING_KICK.name(),
				tree.getAction(EOffensiveActionMove.CLEARING_KICK.name(), EOffensiveActionMove.GOAL_KICK.name()).getParent()
						.getType());
		
		Assert.assertEquals(EOffensiveActionMove.CLEARING_KICK.name(),
				tree.getAction(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(),
						EOffensiveActionMove.CLEARING_KICK.name(),
						EOffensiveActionMove.CLEARING_KICK.name()).getType());
		
		List<EOffensiveActionMove> path = new ArrayList<>();
		List<Double> scores = new ArrayList<>();
		
		path.add(EOffensiveActionMove.FORCED_PASS);
		scores.add(1.0);
		
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
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
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		path.add(EOffensiveActionMove.GOAL_KICK);
		scores.add(1.0);
		scores.add(1.1);
		scores.add(1.2);
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.GOAL_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(0.8);
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		Map<String, Double> adjustedValues = tree
				.getAdjustedScoresForCurrentPath();
		
		String maxMove = calcMaxMove(adjustedValues);
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.STANDARD_PASS.name(), maxMove);
		
		adjustedValues = tree
				.getAdjustedScoresForCurrentPath(EOffensiveActionMove.STANDARD_PASS.name());
		
		maxMove = calcMaxMove(adjustedValues);
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.STANDARD_PASS.name(), maxMove);
	}
	
	
	private String calcMaxMove(final Map<String, Double> adjustedValues)
	{
		Double max = Double.MIN_VALUE;
		String maxMove = null;
		for (Map.Entry<String, Double> entry : adjustedValues.entrySet())
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
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.GOAL_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(0.7);
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		path.clear();
		scores.clear();
		path.add(EOffensiveActionMove.CLEARING_KICK);
		path.add(EOffensiveActionMove.STANDARD_PASS);
		scores.add(1.0);
		scores.add(1.5);
		tree.updateTree(path.stream().map(Enum::name).collect(Collectors.toList()), scores);
		
		Map<String, Double> adjustedValues = tree
				.getAdjustedScoresForCurrentPath();
		
		String maxMove = calcMaxMove(adjustedValues);
		
		assert maxMove != null;
		assertEquals(EOffensiveActionMove.CLEARING_KICK.name(), maxMove);
	}
	
	
	@Test
	public void testTreeIO()
	{
		final String path = "OffensiveActionTree.json";
		OffensiveActionTree tree = new OffensiveActionTree();
		generateTree(tree);
		Map<EOffensiveSituation, OffensiveActionTree> trees = new EnumMap<>(EOffensiveSituation.class);
		trees.put(EOffensiveSituation.DEFAULT_SITUATION, tree);
		OffensiveActionTreeMap map = new OffensiveActionTreeMap(trees);
		map.saveTreeDataToFile(path);
		Optional<OffensiveActionTreeMap> node = OffensiveActionTreeMap
				.loadTreeDataFromFile(path);
		
		assert node.isPresent();
		assert node.get().getActionTrees().containsKey(EOffensiveSituation.DEFAULT_SITUATION);
		
		OffensiveActionTree newTree = node.get().getActionTrees().get(EOffensiveSituation.DEFAULT_SITUATION);
		for (OffensiveActionTreeNode child : newTree.getHead().getChildren().values())
		{
			assertEquals(child.getParent(), newTree.getHead());
			assert tree.getHead().getChildren().containsKey(child.getType());
		}
		
		File file = new File(path);
		try
		{
			assert Files.deleteIfExists(file.toPath());
		} catch (IOException e)
		{
			assertNull(e.getMessage());
		}
	}
	
	
	private void generateTree(final OffensiveActionTree tree)
	{
		tree.addNode(EOffensiveActionMove.CLEARING_KICK.name());
		tree.addNode(EOffensiveActionMove.CLEARING_KICK.name(), EOffensiveActionMove.GOAL_KICK.name());
		tree.addNode(EOffensiveActionMove.CLEARING_KICK.name(), EOffensiveActionMove.LOW_CHANCE_GOAL_KICK.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(), EOffensiveActionMove.GOAL_KICK.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(), EOffensiveActionMove.STANDARD_PASS.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(), EOffensiveActionMove.CLEARING_KICK.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(), EOffensiveActionMove.CLEARING_KICK.name(),
				EOffensiveActionMove.CLEARING_KICK.name());
		tree.addNode(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF.name(), EOffensiveActionMove.CLEARING_KICK.name(),
				EOffensiveActionMove.GOAL_KICK.name());
	}
	
}
