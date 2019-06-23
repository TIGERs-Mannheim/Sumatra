/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveBotFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * Calculates offensive Actions for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStatisticsCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger log = Logger
			.getLogger(OffensiveStrategyCalc.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------- ------------------------------------
	
	public OffensiveStatisticsCalc()
	{
		// nothing to do here yet.
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (OffensiveConstants.isEnableOffensiveStatistics())
		{
			createEmptyDataset(newTacticalField, baseAiFrame);
		}
	}
	
	
	/**
	 * creates empty datasets. they get filled in the OffensiveActions and OffensiveStrategy Calculators.
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	private void createEmptyDataset(final TacticalField newTacticalField, BaseAiFrame baseAiFrame)
	{
		OffensiveStatisticsFrame frame = new OffensiveStatisticsFrame();
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			OffensiveBotFrame botFrame = new OffensiveBotFrame();
			frame.getBotFrames().put(key, botFrame);
		}
		newTacticalField.setOffensiveStatistics(frame);
	}
}
