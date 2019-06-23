/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;


/**
 * @author: MarkG
 */
public abstract class AOffensiveStrategyFeature
{
	
	/**
	 * Used to describe the activity of the current calculations
	 */
	public enum EFeatureActivity
	{
		/**
		 *
		 */
		NONE,
		/**
		 *
		 */
		PARTIALLY,
		/**
		 *
		 */
		FULL
	}
	
	protected static final Logger	log		= Logger
			.getLogger(AOffensiveStrategyFeature.class.getName());
	
	protected EFeatureActivity		activity	= EFeatureActivity.NONE;
	
	
	/**
	 * Resetting some private parameters for next frame
	 */
	public void initFeature()
	{
		activity = EFeatureActivity.NONE;
	}
	
	
	/**
	 * @param newTacticalField current Tactical field
	 * @param baseAiFrame baseAiFrame
	 * @param strategy will be filled with data
	 * @param tempInfo
	 */
	public abstract void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy);
	
	
	public EFeatureActivity getActivity()
	{
		return activity;
	}
	
}
