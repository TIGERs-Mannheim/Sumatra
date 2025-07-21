/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.support.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.gui.ai.support.view.SupportBehaviorsPanel;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * SupportBehaviors Presenter
 */
@Log4j2
public class SupportBehaviorsPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final SupportBehaviorsPanel viewPanel = new SupportBehaviorsPanel();


	@Override
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.addVisObserver(this));
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.removeVisObserver(this));
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		ETeamColor color = frame.getTeamColor();

		viewPanel.updateData(color,
				frame.getSupportBehaviorAssignment(),
				frame.getSupportBehaviorViabilities(),
				frame.getActiveSupportBehaviors()
		);
	}
}
