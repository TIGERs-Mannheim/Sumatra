/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.aicenter.presenter;

import edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel;
import edu.tigers.sumatra.gui.ai.aicenter.view.TeamPanel;
import edu.tigers.sumatra.ids.EAiTeam;
import lombok.Getter;

import java.awt.Component;


@Getter
public class TeamPresenter
{
	private final TeamPanel teamPanel;
	private final MetisPresenter metisPresenter;
	private final AthenaPresenter athenaPresenter;

	private boolean teamPanelShown = true;


	public TeamPresenter(final EAiTeam team, final TeamPanel teamPanel)
	{
		this.teamPanel = teamPanel;
		this.metisPresenter = new MetisPresenter(teamPanel.getMetisPanel());
		this.athenaPresenter = new AthenaPresenter(team, teamPanel.getAthenaPanel());

		teamPanel.getTabbedPane().addChangeListener(changeEvent -> updateTeamPanelShown());

		updateTeamPanelShown();
	}


	public void setTeamPanelShown(boolean teamPanelShown)
	{
		this.teamPanelShown = teamPanelShown;
		updateTeamPanelShown();
	}


	private void updateTeamPanelShown()
	{
		if (teamPanelShown)
		{
			Component selectedComponent = teamPanel.getTabbedPane().getSelectedComponent();
			metisPresenter.setShown((selectedComponent instanceof MetisPanel));
		} else
		{
			metisPresenter.setShown(false);
		}
	}
}
