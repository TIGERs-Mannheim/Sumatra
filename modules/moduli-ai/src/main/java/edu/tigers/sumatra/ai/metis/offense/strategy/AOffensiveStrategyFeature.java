/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author: MarkG
 */
public abstract class AOffensiveStrategyFeature
{
	private BaseAiFrame baseAiFrame;
	private TacticalField tacticalField;
	
	
	/**
	 * Resetting some private parameters for next frame
	 */
	public void update(final BaseAiFrame baseAiFrame, final TacticalField tacticalField)
	{
		this.baseAiFrame = baseAiFrame;
		this.tacticalField = tacticalField;
	}
	
	
	/**
	 * @param newTacticalField current Tactical field
	 * @param strategy will be filled with data
	 */
	public abstract void doCalc(final TacticalField newTacticalField,
			OffensiveStrategy strategy);
	
	
	protected BaseAiFrame getAiFrame()
	{
		return baseAiFrame;
	}
	
	
	protected TacticalField getTacticalField()
	{
		return tacticalField;
	}
	
	
	protected WorldFrame getWFrame()
	{
		return baseAiFrame.getWorldFrame();
	}
	
	
	protected ITrackedBall getBall()
	{
		return baseAiFrame.getWorldFrame().getBall();
	}
}
