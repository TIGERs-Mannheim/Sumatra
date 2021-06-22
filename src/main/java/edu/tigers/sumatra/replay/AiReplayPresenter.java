/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.replay;

import edu.tigers.sumatra.presenter.replay.ReplayPresenter;
import edu.tigers.sumatra.view.AiReplayWindow;


public class AiReplayPresenter extends ReplayPresenter
{
	/**
	 * Default
	 */
	public AiReplayPresenter()
	{
		super(new AiReplayWindow());
		addReplayController(new ReplayAiReCalcController(getMainFrame().getViews()));
		addReplayController(new ReplayAiController(getMainFrame().getViews()));
	}
}
