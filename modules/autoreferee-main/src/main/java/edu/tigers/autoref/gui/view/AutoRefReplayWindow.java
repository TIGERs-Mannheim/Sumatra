/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoref.gui.view;

import edu.tigers.sumatra.view.replay.ReplayWindow;
import edu.tigers.sumatra.visualizer.VisualizerView;


public class AutoRefReplayWindow extends ReplayWindow
{
	public AutoRefReplayWindow()
	{
		super();
		addView(new VisualizerView());
	}
}
