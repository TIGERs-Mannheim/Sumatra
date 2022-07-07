/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view;

import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.offensive.OffensiveInterceptionsView;
import edu.tigers.sumatra.offensive.OffensiveStatisticsView;
import edu.tigers.sumatra.offensive.OffensiveStrategyView;
import edu.tigers.sumatra.statistics.StatisticsView;
import edu.tigers.sumatra.support.SupportBehaviorsView;
import edu.tigers.sumatra.view.replay.ReplayWindow;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 */
public class AiReplayWindow extends ReplayWindow
{
	/**
	 * display replays
	 */
	public AiReplayWindow()
	{
		super();

		addView(new BotOverviewView());
		addView(new StatisticsView());
		addView(new OffensiveStrategyView());
		addView(new OffensiveStatisticsView());
		addView(new OffensiveInterceptionsView());
		addView(new SupportBehaviorsView());
		updateViewMenu();
	}
}
