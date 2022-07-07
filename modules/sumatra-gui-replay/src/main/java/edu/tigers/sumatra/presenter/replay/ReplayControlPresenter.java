/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.view.replay.ReplayControlPanel;
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
