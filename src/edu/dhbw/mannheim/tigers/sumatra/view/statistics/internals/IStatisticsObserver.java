/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 20, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.MatchStatistics;


/**
 * observer interface for the statistic view
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public interface IStatisticsObserver
{
	/**
	 * 
	 * @param matchStatistics
	 */
	void onNewStatistics(MatchStatistics matchStatistics);
	
}
