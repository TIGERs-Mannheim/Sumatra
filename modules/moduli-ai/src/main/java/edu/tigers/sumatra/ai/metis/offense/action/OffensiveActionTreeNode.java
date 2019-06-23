/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import java.util.EnumMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;


/**
 */
@Persistent
public class OffensiveActionTreeNode
{
	private Map<EOffensiveActionMove, OffensiveActionTreeNode> children = new EnumMap<>(EOffensiveActionMove.class);
	
	private OffensiveActionTreeNode parent = null;
	
	private EOffensiveActionMove type = null;
	
	private double weight = 1.0;
	
	private static final double MIN_COUNTER_LEARNING_RATE = 0.05;
	
	private static final double LEARNING_SLOW_DOWN = 5;
	
	
	@SuppressWarnings("unused")
	private OffensiveActionTreeNode()
	{
		// for berkley
	}
	
	
	public OffensiveActionTreeNode(EOffensiveActionMove type, OffensiveActionTreeNode parent)
	{
		this.type = type;
		this.parent = parent;
	}
	
	
	public Map<EOffensiveActionMove, OffensiveActionTreeNode> getChildren()
	{
		return children;
	}
	
	
	public void addChild(EOffensiveActionMove move)
	{
		children.put(move, new OffensiveActionTreeNode(move, this));
	}
	
	
	public OffensiveActionTreeNode getParent()
	{
		return parent;
	}
	
	
	public EOffensiveActionMove getType()
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
	}
}
