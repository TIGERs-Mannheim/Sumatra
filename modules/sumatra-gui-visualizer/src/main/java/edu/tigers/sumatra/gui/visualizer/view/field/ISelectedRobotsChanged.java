/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.field;

import edu.tigers.sumatra.ids.BotID;

import java.util.List;


public interface ISelectedRobotsChanged
{
	void selectedRobotsChanged(List<BotID> selectedBots);
}
