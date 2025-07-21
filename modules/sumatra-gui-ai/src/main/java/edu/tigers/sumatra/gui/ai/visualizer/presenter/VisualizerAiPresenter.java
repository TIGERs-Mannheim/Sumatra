/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.visualizer.presenter;


import edu.tigers.sumatra.gui.visualizer.presenter.VisualizerPresenter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.views.ISumatraPresenter;
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
		this.getViewPanel().add(robotsPresenter.getRobotScrollPanel(), BorderLayout.WEST);
	}


	@Override
	public void onModuliStarted()
	{
		getFieldPresenter().getOnFieldClicks().add(this::fieldClick);
		getFieldPresenter().getOnMouseMoves().add(this::mouseMoved);
		getFieldPresenter().getOnRobotMove().add(this::robotMove);

		getFieldPresenter().setSelectedRobotsChangedListener(robotsPresenter.getRobotScrollPanel().getRobotsPanel());
		super.onModuliStarted();
	}


	private void robotMove(BotID botID, IVector2 pos)
	{
		robotInteractor.onRobotMove(botID, pos);
	}


	@Override
	public List<ISumatraPresenter> getChildPresenters()
	{
		return Stream.concat(Stream.of(robotsPresenter), super.getChildPresenters().stream()).toList();
	}


	private void mouseMoved(IVector2 pos, MouseEvent e)
	{
		robotsPresenter.getSelectedBots().forEach(botId -> robotInteractor.onMouseMove(botId, pos));
	}


	private void fieldClick(IVector2 pos, MouseEvent e)
	{
		robotsPresenter.getSelectedBots().forEach(botId -> robotInteractor.onFieldClick(botId, pos, e));
	}
}
