/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoref;

import edu.tigers.autoref.gui.view.AutoRefReplayWindow;
import edu.tigers.sumatra.presenter.replay.ReplayPresenter;


public class AutoRefReplayPresenter extends ReplayPresenter
{
	public AutoRefReplayPresenter()
	{
		super(new AutoRefReplayWindow());
	}
}
