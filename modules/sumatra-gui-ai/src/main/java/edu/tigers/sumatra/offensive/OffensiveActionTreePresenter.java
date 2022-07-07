/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveActionTreePanel;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;


/**
 * Presenter for offensive actions tree.
 */
@Log4j2
public class OffensiveActionTreePresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final OffensiveActionTreePanel viewPanel = new OffensiveActionTreePanel();


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.addVisObserver(this));
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.removeVisObserver(this));
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		Map<EOffensiveSituation, OffensiveActionTree> map = frame.getActionTrees();
		viewPanel.setActionTree(frame.getTeamColor(), map);
		viewPanel.setCurrentPath(frame.getTeamColor(), frame.getCurrentPath());
	}
}
