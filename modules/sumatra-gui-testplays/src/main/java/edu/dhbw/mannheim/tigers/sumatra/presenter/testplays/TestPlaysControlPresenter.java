/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.presenter.testplays;

import java.awt.*;

import edu.dhbw.mannheim.tigers.sumatra.view.testplays.TestPlayPanel;
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
