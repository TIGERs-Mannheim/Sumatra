/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trees;import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sleepycat.persist.model.Persistent;

import java.util.HashMap;
import java.util.Map;


/**
 */
@Persistent
public class OffensiveActionTreeNode
{
	private Map<String, OffensiveActionTreeNode> children = new HashMap<>();

	@JsonIgnore
	private OffensiveActionTreeNode parent = null;
	
	private String type = null;
	
	private double weight = 1.0;

	private int numOfUpdates = 0;
	
	private static final double MIN_COUNTER_LEARNING_RATE = 0.05;
	
	private static final double LEARNING_SLOW_DOWN = 5;
	
	
	@SuppressWarnings("unused")
	private OffensiveActionTreeNode()
	{
		// for berkley
	}
	
	
	public OffensiveActionTreeNode(String type, OffensiveActionTreeNode parent)
	{
		this.type = type;
		this.parent = parent;
	}
	
	
	public Map<String, OffensiveActionTreeNode> getChildren()
	{
		return children;
	}
	
	
	public void addChild(String move)
	{
		children.put(move, new OffensiveActionTreeNode(move, this));
	}


	@JsonIgnore
	public OffensiveActionTreeNode getParent()
	{
		return parent;
	}

	@JsonIgnore
	public void setParent(final OffensiveActionTreeNode parent)
	{
		this.parent = parent;
	}

	
	public String getType()
	{
		return type;
	}
	
	
	public double getWeight()
	{
		return weight;
	}
	
	
	/**
	 * @param dif [-1,+1]
	 */
	public void updateWeight(final double dif)
	{
		assert dif >= -1 && dif <= 1;
		// maximal learning difs
		double adjustedDif = Math.min(0.5, Math.max(-0.5, dif));
		double w = weight;
		double x = 1 - Math.abs(1 - w);
		assert x <= 1 && x >= 0;
		
		// y = e^(((x-1)/x)*10)
		double lambda = Math.pow(Math.E, ((x - 1) / x) * 10) / LEARNING_SLOW_DOWN;
		if ((weight < 1 && adjustedDif > 0)
				|| (weight > 1 && adjustedDif < 0))
		
		{
			// learning in counter direction
			// minimum learning rate towards other direction
			lambda = Math.max(MIN_COUNTER_LEARNING_RATE, lambda);
		}
		double add = adjustedDif * lambda;
		weight += add;
		numOfUpdates++;
	}

	public int getNumOfUpdates()
	{
		return numOfUpdates;
	}

	public void setNumOfUpdates(final int numOfUpdates)
	{
		this.numOfUpdates = numOfUpdates;
	}
}
