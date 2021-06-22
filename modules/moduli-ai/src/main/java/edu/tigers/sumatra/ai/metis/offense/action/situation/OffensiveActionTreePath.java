/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.situation;import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;

import java.util.ArrayList;
import java.util.List;


/**
 * Stores the current path and scores to iterate through an offensiveActionTree
 */
@Persistent
public class OffensiveActionTreePath
{
	private List<EOffensiveActionMove> currentPath = new ArrayList<>();
	private List<Double> currentScores = new ArrayList<>();


	public OffensiveActionTreePath()
	{
	}


	public OffensiveActionTreePath(final OffensiveActionTreePath copy)
	{
		currentPath.addAll(copy.currentPath);
		currentScores.addAll(copy.currentScores);
	}


	public List<Double> getCurrentScores()
	{
		return currentScores;
	}


	public void setCurrentScores(final List<Double> currentScores)
	{
		this.currentScores = currentScores;
	}


	public List<EOffensiveActionMove> getCurrentPath()
	{
		return currentPath;
	}


	public void setCurrentPath(final List<EOffensiveActionMove> currentPath)
	{
		this.currentPath = currentPath;
	}


	public void addEntry(final EOffensiveActionMove move, final Double score)
	{
		currentPath.add(move);
		currentScores.add(score);
	}


	public void clear()
	{
		currentPath.clear();
		currentScores.clear();
	}


	public boolean isEmpty()
	{
		return currentPath.isEmpty();
	}
}
