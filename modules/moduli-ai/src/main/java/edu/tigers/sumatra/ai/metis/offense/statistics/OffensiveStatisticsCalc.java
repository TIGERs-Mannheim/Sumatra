/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;


/**
 * Calculates offensive Actions for the OffenseRole.
 */
public class OffensiveStatisticsCalc extends ACalculator
{
	@Getter
	private OffensiveStatisticsFrame offensiveStatistics;


	@Override
	public boolean isCalculationNecessary()
	{
		return OffensiveConstants.isEnableOffensiveStatistics();
	}


	@Override
	protected void reset()
	{
		offensiveStatistics = null;
	}


	@Override
	public void doCalc()
	{
		createEmptyDataset();
	}


	/**
	 * creates empty datasets. they get filled in the OffensiveActions and OffensiveStrategy Calculators.
	 */
	private void createEmptyDataset()
	{
		offensiveStatistics = new OffensiveStatisticsFrame();
		for (BotID key : getWFrame().getTigerBotsAvailable().keySet())
		{
			OffensiveBotFrame botFrame = new OffensiveBotFrame();
			offensiveStatistics.getBotFrames().put(key, botFrame);
		}
	}
}
