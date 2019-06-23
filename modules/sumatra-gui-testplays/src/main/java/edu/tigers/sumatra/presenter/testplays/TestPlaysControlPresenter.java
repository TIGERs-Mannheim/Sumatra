/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.testplays;

import java.awt.*;

import edu.tigers.sumatra.view.testplays.TestPlayPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class TestPlaysControlPresenter extends ASumatraViewPresenter {

	private TestPlayPanel panel = new TestPlayPanel();

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public ISumatraView getSumatraView() {
		return panel;
	}
}
