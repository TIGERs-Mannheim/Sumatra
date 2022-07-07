/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;


import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.visualizer.robots.RobotsPresenter;
import lombok.extern.log4j.Log4j2;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Stream;


/**
 * Extended presenter for the visualizer with AI specific stuff.
 */
@Log4j2
public class VisualizerAiPresenter extends VisualizerPresenter
{
	private final RobotsPresenter robotsPresenter = new RobotsPresenter();
	private final RobotInteractor robotInteractor = new RobotInteractor();


	public VisualizerAiPresenter()
	{
		this.getViewPanel().add(robotsPresenter.getRobotsPanel(), BorderLayout.WEST);
	}


	@Override
	public void onStartModuli()
	{
		getFieldPresenter().getOnFieldClicks().add(this::fieldClick);
		getFieldPresenter().getOnMouseMoves().add(this::mouseMoved);

		super.onStartModuli();
	}


	@Override
	public List<ISumatraPresenter> getChildPresenters()
	{
		return Stream.concat(Stream.of(robotsPresenter), super.getChildPresenters().stream()).toList();
	}


	private void mouseMoved(IVector2 pos, MouseEvent e)
	{
		robotInteractor.onMouseMove(robotsPresenter.getSelectedRobotId(), pos);
	}


	private void fieldClick(IVector2 pos, MouseEvent e)
	{
		robotInteractor.onFieldClick(robotsPresenter.getSelectedRobotId(), pos, e);
	}
}
