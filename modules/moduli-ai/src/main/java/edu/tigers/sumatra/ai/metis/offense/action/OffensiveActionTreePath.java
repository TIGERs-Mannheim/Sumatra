/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;


/**
 * Stores the current path and scores to iterate through an offensiveActionTree
 */
@Persistent
public class OffensiveActionTreePath
{
	
	private List<EOffensiveActionMove> currentPath = new ArrayList<>();
	private List<Double> currentScores = new ArrayList<>();
	
	
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
	
	
	public void addEntry(EOffensiveActionMove move, Double score)
	{
		this.currentPath.add(move);
		this.currentScores.add(score);
	}
	
	
	public void clear()
	{
		this.currentPath.clear();
		this.currentScores.clear();
	}
	
	
	public boolean isEmpty()
	{
		return this.currentPath.isEmpty();
	}
}
