/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.replay.presenter;

import edu.tigers.sumatra.gui.replay.view.ReplayControlPanel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;


/**
 * Replay control view presenter
 */
public class ReplayControlPresenter implements ISumatraViewPresenter
{
	@Getter
	private final ReplayControlPanel viewPanel = new ReplayControlPanel();
}
