/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view;

import edu.tigers.sumatra.aicenter.AICenterView;
import edu.tigers.sumatra.botoverview.BotOverviewView;
import edu.tigers.sumatra.offensive.OffensiveInterceptionsView;
import edu.tigers.sumatra.offensive.OffensiveStatisticsView;
import edu.tigers.sumatra.offensive.OffensiveStrategyView;
import edu.tigers.sumatra.statistics.StatisticsView;
import edu.tigers.sumatra.support.SupportBehaviorsView;
import edu.tigers.sumatra.view.replay.ReplayWindow;
import edu.tigers.sumatra.visualizer.VisualizerAiView;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiReplayWindow extends ReplayWindow
{
	/**
	 * display replays
	 */
	public AiReplayWindow()
	{
		super();

		addView(new AICenterView());
		addView(new VisualizerAiView());
		addView(new BotOverviewView());
		addView(new StatisticsView());
		addView(new OffensiveStrategyView());
		addView(new OffensiveStatisticsView());
		addView(new OffensiveInterceptionsView());
		addView(new SupportBehaviorsView());
		updateViewMenu();
	}
}
